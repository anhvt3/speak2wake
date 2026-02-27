import { evaluateChallenge } from '../engine/scoring';
import type { ScoringResult, ChallengeItem } from '../types/challenge';

export const ScoringService = {
  evaluate(item: ChallengeItem, spoken: string, confidence: number): ScoringResult {
    return evaluateChallenge(item, spoken, confidence);
  },
};
