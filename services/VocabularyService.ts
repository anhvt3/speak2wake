import vocabularyData from '../data/vocabulary-de-a1a2.json';
import { ChallengeLevel, getEffectiveLevel, type ChallengeItem } from '../types/challenge';

let cachedVocabulary: ChallengeItem[] | null = null;

export const VocabularyService = {
  load(): ChallengeItem[] {
    if (!cachedVocabulary) {
      cachedVocabulary = vocabularyData as ChallengeItem[];
    }
    return cachedVocabulary;
  },

  getRandomItem(category?: string, level?: number): ChallengeItem {
    const vocab = this.load();
    let filtered = vocab;

    if (category) {
      filtered = filtered.filter((w) => w.category === category);
    }
    if (level !== undefined) {
      filtered = filtered.filter((w) => getEffectiveLevel(w) === level);
    }

    const pool = filtered.length > 0 ? filtered : vocab;
    return pool[Math.floor(Math.random() * pool.length)];
  },

  getItemById(id: string): ChallengeItem | undefined {
    return this.load().find((w) => w.id === id);
  },

  getCategories(): string[] {
    const cats = new Set(this.load().map((w) => w.category));
    return Array.from(cats).sort();
  },

  getWordCount(): number {
    return this.load().length;
  },
};
