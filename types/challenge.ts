export enum ChallengeLevel {
  WORD = 1,
  SHORT_ANSWER = 2,
  SENTENCE = 3,
}

export interface ChallengeItem {
  id: string;
  level: ChallengeLevel | 'A1' | 'A2'; // 'A1'/'A2' maps to Level 1
  category: string;
  translation: string;
  phonetic?: string;

  // Level 1: Word Recognition
  word?: string;          // "der Hund"
  bare?: string;          // "Hund"
  article?: string;       // "der"
  difficulty?: 'single' | 'compound' | 'sentence';

  // Level 2: Short Answer
  question?: string;      // "Wie heißt du?"
  keywords?: string[];    // ["heiße", "Namen"]

  // Level 3: Sentence Construction
  targetText?: string;    // "Ich trinke Kaffee"

  hint?: string;          // Revealed after failed attempts
}

/**
 * Resolve effective ChallengeLevel from an item.
 * JSON data uses 'A1'/'A2' strings for legacy vocab — those map to WORD.
 */
export function getEffectiveLevel(item: ChallengeItem): ChallengeLevel {
  const lvl = item.level as unknown;
  if (lvl === ChallengeLevel.SHORT_ANSWER || lvl === 2) return ChallengeLevel.SHORT_ANSWER;
  if (lvl === ChallengeLevel.SENTENCE || lvl === 3) return ChallengeLevel.SENTENCE;
  return ChallengeLevel.WORD; // 'A1', 'A2', 1, or any other value
}

export interface ScoringResult {
  levenshteinScore: number;  // 0-1
  phoneticScore: number;     // 0-1
  confidenceScore: number;   // 0-1
  combinedScore: number;     // weighted 0-1
  threshold: number;
  passed: boolean;
  feedback: string;
}

export interface ChallengeState {
  currentItem: ChallengeItem | null;
  attempts: number;
  maxAttempts: number;
  lastResult: ScoringResult | null;
  isListening: boolean;
  partialText: string;
  failsafeActive: boolean;
}

export interface MathProblem {
  question: string;
  answer: number;
}
