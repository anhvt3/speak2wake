import React, { useState } from 'react';
import { View, Text, TextInput, Pressable } from 'react-native';
import { GlassCard } from '../ui/GlassCard';
import { GradientButton } from '../ui/GradientButton';
import { generateMathProblem, checkTextFailsafe } from '../../engine/failsafe';
import type { VocabWord, MathProblem } from '../../types/challenge';

interface FailsafeModalProps {
  word: VocabWord;
  onDismiss: () => void;
}

export function FailsafeModal({ word, onDismiss }: FailsafeModalProps) {
  const [mode, setMode] = useState<'choose' | 'text' | 'math'>('choose');
  const [input, setInput] = useState('');
  const [error, setError] = useState('');
  const [mathProblem] = useState<MathProblem>(() => generateMathProblem());

  const handleTextSubmit = () => {
    if (checkTextFailsafe(word.bare, input)) {
      onDismiss();
    } else {
      setError('Incorrect. Try again.');
      setInput('');
    }
  };

  const handleMathSubmit = () => {
    if (parseInt(input, 10) === mathProblem.answer) {
      onDismiss();
    } else {
      setError('Wrong answer. Try again.');
      setInput('');
    }
  };

  if (mode === 'choose') {
    return (
      <View className="flex-1 justify-center px-5">
        <GlassCard className="items-center">
          <Text className="text-white font-jost-semibold text-xl mb-2">
            Too many attempts
          </Text>
          <Text className="text-white/60 font-jost-regular text-sm mb-6 text-center">
            Choose an alternative way to dismiss:
          </Text>
          <GradientButton
            label={`Type "${word.bare}"`}
            onPress={() => { setMode('text'); setError(''); setInput(''); }}
            className="mb-3 w-full"
          />
          <GradientButton
            label="Solve Math"
            onPress={() => { setMode('math'); setError(''); setInput(''); }}
            variant="accent"
            className="w-full"
          />
        </GlassCard>
      </View>
    );
  }

  return (
    <View className="flex-1 justify-center px-5">
      <GlassCard>
        <Text className="text-white font-jost-semibold text-xl mb-4 text-center">
          {mode === 'text' ? `Type: "${word.bare}"` : mathProblem.question}
        </Text>

        <TextInput
          value={input}
          onChangeText={(t) => { setInput(t); setError(''); }}
          placeholder={mode === 'text' ? 'Type the word...' : 'Your answer...'}
          placeholderTextColor="rgba(255,255,255,0.3)"
          keyboardType={mode === 'math' ? 'numeric' : 'default'}
          autoFocus
          className="text-white font-jost-regular text-lg border-b border-white/30 pb-2 mb-2 text-center"
        />

        {error ? (
          <Text className="text-error font-jost-regular text-sm text-center mb-3">
            {error}
          </Text>
        ) : null}

        <GradientButton
          label="Submit"
          onPress={mode === 'text' ? handleTextSubmit : handleMathSubmit}
          variant="accent"
          className="mt-4"
        />

        <Pressable onPress={() => setMode('choose')} className="mt-3 items-center">
          <Text className="text-white/40 font-jost-regular text-sm">Back</Text>
        </Pressable>
      </GlassCard>
    </View>
  );
}
