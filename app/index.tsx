import React from 'react';
import { View, FlatList } from 'react-native';
import { useRouter } from 'expo-router';
import { LinearGradient } from 'expo-linear-gradient';
import { SafeAreaView } from 'react-native-safe-area-context';
import { ScreenHeader } from '../components/shared/ScreenHeader';
import { IconButton } from '../components/ui/IconButton';
import { NextAlarmBanner } from '../components/alarm/NextAlarmBanner';
import { AlarmCard } from '../components/alarm/AlarmCard';
import { useAlarmStore } from '../stores/alarmStore';

export default function HomeScreen() {
  const router = useRouter();
  const alarms = useAlarmStore((s) => s.alarms);
  const toggleAlarm = useAlarmStore((s) => s.toggleAlarm);

  return (
    <LinearGradient
      colors={['#8A70F8', '#D28AED']}
      start={{ x: 0, y: 0 }}
      end={{ x: 0.5, y: 1 }}
      className="flex-1"
    >
      <SafeAreaView className="flex-1">
        <ScreenHeader
          title="Speak2Wake"
          rightAction={
            <IconButton
              icon="+"
              onPress={() => router.push('/alarm/create')}
            />
          }
          leftAction={
            <IconButton
              icon="⚙"
              onPress={() => router.push('/settings')}
              size={40}
            />
          }
        />

        <NextAlarmBanner />

        <FlatList
          data={alarms}
          keyExtractor={(item) => item.id}
          contentContainerStyle={{ paddingHorizontal: 20, paddingBottom: 40 }}
          renderItem={({ item }) => (
            <AlarmCard
              alarm={item}
              onToggle={() => toggleAlarm(item.id)}
              onPress={() => router.push(`/alarm/${item.id}`)}
            />
          )}
        />
      </SafeAreaView>
    </LinearGradient>
  );
}
