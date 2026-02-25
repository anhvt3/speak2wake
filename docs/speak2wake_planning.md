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
| Độ chính xác STT (giọng ngái ngủ) | Người dùng nói đúng nhưng bị từ chối, gây bực bội | Ngưỡng chấp nhận 70%. Fail-safe sau 5 lần. Cho phép gõ chữ. Dùng phonetic matching. | **P0 - Blocker** |
| Giới hạn của Expo | Managed workflow không hỗ trợ full-screen alarm intent | Dùng Expo Dev Client (truy cập native module) hoặc eject sang bare workflow từ ngày đầu. | **P1 - Cao** |
| Voice đa nền tảng | react-native-voice hoạt động khác nhau giữa iOS và Android | Trừu tượng hóa STT qua interface. Test từng platform sớm. Fallback sang Whisper API. | **P1 - Cao** |
| Full-screen Alarm UI | App Store có thể từ chối giao diện alarm luôn ở trên | Tuân thủ hướng dẫn `FULL_SCREEN_INTENT` của Android. Dùng đúng permission. | **P1 - Cao** |

---

## 4. Ngăn xếp Công nghệ Đề xuất (Cập nhật)

> Dựa trên phân tích rủi ro kỹ thuật, các điều chỉnh sau được đề xuất so với tech stack gốc:

| Thành phần | Đề xuất | Lý do |
|---|---|---|
| Framework | React Native + Expo Dev Client | Dev Client cho phép truy cập native module trong khi vẫn giữ lợi ích của Expo (OTA updates, EAS Build) |
| Alarm Engine | notifee + native module tùy chỉnh | expo-notifications KHÔNG phải hệ thống alarm. notifee hỗ trợ full-screen intent, âm thanh, kênh thông báo |
| STT (Nhận diện giọng) | @react-native-voice/voice + Whisper dự phòng | STT trên thiết bị cho tốc độ, Whisper API dự phòng khi cần độ chính xác cao |
| TTS (Đọc mẫu) | expo-speech | Hoạt động tốt với Dev Client. TTS native đủ dùng cho việc phát âm mẫu |
| So khớp chuỗi | Levenshtein + Điểm Ngữ âm | So sánh text thuần bỏ sót phát âm hợp lệ. Phonetic matching (Soundex/Metaphone) cải thiện UX |
| UI Library | NativeWind (TailwindCSS) | Styling nhanh, cú pháp quen thuộc, tốt cho prototyping |
| Lưu trữ (Phase 1) | AsyncStorage + JSON | Đơn giản, nhanh cho 500 từ. Chuyển sang SQLite ở Phase 3 |
| Lưu trữ (Phase 3+) | expo-sqlite hoặc WatermelonDB | Cần thiết cho 5000+ từ, từ vựng tùy chỉnh, và truy vấn phức tạp |
| Quản lý State | Zustand | Nhẹ, API đơn giản, phù hợp cho alarm/challenge state. Không boilerplate. |
| Điều hướng | expo-router | File-based routing, hỗ trợ deep linking cho alarm triggers |

---

## 5. Kế hoạch Sprint Chi tiết

### Giai đoạn 1: MVP — Sản phẩm khả dụng tối thiểu (8 Tuần)

> **Trọng tâm:** Báo thức hoạt động được với thử thách Cấp độ 1 cho Tiếng Đức. Vòng lặp cốt lõi "forced habit" phải hoạt động hoàn hảo.

#### Sprint 1-2: Khởi tạo Dự án & Alarm Engine (Tuần 1-4)

