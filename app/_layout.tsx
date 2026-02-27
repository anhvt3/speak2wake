import React, { useEffect, useState } from 'react';
import { useColorScheme } from 'react-native';
import { Stack, useRouter } from 'expo-router';
import { StatusBar } from 'expo-status-bar';
import * as SplashScreen from 'expo-splash-screen';
import AsyncStorage from '@react-native-async-storage/async-storage';
import {
  useFonts,
  Jost_400Regular,
  Jost_500Medium,
  Jost_600SemiBold,
} from '@expo-google-fonts/jost';
import { ThemeProvider } from '../theme';
import { VocabularyService } from '../services/VocabularyService';
import { StorageService } from '../services/StorageService';
import { AlarmService } from '../services/AlarmService';

import '../global.css';

SplashScreen.preventAutoHideAsync();

export default function RootLayout() {
  const colorScheme = useColorScheme();
  const [appReady, setAppReady] = useState(false);
  const router = useRouter();

  const [fontsLoaded] = useFonts({
    Jost_400Regular,
    Jost_500Medium,
    Jost_600SemiBold,
  });

  useEffect(() => {
    let onboardingTimer: ReturnType<typeof setTimeout> | null = null;

    async function init() {
      try {
        await StorageService.initialize();
        VocabularyService.load();
        await AlarmService.initialize();

        // Check onboarding status
        const hasOnboarded = await AsyncStorage.getItem('@speak2wake/has_onboarded');
        if (!hasOnboarded) {
          // Delay to ensure layout is mounted before navigating
          onboardingTimer = setTimeout(() => router.replace('/onboarding'), 100);
        }
      } catch (e) {
        console.warn('Init error:', e);
      } finally {
        setAppReady(true);
      }
    }
    init();

    // Listen for native alarm events → navigate to ring screen
    const unsubscribe = AlarmService.onAlarmFired((alarmId) => {
      router.replace(`/ring/${alarmId}`);
    });

    return () => {
      if (onboardingTimer) clearTimeout(onboardingTimer);
      unsubscribe();
      AlarmService.destroy();
    };
  }, []);

  useEffect(() => {
    if (fontsLoaded && appReady) {
      SplashScreen.hideAsync();
    }
  }, [fontsLoaded, appReady]);

  if (!fontsLoaded || !appReady) {
    return null;
  }

  return (
    <ThemeProvider>
      <StatusBar style={colorScheme === 'dark' ? 'light' : 'dark'} />
      <Stack screenOptions={{ headerShown: false }}>
        <Stack.Screen name="index" />
        <Stack.Screen
          name="alarm/create"
          options={{ presentation: 'modal' }}
        />
        <Stack.Screen
          name="alarm/[id]"
          options={{ presentation: 'modal' }}
        />
        <Stack.Screen
          name="ring/[alarmId]"
          options={{
            presentation: 'fullScreenModal',
            gestureEnabled: false,
          }}
        />
        <Stack.Screen
          name="challenge/[alarmId]"
          options={{
            presentation: 'fullScreenModal',
            gestureEnabled: false,
          }}
        />
        <Stack.Screen name="settings/index" />
        <Stack.Screen
          name="onboarding"
          options={{
            presentation: 'fullScreenModal',
            gestureEnabled: false,
          }}
        />
      </Stack>
    </ThemeProvider>
  );
}
