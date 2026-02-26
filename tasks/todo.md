# Speak2Wake — Task Tracker

## Current Version: v0.4.0 (2026-02-26)
> Git: `5447ed1` on `main` branch (github.com/anhvt3/speak2wake)

---

## Phase 0: Pre-MVP (HTML Demo) ✅ COMPLETE
- [x] Plan UI components based on Locofy reference
- [x] Build scoring engine (Levenshtein + Cologne Phonetic + Confidence)
- [x] Unit tests (31 tests)
- [x] Vocabulary expansion 100→500 words (21 categories)
- [x] Build interactive HTML demo
- [x] Deploy HTML demo to Vercel
- [x] Theme: Black + Orange, SVG icons
- [x] Auto-mic, 10s timeout, alarm loop
- [x] Voice Challenge + Fail-safe in HTML

## Phase 1: MVP — Sprint 1-2 (Alarm Engine)
- [x] Expo Dev Client + TypeScript init
- [x] Navigation (expo-router): 6 screens (Home, Create, Edit, Ring, Challenge, Settings)
- [x] UI Alarm CRUD (add/edit/delete, repeat, toggle, snooze config)
- [x] Zustand stores + AsyncStorage persist
- [x] Kotlin alarm module — full implementation:
  - [x] `AlarmEngineModule.kt` — JS bridge (schedule, cancel, snooze, dismiss, pauseSound, resumeSound)
  - [x] `AlarmReceiver.kt` — BroadcastReceiver trigger
  - [x] `AlarmForegroundService.kt` — Sound + vibrate + notification + pause/resume
  - [x] `AlarmFullScreenActivity.kt` — Lock screen display + launches main app
  - [x] `BootReceiver.kt` — Re-register after reboot
  - [x] Static companion bridge pattern (Service→JS events)
- [x] `AlarmService.ts` — JS wrapper (native + notification fallback)
- [x] Alarm flow: schedule → AlarmManager → Receiver → ForegroundService → JS event → Ring screen
- [ ] **CI/CD: GitHub Actions + EAS Build**
- [ ] **Sentry error tracking**
- [ ] **Android: Doze mode test on Samsung/Xiaomi/Oppo real devices**
- [ ] **iOS: Time Sensitive notification + custom sound** (deferred)
- [ ] Alarm sound management (select custom tone — currently system default only)
- [ ] Snooze: increase challenge difficulty after snooze
- [ ] Multi-alarm queue (conflict handling when 2 alarms fire close together)
- [ ] Anonymous analytics (Mixpanel/Amplitude)

## Phase 1: MVP — Sprint 3-4 (Voice Challenge)
- [x] STT integration (expo-speech-recognition — replaced deprecated @react-native-voice/voice)
- [x] TTS integration (expo-speech)
- [x] Challenge UI: WordDisplay, MicButton (pulse animation), WaveformVisual, ScoringFeedback, FailsafeModal
- [x] Scoring engine + dynamic thresholds (engine/scoring.ts)
- [x] Fail-safe mechanism (after 5 failed attempts → type answer)
- [x] Vocabulary data (550 words JSON, A1-A2, 21 categories)
- [x] VoiceService: listener leak fix, 10s timeout, permission cache
- [ ] **Unit tests for scoring engine** (plan requires ≥80% coverage)
- [ ] **Language pack detection + onboarding flow**
- [ ] **End-to-end test on 3+ real devices**

## Phase 2-5: Future
- [ ] Challenge Level 2 (Q&A) + Level 3 (Sentences)
- [ ] Vocabulary expansion to 5000 words
- [ ] Scoring calibration with real voice data (20+ speakers)
- [ ] Gamification (Streaks, Stats, Achievements)
- [ ] SQLite migration (from AsyncStorage)
- [ ] AI integration (ChatGPT dynamic content)
- [ ] GDPR + Privacy Policy
- [ ] App Store + Google Play submission
- [ ] Multi-language support (English, Spanish, French, Japanese)
- [ ] Freemium + IAP (RevenueCat)

---

## ✅ Completed Changes (Antigravity Session 2026-02-26)

### v0.4.0 Changes Made This Session:

