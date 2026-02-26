import React from 'react';
import { View } from 'react-native';
import Animated, {
  useAnimatedStyle,
  withTiming,
} from 'react-native-reanimated';

interface WaveformVisualProps {
  volume: number; // 0-10
  isActive: boolean;
}

const BAR_COUNT = 12;

export function WaveformVisual({ volume, isActive }: WaveformVisualProps) {
  return (
    <View className="flex-row items-center justify-center gap-1 h-12">
      {Array.from({ length: BAR_COUNT }).map((_, i) => (
        <WaveBar key={i} index={i} volume={volume} isActive={isActive} />
      ))}
    </View>
  );
}

function WaveBar({
  index,
  volume,
  isActive,
}: {
  index: number;
  volume: number;
  isActive: boolean;
}) {
  const normalizedVolume = Math.min(volume / 10, 1);
  const baseHeight = 8;
  const maxExtra = 32;
  const phase = Math.sin((index / BAR_COUNT) * Math.PI);
  const targetHeight = isActive
    ? baseHeight + maxExtra * normalizedVolume * phase
    : baseHeight;

  const animatedStyle = useAnimatedStyle(() => ({
    height: withTiming(targetHeight, { duration: 100 }),
  }));

  return (
    <Animated.View
      style={[animatedStyle]}
      className="w-1.5 rounded-full bg-accent-orange/80"
    />
  );
}
