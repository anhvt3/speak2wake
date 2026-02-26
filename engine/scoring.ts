import { levenshteinScore } from './levenshtein';
import { phoneticScore } from './phonetic';
import { normalize } from './normalizer';
import { SCORING_WEIGHTS, DYNAMIC_THRESHOLDS } from '../constants';
import type { ScoringResult } from '../types/challenge';

/**
 * Get dynamic threshold based on word length.
 * Short words need higher accuracy, long words are more lenient.
 */
export function getDynamicThreshold(wordLength: number): number {
  if (wordLength <= DYNAMIC_THRESHOLDS.SHORT.maxLength) {
    return DYNAMIC_THRESHOLDS.SHORT.threshold;
  }
  if (wordLength <= DYNAMIC_THRESHOLDS.MEDIUM.maxLength) {
    return DYNAMIC_THRESHOLDS.MEDIUM.threshold;
  }
  return DYNAMIC_THRESHOLDS.LONG.threshold;
}

/**
 * Evaluate a voice challenge attempt.
 * Combines Levenshtein, phonetic, and STT confidence scores.
 */
export function evaluateChallenge(
  expected: string,
  spoken: string,
  sttConfidence: number
): ScoringResult {
  const normalizedExpected = normalize(expected);
  const normalizedSpoken = normalize(spoken);

  const levScore = levenshteinScore(normalizedExpected, normalizedSpoken);
  const phonScore = phoneticScore(normalizedExpected, normalizedSpoken);
  const clampedConfidence = Math.max(0, Math.min(1, sttConfidence));

  const combinedScore =
    levScore * SCORING_WEIGHTS.LEVENSHTEIN +
    phonScore * SCORING_WEIGHTS.PHONETIC +
    clampedConfidence * SCORING_WEIGHTS.CONFIDENCE;

  const threshold = getDynamicThreshold(normalizedExpected.length);
  const passed = combinedScore >= threshold;

  let feedback: string;
  if (passed) {
    feedback = combinedScore >= 0.9 ? 'Perfect!' : 'Correct! Well done.';
  } else if (combinedScore >= threshold - 0.1) {
    feedback = 'Almost! Try once more.';
  } else {
    feedback = 'Not quite. Listen and try again.';
  }

  return {
    levenshteinScore: levScore,
    phoneticScore: phonScore,
    confidenceScore: clampedConfidence,
    combinedScore,
    threshold,
    passed,
    feedback,
  };
}
