import React from 'react';
import { Pressable, View } from 'react-native';
import Animated, {
  useAnimatedStyle,
  withTiming,
  interpolateColor,
} from 'react-native-reanimated';

interface ToggleSwitchProps {
  value: boolean;
  onToggle: () => void;
}

export function ToggleSwitch({ value, onToggle }: ToggleSwitchProps) {
  const trackStyle = useAnimatedStyle(() => ({
    backgroundColor: withTiming(
      value ? '#FF914D' : 'rgba(255,255,255,0.2)',
      { duration: 200 }
    ),
  }));

  const thumbStyle = useAnimatedStyle(() => ({
    transform: [
      {
        translateX: withTiming(value ? 22 : 2, { duration: 200 }),
      },
    ],
    backgroundColor: withTiming(
      value ? '#FBF66E' : 'rgba(255,255,255,0.6)',
      { duration: 200 }
    ),
  }));

  return (
    <Pressable onPress={onToggle}>
      <Animated.View
        style={[trackStyle]}
        className="w-[50px] h-[28px] rounded-full justify-center"
      >
        <Animated.View
          style={[thumbStyle]}
          className="w-[24px] h-[24px] rounded-full"
        />
      </Animated.View>
    </Pressable>
  );
}