#### 1. Dark Theme Fix (readability)
All screens had bright orange gradient (#FF6B35 → #FFBE5C) making white text unreadable.
**Fix:** Changed to dark gradient (#141018 → #1E1020 → #2A1525) on ALL screens:
- `app/index.tsx` — Home
- `app/alarm/[id].tsx` — Edit Alarm
- `app/alarm/create.tsx` — Create Alarm
- `app/settings/index.tsx` — Settings
- `app/ring/[alarmId].tsx` — Ring (failsafe gradient also fixed)
- `components/ui/GlassCard.tsx` — dark glass effect (bg-white/8% + orange border)

#### 2. Challenge Screen UX Overhaul
**File:** `app/challenge/[alarmId].tsx` — completely rewritten

New behavior:
- **Auto-start mic** after 2s delay (no "tap to speak" needed)
- **10s timeout**: if no speech → resume alarm → auto-retry after 3s
- **Alarm keeps playing** when entering challenge (was being dismissed before)
- **Alarm pauses** only while mic is active or TTS is speaking
- **Alarm resumes** when mic stops or TTS finishes
- Status text shows current state ("Listening...", "No speech detected. Retrying...")
- Attempt counter visible at top

#### 3. Native Module: Pause/Resume Alarm Sound
**Files changed:**
- `AlarmForegroundService.kt` — Added `ACTION_PAUSE_SOUND` and `ACTION_RESUME_SOUND`
  - Pause: `mediaPlayer.pause()` + `vibrator.cancel()`
  - Resume: `mediaPlayer.start()` + `startVibration()`
- `AlarmEngineModule.kt` — Added `pauseAlarmSound()` and `resumeAlarmSound()` functions
- `modules/expo-alarm-engine/index.ts` — Exported pauseAlarmSound/resumeAlarmSound
- `services/AlarmService.ts` — Added pauseAlarmSound()/resumeAlarmSound() methods

#### 4. AlarmFullScreenActivity → Launches Main App
**File:** `AlarmFullScreenActivity.kt`
**Problem:** Was calling `finish()` immediately — if app killed, JS never receives event
**Fix:** Now calls `packageManager.getLaunchIntentForPackage()` → `startActivity()` to bring React Native app to foreground before finishing

#### 5. WordDisplay → TTS Callbacks
**File:** `components/challenge/WordDisplay.tsx`
Added `onSpeakStart` and `onSpeakEnd` optional props so challenge screen can pause/resume alarm during TTS playback

#### 6. MicButton Label
**File:** `components/challenge/MicButton.tsx`
Changed "Tap to speak" → "Tap to retry"

#### 7. Ring Screen → Don't Dismiss Before Challenge
**File:** `app/ring/[alarmId].tsx`
When challengeEnabled, navigate to challenge WITHOUT dismissing alarm — sound keeps playing

#### 8. Build & Git
- `.gitignore` — Added build artifacts, APKs, .gradle, .claude, .vscode
- `app.json` — Version bumped to 0.4.0
- Built `Speak2Wake_v0.4.0.apk` (105.8MB, all architectures)
- `android/gradle.properties` — reactNativeArchitectures = all 4 (for emulator)

---

## 🔴 Known Bugs Needing Fix (for Claude Code)

### P0 — Must verify on real device:
1. **Alarm popup when app is killed** — AlarmFullScreenActivity launches main app but untested when app is force-stopped
2. **German TTS not working** — expo-speech with 'de-DE' may need language pack. Emulator doesn't have it.
3. **Mic not working on emulator** — Need real device with mic to test voice recognition

### P1 — Should fix:
4. **Alarm after device reboot** — BootReceiver.kt exists but needs verification
5. **VoiceService.onError handler** — Challenge screen calls it but VoiceService timeout error code might not match the check in challenge screen

### P2 — Nice to have:
6. **Custom alarm sounds** — UI has 5 options but all play system default
7. **Snooze difficulty increase** — Planning requires harder challenge after snooze
8. **Multi-alarm conflict** — No queue logic for 2 alarms at similar time

---

## 📋 Architecture Reference

See `docs/ARCHITECTURE.md` for:
- Full alarm flow diagram (schedule → trigger → challenge → dismiss)
- Native module component map
- Voice pipeline (STT → normalize → score)
- Design system (Warm Sunrise Orange palette)
- v0.1.0 → v0.3.0 → v0.4.0 changelog

---

## 📝 Build Instructions

```bash
# Dev build (arm64 only, fast)
# Set android/gradle.properties: reactNativeArchitectures=arm64-v8a
cd android && ./gradlew.bat assembleRelease

# Full build (all architectures, for emulator)
# Set android/gradle.properties: reactNativeArchitectures=armeabi-v7a,arm64-v8a,x86,x86_64
cd android && ./gradlew.bat assembleRelease

# APK location:
# android/app/build/outputs/apk/release/app-release.apk

# Install on device/emulator:
adb install -r android/app/build/outputs/apk/release/app-release.apk
adb shell monkey -p com.speak2wake.app -c android.intent.category.LAUNCHER 1

# IMPORTANT: After build, copy APK with version name:
# cp app-release.apk ../../Speak2Wake_v{VERSION}.apk
```
