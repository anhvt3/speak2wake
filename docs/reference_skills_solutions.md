# 📱 Speak2Wake — Skills & Giải pháp từ Reference Repos

> **Nguồn phân tích:** 4 repos GitHub
> - `kuraykaraaslan/expo-react-native-boilerplate` — Expo starter
> - `react-native-voice/voice` — STT library (2.2k stars)
> - `bluesky-social/social-app` — Production Expo app (10k+ stars)
> - `infinitered/ignite` — RN boilerplate CLI (9k+ stars)

---

## 1. Kiến trúc Dự án (Project Structure)

### Từ Ignite — Cấu trúc thư mục chuẩn

```
app/
├── components/     # UI components tái sử dụng
├── screens/        # Màn hình (1 file = 1 screen)
├── navigators/     # React Navigation config
├── services/       # API, storage, external services
├── theme/          # Colors, spacing, typography, ThemeProvider
├── config/         # App config, environment vars
├── context/        # React Context (Auth, etc.)
├── i18n/           # Đa ngôn ngữ (i18next)
├── utils/          # Helper functions
├── devtools/       # Reactotron, debug tools
└── app.tsx         # Root component
```

> **Áp dụng cho Speak2Wake:** Dùng cấu trúc này làm base, thêm:
> - `engine/` — Alarm engine, scoring engine, STT pipeline
> - `data/` — Từ vựng JSON, migration utils
> - `native-modules/` — Custom Kotlin/Swift modules (alarm, foreground service)

### Từ Bluesky — Module hóa Native Code

Bluesky tách native code thành **Expo Modules riêng** trong `modules/`:

```
modules/
├── expo-background-notification-handler/  # Xử lý notification nền
│   ├── android/    # Kotlin code
│   ├── ios/        # Swift code
│   ├── src/        # TypeScript wrapper
│   └── expo-module.config.json
├── expo-receive-android-intents/          # Nhận intents từ Android
└── expo-scroll-forwarder/                 # Custom scroll behavior
```

> **Áp dụng cho Speak2Wake:** Tạo custom Expo module cho alarm:
> ```
> modules/
> ├── expo-alarm-engine/        # AlarmManager + ForegroundService
> │   ├── android/src/main/     # Kotlin: AlarmReceiver, ForegroundService
> │   ├── ios/                  # Swift: UNNotification + Time Sensitive
> │   ├── src/index.ts          # TypeScript API
> │   └── expo-module.config.json
> └── expo-voice-challenge/     # STT wrapper với scoring
> ```

---

## 2. State Management

### Pattern 1: Zustand Store (từ expo-boilerplate)

```typescript
// libs/zustand/alarmStore.ts
import { create } from 'zustand'

type AlarmState = {
  alarms: Alarm[]
  activeAlarm: Alarm | null
  setAlarms: (alarms: Alarm[]) => void
  setActiveAlarm: (alarm: Alarm | null) => void
  addAlarm: (alarm: Alarm) => void
  removeAlarm: (id: string) => void
}

const useAlarmStore = create<AlarmState>((set) => ({
  alarms: [],
  activeAlarm: null,
  setAlarms: (alarms) => set({ alarms }),
  setActiveAlarm: (alarm) => set({ activeAlarm: alarm }),
  addAlarm: (alarm) => set((s) => ({ alarms: [...s.alarms, alarm] })),
  removeAlarm: (id) => set((s) => ({ alarms: s.alarms.filter(a => a.id !== id) })),
}))
```

### Pattern 2: Persisted State Schema (từ Bluesky)

Bluesky dùng **versioned schema** cho persisted state — rất relevant cho Speak2Wake khi cần migrate AsyncStorage → SQLite:

```typescript
// state/persisted/schema.ts
export const schema = {
  version: 1,
  data: {
    alarms: [],
    streakCount: 0,
    vocabularyProgress: {},
    settings: { language: 'de', difficulty: 1 }
  }
}
// Khi upgrade version: transform old data → new schema
```

### Pattern 3: Service Injection (từ expo-boilerplate)

Services được inject vào layout root — tách business logic khỏi UI:

```typescript
// app/_layout.tsx
import { AlarmService } from '@/services/AlarmService'
import * as ZustandStore from '@/libs/zustand'
import * as SecureStore from 'expo-secure-store'

export default function RootLayout() {
  AlarmService.initialize(ZustandStore, SecureStore)
  // ...
}
```

---

## 3. STT Integration — API & Patterns (từ react-native-voice)

### Core API

```typescript
import Voice, { SpeechResultsEvent, SpeechErrorEvent } from '@react-native-voice/voice'

// Bắt đầu nhận diện giọng nói (với locale)
await Voice.start('de-DE')  // Tiếng Đức

// Dừng
await Voice.stop()

// Hủy
await Voice.cancel()

// Kiểm tra khả dụng
const available = await Voice.isAvailable()  // 0 | 1

// Android: Lấy danh sách STT engines
const services = Voice.getSpeechRecognitionServices()
```

### Event Handling

```typescript
// Kết quả (có thể nhiều alternatives)
Voice.onSpeechResults = (e: SpeechResultsEvent) => {
  const results: string[] = e.value || []
  // results[0] = best match, results[1..n] = alternatives
  processResults(results)
}

// Partial results (real-time khi đang nói)
Voice.onSpeechPartialResults = (e: SpeechResultsEvent) => {
  // Hiển thị text real-time trên UI
  setPartialText(e.value?.[0] || '')
}

// Volume change (cho hiệu ứng sóng âm)
Voice.onSpeechVolumeChanged = (e) => {
  setVolume(e.value || 0)  // Dùng cho animation waveform
}

// Error handling
Voice.onSpeechError = (e: SpeechErrorEvent) => {
  switch(e.error?.code) {
    case '7': // No speech detected
    case '6': // Speech timeout
    case '2': // Network error (offline STT unavailable)
  }
}
```

### Platform-Specific Options (Android)

```typescript
// Android cho phép tùy chỉnh nhiều hơn
Voice.start('de-DE', {
  EXTRA_LANGUAGE_MODEL: 'LANGUAGE_MODEL_FREE_FORM',
  EXTRA_MAX_RESULTS: 5,
  EXTRA_PARTIAL_RESULTS: true,
  REQUEST_PERMISSIONS_AUTO: true,
})
```

### Expo Compatibility

> ⚠️ **react-native-voice không dùng được với Expo Go** — cần Expo Dev Client.
> Thư viện có sẵn **Expo config plugin** (`app.plugin.js`) để tự động config native.

---

## 4. Navigation & Routing

### expo-router File-Based (từ expo-boilerplate)

```
app/
├── _layout.tsx          # Root layout
├── index.tsx            # Home screen
├── +not-found.tsx       # 404 screen
├── auth/
│   ├── _layout.tsx      # Auth stack layout
│   ├── login.tsx
│   └── register.tsx
├── alarm/
│   ├── [id].tsx         # Dynamic route: alarm detail
│   └── create.tsx
└── challenge/
    └── [alarmId].tsx    # Challenge screen (triggered by alarm)
```

### Deep Linking cho Alarm Trigger (từ Ignite pattern)

```typescript
// app.config.ts
const config = {
  screens: {
    Challenge: 'challenge/:alarmId',  // alarm trigger → deep link to challenge
    Settings: 'settings',
  }
}

// Khi alarm kêu → native module gửi deep link
// → app mở trực tiếp Challenge screen
```

---

## 5. EAS Build & CI/CD (từ Bluesky)

### eas.json Configuration

```json
{
  "build": {
    "base": { "node": "20.19.4" },
    "development": {
      "extends": "base",
      "developmentClient": true,
      "distribution": "internal",
      "channel": "development",
      "ios": { "simulator": true }
    },
    "preview": {
      "extends": "base",
      "distribution": "internal",
      "channel": "production"
    },
    "production": {
      "extends": "base",
      "ios": { "autoIncrement": true },
      "android": { "autoIncrement": true },
      "channel": "production"
    },
    "testflight": {
      "extends": "base",
      "ios": { "autoIncrement": true },
      "channel": "testflight"
    }
  }
}
```

> **Áp dụng:** Copy cấu trúc này cho Speak2Wake — development (Dev Client), preview (internal testing), production (Store), testflight (beta).

---

## 6. Testing Patterns

