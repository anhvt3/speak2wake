import React from 'react';
import { Pressable, Text, View } from 'react-native';
import { LinearGradient } from 'expo-linear-gradient';

interface GradientButtonProps {
  label: string;
  onPress: () => void;
  icon?: React.ReactNode;
  variant?: 'primary' | 'accent';
  disabled?: boolean;
  className?: string;
}

export function GradientButton({
  label,
  onPress,
  icon,
  variant = 'primary',
  disabled = false,
  className = '',
}: GradientButtonProps) {
  const colors: [string, string] =
    variant === 'accent'
      ? ['#F79F40', '#FBF66E']
      : ['#8A70F8', '#D28AED'];

  return (
    <Pressable
      onPress={onPress}
      disabled={disabled}
      className={`overflow-hidden rounded-button ${className}`}
      style={{ opacity: disabled ? 0.5 : 1 }}
    >
      <LinearGradient
        colors={colors}
        start={{ x: 0, y: 0 }}
        end={{ x: 1, y: 1 }}
        className="flex-row items-center justify-center px-8 py-4 rounded-button"
      >
        {icon && <View className="mr-2">{icon}</View>}
        <Text className="text-white font-jost-medium text-lg">{label}</Text>
      </LinearGradient>
    </Pressable>
  );
}
