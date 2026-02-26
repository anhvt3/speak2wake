# Speak2Wake — Architecture & Code Separation

## ⚠️ QUAN TRỌNG: Phân biệt Preview vs App

Dự án này có **2 phần code hoàn toàn tách biệt**:

---

## 1. 📱 App (React Native + Expo) — SẢN PHẨM CHÍNH

> **Mục tiêu cuối cùng:** App Android (ưu tiên), sau đó iOS.

| Thư mục | Nội dung | Công nghệ |
|---|---|---|
| `app/` | Screens (expo-router) | React Native + TypeScript |
| `components/` | UI components | React Native + NativeWind |
| `engine/` | Scoring engine | TypeScript (unit tested) |
| `services/` | Voice, TTS, Storage, Vocabulary, Alarm | TypeScript |
| `stores/` | Zustand state management | TypeScript |
| `modules/expo-alarm-engine/` | Native alarm module | Kotlin (Android) |
| `constants/` | App constants | TypeScript |
| `types/` | TypeScript type definitions | TypeScript |
| `theme/` | Design system (Warm Sunrise palette) | TypeScript |
| `data/vocabulary-de-a1a2.json` | Vocabulary source data | JSON (550 items) |
| `__tests__/` | Unit tests | Jest |

### Build Commands:
```bash
# Development (cần máy tính kết nối)
npx expo start --dev-client

# Build standalone APK (chạy độc lập, không cần máy tính)
npx expo prebuild --platform android --clean
cd android && ./gradlew.bat assembleRelease

# APK location:
# android/app/build/outputs/apk/release/app-release.apk (~43MB arm64)
```

### Build Optimization:
- **arm64-only dev build**: Chỉ build 1 architecture → build nhanh 4x
- Config: `android/gradle.properties` → `reactNativeArchitectures=arm64-v8a`
- Production: đổi về `armeabi-v7a,arm64-v8a,x86,x86_64`
- **⚠️ `expo prebuild --clean` sẽ reset gradle.properties** → cần set lại arm64

---

## 2. 🌐 Preview (HTML Demo) — CHỈ ĐỂ XEM TRƯỚC

> **Mục đích:** Preview giao diện & tính năng để review trước khi code native app. **KHÔNG PHẢI sản phẩm cuối.**

| Thư mục | Nội dung | Công nghệ |
|---|---|---|
| `previews/index.html` | Interactive demo | HTML + CSS + vanilla JS |
| `previews/vocab.js` | Vocabulary data cho demo | Generated JS |
| `data/update_vocab.py` | Script generate vocab.js | Python |

### Deploy:
```bash
cd previews && npx vercel --prod --yes
```

### URL: https://speak2wake.vercel.app

### Lưu ý:
- Preview dùng **Web Speech API** thay vì `expo-speech-recognition`
- Preview dùng **localStorage** thay vì AsyncStorage + Zustand
- Preview dùng **Web Audio API** tạo alarm tone thay vì native AlarmManager
- Preview dùng **SpeechSynthesis** thay vì `expo-speech`
- Scoring engine trong preview là **port JS** từ TypeScript engine gốc

---

## 3. 📋 Docs & Planning

| File | Nội dung |
|---|---|
| `docs/speak2wake_planning.md` | Kế hoạch dự án chi tiết (5 phases) |
| `docs/reference_skills_solutions.md` | Reference patterns & solutions |
| `docs/ui_design_sample_review.md` | UI design review |
| `docs/ARCHITECTURE.md` | **File này** — kiến trúc & separation |
| `tasks/todo.md` | Task tracker |

---

## 4. 🔄 Quy trình Phát triển

```
Step 1: Preview (HTML demo)
  → Thiết kế UI + test flow + review với bạn
  → Deploy Vercel để share link
  
Step 2: React Native App  
  → Port UI từ preview sang React Native components
  → Tích hợp native modules (alarm, STT, TTS)
  → Build APK local cho device testing
  
Step 3: Device Testing
  → Build standalone release APK
  → Test trên thiết bị thật (Samsung, Xiaomi, Pixel)
  
Step 4: iOS
  → Implement iOS alarm module (Time Sensitive notification)
  → Test trên iPhone
```

