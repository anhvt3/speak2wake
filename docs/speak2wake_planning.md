# SPEAK2WAKE

> **Kế hoạch Dự án & Phân tích Kỹ thuật**
> Ứng dụng Báo thức kết hợp Học Ngôn ngữ qua Giọng nói
> Phiên bản 1.0 | Tháng 2/2026

---

## 1. Tóm tắt Dự án

Speak2Wake là ứng dụng báo thức di động buộc người dùng hoàn thành thử thách giọng nói bằng ngoại ngữ để tắt chuông. Đây là sự kết hợp độc đáo giữa **"Forced Habit"** (thói quen bắt buộc) và **"Gamification"** (game hóa), giúp người học duy trì việc luyện phát âm mỗi ngày.

Tài liệu này cung cấp bản phân tích kỹ thuật chi tiết, đánh giá rủi ro, và kế hoạch sprint cho toàn bộ dự án.

---

## 2. Phân tích SWOT

| | Tích cực | Tiêu cực |
|---|---|---|
| **Nội bộ** | **ĐIỂM MẠNH (Strengths)** | **ĐIỂM YẾU (Weaknesses)** |
| | • Giá trị độc đáo: kết hợp báo thức + học ngôn ngữ | • Quản lý Alarm trên Android rất phức tạp về kỹ thuật |
| | • Cơ chế "Forced Habit" tạo thói quen hiệu quả | • Độ chính xác STT thấp khi vừa ngủ dậy |
| | • Hệ thống thử thách đa cấp độ (mở rộng tốt) | • Expo Managed Workflow bị giới hạn với alarm native |
| | • Cơ chế Fail-safe tránh gây ức chế người dùng | • Thư viện nhận diện giọng nói chưa ổn định đa nền tảng |
| | • Đường đi kiếm tiền rõ ràng (freemium) | • Phụ thuộc vào chất lượng STT của thiết bị |
| **Bên ngoài** | **CƠ HỘI (Opportunities)** | **THÁCH THỨC (Threats)** |
| | • Thị trường học ngôn ngữ toàn cầu ($73 tỷ USD năm 2028) | • Người dùng có thể thấy khó chịu và gỡ cài |
| | • Chưa có đối thủ trực tiếp trong phân khúc này | • Hệ điều hành giới hạn dịch vụ nền cho alarm |
| | • Tích hợp AI (GPT) tạo nội dung động | • Duolingo/Babbel có thể sao chép ý tưởng |
| | • Tiềm năng viral (chia sẻ streak lên mạng xã hội) | • STT kém = trải nghiệm xấu |
| | • Hợp tác với các trung tâm ngoại ngữ | • App Store có thể từ chối (fullscreen alarm UI) |

---

## 3. Rủi ro Kỹ thuật Nghiêm trọng & Giải pháp

