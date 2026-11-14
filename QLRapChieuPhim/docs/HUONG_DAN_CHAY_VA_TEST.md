# Hướng dẫn chạy và test toàn bộ hệ thống

## 1. Chuẩn bị
1. Chạy file `sql/QuanLyPhimVaDatVe_tao_CSDL.sql` trong SSMS (tạo lại CSDL + dữ liệu mẫu).
2. Sao chép `src/main/resources/application.properties.example` thành `application.properties`, rồi sửa `spring.datasource.password=<mật khẩu sa của máy bạn>` (tệp này đã nằm trong `.gitignore`, KHÔNG commit mật khẩu thật).
3. IntelliJ: mở thư mục `QLRapChieuPhim` (có pom.xml) -> Maven Reload -> chạy `QlRapChieuPhimApplication`. Dòng cuối console phải có "Started QlRapChieuPhimApplication" và "SoatVeServer đã khởi động, lắng nghe trên cổng: 9999".

Tài khoản mẫu (mật khẩu chung `123456`): `admin` (QUAN_TRI), `nhanvien1..3` (NHAN_VIEN), `khach01..` (KHACH_HANG).

## 2. Kịch bản test (làm theo thứ tự)
| # | Việc | Kết quả đúng |
|---|------|--------------|
| 1 | Mở http://localhost:8080, xem danh sách phim, lọc/tìm, bấm 1 phim | Poster hiện, trang chi tiết có các suất sắp tới |
| 2 | Đăng ký tài khoản mới, đăng nhập, đổi mật khẩu ở /ho-so | Thành công, đăng xuất rồi đăng nhập lại bằng mật khẩu mới |
| 3 | Đăng nhập `khach01` -> chi tiết phim -> chọn suất -> chọn 2 ghế -> Giữ ghế | Sang trang thanh toán, đồng hồ đếm ngược 5 phút |
| 4 | Bấm thanh toán | Trang kết quả hiện mã đơn + mã từng vé |
| 5 | /lich-su -> chi tiết đơn vừa đặt -> hủy (suất còn >= 60 phút) | Đơn DA_HUY, ghế trống lại ở trang chọn ghế |
| 6 | Đặt ghế nhưng KHÔNG thanh toán, chờ > 5 phút (scheduler chạy mỗi phút) | Đơn chuyển HET_HAN, ghế trống lại |
| 7 | Mở 2 trình duyệt (2 khách) cùng chọn 1 ghế, bấm giữ gần như cùng lúc | Chỉ 1 người thành công, người kia báo "ghế đã có người đặt" |
| 8 | Đăng nhập `nhanvien1` -> /quay/ban-ve -> chọn suất, ghế, nhập tiền khách đưa -> thu tiền | Trang kết quả có mã vé; ô tiền thối hiện đúng |
| 9 | /tra-cuu: tìm theo mã đơn / SĐT / họ tên, xem chi tiết, thử hủy đơn | Tìm được; hủy theo đúng quy tắc 60 phút |
| 10 | Đăng nhập `admin`: /admin/phim, /admin/theloai, /admin/phong, /admin/suat, /admin/taikhoan, /admin/thong-ke | Thêm/sửa/xóa chạy; xóa thể loại có phim và xóa phòng có suất bị chặn; thêm suất trùng giờ cùng phòng bị chặn; thống kê có số liệu |
| 11 | Phân quyền: đăng nhập `khach01` rồi vào /admin/phim, /quay/ban-ve; chưa đăng nhập vào /lich-su | Bị chuyển tới /khong-co-quyen hoặc /dang-nhap |

## 3. Test soát vé (Socket, cổng 9999)
Giữ server đang chạy, mở project `QLRapChieuPhimClient` trong IntelliJ khác, chạy `SoatVeClient`:
- Nhập mã vé hợp lệ (lấy ở bước 4 hoặc 8) -> "SOÁT VÉ THÀNH CÔNG"; nhập lại lần 2 -> "ĐÃ ĐƯỢC SỬ DỤNG".
- `INFO <mã vé>` -> chỉ xem thông tin, không đổi trạng thái.
- Mã sai -> "không tồn tại"; vé đã hủy -> "ĐÃ BỊ HỦY"; vé của suất chưa tới giờ (> 60 phút) hoặc đã hết -> "sai suất".
- Mở 3 client cùng lúc, soát cùng 1 mã -> chỉ 1 client thành công.
- `QUIT` để thoát.

## 4. Nếu lỗi
Chụp màn hình trang lỗi và copy các dòng `ERROR` / `Caused by` trong console IntelliJ gửi lại.
