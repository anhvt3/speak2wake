import React from 'react';
import { View, Text, Pressable } from 'react-native';
import { GlassCard } from '../ui/GlassCard';
import { ToggleSwitch } from '../ui/ToggleSwitch';
import { formatTime, getRepeatDaysText } from '../../utils/time';
import type { Alarm } from '../../types/alarm';

interface AlarmCardProps {
  alarm: Alarm;
  onToggle: () => void;
  onPress: () => void;
}

export function AlarmCard({ alarm, onToggle, onPress }: AlarmCardProps) {
  return (
    <Pressable onPress={onPress}>
      <GlassCard className="mb-3">
        <View className="flex-row items-center justify-between">
          <View className="flex-1">
            <Text
              className={`font-jost-semibold text-4xl ${
                alarm.enabled ? 'text-white' : 'text-white/40'
              }`}
            >
              {formatTime(alarm.time.hour, alarm.time.minute)}
            </Text>
            <View className="flex-row items-center mt-1 gap-2">
              <Text className="text-white/60 font-jost-regular text-sm">
                {alarm.label || 'Alarm'}
              </Text>
              <Text className="text-white/40 font-jost-regular text-xs">
                {getRepeatDaysText(alarm.repeatDays)}
              </Text>
            </View>
            {alarm.challengeEnabled && (
              <View className="mt-2 flex-row">
                <View className="bg-violet/30 rounded-pill px-3 py-1">
                  <Text className="text-violet-pink font-jost-medium text-xs">
                    Voice Challenge
                  </Text>
                </View>
              </View>
            )}
          </View>
          <ToggleSwitch value={alarm.enabled} onToggle={onToggle} />
        </View>
      </GlassCard>
    </Pressable>
  );
}
