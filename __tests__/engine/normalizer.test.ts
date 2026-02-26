import { normalize, normalizeUmlauts } from '../../engine/normalizer';

describe('normalize', () => {
  it('converts to lowercase', () => {
    expect(normalize('HUND')).toBe('hund');
  });

  it('trims whitespace', () => {
    expect(normalize('  hund  ')).toBe('hund');
  });

  it('strips German articles', () => {
    expect(normalize('der Hund')).toBe('hund');
    expect(normalize('die Katze')).toBe('katze');
    expect(normalize('das Haus')).toBe('haus');
    expect(normalize('ein Hund')).toBe('hund');
    expect(normalize('eine Katze')).toBe('katze');
  });

  it('removes punctuation', () => {
    expect(normalize('Hund!')).toBe('hund');
    expect(normalize('Hund?')).toBe('hund');
  });

  it('handles word without article', () => {
    expect(normalize('Wasser')).toBe('wasser');
  });
});

describe('normalizeUmlauts', () => {
  it('converts umlauts to ASCII', () => {
    expect(normalizeUmlauts('\u00FCber')).toBe('ueber');
    expect(normalizeUmlauts('sch\u00F6n')).toBe('schoen');
    expect(normalizeUmlauts('\u00C4rger')).toBe('Aerger');
  });

  it('converts \u00DF to ss', () => {
    expect(normalizeUmlauts('Stra\u00DFe')).toBe('Strasse');
  });
});
