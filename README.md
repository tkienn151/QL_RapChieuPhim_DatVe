# Hệ Thống Quản Lý Rạp Chiếu Phim & Đặt Vé Xem Phim (CineStar)

Ứng dụng Web quản lý rạp chiếu phim, đặt vé trực tuyến và bán vé tại quầy xây dựng bằng **Spring Boot 3**, **Thymeleaf**, **Bootstrap 5** và **Microsoft SQL Server**, kết hợp **xử lý đồng thời** (`@Transactional`, khóa bi quan `PESSIMISTIC_WRITE`, `@Scheduled`) và **lập trình mạng TCP Socket** phục vụ soát vé đa luồng.

---

## 1. Cấu trúc kho mã nguồn

Kho lưu trữ gồm hai module Maven độc lập:

| Thư mục | Vai trò |
|---|---|
| `QLRapChieuPhim/` | Ứng dụng Web Spring Boot (kiến trúc Controller – Service – Repository – Entity/View), máy chủ TCP Socket soát vé cổng `9999`, script CSDL `sql/QuanLyPhimVaDatVe.sql`, bộ kiểm thử JUnit 5 (`src/test`) và tài liệu kỹ thuật (`docs/`). |
| `QLRapChieuPhimClient/` | Chương trình Console Client Java thuần tách biệt, kết nối TCP Socket tới máy chủ soát vé tại cổng `9999` (`SoatVeClient` tương tác và `KiemThuNhieuClient` chạy nhiều luồng đồng thời). |

---

## 2. Yêu cầu môi trường

- **Java Development Kit (JDK):** Phiên bản **17** trở lên (`pom.xml` biên dịch mục tiêu Java `17`).
- **Apache Maven:** Phiên bản **3.9+** (hoặc sử dụng Maven tích hợp trong IntelliJ IDEA / Eclipse).
- **Cơ sở dữ liệu:** **Microsoft SQL Server** (`2019`/`2022` hoặc Express), bật giao thức TCP/IP cổng mặc định `1433`.
- **Trình duyệt & Internet:** Cần kết nối Internet khi mở giao diện web để tải thư viện Bootstrap `5.3.3`, Bootstrap Icons `1.11.3` và phông chữ `Be Vietnam Pro` từ CDN.

---

## 3. Hướng dẫn cài đặt và chạy

### Bước 1: Tạo cơ sở dữ liệu và dữ liệu mẫu
1. Mở **SQL Server Management Studio (SSMS)** (hoặc công cụ dòng lệnh `sqlcmd`).
2. Mở và thực thi toàn bộ tập lệnh **`QLRapChieuPhim/sql/QuanLyPhimVaDatVe.sql`**.
3. Tập lệnh sẽ tự động tạo mới cơ sở dữ liệu `QuanLyPhimVaDatVe` gồm **8 bảng** (`NguoiDung`, `TheLoai`, `Phim`, `PhongChieu`, `Ghe`, `SuatChieu`, `DonDat`, `Ve`) và nạp sẵn dữ liệu mẫu đầy đủ:
   - `25` thể loại, `25` phim (`P001`..`P025`), `25` phòng chiếu (`P01`..`P25`, mỗi phòng `6` hàng × `8` cột = `48` ghế), `1.200` ghế (`A`–`D` thường, `E`–`F` VIP).
   - `25` tài khoản (`admin`, `nhanvien1`..`nhanvien3`, `khach01`..`khach21`).
   - `45` suất chiếu, `25` đơn đặt (`DD0001`..`DD0025`) và `50` vé (`VE000001`..`VE000050`).

### Bước 2: Cấu hình kết nối ứng dụng
1. Sao chép tệp mẫu `QLRapChieuPhim/src/main/resources/application.properties.example` thành:
   `QLRapChieuPhim/src/main/resources/application.properties` *(tệp cấu hình thật đã được đưa vào `.gitignore`)*.
2. Chỉnh thông tin tài khoản SQL Server trong `application.properties`:
   ```properties
   spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=QuanLyPhimVaDatVe;encrypt=true;trustServerCertificate=true
   spring.datasource.username=sa
   spring.datasource.password=MAT_KHAU_SA_CUA_BAN
   server.port=8080
   soatve.socket.port=9999
   soatve.server.enabled=true
   ```

### Bước 3: Chạy máy chủ (Web Spring Boot + TCP Socket Server)
Tại thư mục `QLRapChieuPhim/`, chạy lệnh:
```bash
mvn spring-boot:run
```
(Hoặc mở lớp `com.qlrapchieuphim.QlRapChieuPhimApplication` trong IDE và chọn Run.)

Khi khởi động thành công, console hiển thị thông báo:
```text
SoatVeServer đã khởi động, lắng nghe trên cổng TCP: 9999
```
- Truy cập giao diện Web tại: **http://localhost:8080**
- Kiểm tra nhanh kết nối CSDL và số dòng 8 bảng tại: **http://localhost:8080/kiem-tra**

