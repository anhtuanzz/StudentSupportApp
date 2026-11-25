# Student Support V4 — Thuộc tính gói hỗ trợ học tập

**Android Studio · Java · XML · SQLite · giao diện cam–trắng**

## Thay đổi so với V3

Phần **Quản lý gói học tập** đã bổ sung các thuộc tính có thể nhập và sửa từ màn hình Admin:

| Trường | Cột SQLite | Mô tả |
|---|---|---|
| Mã gói hỗ trợ | `package_extras.package_code` | Duy nhất, ví dụ `SV-CNTT` |
| Tên, danh mục, mô tả | `packages.name/category/description` | Thuộc tính chính của gói |
| Giá gốc | `packages.price` | Số tiền đồng |
| Giá ưu đãi | `package_extras.sale_price` | 0 nghĩa là không giảm |
| Thời hạn | `packages.duration_days` | Số ngày sử dụng |
| Số bài học | `package_extras.lesson_count` | Số lượng bài học, không âm |
| Đối tượng sinh viên | `package_extras.target_students` | Người phù hợp |
| Trình độ | `package_extras.level` | Cơ bản / trung cấp / nâng cao |
| Giảng viên / mentor | `package_extras.lecturer` | Người phụ trách |
| Mã học phần | `package_extras.course_code` | Có thể để trống |
| Lộ trình học | `package_extras.study_path` | Các giai đoạn học tập |
| Quyền lợi | `package_extras.benefits` | Mỗi dòng một quyền lợi |
| Hình thức hỗ trợ | `package_extras.support_channel` | Chat, Q&A, mentor… |
| Yêu cầu đầu vào | `package_extras.prerequisites` | Kiến thức cần trước khóa học |
| Học thử | `package_extras.trial_url` | Liên kết HTTPS, có thể trống |

Giao diện **Gói học tập** hiển thị giá ưu đãi và số bài; **Chi tiết gói** hiển thị toàn bộ thông tin mở rộng. Tìm kiếm nhận tên, mô tả, giảng viên, mã học phần, mã gói và đối tượng.

## Mở project

1. Giải nén ZIP và chọn thư mục `StudentSupportProject` tại Android Studio → **File → Open**.
2. Đảm bảo JDK 17 và Android SDK API 35, rồi Sync Gradle.
3. Chạy máy ảo, chọn cấu hình `app` → **Run**.
4. Đăng nhập Admin: `admin@demo.local` / `Admin@123`.
5. Chọn **Gói học tập** → **Thêm gói** để tạo gói với toàn bộ thuộc tính; để sửa hãy **nhấn giữ gói** → **Sửa gói**, hoặc bấm **Chỉnh sửa toàn bộ thuộc tính gói** trong trang chi tiết.
6. Đăng nhập sinh viên: `sv01@demo.local` / `Student@123` để kiểm tra chi tiết gói.

## Kết nối DB Browser for SQLite

Có sẵn cơ sở dữ liệu mẫu tại `database/student_support.db` và `app/src/main/assets/student_support.db`.
Ứng dụng dùng tệp riêng trong máy ảo:
`/data/data/com.example.studentsupport/databases/student_support.db`.
**Cơ sở dữ liệu mẫu không tự cập nhật** khi bạn thao tác trong máy ảo. Vào Device Explorer để xuất file SQLite thực tế và mở bằng DB Browser.

Câu SQL kiểm tra các thuộc tính:

```sql
SELECT p.id, x.package_code, p.name, p.category,
       p.price, x.sale_price, p.duration_days, x.lesson_count,
       x.level, x.target_students, x.lecturer,
       x.support_channel, x.benefits
FROM packages AS p
LEFT JOIN package_extras AS x ON x.package_id = p.id
ORDER BY p.id;
```

**Nâng cấp an toàn:** `DbHelper` đã nâng SQLite lên phiên bản 4; khi mở dữ liệu V3, hàm `onUpgrade` chỉ thêm cột trong `package_extras`, không DROP TABLE, không xóa tài khoản hoặc lịch sử đơn hàng. Nếu đang dùng database V3 trong máy ảo, không cần Clear Storage.

**Đã kiểm tra:** XML, tài nguyên, dữ liệu mẫu, độ toàn vẹn SQLite, mô phỏng thao tác thêm/sửa/xóa và nâng cấp database. **Chưa xác nhận biên dịch APK và chạy trực tiếp trên Android Emulator.**

---

