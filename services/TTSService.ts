import * as Speech from 'expo-speech';

export const TTSService = {
  async speak(text: string, locale: string = 'de-DE'): Promise<void> {
    await Speech.speak(text, {
      language: locale,
      rate: 0.8,
      pitch: 1.0,
    });
  },

  async stop(): Promise<void> {
    await Speech.stop();
  },

  isSpeaking(): Promise<boolean> {
    return Speech.isSpeakingAsync();
  },
};
