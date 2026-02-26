import { evaluateChallenge, getDynamicThreshold } from '../../engine/scoring';

describe('getDynamicThreshold', () => {
  it('returns 0.80 for short words (<=3 chars)', () => {
    expect(getDynamicThreshold(1)).toBe(0.80);
    expect(getDynamicThreshold(3)).toBe(0.80);
  });

  it('returns 0.70 for medium words (4-8 chars)', () => {
    expect(getDynamicThreshold(4)).toBe(0.70);
    expect(getDynamicThreshold(8)).toBe(0.70);
  });

  it('returns 0.60 for long words (>8 chars)', () => {
    expect(getDynamicThreshold(9)).toBe(0.60);
    expect(getDynamicThreshold(15)).toBe(0.60);
  });
});

describe('evaluateChallenge', () => {
  it('passes exact match with high confidence', () => {
    const result = evaluateChallenge('Hund', 'hund', 0.95);
    expect(result.passed).toBe(true);
    expect(result.combinedScore).toBeGreaterThan(0.9);
  });

  it('passes close match with good confidence', () => {
    const result = evaluateChallenge('Wasser', 'waser', 0.8);
    expect(result.passed).toBe(true);
  });

  it('fails completely wrong word', () => {
    const result = evaluateChallenge('Hund', 'katze', 0.3);
    expect(result.passed).toBe(false);
  });

  it('strips articles before comparing', () => {
    const result = evaluateChallenge('der Hund', 'hund', 0.9);
    expect(result.passed).toBe(true);
  });

  it('provides encouraging feedback for near-miss', () => {
    const result = evaluateChallenge('Schmetterling', 'schmetterlin', 0.7);
    // Near miss on a long word (threshold 0.60)
    expect(result.feedback).toBeTruthy();
  });

  it('clamps confidence between 0 and 1', () => {
    const result = evaluateChallenge('Hund', 'hund', 1.5);
    expect(result.confidenceScore).toBeLessThanOrEqual(1);
  });
});
