import { type EventSubscription } from 'expo-modules-core';
import ExpoAlarmEngineModule from './src/ExpoAlarmEngineModule';

// Create EventEmitter once at module level (use require to avoid TS generic inference issue)
const { EventEmitter } = require('expo-modules-core');
const emitter = new EventEmitter(ExpoAlarmEngineModule);

export type AlarmFiredEvent = {
  alarmId: string;
};

/**
 * Schedule an alarm using Android's AlarmManager.setExactAndAllowWhileIdle.
 * The alarm will fire even when the app is killed or the device is in Doze mode.
 *
 * @param alarmId - Unique identifier for this alarm
 * @param hour - Hour of day (0-23)
 * @param minute - Minute (0-59)
 * @param repeatDays - Days of week to repeat (0=Sun, 1=Mon, ..., 6=Sat). Empty for one-shot.
 * @param soundId - Sound identifier (for future custom sound support)
 * @param label - Display label for the alarm notification
 */
export function scheduleAlarm(
  alarmId: string,
  hour: number,
  minute: number,
  repeatDays: number[],
  soundId: string,
  label: string
): void {
  return ExpoAlarmEngineModule.scheduleAlarm(
    alarmId,
    hour,
    minute,
    repeatDays,
    soundId,
    label
  );
}

/**
 * Cancel a previously scheduled alarm.
 *
 * @param alarmId - The ID of the alarm to cancel
 */
export function cancelAlarm(alarmId: string): void {
  return ExpoAlarmEngineModule.cancelAlarm(alarmId);
}

/**
 * Snooze an active alarm for the specified number of minutes.
 * Dismisses the current alarm and reschedules it.
 *
 * @param alarmId - The ID of the alarm to snooze
 * @param minutes - Number of minutes to snooze
 */
export function snoozeAlarm(alarmId: string, minutes: number): void {
  return ExpoAlarmEngineModule.snoozeAlarm(alarmId, minutes);
}

/**
 * Dismiss an active alarm. Stops the alarm sound and foreground service.
 * If the alarm is repeating, it will be rescheduled for the next occurrence.
 *
 * @param alarmId - The ID of the alarm to dismiss
 */
export function dismissAlarm(alarmId: string): void {
  return ExpoAlarmEngineModule.dismissAlarm(alarmId);
}

/**
 * Get the next scheduled trigger time for an alarm.
 *
 * @param alarmId - The ID of the alarm
 * @returns The next trigger time in milliseconds since epoch, or -1 if not found
 */
export function getNextAlarmTime(alarmId: string): number {
  return ExpoAlarmEngineModule.getNextAlarmTime(alarmId);
}

/**
 * Pause the alarm sound and vibration.
 * Used during voice challenge when mic is active or TTS is speaking.
 */
export function pauseAlarmSound(): void {
  return ExpoAlarmEngineModule.pauseAlarmSound();
}

/**
 * Resume the alarm sound and vibration.
 * Called when mic stops or TTS finishes.
 */
export function resumeAlarmSound(): void {
  return ExpoAlarmEngineModule.resumeAlarmSound();
}

/**
 * Subscribe to alarm fired events. Called when an alarm triggers.
 *
 * @param listener - Callback receiving the alarm ID that fired
 * @returns Subscription that can be removed to stop listening
 */
export function addAlarmFiredListener(
  listener: (event: AlarmFiredEvent) => void
): EventSubscription {
  return emitter.addListener('onAlarmFired', listener);
}

export default {
  scheduleAlarm,
  cancelAlarm,
  snoozeAlarm,
  dismissAlarm,
  getNextAlarmTime,
  pauseAlarmSound,
  resumeAlarmSound,
  addAlarmFiredListener,
};
