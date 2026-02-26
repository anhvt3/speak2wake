import { levenshteinDistance, levenshteinScore } from '../../engine/levenshtein';

describe('levenshteinDistance', () => {
  it('returns 0 for identical strings', () => {
    expect(levenshteinDistance('hund', 'hund')).toBe(0);
  });

  it('returns correct distance for single edit', () => {
    expect(levenshteinDistance('hund', 'hunt')).toBe(1);
  });

  it('returns correct distance for multiple edits', () => {
    expect(levenshteinDistance('hund', 'katze')).toBeGreaterThan(3);
  });

  it('handles empty strings', () => {
    expect(levenshteinDistance('', 'abc')).toBe(3);
    expect(levenshteinDistance('abc', '')).toBe(3);
    expect(levenshteinDistance('', '')).toBe(0);
  });
});

describe('levenshteinScore', () => {
  it('returns 1 for exact match', () => {
    expect(levenshteinScore('wasser', 'wasser')).toBe(1);
  });

  it('returns 0 for completely different strings of same length', () => {
    expect(levenshteinScore('abc', 'xyz')).toBeLessThan(0.1);
  });

  it('returns high score for close match', () => {
    expect(levenshteinScore('wasser', 'waser')).toBeGreaterThan(0.7);
  });

  it('returns 1 for two empty strings', () => {
    expect(levenshteinScore('', '')).toBe(1);
  });
});
