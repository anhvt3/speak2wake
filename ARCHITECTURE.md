# Speak2Wake — Architecture (v0.4.0)

## Structure
| Folder | Purpose |
|---|---|
| `app/` | 6 screens: index, alarm/create, alarm/[id], ring/[alarmId], challenge/[alarmId], settings |
| `components/` | UI (GlassCard, MicButton, WaveformVisual, WordDisplay, ScoringFeedback, AlarmCard...) |
| `engine/` | Scoring (Levenshtein + Phonetic + Confidence) |
| `services/` | AlarmService, VoiceService, TTSService, ScoringService, VocabularyService |
| `stores/` | Zustand: alarmStore, challengeStore, settingsStore |
| `modules/expo-alarm-engine/` | Kotlin native alarm module |
| `theme/` | Colors (Warm Sunrise Orange, dark bg #141018) |
| `data/` | vocabulary-de-a1a2.json (550 words) |
| `previews/` | HTML demo (Vercel) — NOT the app |

## Native Module (Android)
| File | Role |
|---|---|
| `AlarmEngineModule.kt` | JS bridge: schedule, cancel, snooze, dismiss, pauseSound, resumeSound |
| `AlarmReceiver.kt` | BroadcastReceiver → starts ForegroundService |
| `AlarmForegroundService.kt` | Plays sound (gradual 0→1), vibrates, notification. Supports PAUSE/RESUME |
| `AlarmFullScreenActivity.kt` | Turns on screen + launches main RN app |
| `BootReceiver.kt` | Re-registers alarms after reboot |

## Alarm Flow
```
Schedule → AlarmManager.setExactAndAllowWhileIdle()
Fire → AlarmReceiver → ForegroundService (sound + vibrate + notification)
     → AlarmFullScreenActivity (wake screen + launch app)
     → sendEventToJS(alarmId) via static companion bridge
JS   → _layout.tsx listener → router.replace('/ring/{alarmId}')
     → Snooze or Start Challenge
     → Challenge: auto-mic, 10s timeout, alarm pauses while speaking
     → Pass → dismissAlarm → Home
     → Fail 5x → Failsafe (type answer)
```

## Voice Pipeline
```
User speaks → expo-speech-recognition (de-DE, 10s timeout)
→ Normalize (lowercase, trim) → Score:
  40% Levenshtein + 30% Phonetic + 30% Confidence
→ Dynamic threshold: short(≤3)=80%, medium(4-8)=70%, long(>8)=60%
→ Pass/Fail
```

## Design
- **Dark bg**: #141018 → #1E1020 → #2A1525 (dark gradient)
- **Accent**: #FF914D (orange), #FFBE5C (gold)
- **Font**: Jost (400/500/600)
- **Cards**: GlassCard = bg-white/8% + orange border
- **Animations**: Reanimated (pulse rings on mic, waveform)

## Changelog
- **v0.4.0**: Challenge UX (auto-mic, timeout retry, alarm pause/resume), dark theme, AlarmFullScreenActivity popup fix
- **v0.3.0**: Alarm bridge fix (static companion), VoiceService leak fix, 10s timeout, edit/delete reschedule
- **v0.2.0**: Native module integration, orange theme, expo-speech-recognition
- **v0.1.0**: Initial build, all screens, alarm skeleton