---

## 5. 🎨 Design System — Warm Sunrise (Orange)

> Áp dụng UI UX Pro Max skill. Tone cam ấm gợi cảm giác bình minh khi thức dậy.

### Color Palette:
| Role | Hex | Mô tả |
|---|---|---|
| **Primary** | `#FF914D` | Warm Orange — màu chủ đạo |
| **Primary Light** | `#FFB380` | Orange nhạt cho surface |
| **Primary Dark** | `#E8732A` | Orange đậm cho emphasis |
| **Accent** | `#FFBE5C` | Golden Amber — nút accent |
| **Accent Gold** | `#FFD93D` | Vàng cho highlights |
| **Gradient Start** | `#FF6B35` | Gradient đỏ cam |
| **Gradient End** | `#FFBE5C` | Gradient vàng amber |
| **Background Dark** | `#141018` | Nền tối — dễ nhìn khi vừa thức |
| **Surface Dark** | `#1E1924` | Card/Glass surface |
| **Success** | `#4ADE80` | Xanh lá cho kết quả tốt |
| **Error** | `#FF6B6B` | Đỏ cho lỗi/thất bại |

### Typography:
- **Primary font**: Jost (400 Regular, 500 Medium, 600 SemiBold)
- **Import**: `@expo-google-fonts/jost`

### Principles (UX UI Pro Max):
- Nền tối → dễ nhìn lúc mới thức
- Gradient cam-vàng → gợi bình minh
- Glassmorphism cards → hiện đại, premium
- Smooth animations → micro-interactions via Reanimated
- WCAG AA contrast → 4.5:1+

---

## 6. 🔧 Native Modules

### expo-alarm-engine (Android)
Kotlin module với đầy đủ tính năng:

| Component | File | Chức năng |
|---|---|---|
| **AlarmEngineModule** | `AlarmEngineModule.kt` | JS bridge — schedule, cancel, snooze, dismiss. Static companion bridge (`instance`) cho Service→JS events |
| **AlarmReceiver** | `AlarmReceiver.kt` | BroadcastReceiver → trigger alarm |
| **AlarmForegroundService** | `AlarmForegroundService.kt` | Play sound, vibrate, notification. Gọi `AlarmEngineModule.sendEventToJS()` để gửi event về JS |
| **AlarmFullScreenActivity** | `AlarmFullScreenActivity.kt` | Lock screen display |
| **BootReceiver** | `BootReceiver.kt` | Re-register alarms after reboot |

### Permissions (AndroidManifest):
- `SCHEDULE_EXACT_ALARM`, `USE_EXACT_ALARM`
- `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_MEDIA_PLAYBACK`
- `USE_FULL_SCREEN_INTENT`
- `RECEIVE_BOOT_COMPLETED`, `WAKE_LOCK`, `VIBRATE`

### Alarm Flow:
```
User sets alarm → AlarmService.scheduleAlarm()
  → ExpoAlarmEngine.scheduleAlarm() (native)
    → AlarmManager.setExactAndAllowWhileIdle()
    → Save to SharedPreferences

User edits alarm → AlarmService.cancelAlarm() + scheduleAlarm()
  → Cancel old PendingIntent → Schedule new one

User deletes alarm → AlarmService.cancelAlarm() + removeAlarm()
  → Cancel native alarm → Remove from Zustand store

Time arrives → AlarmReceiver.onReceive()
  → Start AlarmForegroundService
    → Play sound (gradual volume 0→1 over 30s)
    → Vibrate pattern
    → Show full-screen notification
    → AlarmEngineModule.sendEventToJS(alarmId)  ← static companion bridge
      → AlarmEngineModule.instance.sendEvent('onAlarmFired', {alarmId})

JS receives event → _layout.tsx listener
  → router.replace(`/ring/${alarmId}`)  ← replace, not push
    → User: Snooze or Dismiss
      → Dismiss + challengeEnabled → /challenge/[alarmId]
        → Voice challenge (STT, 10s timeout) → ScoringEngine
          → Pass → dismissAlarm() → Home
          → Fail 5x → Failsafe (type answer)
```

