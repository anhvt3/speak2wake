import { evaluateChallenge, getDynamicThreshold } from '../../engine/scoring';
import { ChallengeLevel, type ChallengeItem } from '../../types/challenge';

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

describe('evaluateChallenge - Level 1 (Word)', () => {
  const mockWord = (word: string, overrides?: Partial<ChallengeItem>): ChallengeItem => ({
    id: '1', level: ChallengeLevel.WORD, category: 'test', translation: 'test', word, bare: word,
    ...overrides,
  });

  it('passes exact match with high confidence', () => {
    const result = evaluateChallenge(mockWord('Hund'), 'hund', 0.95);
    expect(result.passed).toBe(true);
    expect(result.combinedScore).toBeGreaterThan(0.9);
  });

  it('passes close match with good confidence', () => {
    const result = evaluateChallenge(mockWord('Wasser'), 'waser', 0.8);
    expect(result.passed).toBe(true);
  });

  it('fails completely wrong word', () => {
    const result = evaluateChallenge(mockWord('Hund'), 'katze', 0.3);
    expect(result.passed).toBe(false);
  });

  it('strips articles before comparing', () => {
    const result = evaluateChallenge(mockWord('der Hund'), 'hund', 0.9);
    expect(result.passed).toBe(true);
  });

  it('provides encouraging feedback for near-miss', () => {
    const result = evaluateChallenge(mockWord('Schmetterling'), 'schmetterlin', 0.7);
    expect(result.feedback).toBeTruthy();
  });

  it('clamps confidence between 0 and 1', () => {
    const result = evaluateChallenge(mockWord('Hund'), 'hund', 1.5);
    expect(result.confidenceScore).toBeLessThanOrEqual(1);
  });

  it('handles legacy "A1" string level as Level 1', () => {
    const item = mockWord('Hund', { level: 'A1' as any });
    const result = evaluateChallenge(item, 'hund', 0.95);
    expect(result.passed).toBe(true);
  });

  it('handles legacy "A2" string level as Level 1', () => {
    const item = mockWord('Bär', { level: 'A2' as any });
    const result = evaluateChallenge(item, 'bär', 0.9);
    expect(result.passed).toBe(true);
  });

  it('handles empty spoken text gracefully', () => {
    const result = evaluateChallenge(mockWord('Hund'), '', 0.0);
    expect(result.passed).toBe(false);
  });
});

describe('evaluateChallenge - Level 2 (Short Answer)', () => {
  const mockLevel2 = (keywords: string[]): ChallengeItem => ({
    id: '2', level: ChallengeLevel.SHORT_ANSWER, category: 'test', translation: 'test', keywords
  });

  it('passes if any keyword is present', () => {
    const result = evaluateChallenge(mockLevel2(['heiße', 'bin']), 'ich heiße Anna', 0.9);
    expect(result.passed).toBe(true);
    expect(result.combinedScore).toBe(1);
    expect(result.feedback).toBe('Correct keyword detected!');
  });

  it('fails if no keyword is present', () => {
    const result = evaluateChallenge(mockLevel2(['heiße', 'bin']), 'ich mache Anna', 0.9);
    expect(result.passed).toBe(false);
    expect(result.combinedScore).toBe(0);
  });

  it('matches keywords case-insensitively', () => {
    const result = evaluateChallenge(mockLevel2(['Heiße']), 'ich heiße', 0.9);
    expect(result.passed).toBe(true);
  });

  it('handles empty keywords array gracefully', () => {
    const result = evaluateChallenge(mockLevel2([]), 'ich heiße Anna', 0.9);
    expect(result.passed).toBe(false);
  });

  it('handles raw number 2 as level from JSON', () => {
    const item: ChallengeItem = {
      id: '2j', level: 2 as any, category: 'test', translation: 'test', keywords: ['trinke']
    };
    const result = evaluateChallenge(item, 'ich trinke Kaffee', 0.9);
    expect(result.passed).toBe(true);
  });
});

describe('evaluateChallenge - Level 3 (Sentence)', () => {
  const mockLevel3 = (targetText: string): ChallengeItem => ({
    id: '3', level: ChallengeLevel.SENTENCE, category: 'test', translation: 'test', targetText
  });

  it('passes close sentence match', () => {
    const result = evaluateChallenge(mockLevel3('Ich trinke Kaffee mit Milch'), 'ich trinke kaffe mit milch', 0.9);
    expect(result.passed).toBe(true);
  });

  it('fails completely different sentence', () => {
    const result = evaluateChallenge(mockLevel3('Ich trinke Kaffee mit Milch'), 'das wetter ist schön', 0.9);
    expect(result.passed).toBe(false);
  });

  it('provides feedback text on failure', () => {
    const result = evaluateChallenge(mockLevel3('Ich trinke Kaffee mit Milch'), 'Hund', 0.5);
    expect(result.feedback).toBe('Not quite right. Try again.');
  });

  it('handles raw number 3 as level from JSON', () => {
    const item: ChallengeItem = {
      id: '3j', level: 3 as any, category: 'test', translation: 'I learn German', targetText: 'Ich lerne Deutsch'
    };
    const result = evaluateChallenge(item, 'ich lerne deutsch', 0.9);
    expect(result.passed).toBe(true);
  });
});

