import * as Speech from 'expo-speech';

export const TTSService = {
  speak(text: string, locale: string = 'de-DE'): Promise<void> {
    return new Promise((resolve, reject) => {
      Speech.speak(text, {
        language: locale,
        rate: 0.8,
        pitch: 1.0,
        onDone: () => resolve(),
        onError: (err) => reject(err),
        onStopped: () => resolve(),
      });
    });
  },

  stop(): void {
    Speech.stop();
  },

  isSpeaking(): Promise<boolean> {
    return Speech.isSpeakingAsync();
  },
};
