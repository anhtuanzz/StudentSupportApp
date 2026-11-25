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

