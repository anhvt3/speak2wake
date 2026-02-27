import React, { useEffect, useCallback, useState, useRef } from 'react';
import { View, Text, BackHandler } from 'react-native';
import { useRouter, useLocalSearchParams } from 'expo-router';
import { LinearGradient } from 'expo-linear-gradient';
import { SafeAreaView } from 'react-native-safe-area-context';
import { WordDisplay } from '../../components/challenge/WordDisplay';
import { MicButton } from '../../components/challenge/MicButton';
import { WaveformVisual } from '../../components/challenge/WaveformVisual';
import { ScoringFeedback } from '../../components/challenge/ScoringFeedback';
import { FailsafeModal } from '../../components/challenge/FailsafeModal';
import { useChallengeStore } from '../../stores/challengeStore';
import { VocabularyService } from '../../services/VocabularyService';
import { VoiceService } from '../../services/VoiceService';
import { ScoringService } from '../../services/ScoringService';
import { AlarmService } from '../../services/AlarmService';

/**
 * Challenge Screen UX Flow:
 * 1. Alarm sound KEEPS PLAYING when entering challenge (not dismissed yet)
 * 2. Mic auto-starts after 2s delay (no "tap to speak")
 * 3. While mic is listening: alarm sound is PAUSED
 * 4. While TTS is speaking (user pressed Listen): alarm sound is PAUSED
 * 5. If 10s timeout (no speech) or error: mic stops → alarm sound RESUMES → auto-retry after 3s
 * 6. If attempt fails: show feedback 2s → auto-retry
 * 7. If attempt passes: dismiss alarm → go home
 */