### E2E Testing với Maestro (từ Ignite)

```yaml
# .maestro/alarm_flow.yaml
appId: com.speak2wake
---
- launchApp
- tapOn: "Add Alarm"
- inputText: "07:00"
- tapOn: "Save"
- assertVisible: "07:00"
- tapOn:
    id: "alarm-toggle-1"
- assertVisible: "Alarm ON"
```

### Unit Test Structure (từ Ignite)

```
test/
├── components/     # Component tests
├── screens/        # Screen tests
├── services/       # Service tests (scoring engine!)
└── utils/          # Utility tests
```

---

## 7. i18n — Đa ngôn ngữ

### Pattern từ expo-boilerplate (react-i18next)

```
locales/
├── en/
│   └── translation.json
├── de/
│   └── translation.json
├── vi/
│   └── translation.json
└── index.ts  # i18n config
```

### Pattern từ Bluesky (Lingui)

Bluesky dùng `@lingui/react` — nặng hơn nhưng tốt cho large-scale. Cho Speak2Wake ở phase đầu, **react-i18next là đủ**.

---

## 8. App Initialization Pattern (từ Ignite)

Pattern "chờ tất cả sẵn sàng mới render":

```typescript
export function App() {
  const [areFontsLoaded] = useFonts(customFontsToLoad)
  const [isI18nReady, setI18nReady] = useState(false)
  const { isRestored } = useNavigationPersistence(storage, NAV_KEY)

  useEffect(() => {
    initI18n().then(() => setI18nReady(true))
  }, [])

  // Chờ tất cả ready
  if (!isRestored || !isI18nReady || !areFontsLoaded) return null

  return (
    <SafeAreaProvider>
      <KeyboardProvider>
        <ThemeProvider>
          <AppNavigator />
        </ThemeProvider>
      </KeyboardProvider>
    </SafeAreaProvider>
  )
}
```

> **Áp dụng cho Speak2Wake:** Thêm `isAlarmServiceReady` và `isVocabularyLoaded` vào danh sách chờ.

---

## 9. Tổng hợp Solutions cho Speak2Wake

| Vấn đề | Giải pháp từ Repos | Áp dụng |
|---|---|---|
| **Project structure** | Ignite boilerplate pattern | `app/components/screens/services/engine/` |
| **Native alarm module** | Bluesky custom Expo modules pattern | `modules/expo-alarm-engine/` với Kotlin + Swift |
| **State management** | Zustand (expo-boilerplate) + persisted schema (Bluesky) | Zustand stores + versioned migration |
| **STT integration** | RN-Voice API + events + platform options | `Voice.start('de-DE')` với Android options |
| **Partial results UI** | RN-Voice `onSpeechPartialResults` + `onSpeechVolumeChanged` | Real-time text + waveform animation |
| **Navigation** | expo-router file-based (expo-boilerplate) | Deep linking cho alarm → challenge |
| **EAS Build CI/CD** | Bluesky eas.json multi-channel | dev/preview/production/testflight |
| **E2E testing** | Ignite Maestro tests | `.maestro/` YAML test flows |
| **i18n** | react-i18next (expo-boilerplate) | `locales/de/`, `locales/en/` |
| **App init** | Ignite "wait for ready" pattern | Fonts + i18n + alarm service + vocabulary |
| **Service layer** | expo-boilerplate service injection | `AlarmService.initialize()` in root layout |
| **Expo compatibility** | RN-Voice config plugin + Dev Client | `expo prebuild` + Dev Client builds |

---

## 10. Quick Start Template cho Speak2Wake

```bash
# 1. Tạo project
npx create-expo-app@latest speak2wake --template tabs

# 2. Cài core dependencies
npx expo install expo-router zustand @react-native-voice/voice expo-speech
npx expo install expo-secure-store expo-notifications
npm install nativewind tailwindcss react-i18next i18next
npm install @sentry/react-native

# 3. Dev Client (cần cho native modules)
npx expo install expo-dev-client

# 4. Tạo cấu trúc thư mục
mkdir -p app/{alarm,challenge,settings}
mkdir -p libs/zustand services engine data modules
mkdir -p locales/{de,en,vi}
mkdir -p .maestro
```
