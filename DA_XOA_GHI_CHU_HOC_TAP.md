# Bản Student Support V3 – đã xóa Ghi chú học tập

Đã xóa:
- Thẻ “SQLite & Ghi chú học tập” trên trang chủ.
- `StudyNotesActivity.java`, `StudyNotesDao.java`, `activity_study_notes.xml`.
- Khai báo `StudyNotesActivity` trong AndroidManifest.
- Các chuỗi riêng của màn hình ghi chú và tham chiếu từ `HomeActivity`, `DbHelper`.

Giữ nguyên:
- `DbHelper.java`, `DbInstaller.java`, `ExtraStore.java` và database `student_support.db`.
- Chức năng tài khoản, gói học, giỏ hàng, đơn hàng, thanh toán demo, hỗ trợ, học liệu, Quiz, Chatbot FAQ.
- Bảng `study_notes` cũ trong file SQLite mẫu để tránh xóa dữ liệu cũ khi đổi phiên bản; ứng dụng không còn truy cập bảng đó.

Đã kiểm tra: cú pháp XML, tham chiếu resource/ID, toàn vẹn SQLite và thử thao tác CRUD chính trên database bản sao.
Chưa build APK bằng Android Studio/Android SDK trong môi trường tạo ZIP.
