import React from 'react';
import { View, Text } from 'react-native';
import { GlassCard } from '../ui/GlassCard';
import type { ScoringResult } from '../../types/challenge';

interface ScoringFeedbackProps {
  result: ScoringResult;
  attempts: number;
  maxAttempts: number;
}

export function ScoringFeedback({ result, attempts, maxAttempts }: ScoringFeedbackProps) {
  const scorePercent = Math.round(result.combinedScore * 100);
  const colorClass = result.passed
    ? 'text-success'
    : result.combinedScore >= result.threshold - 0.1
    ? 'text-warning'
    : 'text-error';

  return (
    <GlassCard className="mx-5 mt-4">
      <View className="flex-row items-center justify-between">
        <View className="flex-1">
          <Text className={`font-jost-semibold text-2xl ${colorClass}`}>
            {result.passed ? '✓' : '✗'} {scorePercent}%
          </Text>
          <Text className="text-white/60 font-jost-regular text-sm mt-1">
            {result.feedback}
          </Text>
        </View>
        <View className="items-end">
          <Text className="text-white/40 font-jost-regular text-xs">
            Attempt {attempts}/{maxAttempts}
          </Text>
        </View>
      </View>

      {/* Score breakdown */}
      <View className="flex-row mt-3 gap-3">
        <ScoreChip label="Text" value={result.levenshteinScore} />
        <ScoreChip label="Sound" value={result.phoneticScore} />
        <ScoreChip label="Conf" value={result.confidenceScore} />
      </View>
    </GlassCard>
  );
}

function ScoreChip({ label, value }: { label: string; value: number }) {
  return (
    <View className="bg-white/10 rounded-pill px-3 py-1">
      <Text className="text-white/40 font-jost-regular text-xs">{label}</Text>
      <Text className="text-white font-jost-medium text-sm">
        {Math.round(value * 100)}%
      </Text>
    </View>
  );
}