export default function ChallengeScreen() {
  const router = useRouter();
  const { alarmId } = useLocalSearchParams<{ alarmId: string }>();
  const [volume, setVolume] = useState(0);
  const [statusText, setStatusText] = useState('Preparing...');
  const retryTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null);
  const autoStartTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null);
  const isMountedRef = useRef(true);
  const dismissedRef = useRef(false);

  const {
    currentWord,
    attempts,
    maxAttempts,
    lastResult,
    isListening,
    partialText,
    failsafeActive,
    startChallenge,
    recordAttempt,
    setListening,
    setPartialText,
    reset,
  } = useChallengeStore();

  // Prevent back navigation
  useEffect(() => {
    const handler = BackHandler.addEventListener('hardwareBackPress', () => true);
    return () => handler.remove();
  }, []);

  // Clear all timers
  const clearTimers = useCallback(() => {
    if (retryTimerRef.current) {
      clearTimeout(retryTimerRef.current);
      retryTimerRef.current = null;
    }
    if (autoStartTimerRef.current) {
      clearTimeout(autoStartTimerRef.current);
      autoStartTimerRef.current = null;
    }
  }, []);

  // Start listening (pause alarm while mic is active)
  const startMic = useCallback(async () => {
    if (!isMountedRef.current) return;
    const store = useChallengeStore.getState();
    if (store.failsafeActive) return;

    setStatusText('Listening... say the word');
    setListening(true);

    // Pause alarm sound while user speaks
    try { await AlarmService.pauseAlarmSound(); } catch { }

    await VoiceService.startListening('de-DE');
  }, []);

  // Resume alarm sound
  const resumeAlarm = useCallback(async () => {
    try { await AlarmService.resumeAlarmSound(); } catch { }
  }, []);

  // Schedule auto-retry after delay
  const scheduleRetry = useCallback((delayMs: number) => {
    clearTimers();
    retryTimerRef.current = setTimeout(() => {
      if (isMountedRef.current) {
        startMic();
      }
    }, delayMs);
  }, [startMic, clearTimers]);

  // Initialize challenge
  useEffect(() => {
    isMountedRef.current = true;
    const word = VocabularyService.getRandomWord();
    startChallenge(word);

    // Voice result handler
    VoiceService.onResult((text, confidence) => {
      setListening(false);
      const store = useChallengeStore.getState();
      if (store.currentWord) {
        const result = ScoringService.evaluate(store.currentWord.bare, text, confidence);
        recordAttempt(result);
        if (result.passed) {
          setStatusText('✓ Correct! Dismissing alarm...');
          handleDismiss();
        } else {
          setStatusText(`✗ Try again (${store.attempts + 1}/${store.maxAttempts})`);
          // Resume alarm sound, then auto-retry after 3s feedback time
          resumeAlarm();
          scheduleRetry(3000);
        }
      }
    });

    VoiceService.onPartialResult((text) => {
      setPartialText(text);
    });

    VoiceService.onVolumeChange((vol) => {
      setVolume(vol);
    });

    // Voice error/timeout handler → resume alarm + auto-retry
    VoiceService.onError((error) => {
      setListening(false);
      if (error.code === 'timeout') {
        setStatusText('No speech detected. Retrying...');
      } else {
        setStatusText('Mic error. Retrying...');
      }
      // Resume alarm sound, auto-retry after 3s
      resumeAlarm();
      scheduleRetry(3000);
    });

    // Auto-start mic after 2s (give user time to see the word)
    setStatusText('Read the word below, mic starts in 2s...');
    autoStartTimerRef.current = setTimeout(() => {
      startMic();
    }, 2000);

    return () => {
      isMountedRef.current = false;
      clearTimers();
      VoiceService.destroy();
      reset();
      // Safety: ensure alarm sound is stopped if screen unmounts unexpectedly
      if (!dismissedRef.current) {
        AlarmService.dismissAlarm(alarmId!).catch(() => { });
      }
    };
  }, []);

  const handleDismiss = useCallback(async () => {
    dismissedRef.current = true;
    clearTimers();
    await AlarmService.dismissAlarm(alarmId!);
    reset();
    router.replace('/');
  }, [alarmId, clearTimers]);

  // Manual mic toggle (user can still tap to restart manually)
  const handleMicPress = async () => {
    clearTimers();
    if (isListening) {
      await VoiceService.stopListening();
      setListening(false);
      setStatusText('Stopped. Tap mic or wait...');
      resumeAlarm();
      scheduleRetry(3000);
    } else {
      startMic();
    }
  };

  // Stop voice/timers when entering failsafe mode
  useEffect(() => {
    if (failsafeActive) {
      clearTimers();
      VoiceService.stopListening().catch(() => { });
      setListening(false);
    }
  }, [failsafeActive, clearTimers]);

  if (!currentWord) return null;

  if (failsafeActive) {
    return (
      <LinearGradient
        colors={['#141018', '#2A1525']}
        className="flex-1"
      >
        <SafeAreaView className="flex-1">
          <FailsafeModal word={currentWord} onDismiss={handleDismiss} />
        </SafeAreaView>
      </LinearGradient>
    );
  }

  return (
    <LinearGradient
      colors={['#141018', '#1E1020', '#2A1525']}
      locations={[0, 0.5, 1]}
      className="flex-1"
    >
      <SafeAreaView className="flex-1">
        {/* Header */}
        <View className="items-center mt-4 mb-2">
          {/* We moved statusText to the MicButton, but keep the space or you can leave it here as well. 
              Let's hide it here if we pass it down to avoid duplication, OR we keep it here and don't pass it to MicButton.
              Wait, the bug report says "Label dưới nút Record vẫn hiển thị chữ Listening...".
              Ah, that MEANS we don't need statusText up here anymore, or we just keep it in one place. Let's hide the top one to clean up UI! */}
        </View>

        {/* Word */}
        <WordDisplay
          word={currentWord}
          onSpeakStart={async () => {
            // Stop mic and pause alarm while TTS speaks the word
            clearTimers();
            if (useChallengeStore.getState().isListening) {
              await VoiceService.stopListening();
              setListening(false);
            }
            setStatusText('Hearing pronunciation...');
            AlarmService.pauseAlarmSound().catch(() => { });
          }}
          onSpeakEnd={() => {
            // Resume alarm and start mic after TTS finishes
            scheduleRetry(500); // add a short 500ms delay to let TTS fully complete before starting mic
          }}
        />

        {/* Mic + Waveform */}
        <View className="flex-1 items-center justify-center">
          <MicButton
            isListening={isListening}
            onPress={handleMicPress}
            customStatusText={statusText}
          />
          <View className="mt-4">
            <WaveformVisual volume={volume} isActive={isListening} />
          </View>
          {partialText ? (
            <Text className="text-white/50 font-jost-regular text-base mt-3">
              "{partialText}"
            </Text>
          ) : null}
        </View>

        {/* Scoring Feedback */}
        {lastResult && (
          <ScoringFeedback
            result={lastResult}
            attempts={attempts}
            maxAttempts={maxAttempts}
          />
        )}

        {/* Bottom padding */}
        <View className="h-10" />
      </SafeAreaView>
    </LinearGradient>
  );
}
