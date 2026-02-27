import React from 'react';
import { View, Text, Pressable } from 'react-native';
import { GlassCard } from '../ui/GlassCard';
import { TTSService } from '../../services/TTSService';
import { ChallengeLevel, getEffectiveLevel, type ChallengeItem } from '../../types/challenge';

interface WordDisplayProps {
  item: ChallengeItem;
  onSpeakStart?: () => void;
  onSpeakEnd?: () => void;
}

export function WordDisplay({ item, onSpeakStart, onSpeakEnd }: WordDisplayProps) {
  const level = getEffectiveLevel(item);
  const handleSpeak = async () => {
    onSpeakStart?.();
    try {
      const textToSpeak = level === ChallengeLevel.SENTENCE ? (item.targetText || item.translation) : (item.word || item.bare || '');
      await TTSService.speak(textToSpeak);
    } catch (e) {
      console.warn('TTS error:', e);
    }
    // Wait for TTS to finish, then call onSpeakEnd
    let ended = false;
    const finish = () => {
      if (ended) return;
      ended = true;
      clearInterval(checkDone);
      clearTimeout(safetyTimeout);
      onSpeakEnd?.();
    };
    const checkDone = setInterval(async () => {
      const speaking = await TTSService.isSpeaking();
      if (!speaking) finish();
    }, 300);
    const safetyTimeout = setTimeout(finish, 5000);
  };

  return (
    <GlassCard className="items-center mx-5 py-6">
      {level === ChallengeLevel.WORD && item.article ? (
        <Text className="text-white/50 font-jost-regular text-lg mb-1">
          {item.article}
        </Text>
      ) : null}

      <Text className="text-white font-jost-semibold text-3xl text-center">
        {level === ChallengeLevel.SHORT_ANSWER ? item.question :
          level === ChallengeLevel.SENTENCE ? `Translate:\n"${item.translation}"` : item.bare}
      </Text>

      {level === ChallengeLevel.WORD && (
        <Text className="text-white/40 font-jost-regular text-base mt-2">
          {item.translation}
        </Text>
      )}

      {level === ChallengeLevel.WORD && item.phonetic && (
        <Text className="text-violet-pink font-jost-regular text-sm mt-1">
          /{item.phonetic}/
        </Text>
      )}

      {level !== ChallengeLevel.SHORT_ANSWER && (
        <Pressable
          onPress={handleSpeak}
          className="mt-6 bg-white/10 rounded-full px-5 py-2 flex-row items-center"
        >
          <Text className="text-white text-lg mr-2">🔊</Text>
          <Text className="text-white font-jost-medium text-sm">
            {level === ChallengeLevel.SENTENCE ? 'Hear Answer' : 'Listen'}
          </Text>
        </Pressable>
      )}
    </GlassCard>
  );
}
