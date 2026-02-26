# Speak2Wake — v0.4.0 Task Tracker

## ✅ Done
- Expo + TypeScript + expo-router (6 screens)
- Alarm CRUD UI + Zustand + AsyncStorage
- Kotlin native module: AlarmManager + ForegroundService + FullScreen + BootReceiver
- pauseAlarmSound / resumeAlarmSound (native + JS)
- AlarmFullScreenActivity launches main app
- STT (expo-speech-recognition) + TTS (expo-speech)
- Scoring engine (Levenshtein + Phonetic + Confidence)
- Challenge: auto-mic 2s, 10s timeout retry, alarm pause/resume
- Failsafe after 5 failed attempts
- 550 German A1-A2 words JSON
- Dark theme (was bright orange → unreadable)
- HTML preview on Vercel

## 🔴 TODO — P0 (must fix)
- [ ] Test alarm on REAL device (Samsung/Xiaomi) — emulator can't test mic/TTS/popup
- [ ] Verify alarm fires when app is killed
- [ ] Verify alarm survives device reboot (BootReceiver)

## 🟠 TODO — P1 (should fix)
- [ ] Unit tests for scoring engine (≥80% coverage)
- [ ] Sentry crash tracking
- [ ] Onboarding flow (language pack check + mic permission)
- [ ] Custom alarm sounds (5 options exist in UI but all play system default)

## 🟡 TODO — P2 (nice to have)
- [ ] CI/CD (GitHub Actions + EAS Build)
- [ ] Analytics (Mixpanel/Amplitude)
- [ ] Snooze increases challenge difficulty
- [ ] Multi-alarm queue conflict handling
- [ ] iOS support

## 📦 Build
```bash
# Dev (arm64 only, fast): set gradle.properties reactNativeArchitectures=arm64-v8a
# Full (emulator): set reactNativeArchitectures=armeabi-v7a,arm64-v8a,x86,x86_64
cd android && ./gradlew.bat assembleRelease
# APK: android/app/build/outputs/apk/release/app-release.apk
# After build: copy as Speak2Wake_v{VERSION}.apk, bump version in app.json, commit
```
