import { generateMathProblem, checkTextFailsafe } from '../../engine/failsafe';

describe('generateMathProblem', () => {
    it('returns a question string and numeric answer', () => {
        const problem = generateMathProblem();
        expect(typeof problem.question).toBe('string');
        expect(typeof problem.answer).toBe('number');
        expect(problem.question).toContain('=');
    });

    it('uses only + or - operators', () => {
        for (let i = 0; i < 50; i++) {
            const problem = generateMathProblem();
            const hasPlus = problem.question.includes('+');
            const hasMinus = problem.question.includes('-');
            expect(hasPlus || hasMinus).toBe(true);
        }
    });

    it('produces correct answers for addition', () => {
        // Run multiple times to catch both operators
        for (let i = 0; i < 100; i++) {
            const problem = generateMathProblem();
            const match = problem.question.match(/(\d+)\s*([+\-])\s*(\d+)/);
            expect(match).not.toBeNull();
            if (match) {
                const a = parseInt(match[1]);
                const op = match[2];
                const b = parseInt(match[3]);
                const expected = op === '+' ? a + b : a - b;
                expect(problem.answer).toBe(expected);
            }
        }
    });

    it('subtraction always produces non-negative result', () => {
        for (let i = 0; i < 100; i++) {
            const problem = generateMathProblem();
            expect(problem.answer).toBeGreaterThanOrEqual(0);
        }
    });

    it('uses 2-digit numbers (10-99 range)', () => {
        for (let i = 0; i < 50; i++) {
            const problem = generateMathProblem();
            const match = problem.question.match(/(\d+)\s*[+\-]\s*(\d+)/);
            expect(match).not.toBeNull();
            if (match) {
                const a = parseInt(match[1]);
                expect(a).toBeGreaterThanOrEqual(10);
                expect(a).toBeLessThanOrEqual(99);
            }
        }
    });
});

describe('checkTextFailsafe', () => {
    it('returns true for exact match', () => {
        expect(checkTextFailsafe('Hund', 'Hund')).toBe(true);
    });

    it('is case-insensitive', () => {
        expect(checkTextFailsafe('Hund', 'hund')).toBe(true);
        expect(checkTextFailsafe('WASSER', 'wasser')).toBe(true);
    });

    it('trims whitespace', () => {
        expect(checkTextFailsafe('Hund', '  hund  ')).toBe(true);
    });

    it('strips German articles from expected', () => {
        expect(checkTextFailsafe('der Hund', 'hund')).toBe(true);
        expect(checkTextFailsafe('die Katze', 'katze')).toBe(true);
        expect(checkTextFailsafe('das Haus', 'haus')).toBe(true);
    });

    it('strips German articles from typed', () => {
        expect(checkTextFailsafe('Hund', 'der hund')).toBe(true);
    });

    it('returns false for wrong word', () => {
        expect(checkTextFailsafe('Hund', 'katze')).toBe(false);
    });

    it('returns false for partial match', () => {
        expect(checkTextFailsafe('Schmetterling', 'schmet')).toBe(false);
    });

    it('handles empty strings', () => {
        expect(checkTextFailsafe('', '')).toBe(true);
        expect(checkTextFailsafe('Hund', '')).toBe(false);
    });
});
