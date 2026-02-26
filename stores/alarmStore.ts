import { create } from 'zustand';
import { persist, createJSONStorage } from 'zustand/middleware';
import AsyncStorage from '@react-native-async-storage/async-storage';
import type { Alarm } from '../types/alarm';
import { generateId } from '../utils/id';

interface AlarmState {
  alarms: Alarm[];
  activeAlarmId: string | null;
  addAlarm: (alarm: Omit<Alarm, 'id' | 'createdAt'>) => Alarm;
  updateAlarm: (id: string, updates: Partial<Alarm>) => void;
  removeAlarm: (id: string) => void;
  toggleAlarm: (id: string) => void;
  setActiveAlarm: (id: string | null) => void;
  getAlarm: (id: string) => Alarm | undefined;
  getNextAlarm: () => Alarm | null;
}

export const useAlarmStore = create<AlarmState>()(
  persist(
    (set, get) => ({
      alarms: [],
      activeAlarmId: null,

      addAlarm: (data) => {
        const alarm: Alarm = {
          ...data,
          id: generateId(),
          createdAt: Date.now(),
        };
        set((state) => ({ alarms: [...state.alarms, alarm] }));
        return alarm;
      },

      updateAlarm: (id, updates) => {
        set((state) => ({
          alarms: state.alarms.map((a) =>
            a.id === id ? { ...a, ...updates } : a
          ),
        }));
      },

      removeAlarm: (id) => {
        set((state) => ({
          alarms: state.alarms.filter((a) => a.id !== id),
        }));
      },

      toggleAlarm: (id) => {
        set((state) => ({
          alarms: state.alarms.map((a) =>
            a.id === id ? { ...a, enabled: !a.enabled } : a
          ),
        }));
      },

      setActiveAlarm: (id) => {
        set({ activeAlarmId: id });
      },

      getAlarm: (id) => {
        return get().alarms.find((a) => a.id === id);
      },

      getNextAlarm: () => {
        const now = new Date();
        const currentMinutes = now.getHours() * 60 + now.getMinutes();
        const enabled = get().alarms.filter((a) => a.enabled);

        if (enabled.length === 0) return null;

        let closest: Alarm | null = null;
        let closestDiff = Infinity;

        for (const alarm of enabled) {
          const alarmMinutes = alarm.time.hour * 60 + alarm.time.minute;
          let diff = alarmMinutes - currentMinutes;
          if (diff <= 0) diff += 24 * 60; // tomorrow
          if (diff < closestDiff) {
            closestDiff = diff;
            closest = alarm;
          }
        }

        return closest;
      },
    }),
    {
      name: '@speak2wake/alarms',
      storage: createJSONStorage(() => AsyncStorage),
      partialize: (state) => ({ alarms: state.alarms }),
    }
  )
);