# Student Support v2 · Ứng dụng bán gói học tập (Android Studio)

## SQLite theo Bài 9 (không có màn hình Ghi chú học tập)
- SQLite vẫn được ứng dụng sử dụng để quản lý tài khoản, gói học, giỏ hàng, đơn hàng, thanh toán, hỗ trợ, học liệu và Chatbot FAQ.
- `DbInstaller.java` chép database mẫu từ `assets` khi cài mới; `DbHelper.java` và `ExtraStore.java` xử lý thêm / xem / sửa / xóa dữ liệu.
- Đã bỏ nút "SQLite & ghi chú học tập", màn hình ghi chú và lớp DAO riêng của ghi chú.
- **Không xóa bảng `study_notes` đã có trong database mẫu** để giữ tương thích và tránh làm mất dữ liệu từ phiên bản trước. Bảng cũ không còn được ứng dụng truy cập.
- Xem [`database/HUONG_DAN_KET_NOI_SQLITE.md`](database/HUONG_DAN_KET_NOI_SQLITE.md) để xem dữ liệu SQLite bằng DB Browser.

## Cài đặt cho người mới
1. Giải nén ZIP, vào đúng thư mục `StudentSupportProject` chứa `settings.gradle`.
2. Android Studio → Open → chọn thư mục `StudentSupportProject`, không chọn ZIP và không chọn thư mục ngoài.
3. Chọn **Gradle JDK 17**, cài **Android SDK API 35**; cho Android Studio Gradle Sync.
4. Tạo máy ảo qua Tools → Device Manager → Pixel/Medium Phone, Android API 35 hoặc cao hơn.
5. Chọn cấu hình **app** → nhấn Run ▶.
6. Mở app, chọn **Demo Sinh viên** hoặc **Demo Admin**, rồi nhấn Đăng nhập.

## Tài khoản mẫu
- Admin: `admin@demo.local` / `Admin@123`
- Sinh viên: `sv01@demo.local` / `Student@123`

Đây là bản học phần Android Studio dùng Java + giao diện XML cam/trắng + SQLite. Không có tiền thật.

## Đã lập trình có dữ liệu SQLite

### Phân hệ sinh viên
- Đăng ký qua email, đăng nhập bằng email/mật khẩu, cập nhật hồ sơ (trường/chuyên ngành/khóa), khôi phục bằng mã SV (demo).
- Email kết thúc `.edu.vn` hưởng ưu đãi **10% demo** ở **giỏ hàng**; chưa xác minh tên trường hay quyền sở hữu email.
- Tìm gói theo tên, mô tả, giảng viên, mã học phần; lọc danh mục, giá, thời hạn, điểm đánh giá.
- Chi tiết: giảng viên, lộ trình, học thử URL (nếu admin nhập), đánh giá gói 1–5 sao sau khi kích hoạt.
- Giỏ hàng, yêu thích; voucher `WELCOME10` (10%), `EXAM20` (20%). Giá áp dụng mức giảm cao hơn giữa mã và .edu.vn.
- Mua giỏ tạo một đơn `PENDING` cho mỗi gói (do cấu trúc đơn hàng cũ: một đơn = một gói).
- Xác nhận QR **GIẢ LẬP** tại Đơn hàng để kích hoạt gói; gia hạn gói; lịch sử.
- Không gian học tập: học liệu dạng link HTTPS (demo), đánh dấu hoàn thành, tiến độ %, Quiz lưu kết quả.
- Thông báo **trong app** về gói sắp hết hạn (kiểm tra lúc mở màn hình, chưa có push nền).
- Chatbot **FAQ OFFLINE** trả lời câu hỏi về gói, ôn tập, thanh toán, hỗ trợ; lưu lịch sử chat.

### Phân hệ quản trị
- CRUD/ẩn gói trong Gói học; sửa mã học phần, giảng viên, lộ trình, URL học thử và giá ưu đãi trong trang chi tiết.
- Quản lý đơn hàng, trạng thái thanh toán demo, doanh thu, ticket hỗ trợ.
- Xem người dùng và khóa/mở khóa đăng nhập tài khoản sinh viên.
- Thêm voucher, lưu học liệu URL, thêm câu hỏi trắc nghiệm.