---

## 7. 🎤 Voice Recognition (STT)

**Module:** `expo-speech-recognition` (thay thế deprecated `@react-native-voice/voice`)

| Service | File | Chức năng |
|---|---|---|
| **VoiceService** | `services/VoiceService.ts` | Wrapper cho expo-speech-recognition. Tự động track all subscriptions, `removeAllSubscriptions()` trước khi add mới. Cache permission result. **10s timeout** auto-stop. |
| **ScoringService** | `services/ScoringService.ts` | Đánh giá phát âm |
| **Scoring Engine** | `engine/scoring.ts` | Levenshtein + Phonetic + Confidence |

### Scoring Algorithm:
- **40%** Levenshtein distance (text similarity)
- **30%** Phonetic matching (German sounds)
- **30%** STT confidence score
- **Dynamic threshold**: Short words (≤3 chars) → 80%, Long words → 60%
- **Timeout**: 10 giây không có kết quả → auto-stop + error callback

---

## 8. 🎵 TTS Strategy

**Phase 1 (MVP):** 100% on-device TTS
- Android: Google German TTS (`expo-speech`)
- iOS: Apple German TTS
- Preview: Web SpeechSynthesis API

**Phase 3+:** Hybrid TTS + pre-recorded audio
- Top 50-100 từ hay sai phát âm → native speaker recording
- Analytics quyết định từ nào cần pre-recorded

---

## 9. Code Sharing giữa Preview ↔ App

| Component | Preview (JS) | App (TypeScript) | Shared? |
|---|---|---|---|
| Scoring engine | `index.html` inline JS | `engine/*.ts` | ✅ Logic giống |
| Vocabulary data | `previews/vocab.js` | `data/vocabulary-de-a1a2.json` | ✅ Cùng nguồn JSON |
| Constants | `index.html` inline | `constants/index.ts` | ✅ Giá trị giống |
| UI layout | HTML + CSS | React Native + NativeWind | ⚠️ Tương tự |
| Alarm logic | Web Audio API | Native Kotlin module | ❌ Hoàn toàn khác |
| Voice/STT | Web Speech API | expo-speech-recognition | ❌ Hoàn toàn khác |

---

## 10. 📝 Changelog

### v0.4.0 (2026-02-26) — Challenge UX Overhaul + Dark Theme

#### 🔴 UX Overhaul — Challenge Screen (`app/challenge/[alarmId].tsx`):
- ✅ **Auto-start mic** after 2s delay — no more "tap to speak"
- ✅ **10s timeout auto-retry**: no speech detected → resume alarm → auto-retry after 3s
- ✅ **Alarm keeps playing** when entering challenge (Ring screen no longer dismisses)
- ✅ **Alarm pauses** only while mic active or TTS speaking, resumes otherwise
- ✅ Status text + attempt counter shown at top of screen

#### 🟠 Native Module — Pause/Resume Alarm Sound:
- ✅ **`AlarmForegroundService.kt`**: Added `ACTION_PAUSE_SOUND` (pause MediaPlayer + cancel vibration) and `ACTION_RESUME_SOUND` (resume both)
- ✅ **`AlarmEngineModule.kt`**: Added `pauseAlarmSound()` and `resumeAlarmSound()` functions
- ✅ **`modules/expo-alarm-engine/index.ts`**: Exported new functions
- ✅ **`services/AlarmService.ts`**: Added `pauseAlarmSound()` / `resumeAlarmSound()` methods

#### 🟠 Alarm Popup Fix:
- ✅ **`AlarmFullScreenActivity.kt`**: Now launches main React Native activity via `getLaunchIntentForPackage()` before `finish()` — ensures app comes to foreground even when killed

