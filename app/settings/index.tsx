import React, { useEffect, useState } from 'react';
import { View, Text, ScrollView, Pressable } from 'react-native';
import { useRouter } from 'expo-router';
import { LinearGradient } from 'expo-linear-gradient';
import { SafeAreaView } from 'react-native-safe-area-context';
import { ScreenHeader } from '../../components/shared/ScreenHeader';
import { IconButton } from '../../components/ui/IconButton';
import { GlassCard } from '../../components/ui/GlassCard';
import { ToggleSwitch } from '../../components/ui/ToggleSwitch';
import { useSettingsStore } from '../../stores/settingsStore';
import { VoiceService } from '../../services/VoiceService';
import { VocabularyService } from '../../services/VocabularyService';
import { ALARM_SOUNDS } from '../../constants';

export default function SettingsScreen() {
  const router = useRouter();
  const [sttAvailable, setSttAvailable] = useState<boolean | null>(null);
  const settings = useSettingsStore();

  useEffect(() => {
    VoiceService.isAvailable().then(setSttAvailable);
  }, []);

  const wordCount = VocabularyService.getWordCount();
  const categories = VocabularyService.getCategories();

  return (
    <LinearGradient
      colors={['#141018', '#1E1020', '#2A1525']}
      start={{ x: 0, y: 0 }}
      end={{ x: 0.5, y: 1 }}
      className="flex-1"
    >
      <SafeAreaView className="flex-1">
        <ScreenHeader
          title="Settings"
          leftAction={
            <IconButton icon="←" onPress={() => router.back()} size={40} />
          }
        />

        <ScrollView className="flex-1 px-5" showsVerticalScrollIndicator={false}>
          {/* Voice Recognition */}
          <GlassCard className="mb-4">
            <Text className="text-white font-jost-semibold text-base mb-3">
              Voice Recognition
            </Text>
            <View className="flex-row items-center justify-between mb-2">
              <Text className="text-white/60 font-jost-regular text-sm">
                STT Available
              </Text>
              <Text
                className={`font-jost-medium text-sm ${
                  sttAvailable === null
                    ? 'text-white/40'
                    : sttAvailable
                    ? 'text-success'
                    : 'text-error'
                }`}
              >
                {sttAvailable === null
                  ? 'Checking...'
                  : sttAvailable
                  ? '✓ Ready'
                  : '✗ Not available'}
              </Text>
            </View>
            {sttAvailable === false && (
              <Text className="text-warning font-jost-regular text-xs">
                Install German language pack in device Settings → Language →
                Speech → Download offline speech data.
              </Text>
            )}
          </GlassCard>

          {/* Default Snooze */}
          <GlassCard className="mb-4">
            <Text className="text-white font-jost-semibold text-base mb-3">
              Default Snooze
            </Text>
            <View className="flex-row gap-2">
              {[5, 10, 15, 20].map((mins) => (
                <Pressable
                  key={mins}
                  onPress={() => settings.setDefaultSnoozeDuration(mins)}
                  className={`px-4 py-2 rounded-pill ${
                    settings.defaultSnoozeDuration === mins
                      ? 'bg-[#FF914D]'
                      : 'bg-white/10'
                  }`}
                >
                  <Text className="text-white font-jost-regular text-sm">
                    {mins}m
                  </Text>
                </Pressable>
              ))}
            </View>
          </GlassCard>

          {/* Default Sound */}
          <GlassCard className="mb-4">
            <Text className="text-white font-jost-semibold text-base mb-3">
              Default Sound
            </Text>
            <View className="flex-row flex-wrap gap-2">
              {ALARM_SOUNDS.map((sound) => (
                <Pressable
                  key={sound.id}
                  onPress={() => settings.setDefaultSoundId(sound.id)}
                  className={`px-4 py-2 rounded-pill ${
                    settings.defaultSoundId === sound.id
                      ? 'bg-[#FF914D]'
                      : 'bg-white/10'
                  }`}
                >
                  <Text className="text-white font-jost-regular text-sm">
                    {sound.name}
                  </Text>
                </Pressable>
              ))}
            </View>
          </GlassCard>

          {/* Vocabulary Info */}
          <GlassCard className="mb-4">
            <Text className="text-white font-jost-semibold text-base mb-3">
              Vocabulary
            </Text>
            <Text className="text-white/60 font-jost-regular text-sm">
              {wordCount} German words loaded
            </Text>
            <Text className="text-white/40 font-jost-regular text-xs mt-1">
              Categories: {categories.join(', ')}
            </Text>
          </GlassCard>

          {/* App Info */}
          <GlassCard className="mb-10">
            <Text className="text-white font-jost-semibold text-base mb-2">
              About
            </Text>
            <Text className="text-white/40 font-jost-regular text-sm">
              Speak2Wake v1.0.0
            </Text>
            <Text className="text-white/40 font-jost-regular text-xs mt-1">
              Learn German by waking up
            </Text>
          </GlassCard>
        </ScrollView>
      </SafeAreaView>
    </LinearGradient>
  );
}
