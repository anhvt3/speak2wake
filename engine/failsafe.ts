import type { MathProblem } from '../types/challenge';

/**
 * Generate a random math problem for fail-safe mode.
 * Uses addition or subtraction of 2-digit numbers.
 */
export function generateMathProblem(): MathProblem {
  const ops = ['+', '-'] as const;
  const op = ops[Math.floor(Math.random() * ops.length)];

  let a: number, b: number, answer: number;

  if (op === '+') {
    a = Math.floor(Math.random() * 90) + 10; // 10-99
    b = Math.floor(Math.random() * 90) + 10;
    answer = a + b;
  } else {
    a = Math.floor(Math.random() * 90) + 10;
    b = Math.floor(Math.random() * (a - 1)) + 1; // ensure positive result
    answer = a - b;
  }

  return {
    question: `${a} ${op} ${b} = ?`,
    answer,
  };
}

/**
 * Check if typed text matches expected word (for text fail-safe).
 * Case-insensitive, trims whitespace, strips articles.
 */
export function checkTextFailsafe(expected: string, typed: string): boolean {
  const normalizeText = (s: string) =>
    s.toLowerCase().trim()
      .replace(/^(der|die|das|ein|eine)\s+/i, '')
      .trim();

  return normalizeText(typed) === normalizeText(expected);
}
