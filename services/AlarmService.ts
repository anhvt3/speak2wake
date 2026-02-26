import type { Alarm } from '../types/alarm';

/**
 * AlarmService manages scheduling/canceling alarms.
 * Phase 1: Uses expo-notifications as placeholder.
 * Phase 1.5: Swap to native Kotlin module (modules/expo-alarm-engine).
 */

let Notifications: any = null;
try {
  Notifications = require('expo-notifications');
} catch {
  // Not available in web/preview
}

class AlarmServiceImpl {
  private listeners: ((alarmId: string) => void)[] = [];

  async initialize(): Promise<void> {
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
    if (!Notifications) return;
    await Notifications.cancelAllScheduledNotificationsAsync();
  }

  async snoozeAlarm(alarmId: string, minutes: number): Promise<void> {
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
    // Stop sound, clear active alarm state
    if (!Notifications) return;
    await Notifications.dismissAllNotificationsAsync();
  }

  onAlarmFired(callback: (alarmId: string) => void): () => void {
    this.listeners.push(callback);
    return () => {
      this.listeners = this.listeners.filter((cb) => cb !== callback);
    };
  }
}

export const AlarmService = new AlarmServiceImpl();
