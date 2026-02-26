import { colognePhonetic, phoneticScore } from '../../engine/phonetic';

describe('colognePhonetic', () => {
  it('encodes German words correctly', () => {
    // "Muller" -> "MUELLER" -> should produce a code
    const code = colognePhonetic('Mueller');
    expect(code).toBeTruthy();
    expect(typeof code).toBe('string');
  });

  it('produces same code for phonetically similar words', () => {
    // "Wasser" and "Vasser" should be similar
    expect(colognePhonetic('Wasser')).toBe(colognePhonetic('Vasser'));
  });

  it('handles empty string', () => {
    expect(colognePhonetic('')).toBe('');
  });

  it('handles umlauts', () => {
    const code = colognePhonetic('\u00FCber');
    expect(code).toBeTruthy();
  });
});

describe('phoneticScore', () => {
  it('returns 1 for exact phonetic match', () => {
    expect(phoneticScore('Wasser', 'Wasser')).toBe(1);
  });

  it('returns high score for phonetically similar words', () => {
    expect(phoneticScore('Wasser', 'Vasser')).toBeGreaterThan(0.8);
  });

  it('returns low score for phonetically different words', () => {
    expect(phoneticScore('Hund', 'Katze')).toBeLessThan(0.5);
  });
});