| Rủi ro | Tác động | Giải pháp | Mức độ |
|---|---|---|---|
| Độ tin cậy Alarm trên Android | Alarm có thể không kêu trên một số thiết bị do Doze mode / tiết kiệm pin | Dùng Expo Dev Client + native AlarmManager với `SCHEDULE_EXACT_ALARM`. Test trên Samsung, Xiaomi, Oppo. | **P0 - Blocker** |
| Độ chính xác STT (giọng ngái ngủ) | Người dùng nói đúng nhưng bị từ chối, gây bực bội | Ngưỡng chấp nhận dynamic theo độ dài từ (thay vì fixed). Fail-safe sau 5 lần. Cho phép gõ chữ. Dùng phonetic matching + STT confidence score. | **P0 - Blocker** |
| Giới hạn của Expo | Managed workflow không hỗ trợ full-screen alarm intent | Dùng Expo Dev Client (truy cập native module) hoặc eject sang bare workflow từ ngày đầu. | **P1 - Cao** |
| Voice đa nền tảng | @react-native-voice/voice có lịch sử maintenance không ổn định, hoạt động khác nhau giữa iOS và Android | Trừu tượng hóa STT qua interface. Test compatibility với Expo Dev Client sớm. Fallback sang Whisper on-device (whisper.cpp). Backup plan: native module tự viết. | **P1 - Cao** |
| Full-screen Alarm UI | App Store có thể từ chối giao diện alarm luôn ở trên | Tuân thủ hướng dẫn `FULL_SCREEN_INTENT` của Android. Dùng đúng permission. | **P1 - Cao** |
| iOS Alarm Reliability | Apple rất hiếm khi cấp Critical Alert entitlement cho app không liên quan y tế/an ninh | Dùng **Time Sensitive notification** (iOS 15+) thay vì critical alerts. Kết hợp `UNNotificationSound` với custom sound (max 30s). Document fallback plan cho iOS. | **P1 - Cao** |
| Thư viện Alarm deprecated | notifee (Invertase) đã bị archive, ngừng maintain tích cực | Dùng `expo-notifications` kết hợp custom native module (Kotlin/Swift) cho full-screen alarm intent & ForegroundService. Tránh phụ thuộc thư viện 3rd-party cho core component. | **P1 - Cao** |
| Offline STT | Whisper API cần internet; người dùng vừa thức dậy có thể không có kết nối ổn định | Ưu tiên STT on-device. Yêu cầu cài language pack trong onboarding. Xem xét Whisper on-device (whisper.cpp / react-native-whisper) thay vì Whisper API. | **P1 - Cao** |
| Privacy & Biometric Data | App thu thập giọng nói — là biometric data theo GDPR/CCPA, đặc biệt quan trọng khi target thị trường Đức | Privacy policy rõ ràng. Consent flow cho microphone. Không lưu/gửi giọng nói lên server. GDPR compliance bắt buộc. | **P1 - Cao** |

---

## 4. Ngăn xếp Công nghệ Đề xuất (Cập nhật)

> Dựa trên phân tích rủi ro kỹ thuật, các điều chỉnh sau được đề xuất so với tech stack gốc:

| Thành phần | Đề xuất | Lý do |
|---|---|---|
| Framework | React Native + Expo Dev Client | Dev Client cho phép truy cập native module trong khi vẫn giữ lợi ích của Expo (OTA updates, EAS Build) |
| Alarm Engine | `expo-notifications` + custom native module (Kotlin/Swift) | ~~notifee đã bị deprecated/archive~~. Custom module đảm bảo toàn quyền kiểm soát full-screen intent, ForegroundService, AlarmManager. Bền vững nhất cho long-term. |
| STT (Nhận diện giọng) | @react-native-voice/voice + Whisper on-device dự phòng | STT trên thiết bị cho tốc độ & offline. Whisper on-device (whisper.cpp) dự phòng khi cần độ chính xác cao mà không cần internet. Cần verify compatibility với Expo Dev Client trong POC. |
| TTS (Đọc mẫu) | expo-speech | Hoạt động tốt với Dev Client. TTS native đủ dùng cho việc phát âm mẫu |
| So khớp chuỗi | Levenshtein + Điểm Ngữ âm + STT Confidence | Phonetic matching (Soundex/Metaphone) cải thiện UX. Ngưỡng dynamic theo độ dài từ. Kết hợp confidence score từ STT engine. Cần calibration với dataset thật. |
| UI Library | NativeWind (TailwindCSS) | Styling nhanh, cú pháp quen thuộc, tốt cho prototyping |
| Lưu trữ (Phase 1) | AsyncStorage + JSON | Đơn giản, nhanh cho 500 từ. Chuyển sang SQLite ở Phase 3 |
| Lưu trữ (Phase 3+) | expo-sqlite hoặc WatermelonDB | Cần thiết cho 5000+ từ, từ vựng tùy chỉnh, và truy vấn phức tạp |
| Quản lý State | Zustand | Nhẹ, API đơn giản, phù hợp cho alarm/challenge state. Không boilerplate. |
| Điều hướng | expo-router | File-based routing, hỗ trợ deep linking cho alarm triggers |
| Error Tracking | Sentry (`@sentry/react-native`) | Cài đặt từ Sprint 1 để theo dõi crash, lỗi alarm, và STT failures từ đầu |
| Analytics | Mixpanel hoặc Amplitude | Cài anonymous usage analytics từ Phase 1 để đo MVP metrics (alarm reliability, STT accuracy, fail-safe rate) |
| CI/CD | GitHub Actions + EAS Build | Tự động build, test, và deploy. Cài đặt từ Sprint 1. |

