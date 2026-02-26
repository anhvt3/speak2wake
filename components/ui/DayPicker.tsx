import React from 'react';
import { View, Pressable, Text } from 'react-native';
import type { DayOfWeek } from '../../types/alarm';
import { DAY_LABELS_SHORT } from '../../constants';

interface DayPickerProps {
  selectedDays: DayOfWeek[];
  onToggleDay: (day: DayOfWeek) => void;
}

export function DayPicker({ selectedDays, onToggleDay }: DayPickerProps) {
  return (
    <View className="flex-row gap-2">
      {DAY_LABELS_SHORT.map((label, index) => {
        const day = index as DayOfWeek;
        const isSelected = selectedDays.includes(day);
        return (
          <Pressable
            key={index}
            onPress={() => onToggleDay(day)}
            className={`w-9 h-9 rounded-full items-center justify-center ${
              isSelected
                ? 'bg-[#FF914D]'
                : 'bg-white/20 dark:bg-white/[0.08]'
            }`}
          >
            <Text
              className={`text-xs font-jost-medium ${
                isSelected ? 'text-white' : 'text-white/60'
              }`}
            >
              {label}
            </Text>
          </Pressable>
        );
      })}
    </View>
  );
}
