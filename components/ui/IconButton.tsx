import React from 'react';
import { Pressable, Text } from 'react-native';

interface IconButtonProps {
  icon: string;
  onPress: () => void;
  size?: number;
  className?: string;
}

export function IconButton({
  icon,
  onPress,
  size = 44,
  className = '',
}: IconButtonProps) {
  return (
    <Pressable
      onPress={onPress}
      className={`items-center justify-center rounded-full bg-white/20 dark:bg-white/[0.08] ${className}`}
      style={{ width: size, height: size }}
    >
      <Text className="text-white text-lg">{icon}</Text>
    </Pressable>
  );
}