---

## 5. Kế hoạch Sprint Chi tiết

### Giai đoạn 1: MVP — Sản phẩm khả dụng tối thiểu (10 Tuần)

> **Trọng tâm:** Báo thức hoạt động được với thử thách Cấp độ 1 cho Tiếng Đức. Vòng lặp cốt lõi "forced habit" phải hoạt động hoàn hảo.
>
> ⚠️ **Đã thêm buffer 30%** so với ước tính ban đầu (8→10 tuần) do alarm engine phải debug trên nhiều hãng Android và rủi ro kỹ thuật STT.

#### Sprint 1-2: Khởi tạo Dự án & Alarm Engine (Tuần 1-5)

| # | Công việc | Tiêu chí nghiệm thu | Thời gian |
|---|---|---|---|
| 1.1 | Khởi tạo Expo Dev Client với TypeScript | Build thành công trên iOS + Android | 2 ngày |
| 1.2 | Cài đặt CI/CD (GitHub Actions + EAS Build) và Sentry | Pipeline build + test tự động. Crash reports hoạt động. | 2 ngày |
| 1.3 | Cài đặt điều hướng (expo-router): Home, Thêm Alarm, Thử thách | Chuyển đổi giữa các màn hình | 2 ngày |
| 1.4 | Xây dựng UI quản lý Alarm (thêm/sửa/xóa, lặp ngày, bật/tắt, snooze config) | Quản lý alarm đầy đủ, bao gồm cài đặt snooze | 4 ngày |
| 1.5 | Xây dựng custom native alarm module (Kotlin): AlarmManager + ForegroundService + full-screen Activity | Alarm kêu đúng giờ, hiển full-screen khi app bị kill | 8 ngày |
| 1.6 | Xử lý Android: Doze mode, tiết kiệm pin, SCHEDULE_EXACT_ALARM, OEM-specific issues | Alarm hoạt động trên Samsung, Xiaomi, Oppo sau restart | 4 ngày |
| 1.7 | Xây dựng iOS alarm: Time Sensitive notification + custom sound + UNNotificationSound | Alarm kêu trên iOS kể cả khi app bị kill. KHÔNG dùng Critical Alert. | 3 ngày |
| 1.8 | Quản lý âm thanh alarm (chọn nhạc, rung, tăng dần âm lượng) | Âm thanh phát, tăng dần, dừng khi tắt | 2 ngày |
| 1.9 | Thiết kế Snooze & Multi-alarm handling | Snooze hoạt động (thử thách khó hơn sau snooze). Alarm queue xử lý conflict khi nhiều alarm kêu cùng lúc. | 2 ngày |
| 1.10 | Lưu alarm với AsyncStorage + Zustand store | Alarm không mất sau khi khởi động lại app | 2 ngày |
| 1.11 | Cài đặt anonymous analytics cơ bản | Đo được alarm trigger rate, alarm reliability | 1 ngày |

#### Sprint 3-4: Thử thách Giọng nói & Dữ liệu (Tuần 6-10)

