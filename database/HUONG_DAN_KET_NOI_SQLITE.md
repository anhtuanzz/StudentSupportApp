# Hướng dẫn kết nối SQLite – Student Support (theo Bài 9)

## 1. Cách dự án lưu trữ dữ liệu

- `DbInstaller.java`: chép `app/src/main/assets/student_support.db` vào thư mục database của app khi chưa có dữ liệu.
- `DbHelper.java`: kế thừa `SQLiteOpenHelper`; thao tác tài khoản, gói học, đơn hàng, thanh toán, gói đã mua, hỗ trợ qua `SQLiteDatabase`, `Cursor`, `ContentValues`.
- `ExtraStore.java`: các bảng giỏ hàng, yêu thích, mã khuyến mãi, học liệu, bài tập Quiz, lịch sử Chatbot FAQ và dữ liệu mở rộng.
- `student_support.db`: một cơ sở dữ liệu SQLite dùng chung cho các chức năng trên, xem được bằng DB Browser for SQLite.

**Đã xóa tính năng Ghi chú học tập khỏi giao diện và mã Java.** Bảng `study_notes` trong database mẫu được giữ nguyên chỉ để không làm mất dữ liệu cũ; không còn màn hình hoặc code sử dụng bảng này.

## 2. Kiểm tra dữ liệu

1. Mở Android Studio, chạy ứng dụng; đăng nhập `admin@demo.local / Admin@123` hoặc `sv01@demo.local / Student@123`.
2. Thêm một gói học (Admin) hoặc tạo đơn hàng (Sinh viên).
3. Vào **View → Tool Windows → Device Explorer**.
4. Tìm `/data/data/com.example.studentsupport/databases/student_support.db` (hoặc `/data/user/0/com.example.studentsupport/databases/`).
5. Đóng app để giảm khả năng file WAL còn giao dịch chưa ghi vào DB chính; lưu bản sao SQLite về máy tính. Nếu có WAL/SHM còn dùng, cần sao lưu đúng cách hoặc xuất kèm các tệp liên quan.
6. Mở file `.db` vừa xuất bằng DB Browser for SQLite, chọn Browse Data hoặc Execute SQL.

```sql
SELECT id,name,email,role FROM users;
SELECT id,name,price,duration_days FROM packages ORDER BY id DESC;
SELECT id,user_id,package_id,status,amount FROM orders ORDER BY id DESC;
SELECT id,order_id,amount,status FROM payments ORDER BY id DESC;
```

**Phân biệt:** File mẫu `database/student_support.db` trên máy tính không tự thay đổi khi bạn thêm gói trong máy ảo. Dữ liệu mới được lưu trong file `.db` của máy ảo.

## 3. Cách chạy

Giải nén ZIP → chọn thư mục `StudentSupportProject` (có `settings.gradle`) trong Android Studio → Gradle JDK 17, SDK API 35 → Sync → Run.

Lưu ý: Chatbot hiện là FAQ offline, thanh toán là mô phỏng, SQLite lưu riêng trên từng thiết bị (chưa phải database tập trung qua Internet).
