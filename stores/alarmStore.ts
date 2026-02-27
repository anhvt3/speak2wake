import { create } from 'zustand';
import { persist, createJSONStorage } from 'zustand/middleware';
import AsyncStorage from '@react-native-async-storage/async-storage';
import type { Alarm } from '../types/alarm';
import { generateId } from '../utils/id';
import { AlarmService } from '../services/AlarmService';

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
        const alarm = get().alarms.find((a) => a.id === id);
        if (!alarm) return;

        const newEnabled = !alarm.enabled;
        set((state) => ({
          alarms: state.alarms.map((a) =>
            a.id === id ? { ...a, enabled: newEnabled } : a
          ),
        }));

        // Schedule/cancel with native module — rollback UI on failure
        const rollback = () => {
          set((state) => ({
            alarms: state.alarms.map((a) =>
              a.id === id ? { ...a, enabled: !newEnabled } : a
            ),
          }));
        };

        if (newEnabled) {
          AlarmService.scheduleAlarm({ ...alarm, enabled: true }).catch(
            (e: any) => { console.warn('Failed to schedule:', e); rollback(); }
          );
        } else {
          AlarmService.cancelAlarm(id).catch(
            (e: any) => { console.warn('Failed to cancel:', e); rollback(); }
          );
        }
      },

      setActiveAlarm: (id) => {
        set({ activeAlarmId: id });
      },

      getAlarm: (id) => {
        return get().alarms.find((a) => a.id === id);
      },

      getNextAlarm: () => {
        const now = new Date();
        const currentDay = now.getDay(); // 0=Sun, 1=Mon, ..., 6=Sat
        const currentMinutes = now.getHours() * 60 + now.getMinutes();
        const enabled = get().alarms.filter((a) => a.enabled);

        if (enabled.length === 0) return null;

        let closest: Alarm | null = null;
        let closestDiff = Infinity;

        for (const alarm of enabled) {
          const alarmMinutes = alarm.time.hour * 60 + alarm.time.minute;

          if (alarm.repeatDays.length === 0) {
            // One-shot alarm: next occurrence is today or tomorrow
            let diff = alarmMinutes - currentMinutes;
            if (diff <= 0) diff += 24 * 60;
            if (diff < closestDiff) {
              closestDiff = diff;
              closest = alarm;
            }
          } else {
            // Repeating alarm: find nearest scheduled day
            for (const day of alarm.repeatDays) {
              let daysUntil = (day as number) - currentDay;
              if (daysUntil < 0) daysUntil += 7;
              if (daysUntil === 0 && alarmMinutes <= currentMinutes) daysUntil = 7;
              const diff = daysUntil * 24 * 60 + (alarmMinutes - currentMinutes);
              if (diff < closestDiff) {
                closestDiff = diff;
                closest = alarm;
              }
            }
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