| # | Công việc | Tiêu chí nghiệm thu | Thời gian |
|---|---|---|---|
| 2.1 | Tích hợp STT (@react-native-voice/voice), xử lý quyền, verify compatibility với Expo Dev Client | Nhận giọng nói thành text trên cả 2 nền tảng. Hoạt động với Expo Dev Client. | 3 ngày |
| 2.2 | Kiểm tra & yêu cầu cài language pack trong onboarding (German STT) | App detect và hướng dẫn user cài German language pack nếu chưa có | 2 ngày |
| 2.3 | Tích hợp TTS (expo-speech) đọc mẫu Tiếng Đức | Nhấn để nghe phát âm chuẩn | 1 ngày |
| 2.4 | Xây dựng UI Thử thách (hiển từ, nút mic, sóng âm, phản hồi) | Giao diện trực quan, dễ dùng | 3 ngày |
| 2.5 | Xây dựng engine Cấp 1 (Levenshtein + phonetic + STT confidence, ngưỡng dynamic theo độ dài từ) | Từ đúng đậu, từ sai trượt với phản hồi. Ngưỡng phù hợp cho cả từ ngắn và từ ghép dài. | 4 ngày |
| 2.6 | Unit tests cho scoring engine | Coverage ≥ 80% cho Levenshtein, phonetic matching, dynamic threshold logic | 2 ngày |
| 2.7 | Cài đặt Fail-safe: sau 5 lần sai, cho gõ chữ hoặc giải toán | Người dùng luôn tắt được alarm | 2 ngày |
| 2.8 | Tạo file JSON từ vựng Tiếng Đức (500 từ, A1-A2, có mạo từ + dịch) | 500 từ được load và hiển ngẫu nhiên | 2 ngày |
| 2.9 | Test end-to-end: alarm kêu → thử thách → tắt chuông | Toàn bộ luồng hoạt động trên 3+ thiết bị thật | 3 ngày |
| 2.10 | Sửa lỗi, đánh bóng UX, xử lý edge case (không mạng, mic bị từ chối, language pack thiếu...) | Không crash trong luồng chính | 3 ngày |

> **Sản phẩm Phase 1:** Ứng dụng báo thức hoàn chỉnh, người dùng phải đọc một từ Tiếng Đức để tắt chuông. Fail-safe đảm bảo không ai bị kẹt. Snooze hoạt động. Crash tracking & analytics từ ngày đầu.

---

### Giai đoạn 2: Nâng cấp Thử thách & Nội dung (6 Tuần)

> **Trọng tâm:** Cấp độ 2 (Hỏi đáp) và Cấp độ 3 (Đặt câu). Mở rộng từ vựng lên 5000 từ với phân loại theo chủ đề.

#### Sprint 5-6: Cấp độ Thử thách 2 & 3 (Tuần 11-16)

| # | Công việc | Tiêu chí nghiệm thu | Thời gian |
|---|---|---|---|
| 3.1 | Thiết kế cấu trúc dữ liệu Hỏi đáp (câu hỏi + từ khóa + câu trả lời chấp nhận) | Schema hỗ trợ so khớp linh hoạt | 2 ngày |
| 3.2 | Xây dựng engine Cấp 2: trích xuất từ khóa từ kết quả STT | Câu trả lời chứa từ khóa cốt lõi được chấp nhận | 3 ngày |
| 3.3 | Xây dựng engine Cấp 3: phát hiện chia động từ + kiểm tra độ dài tối thiểu | Câu hợp lệ với từ khóa/biến thể được chấp nhận | 4 ngày |
| 3.4 | Thêm hệ thống Gợi ý cho Cấp 3 (hiện câu mẫu) | Nút Gợi ý hiển câu ví dụ | 1 ngày |
| 3.5 | Calibration scoring engine với dataset thật (ghi âm 20+ người nói) | Tỷ lệ Levenshtein/phonetic/confidence và ngưỡng dynamic được validate. False reject rate < 20%. | 3 ngày |
| 3.6 | Mở rộng từ vựng lên 5000 từ, 10 chủ đề (Đời sống, Công việc, Du lịch...) | Mỗi chủ đề ~500 từ với metadata | 5 ngày |
| 3.7 | Cập nhật UI cài đặt Alarm: chọn độ khó + chọn chủ đề | Người dùng cấu hình được từng alarm | 3 ngày |
| 3.8 | Unit tests cho engine Cấp 2 & 3 | Coverage ≥ 80% cho keyword extraction, verb conjugation detection | 2 ngày |
| 3.9 | Test tích hợp toàn bộ 3 cấp độ + lọc chủ đề | Mọi loại thử thách hoạt động end-to-end | 3 ngày |

