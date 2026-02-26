import React from 'react';
import { View, Text } from 'react-native';

interface ScreenHeaderProps {
  title: string;
  leftAction?: React.ReactNode;
  rightAction?: React.ReactNode;
}

export function ScreenHeader({ title, leftAction, rightAction }: ScreenHeaderProps) {
  return (
    <View className="flex-row items-center justify-between px-5 py-3">
      <View className="w-11">{leftAction}</View>
      <Text className="text-white font-jost-semibold text-xl flex-1 text-center">
        {title}
      </Text>
      <View className="w-11 items-end">{rightAction}</View>
    </View>
  );
}
