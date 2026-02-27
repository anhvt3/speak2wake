import { create } from 'zustand';
import { persist, createJSONStorage } from 'zustand/middleware';
import AsyncStorage from '@react-native-async-storage/async-storage';
import { DEFAULT_SNOOZE_MINUTES, DEFAULT_LANGUAGE } from '../constants';

interface SettingsState {
  defaultSnoozeDuration: number;
  defaultSoundId: string;
  language: string;
  hasCompletedOnboarding: boolean;
  setDefaultSnoozeDuration: (minutes: number) => void;
  setDefaultSoundId: (id: string) => void;
  setLanguage: (lang: string) => void;
  setOnboardingComplete: () => void;
}

export const useSettingsStore = create<SettingsState>()(
  persist(
    (set) => ({
      defaultSnoozeDuration: DEFAULT_SNOOZE_MINUTES,
      defaultSoundId: 'default',
      language: DEFAULT_LANGUAGE,
      hasCompletedOnboarding: false,

      setDefaultSnoozeDuration: (minutes) => {
        set({ defaultSnoozeDuration: minutes });
      },

      setDefaultSoundId: (id) => {
        set({ defaultSoundId: id });
      },

      setLanguage: (lang) => {
        set({ language: lang });
      },

      setOnboardingComplete: () => {
        set({ hasCompletedOnboarding: true });
      },
    }),
    {
      name: '@speak2wake/settings',
      storage: createJSONStorage(() => AsyncStorage),
      merge: (persisted, current) => ({
        ...current,
        ...(persisted as Partial<SettingsState>),
      }),
    }
  )
);
