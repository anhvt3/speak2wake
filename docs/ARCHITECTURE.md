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
| `services/` | Voice, TTS, Storage, Vocabulary | TypeScript |
| `stores/` | Zustand state management | TypeScript |
| `modules/expo-alarm-engine/` | Native alarm module | Kotlin (Android), Swift (iOS) |
| `constants/` | App constants | TypeScript |
| `types/` | TypeScript type definitions | TypeScript |
| `theme/` | Design tokens + ThemeContext | TypeScript |
| `data/vocabulary-de-a1a2.json` | Vocabulary source data | JSON (550 items) |
| `__tests__/` | Unit tests | Jest |

### Build & Test:
```bash
npx expo start --dev-client    # Development
eas build -p android --profile preview   # Android APK
eas build -p ios --profile preview       # iOS IPA
```

---

## 2. 🌐 Preview (HTML Demo) — CHỈ ĐỂ XEM TRƯỚC

> **Mục đích:** Preview giao diện & tính năng để anh và bạn anh review trước khi code native app. **KHÔNG PHẢI sản phẩm cuối.**

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
- Preview dùng **Web Speech API** thay vì `@react-native-voice/voice`
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
| `docs/ARCHITECTURE.md` | **File này** — phân tách code |
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
  → Test trên Android Studio emulator
  
Step 3: Device Testing
  → Build APK qua EAS
  → Test trên thiết bị thật (Samsung, Xiaomi, Pixel)
  
Step 4: iOS
  → Implement iOS alarm module (Time Sensitive notification)
  → Test trên iPhone
```

---

## 5. 🎵 TTS Strategy

**Phase 1 (MVP):** 100% on-device TTS
- Android: Google German TTS (chất lượng cao, miễn phí)
- iOS: Apple German TTS
- Preview: Web SpeechSynthesis API

**Phase 3+:** Hybrid TTS + pre-recorded audio
- Top 50-100 từ hay sai phát âm → native speaker recording
- Từ mới/user-added → TTS
- Analytics quyết định từ nào cần pre-recorded

---

## 6. Code Sharing giữa Preview ↔ App

| Component | Preview (JS) | App (TypeScript) | Shared? |
|---|---|---|---|
| Scoring engine | `index.html` inline JS | `engine/*.ts` | ✅ Logic giống, syntax khác |
| Vocabulary data | `previews/vocab.js` | `data/vocabulary-de-a1a2.json` | ✅ Cùng nguồn JSON |
| Constants | `index.html` inline | `constants/index.ts` | ✅ Giá trị giống |
| UI layout | HTML + CSS | React Native + NativeWind | ⚠️ Tương tự, khác syntax |
| Alarm logic | Web Audio API | Native Kotlin/Swift module | ❌ Hoàn toàn khác |
| Voice/STT | Web Speech API | @react-native-voice/voice | ❌ Hoàn toàn khác |
