export const SCORING_WEIGHTS = {
  LEVENSHTEIN: 0.4,
  PHONETIC: 0.3,
  CONFIDENCE: 0.3,
} as const;

export const DYNAMIC_THRESHOLDS = {
  SHORT: { maxLength: 3, threshold: 0.80 },
  MEDIUM: { maxLength: 8, threshold: 0.70 },
  LONG: { maxLength: Infinity, threshold: 0.60 },
} as const;

export const MAX_VOICE_ATTEMPTS = 5;
export const DEFAULT_SNOOZE_MINUTES = 5;
export const DEFAULT_LANGUAGE = 'de-DE';
export const VOCABULARY_LEVELS = ['A1', 'A2'] as const;

export const ALARM_SOUNDS = [
  { id: 'default', name: 'Default', file: 'alarm_default' },
  { id: 'gentle', name: 'Gentle Rise', file: 'alarm_gentle' },
  { id: 'energetic', name: 'Energetic', file: 'alarm_energetic' },
  { id: 'nature', name: 'Nature', file: 'alarm_nature' },
  { id: 'digital', name: 'Digital', file: 'alarm_digital' },
] as const;

export const DAY_LABELS = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'] as const;
export const DAY_LABELS_SHORT = ['S', 'M', 'T', 'W', 'T', 'F', 'S'] as const;
