export type DayOfWeek = 0 | 1 | 2 | 3 | 4 | 5 | 6;

export interface Alarm {
  id: string;
  time: { hour: number; minute: number };
  label: string;
  enabled: boolean;
  repeatDays: DayOfWeek[];
  soundId: string;
  vibrationEnabled: boolean;
  snoozeEnabled: boolean;
  snoozeDuration: number; // minutes
  challengeEnabled: boolean;
  challengeLevel: number; // 1, 2, or 3
  createdAt: number;
  lastTriggeredAt?: number;
}

export interface AlarmHistory {
  alarmId: string;
  date: string;
  triggeredAt: number;
  completedAt?: number;
  timeToDismiss: number;
  snoozeCount: number;
  usedFailSafe: boolean;
  success: boolean;
}
