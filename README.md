# ĐỒ ÁN MÔN HỌC CÔNG NGHỆ JAVA

**Đề tài:** Xây dựng hệ thống quản lý rạp chiếu phim và đặt vé xem phim

---

## 1. Cấu trúc dự án

- **`QLRapChieuPhim/`**: Ứng dụng chính **Spring Boot 3.2.5 + Spring MVC + Thymeleaf + Spring Data JPA + SQL Server + TCP Socket Server (cổng 9999)**.
- **`QLRapChieuPhimClient/`**: Ứng dụng **Console TCP Socket Client** (`SoatVeClient.java` và `KiemThuNhieuClient.java`) kết nối tới `SoatVeServer` cổng `9999` để soát vé tại cửa phòng chiếu (NV03) và kiểm thử 4 client kết nối đồng thời.

---

## 2. Phân công thành viên

| Nhóm nhiệm vụ | Thành viên | Chức năng phụ trách | Các thành phần chính |
|---|---|---|---|
| **Nhóm 1** | **Phạm Trần Ngọc Nhân** | Đặt vé trực tuyến (KH03), Thanh toán 5 phút (KH04), Nhả ghế quá hạn (`@Scheduled`), Bán vé tại quầy (NV01) | `DatVeService`, `BanVeService`, `NhaGheScheduler`, `DatVeController`, `QuayController`, `templates/datve/*`, `templates/quay/*` |
| **Nhóm 2** | **Nguyễn Trọng Nhân** | Soát vé TCP Socket Server & Client (NV03), Tra cứu & hủy vé (NV02), Quản trị tài khoản (QT04) | `SoatVeServer`, `XuLyClientTask`, `SoatVeProtocol`, `SoatVeService`, `TraCuuVeController`, `AdminTaiKhoanController`, `templates/tracuu/*`, `templates/admin/taikhoan-*.html`, `QLRapChieuPhimClient` |
| **Nhóm 3** | **Phạm Trung Kiên (Nhóm trưởng)** | Đăng nhập/Đăng ký/Đăng xuất (C01), Hồ sơ & đổi mật khẩu (KH01, C01), Danh sách & chi tiết phim công khai (KH02), Phân quyền `HandlerInterceptor` (C02), Giao diện dùng chung | `WebConfig`, `PhienDangNhap`, `PasswordUtil`, `DangNhapInterceptor`, `PhanQuyenInterceptor`, `NguoiDungService`, `LichChieuService`, `AuthController`, `PhimController`, `GlobalExceptionHandler`, `templates/fragments/*`, `templates/auth/*`, `templates/phim/*`, `templates/trang-chu.html`, `templates/loi.html`, `static/css/*`, `static/js/*` |
| **Nhóm 4** | **Nguyễn Thanh Huyền** | Quản trị phim & thể loại (QT01), Quản trị phòng chiếu & sinh sơ đồ ghế tự động (QT02) | `PhimService`, `TheLoaiService`, `PhongChieuService`, `AdminPhimController`, `AdminPhongController`, `templates/admin/phim-*.html`, `templates/admin/theloai-*.html`, `templates/admin/phong-*.html` |
| **Nhóm 5** | **Lê Huy Quốc Bảo** | Quản trị suất chiếu & kiểm tra trùng giờ (QT03), Lịch sử đặt vé & hủy vé (KH05), Thống kê doanh thu (QT05) | `SuatChieuService`, `VeService`, `ThongKeService`, `AdminSuatController`, `LichSuController`, `ThongKeController`, `templates/admin/suat-*.html`, `templates/admin/thongke.html`, `templates/lichsu/*` |

---

## 3. Yêu cầu môi trường & Hướng dẫn chạy đồ án trên IntelliJ IDEA

### Yêu cầu môi trường
- **Java / JDK:** **JDK 17** (bắt buộc theo cấu hình `java.version = 17` và Spring Boot `3.2.5`).
- **Cơ sở dữ liệu:** **Microsoft SQL Server** (**SQL Server 2025**, script tạo CSDL tương thích từ **SQL Server 2012 trở lên**).
- **IDE / Build tool:** IntelliJ IDEA (hỗ trợ Maven) hoặc Maven tích hợp sẵn.

