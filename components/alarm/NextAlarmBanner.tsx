import React, { useEffect, useState, useMemo } from 'react';
import { View, Text } from 'react-native';
import { GlassCard } from '../ui/GlassCard';
import { useAlarmStore } from '../../stores/alarmStore';
import { formatTime, getCountdownText } from '../../utils/time';

export function NextAlarmBanner() {
  const alarms = useAlarmStore((s) => s.alarms);
  const [countdown, setCountdown] = useState('');

  const nextAlarm = useMemo(() => {
    return useAlarmStore.getState().getNextAlarm();
  }, [alarms]);

  useEffect(() => {
    if (!nextAlarm) return;

    const update = () => {
      setCountdown(
        getCountdownText(nextAlarm.time.hour, nextAlarm.time.minute)
      );
    };
    update();
    const interval = setInterval(update, 60000);
    return () => clearInterval(interval);
  }, [nextAlarm?.id, nextAlarm?.time.hour, nextAlarm?.time.minute]);

  if (!nextAlarm) {
    return (
      <GlassCard className="mx-5 mb-5">
        <Text className="text-white/60 font-jost-regular text-base text-center">
          No alarms set
        </Text>
      </GlassCard>
    );
  }

  return (
    <GlassCard className="mx-5 mb-5">
      <Text className="text-white/60 font-jost-regular text-sm">
        Next Alarm in {countdown}
      </Text>
      <Text className="text-white font-jost-semibold text-5xl mt-1">
        {formatTime(nextAlarm.time.hour, nextAlarm.time.minute)}
      </Text>
      <Text className="text-white/40 font-jost-regular text-sm mt-1">
        {nextAlarm.label || 'Alarm'}
      </Text>
    </GlassCard>
  );
}
