import vocabularyData from '../data/vocabulary-de-a1a2.json';
import type { VocabWord } from '../types/challenge';

let cachedVocabulary: VocabWord[] | null = null;

export const VocabularyService = {
  load(): VocabWord[] {
    if (!cachedVocabulary) {
      cachedVocabulary = vocabularyData as VocabWord[];
    }
    return cachedVocabulary;
  },

  getRandomWord(category?: string): VocabWord {
    const vocab = this.load();
    const filtered = category
      ? vocab.filter((w) => w.category === category)
      : vocab;
    const pool = filtered.length > 0 ? filtered : vocab;
    return pool[Math.floor(Math.random() * pool.length)];
  },

  getWordById(id: string): VocabWord | undefined {
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