#### 🟡 Dark Theme (readability fix):
- ✅ All screens: bright orange gradient → dark gradient (`#141018` → `#1E1020` → `#2A1525`)
- ✅ GlassCard: `bg-white/[0.08]` + `border-[#FF914D]/20` (dark glass effect)
- ✅ Files: `index.tsx`, `[id].tsx`, `create.tsx`, `settings/index.tsx`, `ring/[alarmId].tsx`, `GlassCard.tsx`

#### 🟡 Component Updates:
- ✅ **`WordDisplay.tsx`**: Added `onSpeakStart`/`onSpeakEnd` callbacks for alarm pause/resume during TTS
- ✅ **`MicButton.tsx`**: Label changed "Tap to speak" → "Tap to retry"
- ✅ **`app/ring/[alarmId].tsx`**: Challenge navigation without dismissing alarm

#### 📦 Build & Git:
- ✅ `.gitignore`: Added build artifacts, APKs, .gradle, .claude, .vscode
- ✅ `app.json`: Version 0.4.0
- ✅ Built `Speak2Wake_v0.4.0.apk` (105.8MB, all architectures)
- ✅ `android/gradle.properties`: all 4 CPU architectures for emulator testing

#### 📁 Files Modified:
| File | Change |
|---|---|
| `app/challenge/[alarmId].tsx` | Complete rewrite — auto-mic, timeout retry, alarm control |
| `app/ring/[alarmId].tsx` | Don't dismiss alarm before challenge |
| `components/challenge/WordDisplay.tsx` | onSpeakStart/onSpeakEnd TTS callbacks |
| `components/challenge/MicButton.tsx` | Label update |
| `components/ui/GlassCard.tsx` | Dark glass effect |
| `app/index.tsx` | Dark gradient background |
| `app/alarm/[id].tsx` | Dark gradient background |
| `app/alarm/create.tsx` | Dark gradient background |
| `app/settings/index.tsx` | Dark gradient background |
| `modules/.../AlarmEngineModule.kt` | pauseAlarmSound/resumeAlarmSound functions |
| `modules/.../AlarmForegroundService.kt` | PAUSE/RESUME sound actions |
| `modules/.../AlarmFullScreenActivity.kt` | Launch main app on alarm fire |
| `modules/expo-alarm-engine/index.ts` | Export pause/resume |
| `services/AlarmService.ts` | pauseAlarmSound/resumeAlarmSound methods |
| `.gitignore` | Exclude build artifacts |
| `app.json` | Version 0.4.0 |

### v0.3.0 (2026-02-26) — Critical Bug Fixes & Stability

#### 🔴 Critical Fixes:
- ✅ **Fixed alarm event bridge (Service → JS):** `AlarmForegroundService.sendAlarmFiredEvent()` was sending a broadcast that nobody received. Replaced with **static companion bridge pattern** — `AlarmEngineModule` now holds a `companion object { var instance }` and exposes `sendEventToJS(alarmId)` which the ForegroundService calls directly. This ensures alarm-fired events actually reach the JS layer.
- ✅ **Fixed EventEmitter in `expo-alarm-engine/index.ts`:** Was creating new EventEmitter on every call. Now creates once at module level using `require('expo-modules-core')` to avoid TS generic inference issues.

#### 🟠 High-Priority Fixes:
- ✅ **Edit alarm now reschedules native alarm:** `app/alarm/[id].tsx` `handleSave()` now calls `AlarmService.cancelAlarm()` + `AlarmService.scheduleAlarm()` to sync changes with the native AlarmManager.
- ✅ **Delete alarm now cancels native alarm:** `handleDelete()` now calls `AlarmService.cancelAlarm(id)` before `removeAlarm(id)` to prevent orphaned native alarms.
- ✅ **Fixed `tailwind.config.js` stale purple colors:** Completely rewrote color config to match Warm Sunrise Orange palette in `theme/colors.ts`. Added all tokens: `background`, `surface`, `primary`, `accent`, `gradient`, `success`, `error`, `warning` + legacy `violet` aliases.