---

### Giai đoạn 3: Game hóa & Cá nhân hóa (6 Tuần)

> **Trọng tâm:** Giữ chân người dùng qua streaks, thống kê, và di chuyển database để mở rộng.

#### Sprint 7-8: Tính năng Gắn kết (Tuần 17-22)

| # | Công việc | Tiêu chí nghiệm thu | Thời gian |
|---|---|---|---|
| 4.1 | Hệ thống Streak (số ngày liên tiếp, bảo vệ streak) | Bộ đếm streak chính xác, được lưu | 3 ngày |
| 4.2 | Dashboard thống kê (từ đã luyện, tỷ lệ chính xác, từ hay sai) | Biểu đồ hiển thị dữ liệu học tập có ý nghĩa | 4 ngày |
| 4.3 | "Từ hay sai": theo dõi từ sai nhiều nhất để ôn lại | Từ sai xuất hiện thường xuyên hơn | 3 ngày |
| 4.4 | Di chuyển từ AsyncStorage/JSON sang expo-sqlite | Mọi dữ liệu được chuyển, truy vấn nhanh với 5000+ từ | 5 ngày |
| 4.5 | Từ vựng tùy chỉnh: người dùng tự thêm từ | Thêm/sửa/xóa từ, dùng trong thử thách | 3 ngày |
| 4.6 | Hệ thống Thành tích & Huy hiệu | Các mốc được mở khóa với phần thưởng trực quan | 3 ngày |
| 4.7 | Luồng Onboarding + hướng dẫn cho người mới | Người mới hiểu app trong < 2 phút | 3 ngày |
| 4.8 | Tối ưu hiệu suất + test toàn diện | App khởi động < 2s, không giật | 3 ngày |

---

### Giai đoạn 4: Tích hợp AI & Ra mắt (8 Tuần)

> **Trọng tâm:** Tích hợp ChatGPT tạo nội dung động, chấm điểm phát âm, và đưa lên App Store.

| # | Công việc | Tiêu chí nghiệm thu | Thời gian |
|---|---|---|---|
| 5.1 | Tích hợp API ChatGPT: tạo câu hỏi động theo trình độ người dùng | Câu hỏi phù hợp trình độ, đa dạng, không lặp | 5 ngày |
| 5.2 | Lớp cache cho nội dung AI (tải trước để dùng offline) | Nội dung AI khả dụng offline, auto-refresh khi có mạng | 3 ngày |
| 5.3 | Chấm điểm phát âm nâng cao (phân tích âm vị học) | Feedback chi tiết từng âm, không chỉ đúng/sai | 5 ngày |
| 5.4 | Soạn Privacy Policy & GDPR compliance | Privacy policy rõ ràng: giọng nói không lưu/gửi server. Consent flow microphone. Tuân thủ GDPR (thị trường Đức/EU). | 3 ngày |
| 5.5 | Chuẩn bị App Store: ảnh chụp, metadata, chính sách quyền riêng tư | Listing hoàn chỉnh, đúng App Store guidelines | 3 ngày |
| 5.6 | Chuẩn bị Google Play: target API level, giải trình quyền | Đáp ứng Google Play policy, đặc biệt SCHEDULE_EXACT_ALARM & microphone | 3 ngày |
| 5.7 | Beta testing (TestFlight + Google Play nội bộ) | ≥ 20 beta testers, thu thập feedback có cấu trúc | 5 ngày |
| 5.8 | Sửa lỗi từ phản hồi beta + đánh bóng cuối cùng | Tất cả P0/P1 bugs được fix. App Store review pass. | 5 ngày |
| 5.9 | Nộp app lên Store + ra mắt | App live trên cả App Store và Google Play | 3 ngày |

