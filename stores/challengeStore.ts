import { create } from 'zustand';
import type { ChallengeItem, ScoringResult, ChallengeState } from '../types/challenge';
import { MAX_VOICE_ATTEMPTS } from '../constants';

interface ChallengeStoreState extends ChallengeState {
  startChallenge: (item: ChallengeItem) => void;
  recordAttempt: (result: ScoringResult) => void;
  setListening: (listening: boolean) => void;
  setPartialText: (text: string) => void;
  activateFailsafe: () => void;
  reset: () => void;
}

export const useChallengeStore = create<ChallengeStoreState>()((set, get) => ({
  currentItem: null,
  attempts: 0,
  maxAttempts: MAX_VOICE_ATTEMPTS,
  lastResult: null,
  isListening: false,
  partialText: '',
  failsafeActive: false,

  startChallenge: (item) => {
    set({
      currentItem: item,
      attempts: 0,
      lastResult: null,
      isListening: false,
      partialText: '',
      failsafeActive: false,
    });
  },

  recordAttempt: (result) => {
    const newAttempts = get().attempts + 1;
    set({
      lastResult: result,
      attempts: newAttempts,
      isListening: false,
      failsafeActive: !result.passed && newAttempts >= MAX_VOICE_ATTEMPTS,
    });
  },

  setListening: (listening) => {
    set({ isListening: listening, partialText: '' });
  },

  setPartialText: (text) => {
    set({ partialText: text });
  },

  activateFailsafe: () => {
    set({ failsafeActive: true });
  },

  reset: () => {
    set({
      currentItem: null,
      attempts: 0,
      lastResult: null,
      isListening: false,
      partialText: '',
      failsafeActive: false,
    });
  },
}));
