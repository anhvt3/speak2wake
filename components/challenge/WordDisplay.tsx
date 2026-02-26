import React from 'react';
import { View, Text, Pressable } from 'react-native';
import { GlassCard } from '../ui/GlassCard';
import { TTSService } from '../../services/TTSService';
import type { VocabWord } from '../../types/challenge';

interface WordDisplayProps {
  word: VocabWord;
}

export function WordDisplay({ word }: WordDisplayProps) {
  const handleSpeak = () => {
    TTSService.speak(word.word);
  };

  return (
    <GlassCard className="items-center mx-5">
      {word.article ? (
        <Text className="text-white/50 font-jost-regular text-lg mb-1">
          {word.article}
        </Text>
      ) : null}
      <Text className="text-white font-jost-semibold text-4xl text-center">
        {word.bare}
      </Text>
      <Text className="text-white/40 font-jost-regular text-base mt-2">
        {word.translation}
      </Text>
      {word.phonetic && (
        <Text className="text-violet-pink font-jost-regular text-sm mt-1">
          /{word.phonetic}/
        </Text>
      )}
      <Pressable
        onPress={handleSpeak}
        className="mt-4 bg-white/10 rounded-full px-5 py-2 flex-row items-center"
      >
        <Text className="text-white text-lg mr-2">🔊</Text>
        <Text className="text-white font-jost-medium text-sm">Listen</Text>
      </Pressable>
    </GlassCard>
  );
}