### Bước 1: Khởi tạo CSDL SQL Server
1. Mở **SQL Server Management Studio (SSMS)**.
2. Mở và chạy script `QLRapChieuPhim/sql/QuanLyPhimVaDatVe_tao_CSDL.sql` (kiểm tra cả 4 câu truy vấn toàn vẹn ở cuối script đều trả về `0`).
3. Sao chép `QLRapChieuPhim/src/main/resources/application.properties.example` thành `application.properties` (cùng thư mục; tệp này không được đưa lên Git vì chứa mật khẩu), rồi sửa `spring.datasource.password` theo mật khẩu tài khoản `sa` trên máy bạn (cổng `1433`, database `QuanLyPhimVaDatVe`).

### Bước 2: Chạy ứng dụng Web + Socket Server (`QLRapChieuPhim`)
1. Mở thư mục `QLRapChieuPhim` trong IntelliJ IDEA (chọn SDK dự án là **JDK 17**).
2. Chạy class `com.qlrapchieuphim.QlRapChieuPhimApplication` (ứng dụng khởi động Web trên cổng `8080` và `SoatVeServer` trên cổng `9999`).
3. Truy cập trình duyệt tại: `http://localhost:8080`
   - Tài khoản Quản trị (`QUAN_TRI`): `admin` / `123456`
   - Tài khoản Nhân viên (`NHAN_VIEN`): `nhanvien1` / `123456` (hoặc `nhanvien2`, `nhanvien3`)
   - Tài khoản Khách hàng (`KHACH_HANG`): `khach01` / `123456` (đến `khach16`)

### Bước 3: Chạy ứng dụng Console soát vé (`QLRapChieuPhimClient`)
1. Mở thư mục `QLRapChieuPhimClient` trong IntelliJ IDEA (hoặc mở thành module thứ 2, cùng **JDK 17**).
2. Chạy `com.qlrapchieuphimclient.SoatVeClient` (sau khi server `QLRapChieuPhim` đã chạy):
   - Gõ `INFO VE000001` (hoặc `INFO|VE000001`) để xem thông tin vé.
   - Gõ trực tiếp `VE000001` (hoặc `CHECKIN|VE000001`) để soát vé vào phòng chiếu.
   - Gõ `TEST VE000002` (hoặc chạy trực tiếp `KiemThuNhieuClient.main`) để demo 4 Client kết nối TCP Socket đồng thời cùng soát 1 mã vé.
   - Gõ `QUIT` để đóng kết nối.

### Bước 4: Chạy bộ kiểm thử tự động JUnit 5 (Đa luồng, Socket & Web MVC)
Trong IntelliJ IDEA (hoặc chạy bằng Maven CLI `mvn test` trong thư mục `QLRapChieuPhim/`), chạy 3 lớp test trong `QLRapChieuPhim/src/test/java/com/qlrapchieuphim/`:
- `QlRapChieuPhimApplicationTests`: Kiểm thử nạp ngữ cảnh ứng dụng Spring Boot (`contextLoads`).
- `KiemThuDongThoiVaMangTest` (4 test):
  1. **50 luồng** cùng đặt 1 ghế tại cùng 1 thời điểm (`ExecutorService` + `CountDownLatch` + `PESSIMISTIC_WRITE`) -> đúng 1 luồng thành công, 49 luồng bị từ chối.
  2. Tác vụ định kỳ `@Scheduled` (`NhaGheScheduler`) tự động thu hồi đơn giữ ghế quá hạn 5 phút.
  3. **4 Client TCP Socket** kết nối đồng thời tới `SoatVeServer` và cùng soát 1 mã vé -> đúng 1 client nhận `OK|DA_SOAT`, 3 client nhận `ERR|DA_SU_DUNG`.
  4. Kiểm thử các ràng buộc nghiệp vụ: chặn trùng giờ suất chiếu, chặn xóa phim đã có suất chiếu, tự động sinh ghế khi tạo phòng và xóa phòng, chặn hủy vé khi còn dưới 60 phút trước giờ chiếu.
- `KiemThuChucNangWebMvcTest` (3 test): Kiểm thử các màn hình Thymeleaf, luồng đặt vé/in phiếu vé/quầy vé, kiểm thử quản trị và phân quyền `HandlerInterceptor` cho cả 3 vai trò.

> **Ghi chú chạy nhanh qua Maven CLI (tùy chọn ngoài IntelliJ):**
> - Khởi chạy Web + Socket Server: `cd QLRapChieuPhim && mvn spring-boot:run`
> - Chạy toàn bộ 8 ca JUnit 5: `cd QLRapChieuPhim && mvn clean test`
> - Biên dịch và chạy Client TCP Console: `cd QLRapChieuPhimClient && mvn compile && java -cp target/classes com.qlrapchieuphimclient.SoatVeClient`
