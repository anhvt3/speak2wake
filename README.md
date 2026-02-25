# Speak2Wake 🔔🗣️

**Ứng dụng Báo thức kết hợp Học Ngôn ngữ qua Giọng nói**

Speak2Wake là ứng dụng mobile buộc người dùng hoàn thành thử thách giọng nói bằng ngoại ngữ để tắt chuông báo thức mỗi sáng. Kết hợp "Forced Habit" (thói quen bắt buộc) và "Gamification" (game hóa) để giúp người học duy trì việc luyện phát âm hàng ngày.

## Tính năng chính

- **Báo thức thông minh** — Thêm/sửa/xóa alarm, lặp theo ngày, chọn nhạc chuông
- **Thử thách giọng nói đa cấp độ** — Đọc từ vựng, hỏi đáp, đặt câu
- **Nghe phát âm mẫu** — Text-to-Speech cho phép nghe chuẩn trước khi thử
- **Fail-safe** — Gõ chữ hoặc giải toán nếu mic hỏng hoặc sai quá 5 lần
- **Cá nhân hóa** — Chọn chủ đề từ vựng và cấp độ cho mỗi báo thức

## Tech Stack

| Thành phần | Công nghệ |
|---|---|
| Framework | React Native + Expo Dev Client |
| Alarm Engine | notifee + native module |
| STT | @react-native-voice/voice + Whisper fallback |
| TTS | expo-speech |
| UI | NativeWind (TailwindCSS) |
| State | Zustand |
| Navigation | expo-router |
| Storage | AsyncStorage → expo-sqlite |

## Tài liệu

- [Kế hoạch Dự án (docx)](docs/speak2wake_planning.docx)

## Lộ trình

| Giai đoạn | Thời gian | Mục tiêu |
|---|---|---|
| Phase 1: MVP | 8 tuần | Alarm + Thử thách Cấp 1 (Tiếng Đức) |
| Phase 2: Thử thách | 6 tuần | 3 cấp độ + 5000 từ vựng |
| Phase 3: Game hóa | 6 tuần | Streak, thống kê, SQLite |
| Phase 4: AI + Ra mắt | 8 tuần | ChatGPT, App Store |
| Phase 5: Mở rộng | 10 tuần | Đa ngôn ngữ, kiếm tiền |

## License

MIT
