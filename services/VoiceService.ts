/**
 * VoiceService wraps expo-speech-recognition for STT.
 * Uses the modern Expo module instead of deprecated @react-native-voice/voice.
 */

type VoiceCallback = (text: string, confidence: number) => void;
type PartialCallback = (text: string) => void;
type VolumeCallback = (volume: number) => void;
type ErrorCallback = (error: { code: string; message: string }) => void;

let ExpoSpeechRecognition: any = null;

try {
  ExpoSpeechRecognition = require('expo-speech-recognition');
} catch {
  // Module not available (web/preview mode)
}

class VoiceServiceImpl {
  private onResultCallback: VoiceCallback | null = null;
  private onPartialCallback: PartialCallback | null = null;
  private onVolumeCallback: VolumeCallback | null = null;
  private onErrorCallback: ErrorCallback | null = null;
  private subscriptions: any[] = [];
  private permissionGranted: boolean | null = null;
  private listenTimeout: ReturnType<typeof setTimeout> | null = null;

  async startListening(locale: string = 'de-DE'): Promise<void> {
    if (!ExpoSpeechRecognition) {
      // Mock for development — return the expected word so dev can test full flow
      setTimeout(() => {
        this.onPartialCallback?.('mock...');
        setTimeout(() => {
          this.onResultCallback?.('mock result', 0.85);
        }, 1000);
      }, 500);
      return;
    }

    // Request permissions (cache result)
    if (this.permissionGranted === null) {
      const { status } = await ExpoSpeechRecognition.requestPermissionsAsync();
      this.permissionGranted = status === 'granted';
    }
    if (!this.permissionGranted) {
      this.onErrorCallback?.({ code: 'permission', message: 'Microphone permission denied' });
      return;
    }

    // Remove old listeners before adding new ones
    this.removeAllSubscriptions();

    // Listen for results
    const resultSub = ExpoSpeechRecognition.addSpeechEventListener('result', (event: any) => {
      if (event.isFinal && event.results && event.results.length > 0) {
        this.clearTimeout();
        const result = event.results[0];
        this.onResultCallback?.(result.transcript, result.confidence || 0.8);
      } else if (!event.isFinal && event.results && event.results.length > 0) {
        this.onPartialCallback?.(event.results[0].transcript);
      }
    });

    const errorSub = ExpoSpeechRecognition.addSpeechEventListener('error', (event: any) => {
      this.clearTimeout();
      this.onErrorCallback?.({
        code: event.error || 'unknown',
        message: event.message || 'Speech recognition error',
      });
    });

    const volumeSub = ExpoSpeechRecognition.addSpeechEventListener('volumechange', (event: any) => {
      this.onVolumeCallback?.(event.value || 0);
    });

    this.subscriptions = [resultSub, errorSub, volumeSub];

    // Start recognition
    ExpoSpeechRecognition.start({
      lang: locale,
      interimResults: true,
      maxAlternatives: 1,
      continuous: false,
    });

    // 10-second timeout — auto-stop if no final result
    this.listenTimeout = setTimeout(() => {
      this.stopListening();
      this.onErrorCallback?.({ code: 'timeout', message: 'No speech detected (10s timeout)' });
    }, 10000);
  }

  private clearTimeout(): void {
    if (this.listenTimeout) {
      clearTimeout(this.listenTimeout);
      this.listenTimeout = null;
    }
  }

  private removeAllSubscriptions(): void {
    for (const sub of this.subscriptions) {
      try { sub?.remove(); } catch {}
    }
    this.subscriptions = [];
  }

  async stopListening(): Promise<void> {
    this.clearTimeout();
    if (!ExpoSpeechRecognition) return;
    ExpoSpeechRecognition.stop();
  }

  async cancel(): Promise<void> {
    this.clearTimeout();
    if (!ExpoSpeechRecognition) return;
    ExpoSpeechRecognition.abort();
  }

  async isAvailable(): Promise<boolean> {
    if (!ExpoSpeechRecognition) return false;
    try {
      const result = await ExpoSpeechRecognition.isRecognitionAvailable();
      return result === true;
    } catch {
      return false;
    }
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
    this.clearTimeout();
    if (ExpoSpeechRecognition) {
      try {
        ExpoSpeechRecognition.stop();
      } catch {}
    }
    this.removeAllSubscriptions();
    this.onResultCallback = null;
    this.onPartialCallback = null;
    this.onVolumeCallback = null;
    this.onErrorCallback = null;
  }
}

export const VoiceService = new VoiceServiceImpl();