### Bước 4: Chạy chương trình Client Soát vé (`QLRapChieuPhimClient`)
Sau khi máy chủ ở Bước 3 đã mở cổng TCP `9999`, mở terminal mới tại thư mục `QLRapChieuPhimClient/`:
```bash
mvn clean compile
```
- **Chạy 1 client soát vé tương tác:**
  ```bash
  java -cp target/classes com.qlrapchieuphimclient.SoatVeClient
  ```
  Các lệnh hỗ trợ trên cửa sổ dòng lệnh:
  - `INFO <maVe>` hoặc `INFO|<maVe>`: Tra cứu thông tin vé (chưa làm thay đổi trạng thái).
  - `<maVe>` hoặc `CHECKIN|<maVe>`: Thực hiện soát vé và cập nhật trạng thái `DA_SOAT`.
  - `TEST [maVe]`: Kích hoạt kịch bản **4 client** kết nối đồng thời (`KiemThuNhieuClient`) cùng soát một mã vé (mặc định `VE000001`).
  - `QUIT`: Thoát chương trình và đóng socket an toàn.
- **Chạy kiểm thử nhiều client kết nối đồng thời (`4` luồng):**
  ```bash
  java -cp target/classes com.qlrapchieuphimclient.KiemThuNhieuClient
  ```
  (Tuỳ chọn truyền tham số `[host] [port] [maVe]`, mặc định `127.0.0.1 9999 VE000001`.)

### Bước 5: Chạy bộ kiểm thử tự động JUnit 5
Tại thư mục `QLRapChieuPhim/`, bộ test sử dụng cơ sở dữ liệu H2 trong bộ nhớ (đã cấu hình trong `src/test/resources/application-test.properties`, không ảnh hưởng SQL Server thật):
```bash
mvn test
```
Bộ kiểm thử gồm **9 ca kiểm thử** trong **3 lớp**:
- `QlRapChieuPhimApplicationTests` (`1` ca): kiểm tra nạp ngữ cảnh Spring Boot và tắt socket tự động ở profile test.
- `KiemThuChucNangWebMvcTest` (`4` ca): kiểm thử toàn bộ màn hình Thymeleaf, phân quyền, tìm kiếm/sắp xếp, lỗi trường nhập, đặt vé/giữ ghế/thanh toán, bán vé tại quầy, tra cứu và lịch sử đơn quá hạn.
- `KiemThuDongThoiVaMangTest` (`4` ca): kiểm thử `50` luồng cùng đặt một ghế, lịch trình tự động nhả ghế quá hạn `5` phút, `4` client TCP Socket cùng soát đồng thời một vé và các ràng buộc nghiệp vụ khóa bi quan.

---

## 4. Tài khoản mẫu kiểm thử

Mật khẩu chung cho toàn bộ tài khoản mẫu trong cơ sở dữ liệu là **`123456`** (lưu dạng băm SHA-256 trong cột `MatKhau`):

| Vai trò (`VaiTro`) | Tên đăng nhập | Họ và tên |
|---|---|---|
| `QUAN_TRI` | `admin` | Quản Trị Hệ Thống |
| `NHAN_VIEN` | `nhanvien1` | Phạm Trung Kiên |
| `NHAN_VIEN` | `nhanvien2` | Phạm Trần Ngọc Nhân |
| `NHAN_VIEN` | `nhanvien3` | Phạm Quốc Đạt |
| `KHACH_HANG` | `khach01` | Nguyễn Trọng Nhân |
| `KHACH_HANG` | `khach02` | Lê Huy Quốc Bảo |
| `KHACH_HANG` | `khach03` | Nguyễn Thanh Huyền |
| `KHACH_HANG` | `khach04` .. `khach21` | `18` tài khoản khách hàng bổ sung khác |

---

## 5. Điểm nổi bật về Xử lý đồng thời & Lập trình mạng

- **Chống đặt trùng ghế khi truy cập đồng thời:** Giao dịch `@Transactional` kết hợp khóa bi quan (`LockModeType.PESSIMISTIC_WRITE`) trên `VeRepository` và `SuatChieuRepository`, đảm bảo khi nhiều khách hàng hoặc nhân viên quầy cùng chọn một ghế tại một thời điểm thì chỉ một yêu cầu thành công.
- **Tự động giải phóng ghế giữ chỗ quá hạn:** Lớp `NhaGheScheduler` (`@Scheduled(fixedRate = 60000)`) quét định kỳ mỗi 60 giây các đơn ở trạng thái `CHO_THANH_TOAN` đã vượt quá `HanThanhToan` (5 phút) để chuyển sang `HET_HAN` và giải phóng toàn bộ ghế giữ.
- **Máy chủ TCP Socket đa luồng (`SoatVeServer`):** Sử dụng `ServerSocket` lắng nghe cổng `9999` và `ExecutorService` (`Executors.newFixedThreadPool(10)` đặt tên luồng `soat-ve-worker-<n>`, `SO_TIMEOUT = 60s`). Mỗi lệnh `CHECKIN|<maVe>` gọi `SoatVeService.soatVe` với khóa bi quan `findByMaVeForUpdate` để chỉ cho phép soát hợp lệ duy nhất một lần.
- **Phân quyền, phiên & xử lý lỗi nhất quán:** Phiên đăng nhập giới hạn `30` phút (`AuthController`), kiểm tra trạng thái hoạt động từ CSDL ở mỗi request (`DangNhapInterceptor`), phân tách quyền truy cập (`PhanQuyenInterceptor`), thông báo lỗi chi tiết theo từng ô nhập (`LoiTruongException` + `fragments/loi-truong.html`) và trang báo lỗi thân thiện (`GlobalExceptionHandler` + `loi.html`).

