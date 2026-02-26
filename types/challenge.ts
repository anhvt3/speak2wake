export interface VocabWord {
  id: string;
  word: string;          // "der Hund"
  bare: string;          // "Hund"
  article: string;       // "der"
  translation: string;   // "the dog"
  level: 'A1' | 'A2';
  phonetic?: string;     // pronunciation hint
  category: string;
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
  currentWord: VocabWord | null;
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