---

### Giai đoạn 5: Đa Ngôn ngữ & Thương mại hóa (10 Tuần)

> **Trọng tâm:** Mở rộng ngoài Tiếng Đức, triển khai mô hình freemium, và mở rộng toàn cầu.

| # | Công việc | Tiêu chí nghiệm thu | Thời gian |
|---|---|---|---|
| 6.1 | Trừu tượng hóa engine ngôn ngữ (chuyển đổi STT/TTS locale động) | Engine xử lý đúng locale cho mỗi ngôn ngữ, auto-detect language pack | 5 ngày |
| 6.2 | Thêm gói từ vựng Tiếng Anh, Tây Ban Nha, Pháp, Nhật | Mỗi ngôn ngữ có ≥ 500 từ cơ bản với metadata và phonetic hints | 8 ngày |
| 6.3 | Đa ngôn ngữ giao diện với react-i18next (UI 5+ ngôn ngữ) | UI hiển thị đúng cho ≥ 5 ngôn ngữ, auto-detect device locale | 5 ngày |
| 6.4 | Hệ thống Freemium: khóa chủ đề sau paywall, 500 từ miễn phí | Free tier hoạt động đầy đủ, premium unlock rõ ràng | 3 ngày |
| 6.5 | Tích hợp In-App Purchase (RevenueCat) cho gói Premium | Mua/restore subscription hoạt động trên cả iOS và Android | 5 ngày |
| 6.6 | Nhận diện thương hiệu: logo, mascot, tài liệu marketing | Brand kit hoàn chỉnh, consistent trên app + store listing | 5 ngày |
| 6.7 | Tích hợp Analytics nâng cao (Mixpanel/Amplitude) theo dõi hành vi người dùng | Dashboard theo dõi retention, conversion, learning progress | 3 ngày |
| 6.8 | ASO (Tối ưu App Store) + marketing ra mắt | Rankings cải thiện, organic downloads tăng | 5 ngày |

---

## 6. Tổng quan Timeline

| Giai đoạn | Thời gian | Mốc quan trọng | Mục tiêu |
|---|---|---|---|
| Phase 1: MVP | 10 tuần | Alarm hoạt động + Thử thách Cấp 1 + Snooze + Analytics | T4/2026 |
| Phase 2: Thử thách | 6 tuần | 3 cấp độ thử thách + 5000 từ + Scoring calibration | T6/2026 |
| Phase 3: Game hóa | 6 tuần | Streak, thống kê, chuyển SQLite | T7/2026 |
| Phase 4: AI + Ra mắt | 8 tuần | Tích hợp ChatGPT, GDPR, lên Store | T9/2026 |
| Phase 5: Mở rộng | 10 tuần | Đa ngôn ngữ, kiếm tiền | T12/2026 |

> **Tổng thời gian ước tính:** ~40 tuần (~10 tháng) cho sản phẩm hoàn chỉnh với kiếm tiền.

---

## 7. Quyết định Kiến trúc Quan trọng

### 7.1 Kiến trúc Alarm Engine

Hệ thống alarm là thành phần quan trọng và phức tạp nhất về kỹ thuật. Trên Android, alarm phải sống sót qua việc app bị kill, khởi động lại thiết bị, và tối ưu pin. Giải pháp đề xuất sử dụng hệ thống phân lớp:

- **Custom native module (Kotlin)** xử lý `AlarmManager.setExactAndAllowWhileIdle()`, full-screen Activity, và kết nối với React Native
- **Headless JS task** quản lý logic alarm khi app bị kill
- **ForegroundService** giữ alarm kêu cho đến khi người dùng tắt
- **Snooze handler** quản lý snooze state và tăng difficulty sau mỗi lần snooze
- **Alarm queue** xử lý conflict khi nhiều alarm kêu cùng lúc hoặc gần nhau

