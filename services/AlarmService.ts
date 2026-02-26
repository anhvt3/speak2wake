import { Platform } from 'react-native';
import type { Alarm } from '../types/alarm';

/**
 * AlarmService manages scheduling/canceling alarms.
 * Uses native expo-alarm-engine module on Android for reliable alarms
 * (survives app kill, device reboot, Doze mode).
 * Falls back to expo-notifications on iOS or when native module unavailable.
 */

// --- Native module (Android) ---
let ExpoAlarmEngine: any = null;
try {
  if (Platform.OS === 'android') {
    // Import from our local expo module (registered as ExpoAlarmEngine)
    ExpoAlarmEngine = require('../modules/expo-alarm-engine');
  }
} catch (e) {
  console.warn('ExpoAlarmEngine not available:', e);
  // Native module not available — will fall back to notifications
}

// --- Notification fallback ---
let Notifications: any = null;
try {
  Notifications = require('expo-notifications');
} catch {
  // Not available in web/preview
}

const useNativeAlarm = Platform.OS === 'android' && ExpoAlarmEngine != null;

class AlarmServiceImpl {
  private listeners: ((alarmId: string) => void)[] = [];
  private nativeSubscription: any = null;

  async initialize(): Promise<void> {
    if (useNativeAlarm) {
      // Subscribe to native alarm fired events
      this.nativeSubscription = ExpoAlarmEngine.addAlarmFiredListener(
        (event: { alarmId: string }) => {
          this.listeners.forEach((cb) => cb(event.alarmId));
        }
      );
      return;
    }

    // Fallback: expo-notifications
    if (!Notifications) return;

    const { status } = await Notifications.requestPermissionsAsync();
    if (status !== 'granted') {
      console.warn('Notification permissions not granted');
    }

    Notifications.addNotificationResponseReceivedListener((response: any) => {
      const alarmId = response.notification.request.content.data?.alarmId;
      if (alarmId) {
        this.listeners.forEach((cb) => cb(alarmId));
      }
    });
  }

  async scheduleAlarm(alarm: Alarm): Promise<void> {
    if (useNativeAlarm) {
      ExpoAlarmEngine.scheduleAlarm(
        alarm.id,
        alarm.time.hour,
        alarm.time.minute,
        alarm.repeatDays as number[],
        alarm.soundId,
        alarm.label || 'Time to wake up!'
      );
      return;
    }

    // Fallback: expo-notifications
    if (!Notifications) return;

    await Notifications.scheduleNotificationAsync({
      content: {
        title: 'Speak2Wake',
        body: alarm.label || 'Time to wake up!',
        data: { alarmId: alarm.id },
        sound: true,
      },
      trigger: {
        type: 'calendar',
        hour: alarm.time.hour,
        minute: alarm.time.minute,
        repeats: alarm.repeatDays.length > 0,
      } as any,
    });
  }

  async cancelAlarm(alarmId: string): Promise<void> {
    if (useNativeAlarm) {
      ExpoAlarmEngine.cancelAlarm(alarmId);
      return;
    }

    // Fallback: cancel specific notification by identifier
    if (!Notifications) return;
    try {
      const scheduled = await Notifications.getAllScheduledNotificationsAsync();
      for (const notif of scheduled) {
        if (notif.content?.data?.alarmId === alarmId) {
          await Notifications.cancelScheduledNotificationAsync(notif.identifier);
        }
      }
    } catch {
      // Fallback to cancel all if individual cancel fails
      await Notifications.cancelAllScheduledNotificationsAsync();
    }
  }

  async snoozeAlarm(alarmId: string, minutes: number): Promise<void> {
    if (useNativeAlarm) {
      ExpoAlarmEngine.snoozeAlarm(alarmId, minutes);
      return;
    }

    // Fallback: expo-notifications
    if (!Notifications) return;

    await Notifications.scheduleNotificationAsync({
      content: {
        title: 'Speak2Wake',
        body: 'Snooze ended!',
        data: { alarmId },
        sound: true,
      },
      trigger: {
        type: 'timeInterval',
        seconds: minutes * 60,
      } as any,
    });
  }

  async dismissAlarm(alarmId: string): Promise<void> {
    if (useNativeAlarm) {
      ExpoAlarmEngine.dismissAlarm(alarmId);
      return;
    }

    // Fallback: dismiss specific notification by identifier
    if (!Notifications) return;
    try {
      const presented = await Notifications.getPresentedNotificationsAsync();
      for (const notif of presented) {
        if (notif.request?.content?.data?.alarmId === alarmId) {
          await Notifications.dismissNotificationAsync(notif.request.identifier);
        }
      }
    } catch {
      await Notifications.dismissAllNotificationsAsync();
    }
  }

  /**
   * Get the next scheduled trigger time for an alarm (native only).
   * Returns timestamp in milliseconds, or -1 if not found / not supported.
   */
  getNextAlarmTime(alarmId: string): number {
    if (useNativeAlarm) {
      return ExpoAlarmEngine.getNextAlarmTime(alarmId);
    }
    return -1;
  }

  /**
   * Pause alarm sound and vibration (for voice challenge mic/TTS).
   */
  async pauseAlarmSound(): Promise<void> {
    if (useNativeAlarm) {
      ExpoAlarmEngine.pauseAlarmSound();
    }
  }

  /**
   * Resume alarm sound and vibration.
   */
  async resumeAlarmSound(): Promise<void> {
    if (useNativeAlarm) {
      ExpoAlarmEngine.resumeAlarmSound();
    }
  }

  onAlarmFired(callback: (alarmId: string) => void): () => void {
    this.listeners.push(callback);
    return () => {
      this.listeners = this.listeners.filter((cb) => cb !== callback);
    };
  }

  /**
   * Clean up native event subscriptions.
   */
  destroy(): void {
    if (this.nativeSubscription) {
      this.nativeSubscription.remove();
      this.nativeSubscription = null;
    }
  }
}

export const AlarmService = new AlarmServiceImpl();
