# Speak2Wake - Project Knowledge

## Project Overview
Android alarm app that teaches German vocabulary. Built with Jetpack Compose, Hilt DI, multi-module architecture.

## Architecture
```
app/                    → MainActivity, NavHost, Hilt setup
build-logic/            → Convention plugins
core/
  alarm/                → AlarmBroadcastReceiver, AlarmForegroundService, AlarmActivity, AndroidAlarmScheduler
  common/               → ScoringEngine, shared utilities
  data/                 → Repositories (AlarmRepository, VocabularyRepository, ChallengeHistoryRepository)
  database/             → Room DB, DAOs, entities
  designsystem/         → Theme (colors, typography), GlassCard component
  model/                → Domain models (Alarm, VocabularyWord, ChallengeSession, etc.)
feature/
  home/api + impl/      → HomeScreen (alarm list), HomeViewModel
  create/api + impl/    → Create/Edit alarm screen
  ring/api + impl/      → RingScreen (alarm ringing UI), RingViewModel
  challenge/api + impl/ → ChallengeScreen (vocabulary challenge), ChallengeViewModel
  settings/api + impl/  → SettingsScreen
```

## Key Constants
```kotlin
// AlarmForegroundService actions
ACTION_START  = "com.speak2wake.alarm.START"
ACTION_STOP   = "com.speak2wake.alarm.STOP"
ACTION_PAUSE  = "com.speak2wake.alarm.PAUSE"
ACTION_RESUME = "com.speak2wake.alarm.RESUME"

// Package
applicationId = "com.speak2wake"
```

## Navigation Routes
- `HomeRoute` → alarm list
- `CreateAlarmRoute` → new alarm
- `EditAlarmRoute(id)` → edit alarm
- `RingRoute(alarmId)` → alarm ringing screen
- `ChallengeRoute(alarmId)` → vocabulary challenge
- `SettingsRoute` → settings

## Alarm Flow
```
AlarmManager → AlarmBroadcastReceiver → AlarmForegroundService (sound+vibrate)
→ AlarmActivity (wake screen) → MainActivity (pendingAlarmId) → NavHost → RingRoute
→ User taps "Start Challenge" → ChallengeRoute (listen + speak German word)
```

## Build Commands
```bash
# Build debug APK
./gradlew.bat assembleDebug

# Clean build
./gradlew.bat clean assembleDebug

# Stop daemons (fix file lock on Windows)
./gradlew.bat --stop

# APK output path
app/build/outputs/apk/debug/speak2wake-v2.0.apk
```

## ADB Commands
```bash
export PATH="$PATH:$HOME/AppData/Local/Android/Sdk/platform-tools"

# Install APK
adb -s emulator-5554 install -r "D:\\projectlocal\\personal\\speak2wake_ver2\\app\\build\\outputs\\apk\\debug\\speak2wake-v2.0.apk"

# Launch app
adb -s emulator-5554 shell monkey -p com.speak2wake -c android.intent.category.LAUNCHER 1

# Force stop + relaunch
adb -s emulator-5554 shell am force-stop com.speak2wake

# Screenshot
adb -s emulator-5554 exec-out screencap -p > "$HOME/speak2wake_screen.png"

# UI dump (for finding tap coordinates)
adb -s emulator-5554 shell "uiautomator dump /data/local/tmp/ui_dump.xml && cat /data/local/tmp/ui_dump.xml"

# Logcat (check crashes)
adb -s emulator-5554 logcat -d --pid=$(adb -s emulator-5554 shell pidof com.speak2wake)
```

## IMPORTANT: Emulator uses device pixel coordinates (1280x2856), NOT screenshot pixel coordinates. Always use `uiautomator dump` to get correct bounds before tapping.

---

## E2E Test Workflow

Khi user nói "test" hoặc "test lại", chạy workflow sau:

### Step 0: Build & Install
```
1. ./gradlew.bat assembleDebug
2. adb install APK lên emulator
3. Force stop + relaunch app
4. Chờ 3s cho app load xong
```

### Step 1: Home Screen — Verify alarm list
```
1. Screenshot → verify thấy "Speak2Wake" title, alarm cards, ▶ test buttons, toggle switches
2. Nếu cần tọa độ chính xác → uiautomator dump
```

### Step 2: Tap ▶ Test button trên alarm đầu tiên
```
1. uiautomator dump → tìm bounds của content-desc="Test Alarm" đầu tiên
2. Tap center of bounds
3. Chờ 2s
4. Screenshot → verify Ring Screen hiện ra (⏰ icon, clock, "Start Challenge", "Snooze")
```

### Step 3: Tap "Start Challenge"
```
1. uiautomator dump → tìm bounds "Start Challenge"
2. Tap center
3. Chờ 3s (TTS init + auto-listen)
4. Screenshot → verify Challenge Screen:
   - Attempt counter (1/5)
   - German word (gold text) + phonetic + English + Vietnamese
   - 🔊 Listen button
   - 🎤 Mic button
   - Score feedback
```

### Step 4: Tap "Listen" button
```
1. uiautomator dump → tìm bounds "Listen"
2. Tap
3. Check logcat cho "TTS initialized, ready=true" và không có crash
4. Screenshot → verify UI vẫn ổn định
```

### Step 5: Verify Mic/Speak flow
```
1. Trên emulator (không có mic) → speech sẽ timeout
2. Sau 5 attempts → Failsafe input hiện ra
3. Screenshot → verify "Can't speak? Type ..." + text input + Submit button
```

### Step 6: Check logs
```
1. Logcat grep "error|exception|fatal|crash" (loại trừ known warnings)
2. Verify không có crash nào
```

### Pass Criteria
- [ ] Home screen hiển thị đúng với ▶ test buttons
- [ ] Ring screen hiện khi tap ▶ (⏰, clock, Start Challenge, Snooze)
- [ ] Challenge screen hiện đầy đủ (word, Listen, Mic, attempt counter)
- [ ] Listen button hoạt động (TTS ready=true trong logs)
- [ ] Mic button hoạt động (speech recognizer starts)
- [ ] Failsafe hiện sau max attempts
- [ ] Không có crash/exception