> ⚠️ **Không dùng notifee** — thư viện đã bị archive/deprecated. Dùng `expo-notifications` cho scheduling cơ bản, kết hợp custom native module cho full-screen alarm behavior.

Trên iOS, dùng **Time Sensitive notification** (iOS 15+) kết hợp `UNNotificationSound` với custom sound file. Không dùng Critical Alert entitlement vì Apple rất hiếm khi cấp cho app alarm clock.

### 7.2 Pipeline Xử lý Giọng nói

Pipeline STT nên theo luồng:

```
Người dùng nói
  → STT thiết bị bắt text (on-device, offline-first)
  → Chuẩn hóa (viết thường, bỏ dấu câu, trim)
  → Áp dụng quy tắc ngôn ngữ (xử lý mạo từ Tiếng Đức, tách từ ghép)
  → Chấm điểm kết hợp:
      • Levenshtein distance (40%)
      • Điểm ngữ âm Soundex/Metaphone (30%)
      • STT confidence score (30%)
  → Ngưỡng chấp nhận DYNAMIC theo độ dài từ:
      • Từ ngắn (≤3 ký tự): ngưỡng 80% (sai 1 ký tự = sai nhiều %)
      • Từ trung bình (4-8 ký tự): ngưỡng 70%
      • Từ dài/ghép (>8 ký tự): ngưỡng 60% (cho phép sai 1-2 ký tự)
  → Chấp nhận nếu tổng điểm > ngưỡng
```

> **Quan trọng:** Tỷ lệ scoring và ngưỡng trên là giả định ban đầu. Cần **calibration với dataset thật** (ghi âm ≥ 20 người) ở Phase 2 (task 3.5) để validate và điều chỉnh.

Thành phần ngữ âm rất quan trọng vì lỗi STT thường tạo ra kết quả giống về phát âm nhưng khác về chữ viết.

**Yêu cầu Offline:** STT phải hoạt động offline. Trong onboarding, app cần kiểm tra và hướng dẫn cài German language pack trên thiết bị. Whisper on-device (whisper.cpp) là fallback nếu device STT không hỗ trợ locale cần thiết.

### 7.3 Kiến trúc Dữ liệu

Dữ liệu từ vựng bắt đầu là file JSON nhúng trong app (Phase 1-2) để truy cập không độ trễ. Ở Phase 3, chuyển sang SQLite cho phép:

- Chọn từ ngẫu nhiên hiệu quả với bộ lọc chủ đề/cấp độ
- Truy vấn spaced repetition (ưu tiên từ có tỷ lệ đúng thấp)
- Lưu trữ từ vựng tùy chỉnh của người dùng
- Phân tích lịch sử học

Việc chuyển đổi nên không gây lỗi với cờ phiên bản trong AsyncStorage.

### 7.4 Testing & CI/CD Strategy

Chiến lược testing được áp dụng **từ Sprint 1**, không phải cuối project:

| Loại test | Công cụ | Áp dụng từ |
|---|---|---|
| Unit tests | Jest + React Native Testing Library | Sprint 1 |
| Scoring engine tests | Jest (dedicated test suite) | Sprint 3 |
| Integration tests | Detox hoặc Maestro | Sprint 3 |
| CI/CD pipeline | GitHub Actions + EAS Build | Sprint 1 |
| Crash tracking | Sentry (`@sentry/react-native`) | Sprint 1 |
| Device testing matrix | Samsung, Xiaomi, Oppo, Pixel (Android) + iPhone 12+ (iOS) | Sprint 2 |

### 7.5 Privacy & Data Compliance

App thu thập giọng nói — đây là **biometric data** theo GDPR và CCPA:

- **Giọng nói:** Xử lý on-device only. Không lưu trữ, không gửi lên server.
- **Consent flow:** Yêu cầu quyền microphone với giải thích rõ ràng mục đích sử dụng.
- **Privacy policy:** Soạn trước khi submit Store (Phase 4, task 5.4).
- **GDPR compliance:** Bắt buộc vì target chính là thị trường Đức/EU.
- **Data retention:** Chỉ lưu kết quả text (đúng/sai), không lưu audio.

---

## 8. Tiêu chí Thành công MVP

| Chỉ số | Mục tiêu | Cách đo |
|---|---|---|
| Độ tin cậy Alarm | > 99% tỷ lệ kêu | Test trên 5+ thiết bị Android đa hãng |
| Tỷ lệ chấp nhận STT | > 80% từ đúng được chấp nhận lần đầu | Ghi log mọi lần thử STT với điểm số (Sentry + Analytics) |
| Thời gian hoàn thành thử thách | < 60 giây trung bình | Đo từ lúc alarm kêu đến tắt |
| Tỷ lệ dùng Fail-safe | < 15% tổng số lần tắt | Theo dõi fail-safe vs tắt bằng giọng |
| Tỷ lệ crash | < 0.5% phiên | Sentry (cài đặt từ Sprint 1) |
| Thời gian khởi động | < 2 giây | Performance profiling |

---

## 9. Dependencies & Prerequisites

> Danh sách tài khoản và tài nguyên cần chuẩn bị trước khi bắt đầu Sprint 1:

| Hạng mục | Chi tiết | Ghi chú |
|---|---|---|
| Apple Developer Account | $99/năm | Cần cho TestFlight & App Store submission |
| Google Play Developer Account | $25 một lần | Cần cho internal testing & Play Store |
| OpenAI API Key | Pay-as-you-go | Dùng cho Phase 4 (ChatGPT integration) |
| Sentry Account | Free tier đủ cho MVP | Cài đặt Sprint 1 |
| Mixpanel/Amplitude Account | Free tier đủ cho MVP | Cài đặt Sprint 1 |
| GitHub Repository | Private repo | CI/CD pipeline |
| EAS Build Account | Expo account (free tier có giới hạn build) | CI/CD |
| Thiết bị test | Samsung, Xiaomi, Oppo, Pixel, iPhone 12+ | Tối thiểu 3 Android + 1 iOS |
| German language pack | Cài trên thiết bị test | Cần cho STT testing |
| RevenueCat Account | Dùng cho Phase 5 (IAP) | Chuẩn bị trước Phase 5 |

---

## 10. Các Bước Tiếp theo Ngay lập tức

1. **Chuẩn bị prerequisites:** Tạo tài khoản Apple Developer, Google Play, Sentry, GitHub repo
2. **Khởi tạo dự án:** Tạo project Expo Dev Client với TypeScript, NativeWind, expo-router, Zustand, Sentry
3. **POC Alarm:** Xây dựng bản thử nghiệm custom alarm module (Kotlin) với full-screen Activity trên Android (đây là rủi ro kỹ thuật số 1). **Không dùng notifee.**
4. **POC iOS Alarm:** Test Time Sensitive notification + custom sound trên iOS
5. **POC STT:** Test độ chính xác nhận diện giọng Tiếng Đức với `@react-native-voice/voice` + Expo Dev Client. Test trong phòng yên tĩnh vs môi trường ồn. Kiểm tra tình trạng maintenance của thư viện.
6. **POC STT Backup:** Nếu `@react-native-voice/voice` không tương thích Expo Dev Client, test Whisper on-device hoặc custom native STT module
7. **Chuẩn bị dữ liệu:** Tạo file JSON từ vựng Tiếng Đức A1-A2 (500 từ có mạo từ, dịch, gợi ý ngữ âm)
8. **Điểm quyết định:** Sau các POC (tuần 2), đánh giá:
   - Expo Dev Client có đủ hay cần eject sang bare workflow?
   - @react-native-voice/voice có hoạt động ổn không hay cần backup plan?
   - Custom alarm module có đáp ứng yêu cầu full-screen + ForegroundService không?