| # | Công việc | Tiêu chí nghiệm thu | Thời gian |
|---|---|---|---|
| 1.1 | Khởi tạo Expo Dev Client với TypeScript | Build thành công trên iOS + Android | 2 ngày |
| 1.2 | Cài đặt điều hướng (expo-router): Home, Thêm Alarm, Thử thách | Chuyển đổi giữa các màn hình | 2 ngày |
| 1.3 | Xây dựng UI quản lý Alarm (thêm/sửa/xóa, lặp ngày, bật/tắt) | Quản lý alarm đầy đủ | 3 ngày |
| 1.4 | Lập lịch alarm với notifee (exact alarm, full-screen intent) | Alarm kêu đúng giờ, hiển full-screen khi app bị kill | 5 ngày |
| 1.5 | Xử lý Android: Doze mode, tiết kiệm pin, SCHEDULE_EXACT_ALARM | Alarm hoạt động trên Samsung, Xiaomi, Oppo sau restart | 3 ngày |
| 1.6 | Quản lý âm thanh alarm (chọn nhạc, rung, tăng dần âm lượng) | Âm thanh phát, tăng dần, dừng khi tắt | 2 ngày |
| 1.7 | Lưu alarm với AsyncStorage + Zustand store | Alarm không mất sau khi khởi động lại app | 2 ngày |

#### Sprint 3-4: Thử thách Giọng nói & Dữ liệu (Tuần 5-8)

| # | Công việc | Tiêu chí nghiệm thu | Thời gian |
|---|---|---|---|
| 2.1 | Tích hợp STT (@react-native-voice/voice) và xử lý quyền | Nhận giọng nói thành text trên cả 2 nền tảng | 3 ngày |
| 2.2 | Tích hợp TTS (expo-speech) đọc mẫu Tiếng Đức | Nhấn để nghe phát âm chuẩn | 1 ngày |
| 2.3 | Xây dựng UI Thử thách (hiển từ, nút mic, sóng âm, phản hồi) | Giao diện trực quan, dễ dùng | 3 ngày |
| 2.4 | Xây dựng engine Cấp 1 (Levenshtein + ngưỡng 70%) | Từ đúng đậu, từ sai trượt với phản hồi | 3 ngày |
| 2.5 | Cài đặt Fail-safe: sau 5 lần sai, cho gõ chữ hoặc giải toán | Người dùng luôn tắt được alarm | 2 ngày |
| 2.6 | Tạo file JSON từ vựng Tiếng Đức (500 từ, A1-A2, có mạo từ + dịch) | 500 từ được load và hiển ngẫu nhiên | 2 ngày |
| 2.7 | Test end-to-end: alarm kêu → thử thách → tắt chuông | Toàn bộ luồng hoạt động trên 3+ thiết bị thật | 3 ngày |
| 2.8 | Sửa lỗi, đánh bóng UX, xử lý edge case (không mạng, mic bị từ chối...) | Không crash trong luồng chính | 3 ngày |

> **Sản phẩm Phase 1:** Ứng dụng báo thức hoàn chỉnh, người dùng phải đọc một từ Tiếng Đức để tắt chuông. Fail-safe đảm bảo không ai bị kẹt.

---

### Giai đoạn 2: Nâng cấp Thử thách & Nội dung (6 Tuần)

> **Trọng tâm:** Cấp độ 2 (Hỏi đáp) và Cấp độ 3 (Đặt câu). Mở rộng từ vựng lên 5000 từ với phân loại theo chủ đề.

#### Sprint 5-6: Cấp độ Thử thách 2 & 3 (Tuần 9-14)

| # | Công việc | Tiêu chí nghiệm thu | Thời gian |
|---|---|---|---|
| 3.1 | Thiết kế cấu trúc dữ liệu Hỏi đáp (câu hỏi + từ khóa + câu trả lời chấp nhận) | Schema hỗ trợ so khớp linh hoạt | 2 ngày |
| 3.2 | Xây dựng engine Cấp 2: trích xuất từ khóa từ kết quả STT | Câu trả lời chứa từ khóa cốt lõi được chấp nhận | 3 ngày |
| 3.3 | Xây dựng engine Cấp 3: phát hiện chia động từ + kiểm tra độ dài tối thiểu | Câu hợp lệ với từ khóa/biến thể được chấp nhận | 4 ngày |
| 3.4 | Thêm hệ thống Gợi ý cho Cấp 3 (hiện câu mẫu) | Nút Gợi ý hiển câu ví dụ | 1 ngày |
| 3.5 | Mở rộng từ vựng lên 5000 từ, 10 chủ đề (Đời sống, Công việc, Du lịch...) | Mỗi chủ đề ~500 từ với metadata | 5 ngày |
| 3.6 | Cập nhật UI cài đặt Alarm: chọn độ khó + chọn chủ đề | Người dùng cấu hình được từng alarm | 3 ngày |
| 3.7 | Test tích hợp toàn bộ 3 cấp độ + lọc chủ đề | Mọi loại thử thách hoạt động end-to-end | 3 ngày |

