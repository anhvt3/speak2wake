import { evaluateChallenge } from '../engine/scoring';
import type { ScoringResult } from '../types/challenge';

export const ScoringService = {
  evaluate(expected: string, spoken: string, confidence: number): ScoringResult {
    return evaluateChallenge(expected, spoken, confidence);
  },
};
