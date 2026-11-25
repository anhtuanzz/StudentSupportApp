# Kịch bản kiểm thử thủ công cho 5 thành viên

| TC | Người | Thao tác | Kết quả mong đợi |
|---|---|---|---|
| TC01 | 1 | Đăng ký sinh viên với email, mã SV mới | Tạo tài khoản STUDENT, đăng nhập được |
| TC02 | 1 | Đăng ký trùng email/mã SV | Không tạo được tài khoản |
| TC03 | 1 | Đăng nhập sai mật khẩu | Hiển thị thông báo lỗi |
| TC04 | 1 | Quên mật khẩu bằng email + mã SV hợp lệ | Có thể đăng nhập bằng mật khẩu mới |
| TC05 | 1 | Sửa họ tên/số điện thoại | Đăng nhập lại vẫn còn thông tin đã sửa |
| TC06 | 2 | Tìm tên gói “TOEIC” | Chỉ hiện gói phù hợp |
| TC07 | 2 | Lọc danh mục “Tin học” | Chỉ hiện gói Tin học |
| TC08 | 2 | Admin thêm/sửa/xóa mềm gói | Danh sách gói đổi; lịch sử đơn không bị mất |
| TC09 | 2 | Sinh viên thử mở chức năng CRUD | Không có nút CRUD của Admin |
| TC10 | 3 | Chọn gói và đăng ký mua | Có đơn PENDING, lưu đúng giá tại thời điểm mua |
| TC11 | 3 | Hủy đơn PENDING | Chuyển trạng thái CANCELLED |
| TC12 | 3 | Cố hủy đơn PAID | Không thể hủy |
| TC13 | 3 | Admin đánh dấu đơn PAID hoàn tất | Chuyển trạng thái COMPLETED |
| TC14 | 4 | Quét QR | QR mang chuỗi STUDENT_SUPPORT_DEMO, không phải ngân hàng |
| TC15 | 4 | Xác nhận thanh toán mô phỏng | orders=PAID; payments có dòng; subscriptions được kích hoạt |
| TC16 | 4 | Thanh toán lại đơn PAID | Bị từ chối; không cộng tiền hai lần |
| TC17 | 4 | Mua gia hạn gói còn hiệu lực | Cộng ngày vào `expires_at` cũ |
| TC18 | 5 | Sinh viên gửi yêu cầu hỗ trợ | Có phiếu OPEN |
| TC19 | 5 | Admin phản hồi phiếu | Phiếu RESOLVED và lưu câu trả lời |
| TC20 | 5 | Sinh viên đánh giá phiếu RESOLVED | Lưu số sao 1–5, không đánh giá trùng |
| TC21 | 5 | Admin xem thống kê | Doanh thu theo bảng payments, không tính đơn PENDING |
| TC22 | Chung | Xuất file `.db` bằng Device Explorer, mở DB Browser | Kiểm tra được 7 bảng nghiệp vụ và dữ liệu đã thao tác |

> Kiểm thử thực tế trên emulator cần thực hiện sau khi Gradle Sync/build thành công trên Android Studio của người dùng. File ZIP chưa có APK đã biên dịch sẵn.