#### 🟡 Medium-Priority Fixes:
- ✅ **Fixed VoiceService listener leak:** Previously only tracked one subscription; error + volumeChange listeners leaked. Now tracks ALL subscriptions in an array with `removeAllSubscriptions()` called before re-adding. Also cached permission result to avoid requesting on every `startListening()`.
- ✅ **Added 10-second voice recognition timeout:** VoiceService now auto-stops recognition after 10 seconds and fires error callback if no result received. Timeout cleared properly in `stopListening()`, `cancel()`, `destroy()`.
- ✅ **Fixed notification fallback cancelling ALL alarms:** `AlarmService.cancelAlarm()` fallback was calling `cancelAllScheduledNotificationsAsync()`. Now iterates `getAllScheduledNotificationsAsync()` and cancels only the matching `alarmId`. Same fix applied to `dismissAlarm()`.
- ✅ **Added `difficulty` field to `VocabWord` type:** `types/challenge.ts` now includes optional `difficulty?: 'single' | 'compound' | 'sentence'` matching the vocabulary JSON data.

#### 🟢 Low-Priority Fixes:
- ✅ **Fixed `getNextAlarm()` ignoring `repeatDays`:** `stores/alarmStore.ts` rewrote the algorithm — one-shot alarms check today/tomorrow, repeating alarms calculate `daysUntil` for each scheduled day and find the nearest occurrence.
- ✅ **Settings defaults now used in alarm creation:** `app/alarm/create.tsx` reads `defaultSound` and `defaultSnooze` from `settingsStore` instead of hardcoded values.
- ✅ **Fixed splash `backgroundColor`:** Changed from `#1C1721` (old purple) to `#141018` (Warm Sunrise dark) in `app.json`.
- ✅ **Fixed `router.push` → `router.replace`** in `_layout.tsx` alarm-fired listener to prevent stacking duplicate ring screens.

#### 📁 Files Modified:
| File | Change |
|---|---|
| `modules/expo-alarm-engine/.../AlarmEngineModule.kt` | Static companion bridge + lifecycle hooks |
| `modules/expo-alarm-engine/.../AlarmForegroundService.kt` | Use `AlarmEngineModule.sendEventToJS()` |
| `modules/expo-alarm-engine/index.ts` | Module-level EventEmitter singleton |
| `app/alarm/[id].tsx` | Reschedule on edit, cancel on delete |
| `app/alarm/create.tsx` | Read settings defaults |
| `app/_layout.tsx` | `router.replace` for alarm navigation |
| `tailwind.config.js` | Complete rewrite to Warm Sunrise palette |
| `services/VoiceService.ts` | Listener leak fix + 10s timeout + permission cache |
| `services/AlarmService.ts` | Cancel/dismiss specific alarm by ID |
| `stores/alarmStore.ts` | `getNextAlarm()` respects repeatDays |
| `types/challenge.ts` | Added `difficulty` field |
| `app.json` | Splash backgroundColor `#141018` |

### v0.2.0 (2026-02-26) — Native Module Integration + Orange Theme
- ✅ Connected `AlarmService` ↔ native `ExpoAlarmEngine` module
- ✅ `create.tsx` now schedules alarms via native AlarmManager
- ✅ `toggleAlarm` schedules/cancels with native module
- ✅ `_layout.tsx` listens for alarm-fired events → auto-navigate to ring screen
- ✅ Replaced `@react-native-voice/voice` (deprecated) → `expo-speech-recognition`
- ✅ New color scheme: Purple → **Warm Sunrise Orange** (UI UX Pro Max)
- ✅ arm64-only dev builds (4x faster build time)
- ✅ Fixed `babel-preset-expo` missing dependency for release builds
- ✅ Fixed `build.gradle` for expo-alarm-engine (SDK version inheritance)

### v0.1.0 (2026-02-26) — Initial Build
- ✅ First successful debug + release APK build
- ✅ Expo prebuild for Android
- ✅ All screens: Home, Create Alarm, Edit Alarm, Ring, Challenge, Settings
- ✅ Native alarm module skeleton (Kotlin)
- ✅ HTML preview with full feature demo