## Cần dịch vụ bên ngoài – CHƯA phải chức năng thực tế
- Google OAuth sign-in (cần cấu hình Firebase/Google Identity, client ID và backend token verification).
- OTP/email reset (cần nhà cung cấp gửi email/SMS và xác thực an toàn ở backend; bản hiện tại chỉ dùng mã SV demo).
- MoMo/ZaloPay/VietQR thật (cần merchant account, backend và webhook xác minh, **không** xác nhận tiền thật bằng nút trên điện thoại).
- AI sinh câu trả lời thật (chưa có backend); bản offline chỉ là chatbot FAQ có quy tắc, **không phải mô hình AI**.
- Upload video/PDF lên máy chủ, push notification, đa thiết bị thời gian thực (SQLite là CSDL cục bộ trên một thiết bị).

## Kết nối AI qua backend sau này (đã có hợp đồng giao tiếp trong app)
Trong `app/src/main/res/values/strings.xml`, thay giá trị trống của `chat_backend_url` bằng địa chỉ HTTPS của backend bạn tự quản lý.

Android gọi `POST https://your-domain.example/api/chat`, body JSON:
```json
{"message":"Làm sao ôn thi môn Toán cao cấp?"}
```
Backend trả về `200 OK`, content-type JSON:
```json
{"reply":"Bạn nên..."}
```
**Không nhúng khóa API vào Android**. Backend phải tự xác thực người dùng, giới hạn lượt gọi, quản lý khóa và lọc dữ liệu nhạy cảm. Mã khách Android gửi câu hỏi hiện tại; backend phải được bổ sung để có AI thật.

## SQLite
- CSDL: `app/src/main/assets/student_support.db` và `database/student_support.db`.
- Mở bằng **DB Browser for SQLite** → Open Database.
- File thực khi chạy: `/data/data/com.example.studentsupport/databases/student_support.db`, lấy qua Android Studio **Device Explorer**.
- Database `user_version=3` có thêm bảng giỏ, yêu thích, voucher, hồ sơ, học liệu, Quiz, tiến độ, đánh giá, chatbot, thông báo, khóa tài khoản.
- DBHelper `onUpgrade` nâng dữ liệu phiên bản 1 lên 2 rồi lên 3, giữ đơn hàng và tài khoản cũ.
- Nếu bản máy ảo cũ gặp lỗi tài nguyên/database, thử backup qua Device Explorer trước khi xóa dữ liệu app; xóa dữ liệu sẽ xóa đơn hàng cũ.

## Chia công việc 5 người
1. Tài khoản và hồ sơ: `LoginActivity`, `RegisterActivity`, `ProfileActivity`, `Session`, `PasswordUtil`.
2. Danh mục, bộ lọc, chi tiết, đánh giá: `PackagesActivity`, `PackageDetailActivity`.
3. Giỏ/yêu thích/đơn hàng/voucher: `CartActivity`, `OrdersActivity`, `OrderDetailActivity`.
4. Thanh toán demo, subscriptions, học tập: `PaymentActivity`, `SubscriptionsActivity`, `LearningActivity`.
5. Hỗ trợ, thống kê, admin, chatbot FAQ: `SupportActivity`, `StatsActivity`, `AdminToolsActivity`, `ChatbotActivity`, `NotificationsActivity`.

Các thao tác CSDL v2 tập trung ở `ExtraStore.java` và `DbHelper.java`.

## Kiểm thử
Chạy `python kiem_tra_v2.py` nếu có Python 3 để kiểm tra cú pháp XML, tham chiếu resource, database và thử truy vấn SQLite trên bản sao. **Chưa thể khẳng định build APK thành công** khi không có Android SDK/Gradle trong môi trường tạo file. Cần chạy `Build → Rebuild Project` trên Android Studio thực tế.


## Vị trí mở Chatbot ở bản đã sửa

- Sau khi đăng nhập, ngay dưới banner chào mừng Trang chủ có thẻ **🤖 Chatbot trợ lý học tập**. Nhấn vào để mở.
- Ngoài ra vào tab **Hỗ trợ**, nhấn **🤖 Mở Chatbot trợ lý học tập (FAQ offline)**.
- Chatbot hiện có trả lời FAQ offline; chưa phải AI sinh văn bản thực. AI trực tuyến cần cấu hình endpoint HTTPS backend trong `app/src/main/res/values/strings.xml` (`chat_backend_url`), triển khai backend nhận JSON `{\"message\": \"...\"}` và trả JSON `{\"reply\": \"...\"}`. Không đặt khóa API AI trong mã Android.

Khi cập nhật từ bản V2 cũ, có thể chạy trực tiếp bản này lên app đang cài mà không xóa dữ liệu.
