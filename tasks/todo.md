# Speak2Wake — Task Tracker

## Phase 0: Pre-MVP (HTML Demo)
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
- [x] Navigation (expo-router): 5 screens
- [x] UI Alarm CRUD (add/edit/delete, repeat, toggle)
- [x] Zustand stores + AsyncStorage
- [ ] **CI/CD: GitHub Actions + EAS Build**
- [ ] **Sentry error tracking**
- [/] Kotlin alarm module (skeleton exists, needs device testing)
- [ ] **Android: Doze mode, OEM, SCHEDULE_EXACT_ALARM**
- [ ] **iOS: Time Sensitive notification + custom sound**
- [ ] Alarm sound management (select tone, vibrate, crescendo)
- [ ] Snooze + Multi-alarm queue (logic written, untested)
- [ ] Anonymous analytics (Mixpanel/Amplitude)

## Phase 1: MVP — Sprint 3-4 (Voice Challenge)
- [x] STT integration (@react-native-voice)
- [x] TTS integration (expo-speech)
- [x] Challenge UI components
- [x] Scoring engine + dynamic thresholds
- [x] Fail-safe mechanism
- [x] Vocabulary data (500 words JSON)
- [ ] **Language pack detection + onboarding**
- [ ] **End-to-end test on 3+ real devices**
- [ ] **Bug fix, polish, edge cases**

## Phase 2-5: Future
- [ ] Challenge Level 2 (Q&A) + Level 3 (Sentences)
- [ ] Vocabulary expansion to 5000 words
- [ ] Scoring calibration with real voice data
- [ ] Gamification (Streaks, Stats, Achievements)
- [ ] SQLite migration
- [ ] AI integration (ChatGPT)
- [ ] GDPR + Privacy Policy
- [ ] App Store + Google Play submission
- [ ] Multi-language support
- [ ] Freemium + IAP (RevenueCat)
