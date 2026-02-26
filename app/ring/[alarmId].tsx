import React, { useEffect } from 'react';
import { View, Text, BackHandler } from 'react-native';
import { useRouter, useLocalSearchParams } from 'expo-router';
import { LinearGradient } from 'expo-linear-gradient';
import { SafeAreaView } from 'react-native-safe-area-context';
import Animated, {
  useSharedValue,
  useAnimatedStyle,
  withRepeat,
  withTiming,
  withSequence,
} from 'react-native-reanimated';
import { GradientButton } from '../../components/ui/GradientButton';
import { GlassCard } from '../../components/ui/GlassCard';
import { useAlarmStore } from '../../stores/alarmStore';
import { AlarmService } from '../../services/AlarmService';
import { formatTime } from '../../utils/time';

export default function AlarmRingScreen() {
  const router = useRouter();
  const { alarmId } = useLocalSearchParams<{ alarmId: string }>();
  const getAlarm = useAlarmStore((s) => s.getAlarm);
  const alarm = getAlarm(alarmId!);

  // Prevent back navigation
  useEffect(() => {
    const handler = BackHandler.addEventListener('hardwareBackPress', () => true);
    return () => handler.remove();
  }, []);

  // Pulsing time animation
  const pulse = useSharedValue(1);
  useEffect(() => {
    pulse.value = withRepeat(
      withSequence(
        withTiming(1.05, { duration: 1000 }),
        withTiming(1, { duration: 1000 })
      ),
      -1,
      true
    );
  }, []);

  const pulseStyle = useAnimatedStyle(() => ({
    transform: [{ scale: pulse.value }],
  }));

  const handleSnooze = async () => {
    if (alarm) {
      await AlarmService.snoozeAlarm(alarm.id, alarm.snoozeDuration);
    }
    router.back();
  };

  const handleDismiss = () => {
    if (alarm?.challengeEnabled) {
      router.replace(`/challenge/${alarmId}`);
    } else {
      AlarmService.dismissAlarm(alarmId!);
      router.replace('/');
    }
  };

  const now = new Date();
  const displayTime = alarm
    ? formatTime(alarm.time.hour, alarm.time.minute)
    : formatTime(now.getHours(), now.getMinutes());

  return (
    <LinearGradient
      colors={['#1C1721', '#8A70F8', '#D28AED']}
      locations={[0, 0.6, 1]}
      className="flex-1"
    >
      <SafeAreaView className="flex-1 justify-between items-center py-10">
        {/* Top */}
        <View className="items-center mt-10">
          <Text className="text-white/40 font-jost-regular text-lg">
            Wake Up
          </Text>
          <Animated.View style={pulseStyle}>
            <Text className="text-white font-jost-semibold text-7xl mt-2">
              {displayTime}
            </Text>
          </Animated.View>
          <Text className="text-white/60 font-jost-regular text-base mt-2">
            {alarm?.label || 'Alarm'}
          </Text>
        </View>

        {/* Middle — decorative glow */}
        <View className="items-center">
          <View className="w-40 h-40 rounded-full bg-violet/20 items-center justify-center">
            <View className="w-28 h-28 rounded-full bg-violet/30 items-center justify-center">
              <Text className="text-5xl">🔔</Text>
            </View>
          </View>
        </View>

        {/* Bottom — actions */}
        <View className="w-full px-10 gap-4">
          {alarm?.snoozeEnabled && (
            <GlassCard className="items-center">
              <GradientButton
                label={`Snooze ${alarm.snoozeDuration}m`}
                onPress={handleSnooze}
                className="w-full"
              />
            </GlassCard>
          )}
          <GradientButton
            label={alarm?.challengeEnabled ? 'Start Challenge' : 'Dismiss'}
            onPress={handleDismiss}
            variant="accent"
          />
        </View>
      </SafeAreaView>
    </LinearGradient>
  );
}
