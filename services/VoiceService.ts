/**
 * VoiceService wraps @react-native-voice/voice for STT.
 * In development/preview, provides a mock implementation.
 */

type VoiceCallback = (text: string, confidence: number) => void;
type PartialCallback = (text: string) => void;
type VolumeCallback = (volume: number) => void;
type ErrorCallback = (error: { code: string; message: string }) => void;

let Voice: any = null;

try {
  Voice = require('@react-native-voice/voice').default;
} catch {
  // Voice module not available (web/preview mode)
}

class VoiceServiceImpl {
  private onResultCallback: VoiceCallback | null = null;
  private onPartialCallback: PartialCallback | null = null;
  private onVolumeCallback: VolumeCallback | null = null;
  private onErrorCallback: ErrorCallback | null = null;
  private initialized = false;

  async initialize(): Promise<void> {
    if (this.initialized || !Voice) return;

    Voice.onSpeechResults = (e: any) => {
      const results: string[] = e.value || [];
      if (results.length > 0 && this.onResultCallback) {
        this.onResultCallback(results[0], 0.8);
      }
    };

    Voice.onSpeechPartialResults = (e: any) => {
      const results: string[] = e.value || [];
      if (results.length > 0 && this.onPartialCallback) {
        this.onPartialCallback(results[0]);
      }
    };

    Voice.onSpeechVolumeChanged = (e: any) => {
      if (this.onVolumeCallback) {
        this.onVolumeCallback(e.value || 0);
      }
    };

    Voice.onSpeechError = (e: any) => {
      if (this.onErrorCallback) {
        this.onErrorCallback({
          code: e.error?.code || 'unknown',
          message: e.error?.message || 'Speech recognition error',
        });
      }
    };

    this.initialized = true;
  }

  async startListening(locale: string = 'de-DE'): Promise<void> {
    if (!Voice) {
      // Mock for development
      setTimeout(() => {
        this.onPartialCallback?.('mock...');
        setTimeout(() => {
          this.onResultCallback?.('mock result', 0.85);
        }, 1000);
      }, 500);
      return;
    }

    await this.initialize();
    await Voice.start(locale);
  }

  async stopListening(): Promise<void> {
    if (!Voice) return;
    await Voice.stop();
  }

  async cancel(): Promise<void> {
    if (!Voice) return;
    await Voice.cancel();
  }

  async isAvailable(): Promise<boolean> {
    if (!Voice) return false;
    const available = await Voice.isAvailable();
    return available === 1 || available === true;
  }

  onResult(callback: VoiceCallback): void {
    this.onResultCallback = callback;
  }

  onPartialResult(callback: PartialCallback): void {
    this.onPartialCallback = callback;
  }

  onVolumeChange(callback: VolumeCallback): void {
    this.onVolumeCallback = callback;
  }

  onError(callback: ErrorCallback): void {
    this.onErrorCallback = callback;
  }

  destroy(): void {
    if (Voice) Voice.destroy();
    this.onResultCallback = null;
    this.onPartialCallback = null;
    this.onVolumeCallback = null;
    this.onErrorCallback = null;
    this.initialized = false;
  }
}

export const VoiceService = new VoiceServiceImpl();
