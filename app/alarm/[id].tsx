import React, { useState, useEffect } from 'react';
import { View, Text, ScrollView, Pressable, TextInput, Alert } from 'react-native';
import { useRouter, useLocalSearchParams } from 'expo-router';
import { LinearGradient } from 'expo-linear-gradient';
import { SafeAreaView } from 'react-native-safe-area-context';
import { ScreenHeader } from '../../components/shared/ScreenHeader';
import { IconButton } from '../../components/ui/IconButton';
import { GlassCard } from '../../components/ui/GlassCard';
import { GradientButton } from '../../components/ui/GradientButton';
import { DayPicker } from '../../components/ui/DayPicker';
import { ToggleSwitch } from '../../components/ui/ToggleSwitch';
import { useAlarmStore } from '../../stores/alarmStore';
import { AlarmService } from '../../services/AlarmService';
import { ALARM_SOUNDS } from '../../constants';
import type { DayOfWeek } from '../../types/alarm';

export default function EditAlarmScreen() {
  const router = useRouter();
  const { id } = useLocalSearchParams<{ id: string }>();
  const getAlarm = useAlarmStore((s) => s.getAlarm);
  const updateAlarm = useAlarmStore((s) => s.updateAlarm);
  const removeAlarm = useAlarmStore((s) => s.removeAlarm);

  const alarm = getAlarm(id!);

  const [hour, setHour] = useState(alarm?.time.hour ?? 7);
  const [minute, setMinute] = useState(alarm?.time.minute ?? 0);
  const [label, setLabel] = useState(alarm?.label ?? '');
  const [repeatDays, setRepeatDays] = useState<DayOfWeek[]>(alarm?.repeatDays ?? []);
  const [soundId, setSoundId] = useState(alarm?.soundId ?? 'default');
  const [snoozeEnabled, setSnoozeEnabled] = useState(alarm?.snoozeEnabled ?? true);
  const [snoozeDuration, setSnoozeDuration] = useState(alarm?.snoozeDuration ?? 5);
  const [challengeEnabled, setChallengeEnabled] = useState(alarm?.challengeEnabled ?? true);
  const [challengeLevel, setChallengeLevel] = useState(alarm?.challengeLevel ?? 1);
  const [vibrationEnabled, setVibrationEnabled] = useState(alarm?.vibrationEnabled ?? true);

  useEffect(() => {
    if (!alarm) router.back();
  }, [alarm]);

  if (!alarm) return null;

  const toggleDay = (day: DayOfWeek) => {
    setRepeatDays((prev) =>
      prev.includes(day) ? prev.filter((d) => d !== day) : [...prev, day]
    );
  };

  const adjustTime = (field: 'hour' | 'minute', delta: number) => {
    if (field === 'hour') setHour((h) => (h + delta + 24) % 24);
    else setMinute((m) => (m + delta + 60) % 60);
  };

  const handleSave = async () => {
    const updatedAlarm = {
      time: { hour, minute },
      label: label || 'Alarm',
      repeatDays,
      soundId,
      vibrationEnabled,
      snoozeEnabled,
      snoozeDuration,
      challengeEnabled,
      challengeLevel,
    };
    updateAlarm(id!, updatedAlarm);

    // Cancel old native alarm and reschedule with new settings
    try {
      await AlarmService.cancelAlarm(id!);
      if (alarm!.enabled) {
        await AlarmService.scheduleAlarm({ ...alarm!, ...updatedAlarm, id: id! });
      }
    } catch (e) {
      console.warn('Failed to reschedule native alarm:', e);
      Alert.alert('Warning', 'Changes saved but native scheduling failed. Alarm may not ring reliably.');
    }

    router.back();
  };

  const handleDelete = () => {
    Alert.alert('Delete Alarm', 'Are you sure?', [
      { text: 'Cancel', style: 'cancel' },
      {
        text: 'Delete',
        style: 'destructive',
        onPress: async () => {
          // Cancel native alarm before removing from store
          try {
            await AlarmService.cancelAlarm(id!);
          } catch (e) {
            console.warn('Failed to cancel native alarm:', e);
          }
          removeAlarm(id!);
          router.back();
        },
      },
    ]);
  };

  return (
    <LinearGradient
      colors={['#141018', '#1E1020', '#2A1525']}
      start={{ x: 0, y: 0 }}
      end={{ x: 0.5, y: 1 }}
      className="flex-1"
    >
      <SafeAreaView className="flex-1">
        <ScreenHeader
          title="Edit Alarm"
          leftAction={
            <IconButton icon="✕" onPress={() => router.back()} size={40} />
          }
        />

        <ScrollView className="flex-1 px-5" showsVerticalScrollIndicator={false}>
          {/* Time Picker */}
          <GlassCard className="mb-4 items-center">
            <View className="flex-row items-center gap-4">
              <View className="items-center">
                <Pressable onPress={() => adjustTime('hour', 1)}>
                  <Text className="text-white/60 text-2xl">▲</Text>
                </Pressable>
                <Text className="text-white font-jost-semibold text-6xl w-20 text-center">
                  {hour.toString().padStart(2, '0')}
                </Text>
                <Pressable onPress={() => adjustTime('hour', -1)}>
                  <Text className="text-white/60 text-2xl">▼</Text>
                </Pressable>
              </View>
              <Text className="text-white font-jost-semibold text-5xl">:</Text>
              <View className="items-center">
                <Pressable onPress={() => adjustTime('minute', 5)}>
                  <Text className="text-white/60 text-2xl">▲</Text>
                </Pressable>
                <Text className="text-white font-jost-semibold text-6xl w-20 text-center">
                  {minute.toString().padStart(2, '0')}
                </Text>
                <Pressable onPress={() => adjustTime('minute', -5)}>
                  <Text className="text-white/60 text-2xl">▼</Text>
                </Pressable>
              </View>
            </View>
          </GlassCard>

          {/* Label */}
          <GlassCard className="mb-4">
            <Text className="text-white/60 font-jost-regular text-sm mb-2">Label</Text>
            <TextInput
              value={label}
              onChangeText={setLabel}
              placeholder="Alarm name"
              placeholderTextColor="rgba(255,255,255,0.3)"
              className="text-white font-jost-regular text-base border-b border-white/20 pb-2"
            />
          </GlassCard>

          {/* Repeat Days */}
          <GlassCard className="mb-4">
            <Text className="text-white/60 font-jost-regular text-sm mb-3">Repeat</Text>
            <DayPicker selectedDays={repeatDays} onToggleDay={toggleDay} />
          </GlassCard>

          {/* Sound */}
          <GlassCard className="mb-4">
            <Text className="text-white/60 font-jost-regular text-sm mb-3">Sound</Text>
            <View className="flex-row flex-wrap gap-2">
              {ALARM_SOUNDS.map((sound) => (
                <Pressable
                  key={sound.id}
                  onPress={() => setSoundId(sound.id)}
                  className={`px-4 py-2 rounded-pill ${soundId === sound.id ? 'bg-[#FF914D]' : 'bg-white/10'
                    }`}
                >
                  <Text className="text-white font-jost-regular text-sm">{sound.name}</Text>
                </Pressable>
              ))}
            </View>
          </GlassCard>

          {/* Toggles */}
          <GlassCard className="mb-4">
            <View className="flex-row items-center justify-between mb-4">
              <Text className="text-white font-jost-regular text-base">Snooze</Text>
              <ToggleSwitch value={snoozeEnabled} onToggle={() => setSnoozeEnabled(!snoozeEnabled)} />
            </View>
            {snoozeEnabled && (
              <View className="flex-row gap-2 mb-4">
                {[5, 10, 15].map((mins) => (
                  <Pressable
                    key={mins}
                    onPress={() => setSnoozeDuration(mins)}
                    className={`px-4 py-2 rounded-pill ${snoozeDuration === mins ? 'bg-[#FF914D]' : 'bg-white/10'}`}
                  >
                    <Text className="text-white font-jost-regular text-sm">{mins}m</Text>
                  </Pressable>
                ))}
              </View>
            )}
            <View className="flex-row items-center justify-between mb-4">
              <Text className="text-white font-jost-regular text-base">Voice Challenge</Text>
              <ToggleSwitch value={challengeEnabled} onToggle={() => setChallengeEnabled(!challengeEnabled)} />
            </View>
            {challengeEnabled && (
              <View className="flex-row flex-wrap gap-2 mb-4">
                {[
                  { value: 1, label: 'L1 (Word)' },
                  { value: 2, label: 'L2 (Question)' },
                  { value: 3, label: 'L3 (Sentence)' },
                ].map((lvl) => (
                  <Pressable
                    key={lvl.value}
                    onPress={() => setChallengeLevel(lvl.value)}
                    className={`px-4 py-2 rounded-pill ${challengeLevel === lvl.value ? 'bg-[#FF914D]' : 'bg-white/10'
                      }`}
                  >
                    <Text className="text-white font-jost-regular text-sm">
                      {lvl.label}
                    </Text>
                  </Pressable>
                ))}
              </View>
            )}
            <View className="flex-row items-center justify-between">
              <Text className="text-white font-jost-regular text-base">Vibration</Text>
              <ToggleSwitch value={vibrationEnabled} onToggle={() => setVibrationEnabled(!vibrationEnabled)} />
            </View>
          </GlassCard>

          {/* Save + Delete */}
          <GradientButton label="Save Changes" onPress={handleSave} variant="accent" className="mb-4" />
          <Pressable onPress={handleDelete} className="items-center mb-10 py-3">
            <Text className="text-error font-jost-medium text-base">Delete Alarm</Text>
          </Pressable>
        </ScrollView>
      </SafeAreaView>
    </LinearGradient>
  );
}