---

### Giai đoạn 3: Game hóa & Cá nhân hóa (6 Tuần)

> **Trọng tâm:** Giữ chân người dùng qua streaks, thống kê, và di chuyển database để mở rộng.

#### Sprint 7-8: Tính năng Gắn kết (Tuần 15-20)

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

| # | Công việc | Thời gian |
|---|---|---|
| 5.1 | Tích hợp API ChatGPT: tạo câu hỏi động theo trình độ người dùng | 5 ngày |
| 5.2 | Lớp cache cho nội dung AI (tải trước để dùng offline) | 3 ngày |
| 5.3 | Chấm điểm phát âm nâng cao (phân tích âm vị học) | 5 ngày |
| 5.4 | Chuẩn bị App Store: ảnh chụp, metadata, chính sách quyền riêng tư | 3 ngày |
| 5.5 | Chuẩn bị Google Play: target API level, giải trình quyền | 3 ngày |
| 5.6 | Beta testing (TestFlight + Google Play nội bộ) | 5 ngày |
| 5.7 | Sửa lỗi từ phản hồi beta + đánh bóng cuối cùng | 5 ngày |
| 5.8 | Nộp app lên Store + ra mắt | 3 ngày |

---

### Giai đoạn 5: Đa Ngôn ngữ & Thương mại hóa (10 Tuần)

> **Trọng tâm:** Mở rộng ngoài Tiếng Đức, triển khai mô hình freemium, và mở rộng toàn cầu.

| # | Công việc | Thời gian |
|---|---|---|
| 6.1 | Trừu tượng hóa engine ngôn ngữ (chuyển đổi STT/TTS locale động) | 5 ngày |
| 6.2 | Thêm gói từ vựng Tiếng Anh, Tây Ban Nha, Pháp, Nhật | 8 ngày |
| 6.3 | Đa ngôn ngữ giao diện với react-i18next (UI 5+ ngôn ngữ) | 5 ngày |
| 6.4 | Hệ thống Freemium: khóa chủ đề sau paywall, 500 từ miễn phí | 3 ngày |
| 6.5 | Tích hợp In-App Purchase (RevenueCat) cho gói Premium | 5 ngày |
| 6.6 | Nhận diện thương hiệu: logo, mascot, tài liệu marketing | 5 ngày |
| 6.7 | Tích hợp Analytics (Mixpanel/Amplitude) theo dõi hành vi người dùng | 3 ngày |
| 6.8 | ASO (Tối ưu App Store) + marketing ra mắt | 5 ngày |

---

## 6. Tổng quan Timeline

| Giai đoạn | Thời gian | Mốc quan trọng | Mục tiêu |
|---|---|---|---|
| Phase 1: MVP | 8 tuần | Alarm hoạt động + Thử thách Cấp 1 | Tháng 2 |
| Phase 2: Thử thách | 6 tuần | 3 cấp độ thử thách + 5000 từ | Tháng 3.5 |
| Phase 3: Game hóa | 6 tuần | Streak, thống kê, chuyển SQLite | Tháng 5 |
| Phase 4: AI + Ra mắt | 8 tuần | Tích hợp ChatGPT, lên Store | Tháng 7 |
| Phase 5: Mở rộng | 10 tuần | Đa ngôn ngữ, kiếm tiền | Tháng 9.5 |

