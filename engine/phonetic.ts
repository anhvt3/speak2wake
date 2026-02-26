import { levenshteinDistance } from './levenshtein';

/**
 * Cologne Phonetic algorithm — optimized for German pronunciation.
 * Maps letters to phonetic codes based on surrounding context.
 *
 * Rules:
 * A,E,I,O,U -> 0 (vowels)
 * H -> (ignored)
 * B,P -> 1 (not before H)
 * D,T -> 2 (not before C,S,Z)
 * F,V,W -> 3
 * G,K,Q -> 4
 * X -> 48
 * L -> 5
 * M,N -> 6
 * R -> 7
 * S,Z -> 8
 * C -> 8 (initial before A,H,K,L,O,Q,R,U,X) or 4 (before A,H,K,O,Q,U,X after vowel) etc.
 */
export function colognePhonetic(word: string): string {
  if (!word) return '';

  const input = word.toUpperCase()
    .replace(/Ä/g, 'AE')
    .replace(/Ö/g, 'OE')
    .replace(/Ü/g, 'UE')
    .replace(/ß/g, 'SS')
    .replace(/[^A-Z]/g, '');

  if (!input) return '';

  const codes: string[] = [];

  for (let i = 0; i < input.length; i++) {
    const char = input[i];
    const prev = i > 0 ? input[i - 1] : '';
    const next = i < input.length - 1 ? input[i + 1] : '';

    let code = '';

    switch (char) {
      case 'A': case 'E': case 'I': case 'O': case 'U':
        code = '0';
        break;
      case 'H':
        code = '';
        break;
      case 'B':
      case 'P':
        code = next === 'H' ? '3' : '1';
        break;
      case 'D':
      case 'T':
        code = ['C', 'S', 'Z'].includes(next) ? '8' : '2';
        break;
      case 'F':
      case 'V':
      case 'W':
        code = '3';
        break;
      case 'G':
      case 'K':
      case 'Q':
        code = '4';
        break;
      case 'X':
        code = '48';
        break;
      case 'L':
        code = '5';
        break;
      case 'M':
      case 'N':
        code = '6';
        break;
      case 'R':
        code = '7';
        break;
      case 'S':
      case 'Z':
        code = '8';
        break;
      case 'C':
        if (i === 0) {
          code = ['A', 'H', 'K', 'L', 'O', 'Q', 'R', 'U', 'X'].includes(next) ? '4' : '8';
        } else {
          const vowels = ['A', 'E', 'I', 'O', 'U'];
          if (vowels.includes(prev) && ['A', 'H', 'K', 'O', 'Q', 'U', 'X'].includes(next)) {
            code = '4';
          } else if (['S', 'Z'].includes(prev)) {
            code = '8';
          } else {
            code = ['A', 'H', 'K', 'O', 'Q', 'U', 'X'].includes(next) ? '4' : '8';
          }
        }
        break;
      case 'J':
        code = '0';
        break;
      case 'Y':
        code = '0';
        break;
      default:
        code = '';
    }

    codes.push(code);
  }

  // Remove consecutive duplicates and then remove all '0' except leading
  let result = codes.join('');

  // Remove consecutive duplicate digits
  let deduped = '';
  for (let i = 0; i < result.length; i++) {
    if (i === 0 || result[i] !== result[i - 1]) {
      deduped += result[i];
    }
  }

  // Remove zeros except initial position
  if (deduped.length > 0) {
    result = deduped[0] + deduped.substring(1).replace(/0/g, '');
  } else {
    result = '';
  }

  return result;
}

/**
 * Compare phonetic codes and return similarity score (0-1)
 */
export function phoneticScore(expected: string, spoken: string): number {
  const code1 = colognePhonetic(expected);
  const code2 = colognePhonetic(spoken);

  if (!code1 && !code2) return 1;
  if (!code1 || !code2) return 0;
  if (code1 === code2) return 1;

  // Use Levenshtein on phonetic codes for partial match
  const maxLen = Math.max(code1.length, code2.length);
  const dist = levenshteinDistance(code1, code2);
  return 1 - dist / maxLen;
}
