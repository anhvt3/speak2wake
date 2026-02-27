import { levenshteinScore } from './levenshtein';
import { phoneticScore } from './phonetic';
import { normalize } from './normalizer';
import { SCORING_WEIGHTS, DYNAMIC_THRESHOLDS } from '../constants';
import { ChallengeLevel, getEffectiveLevel, type ScoringResult, type ChallengeItem } from '../types/challenge';

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
  item: ChallengeItem,
  spoken: string,
  sttConfidence: number
): ScoringResult {
  const normalizedSpoken = normalize(spoken);
  const clampedConfidence = Math.max(0, Math.min(1, sttConfidence));
  const level = getEffectiveLevel(item);

  if (level === ChallengeLevel.SHORT_ANSWER) {
    const keywords = item.keywords || [];
    let passed = false;
    for (const kw of keywords) {
      if (normalizedSpoken.includes(normalize(kw))) {
        passed = true;
        break;
      }
    }
    return {
      levenshteinScore: passed ? 1 : 0,
      phoneticScore: passed ? 1 : 0,
      confidenceScore: clampedConfidence,
      combinedScore: passed ? 1 : 0,
      threshold: 0.8,
      passed,
      feedback: passed ? 'Correct keyword detected!' : 'Try again. Missing key information.',
    };
  }

  if (level === ChallengeLevel.SENTENCE) {
    const expected = item.targetText || item.translation;
    const normalizedExpected = normalize(expected);
    const levScore = levenshteinScore(normalizedExpected, normalizedSpoken);

    // For sentences, rely more heavily on Levenshtein and slightly less on phonetic nuances
    const phonScore = phoneticScore(normalizedExpected, normalizedSpoken);
    const combinedScore = (levScore * 0.6) + (phonScore * 0.2) + (clampedConfidence * 0.2);

    // Sentences are inherently harder, use a slightly more lenient threshold logic
    const baseThreshold = getDynamicThreshold(normalizedExpected.length);
    const threshold = Math.max(0.6, baseThreshold - 0.05);
    const passed = combinedScore >= threshold;

    return {
      levenshteinScore: levScore,
      phoneticScore: phonScore,
      confidenceScore: clampedConfidence,
      combinedScore,
      threshold,
      passed,
      feedback: passed ? 'Excellent sentence!' : 'Not quite right. Try again.',
    };
  }

  // Fallback to Level 1 (Word recognition)
  const expected = item.bare || item.word || '';
  const normalizedExpected = normalize(expected);

  const levScore = levenshteinScore(normalizedExpected, normalizedSpoken);
  const phonScore = phoneticScore(normalizedExpected, normalizedSpoken);

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
