import React, { useEffect } from 'react';
import { Pressable, View, Text } from 'react-native';
import Animated, {
  useSharedValue,
  useAnimatedStyle,
  withRepeat,
  withTiming,
  withDelay,
  Easing,
  cancelAnimation,
} from 'react-native-reanimated';
import { LinearGradient } from 'expo-linear-gradient';

interface MicButtonProps {
  isListening: boolean;
  onPress: () => void;
  disabled?: boolean;
  customStatusText?: string;
}

export function MicButton({ isListening, onPress, disabled = false, customStatusText }: MicButtonProps) {
  const ring1Scale = useSharedValue(0.8);
  const ring1Opacity = useSharedValue(0);
  const ring2Scale = useSharedValue(0.8);
  const ring2Opacity = useSharedValue(0);

  useEffect(() => {
    if (isListening) {
      ring1Scale.value = withRepeat(
        withTiming(1.4, { duration: 1500, easing: Easing.out(Easing.ease) }),
        -1,
        false
      );
      ring1Opacity.value = withRepeat(
        withTiming(0, { duration: 1500 }),
        -1,
        false
      );
      ring2Scale.value = withDelay(
        500,
        withRepeat(
          withTiming(1.4, { duration: 1500, easing: Easing.out(Easing.ease) }),
          -1,
          false
        )
      );
      ring2Opacity.value = withDelay(
        500,
        withRepeat(withTiming(0, { duration: 1500 }), -1, false)
      );
    } else {
      cancelAnimation(ring1Scale);
      cancelAnimation(ring1Opacity);
      cancelAnimation(ring2Scale);
      cancelAnimation(ring2Opacity);
      ring1Scale.value = withTiming(0.8, { duration: 300 });
      ring1Opacity.value = withTiming(0, { duration: 300 });
      ring2Scale.value = withTiming(0.8, { duration: 300 });
      ring2Opacity.value = withTiming(0, { duration: 300 });
    }
  }, [isListening]);

  const ring1Style = useAnimatedStyle(() => ({
    transform: [{ scale: ring1Scale.value }],
    opacity: ring1Opacity.value,
  }));

  const ring2Style = useAnimatedStyle(() => ({
    transform: [{ scale: ring2Scale.value }],
    opacity: ring2Opacity.value,
  }));

  return (
    <View className="items-center justify-center" style={{ width: 160, height: 160 }}>
      {/* Pulse rings */}
      <Animated.View
        style={[ring1Style, { position: 'absolute', width: 160, height: 160 }]}
        className="rounded-full border-2 border-accent-orange"
      />
      <Animated.View
        style={[ring2Style, { position: 'absolute', width: 160, height: 160 }]}
        className="rounded-full border-2 border-accent-orange"
      />

      {/* Button */}
      <Pressable
        onPress={onPress}
        disabled={disabled}
        style={{ opacity: disabled ? 0.5 : 1 }}
      >
        <LinearGradient
          colors={isListening ? ['#F87171', '#EF4444'] : ['#F79F40', '#FBF66E']}
          start={{ x: 0.5, y: 0 }}
          end={{ x: 0.5, y: 1 }}
          className="w-20 h-20 rounded-full items-center justify-center"
        >
          <Text className="text-3xl">{isListening ? '⏹' : '🎤'}</Text>
        </LinearGradient>
      </Pressable>

      <Text className="text-white/60 font-jost-regular text-sm mt-3">
        {customStatusText || (isListening ? 'Listening...' : 'Tap to retry')}
      </Text>
    </View>
  );
}
