import React, { useEffect, useCallback, useState } from 'react';
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

export default function ChallengeScreen() {
  const router = useRouter();
  const { alarmId } = useLocalSearchParams<{ alarmId: string }>();
  const [volume, setVolume] = useState(0);

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

  // Initialize challenge with random word
  useEffect(() => {
    const word = VocabularyService.getRandomWord();
    startChallenge(word);

    VoiceService.onResult((text, confidence) => {
      setListening(false);
      const store = useChallengeStore.getState();
      if (store.currentWord) {
        const result = ScoringService.evaluate(store.currentWord.bare, text, confidence);
        recordAttempt(result);
        if (result.passed) {
          handleDismiss();
        }
      }
    });

    VoiceService.onPartialResult((text) => {
      setPartialText(text);
    });

    VoiceService.onVolumeChange((vol) => {
      setVolume(vol);
    });

    return () => {
      VoiceService.destroy();
      reset();
    };
  }, []);

  const handleDismiss = useCallback(async () => {
    await AlarmService.dismissAlarm(alarmId!);
    reset();
    router.replace('/');
  }, [alarmId]);

  const handleMicPress = async () => {
    if (isListening) {
      await VoiceService.stopListening();
      setListening(false);
    } else {
      setListening(true);
      await VoiceService.startListening('de-DE');
    }
  };

  if (!currentWord) return null;

  if (failsafeActive) {
    return (
      <LinearGradient
        colors={['#1C1721', '#8A70F8']}
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
      colors={['#1C1721', '#8A70F8', '#D28AED']}
      locations={[0, 0.5, 1]}
      className="flex-1"
    >
      <SafeAreaView className="flex-1">
        {/* Header */}
        <View className="items-center mt-4 mb-6">
          <Text className="text-white/40 font-jost-regular text-sm">
            Read this word to dismiss
          </Text>
        </View>

        {/* Word */}
        <WordDisplay word={currentWord} />

        {/* Mic + Waveform */}
        <View className="flex-1 items-center justify-center">
          <MicButton isListening={isListening} onPress={handleMicPress} />
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