---

## 6. Phân công nhóm phát triển

| Nhóm nhiệm vụ | Thành viên phụ trách (MSSV) | Phạm vi chức năng & thành phần chính | Đóng góp |
|---|---|---|---|
| **Nhóm 3** (Nhóm trưởng) | **Phạm Trung Kiên** (`2001240225`) | Tài khoản, phân quyền & hạ tầng Web (`C01`, `KH01`, `C02`, `KH02`, `NF03`): `AuthController`, `PhimController`, `GlobalExceptionHandler`, `DangNhapInterceptor`, `PhanQuyenInterceptor`, `WebConfig`, `PhienDangNhap`, `PasswordUtil`, `NguoiDungService`, `LichChieuService`, `LoiTruongException`, `fragments/*`, `auth/*`, `phim/*`, `static/*`, tài liệu `docs/*` và bộ kiểm thử tích hợp `src/test/*`. | `20%` |
| **Nhóm 1** | **Phạm Trần Ngọc Nhân** (`2001240326`) | Đặt vé trực tuyến, giữ ghế 5 phút, scheduler nhả ghế & bán vé tại quầy (`KH03`, `KH04`, `NV01`): `DatVeController`, `QuayController`, `DatVeService`, `BanVeService`, `NhaGheScheduler`, `datve/*`, `quay/*`. | `20%` |
| **Nhóm 2** | **Nguyễn Trọng Nhân** (`2001240325`) | TCP Socket Soát vé đa luồng, tra cứu đơn/vé & quản trị tài khoản (`NV03`, `NV02`, `QT04`): `SoatVeServer`, `XuLyClientTask`, `SoatVeProtocol`, `SoatVeService`, `TraCuuVeController`, `AdminTaiKhoanController`, `tracuu/*`, `taikhoan-*` và module `QLRapChieuPhimClient`. | `20%` |
| **Nhóm 5** | **Lê Huy Quốc Bảo** (`2001240029`) | Quản trị suất chiếu, lịch sử đặt vé & thống kê doanh thu (`QT03`, `KH05`, `QT05`): `AdminSuatController`, `LichSuController`, `ThongKeController`, `SuatChieuService`, `VeService`, `ThongKeService`, `dto/*`, `suat-*`, `thongke.html`, `lichsu/*`. | `20%` |
| **Nhóm 4** | **Nguyễn Thanh Huyền** (`2001230316`) | Quản trị phim, upload/preview poster, thể loại & phòng chiếu tự động sinh sơ đồ ghế (`QT01`, `QT02`): `AdminPhimController`, `AdminPhongController`, `PhimService`, `TheLoaiService`, `PhongChieuService`, `phim-*`, `theloai-*`, `phong-*`. | `20%` |

---

## 7. Tài liệu kèm theo

- [QLRapChieuPhim/README.md](QLRapChieuPhim/README.md): Thông tin kỹ thuật module Spring Boot Server.
- [QLRapChieuPhimClient/README.md](QLRapChieuPhimClient/README.md): Hướng dẫn biên dịch, chạy và giao thức Client TCP Soát vé.
- [QLRapChieuPhim/docs/HUONG_DAN_CHAY_VA_TEST.md](QLRapChieuPhim/docs/HUONG_DAN_CHAY_VA_TEST.md): Kịch bản chạy demo, JUnit 5 (`9` ca test) và kiểm thử từng vai trò.
- [QLRapChieuPhim/docs/HUONG_DAN_DUNG_CHUNG.md](QLRapChieuPhim/docs/HUONG_DAN_DUNG_CHUNG.md): Tài liệu tham chiếu các fragment, interceptor, bảo mật phiên và helper dùng chung.
- [QLRapChieuPhim/docs/CAY_THU_MUC.txt](QLRapChieuPhim/docs/CAY_THU_MUC.txt): Cây thư mục đầy đủ của toàn bộ kho mã nguồn.
- [QLRapChieuPhim/docs/DANH_SACH_POSTER.txt](QLRapChieuPhim/docs/DANH_SACH_POSTER.txt): Danh sách tài nguyên ảnh poster khớp `P001`..`P025` trong CSDL.