> **Tổng thời gian ước tính:** ~38 tuần (~9.5 tháng) cho sản phẩm hoàn chỉnh với kiếm tiền.

---

## 7. Quyết định Kiến trúc Quan trọng

### 7.1 Kiến trúc Alarm Engine

Hệ thống alarm là thành phần quan trọng và phức tạp nhất về kỹ thuật. Trên Android, alarm phải sống sót qua việc app bị kill, khởi động lại thiết bị, và tối ưu pin. Giải pháp đề xuất sử dụng hệ thống phân lớp:

- **notifee** xử lý notification channel và full-screen intent
- **Headless JS task** quản lý logic alarm khi app bị kill
- **ForegroundService** giữ alarm kêu cho đến khi người dùng tắt

Trên iOS, `UNNotificationServiceExtension` với critical alerts (đòi hỏi entitlement từ Apple) đảm bảo độ tin cậy.

### 7.2 Pipeline Xử lý Giọng nói

Pipeline STT nên theo luồng:

```
Người dùng nói
  → STT thiết bị bắt text
  → Chuẩn hóa (viết thường, bỏ dấu câu, trim)
  → Áp dụng quy tắc ngôn ngữ (xử lý mạo từ Tiếng Đức, tách từ ghép)
  → Chấm điểm kết hợp Levenshtein (60%) + điểm ngữ âm (40%)
  → Chấp nhận nếu tổng điểm > 70%
```

Thành phần ngữ âm rất quan trọng vì lỗi STT thường tạo ra kết quả giống về phát âm nhưng khác về chữ viết.

### 7.3 Kiến trúc Dữ liệu

Dữ liệu từ vựng bắt đầu là file JSON nhúng trong app (Phase 1-2) để truy cập không độ trễ. Ở Phase 3, chuyển sang SQLite cho phép:

- Chọn từ ngẫu nhiên hiệu quả với bộ lọc chủ đề/cấp độ
- Truy vấn spaced repetition (ưu tiên từ có tỷ lệ đúng thấp)
- Lưu trữ từ vựng tùy chỉnh của người dùng
- Phân tích lịch sử học

Việc chuyển đổi nên không gây lỗi với cờ phiên bản trong AsyncStorage.

---

## 8. Tiêu chí Thành công MVP

| Chỉ số | Mục tiêu | Cách đo |
|---|---|---|
| Độ tin cậy Alarm | > 99% tỷ lệ kêu | Test trên 5+ thiết bị Android đa hãng |
| Tỷ lệ chấp nhận STT | > 80% từ đúng được chấp nhận lần đầu | Ghi log mọi lần thử STT với điểm số |
| Thời gian hoàn thành thử thách | < 60 giây trung bình | Đo từ lúc alarm kêu đến tắt |
| Tỷ lệ dùng Fail-safe | < 15% tổng số lần tắt | Theo dõi fail-safe vs tắt bằng giọng |
| Tỷ lệ crash | < 0.5% phiên | Crashlytics / Sentry |
| Thời gian khởi động | < 2 giây | Performance profiling |

---

## 9. Các Bước Tiếp theo Ngay lập tức

1. **Khởi tạo dự án:** Tạo project Expo Dev Client với TypeScript, NativeWind, expo-router, Zustand
2. **POC Alarm:** Xây dựng bản thử nghiệm alarm full-screen trên Android (đây là rủi ro kỹ thuật số 1)
3. **POC STT:** Test độ chính xác nhận diện giọng Tiếng Đức trong phòng yên tĩnh vs môi trường ồn
4. **Chuẩn bị dữ liệu:** Tạo file JSON từ vựng Tiếng Đức A1-A2 (500 từ có mạo từ, dịch, gợi ý ngữ âm)
5. **Điểm quyết định:** Sau các POC (tuần 2), đánh giá Expo Dev Client có đủ hay cần eject sang bare workflow
