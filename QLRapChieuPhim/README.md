# QLRapChieuPhim - Ứng dụng Web Spring Boot & Máy chủ TCP Socket Soát vé

Module máy chủ trung tâm của hệ thống Quản lý Rạp chiếu phim và Đặt vé xem phim (`CineStar`), đảm nhiệm giao diện Web đa vai trò, xử lý nghiệp vụ giao dịch/đồng thời trên SQL Server và lắng nghe kết nối TCP Socket soát vé tại cổng `9999`.

---

## 1. Công nghệ & Yêu cầu

- **Java:** JDK **17** (`java.version=17`, `UTF-8`)
- **Framework:** Spring Boot **3.2.5** (`spring-boot-starter-web`, `spring-boot-starter-thymeleaf`, `spring-boot-starter-data-jpa`, `spring-boot-starter-test`)
- **Cơ sở dữ liệu:** Microsoft SQL Server (`com.microsoft.sqlserver:mssql-jdbc`); kiểm thử tự động sử dụng **H2 Database** (`com.h2database:h2`)
- **Giao diện:** Thymeleaf kết hợp Bootstrap `5.3.3` & Bootstrap Icons `1.11.3` (tải qua CDN)

---

## 2. Cấu trúc phân tầng (`com.qlrapchieuphim`)

| Package | Vai trò & các lớp tiêu biểu |
|---|---|
| `config/` | Cấu hình phiên đăng nhập (`PhienDangNhap`), mã hóa SHA-256 (`PasswordUtil`), đăng ký interceptor & ánh xạ thư mục poster upload (`WebConfig`). |
| `interceptor/` | Kiểm tra phiên đăng nhập và trạng thái tài khoản trong CSDL (`DangNhapInterceptor`), phân tách quyền URL theo vai trò (`PhanQuyenInterceptor`). |
| `controller/` | Điều hướng web và kiểm tra đầu vào: `PhimController`, `AuthController`, `DatVeController`, `QuayController`, `TraCuuVeController`, `LichSuController`, `AdminPhimController`, `AdminPhongController`, `AdminSuatController`, `AdminTaiKhoanController`, `ThongKeController`, `KiemTraKetNoiController`, cùng xử lý lỗi toàn cục `GlobalExceptionHandler`. |
| `service/` | Tầng nghiệp vụ và giao dịch `@Transactional`: `NguoiDungService`, `LichChieuService`, `LoiTruongException`, `DatVeService`, `BanVeService`, `SoatVeService`, `PhimService`, `TheLoaiService`, `PhongChieuService`, `SuatChieuService`, `VeService`, `ThongKeService`. |
| `repository/` | Tầng truy vấn Spring Data JPA (`JpaRepository`) với truy vấn tùy chỉnh và khóa bi quan: `NguoiDungRepository`, `TheLoaiRepository`, `PhimRepository`, `PhongChieuRepository`, `GheRepository`, `SuatChieuRepository`, `DonDatRepository`, `VeRepository`. |
| `entity/` & `entity/enums/` | `8` thực thể ánh xạ CSDL (`NguoiDung`, `TheLoai`, `Phim`, `PhongChieu`, `Ghe`, `SuatChieu`, `DonDat`, `Ve`) cùng `7` enum trạng thái (`VaiTro`, `TrangThaiPhim`, `LoaiGhe`, `TrangThaiSuat`, `TrangThaiDon`, `KenhDat`, `TrangThaiVe`). |
| `dto/` | Đối tượng tổng hợp dữ liệu thống kê (`DoanhThuNgayDto`, `DoanhThuPhimDto`). |
| `scheduler/` | Tác vụ nền `NhaGheScheduler` (`@Scheduled(fixedRate = 60000)`) tự động hết hạn đơn giữ chỗ quá `5` phút và giải phóng ghế. |
| `server/` | Máy chủ TCP Socket đa luồng (`SoatVeServer` với `FixedThreadPool(10)`, `XuLyClientTask`, `SoatVeProtocol`) lắng nghe mặc định trên cổng `9999`. |

---

## 3. Khởi tạo CSDL, Cấu hình & Chạy

1. **Nạp CSDL SQL Server:** Thực thi tập lệnh `sql/QuanLyPhimVaDatVe.sql` trong SSMS để tạo database `QuanLyPhimVaDatVe` và nạp sẵn `8` bảng dữ liệu mẫu (`25` thể loại, `25` phim, `25` phòng chiếu, `1.200` ghế, `25` tài khoản, `45` suất chiếu, `25` đơn đặt, `50` vé).
2. **Tạo tệp cấu hình cục bộ:** Sao chép `src/main/resources/application.properties.example` thành `src/main/resources/application.properties` và điền `spring.datasource.username` / `spring.datasource.password`.
3. **Chạy Web Server + Socket Server:**
   ```bash
   mvn spring-boot:run
   ```
   - Giao diện Web: **http://localhost:8080**
   - Kiểm tra kết nối 8 bảng: **http://localhost:8080/kiem-tra**
   - Log xác nhận mở cổng TCP Socket: `SoatVeServer đã khởi động, lắng nghe trên cổng TCP: 9999`
4. **Chạy kiểm thử tự động JUnit 5:**
   ```bash
   mvn test
   ```
   Bộ test chạy **9 ca kiểm thử** (`QlRapChieuPhimApplicationTests`, `KiemThuChucNangWebMvcTest`, `KiemThuDongThoiVaMangTest`) trên cơ sở dữ liệu H2 ở cấu hình `src/test/resources/application-test.properties`.

---

## 4. Điểm cuối chính theo vai trò

- **Công khai:** `/`, `/phim`, `/phim/chi-tiet`, `/phim/{id}`, `/dang-nhap`, `/dang-ky`, `/kiem-tra`, `/khong-co-quyen`
- **Mọi tài khoản đã đăng nhập:** `/ho-so`, `/ho-so/cap-nhat`, `/ho-so/doi-mat-khau`, `/dang-xuat`
- **Khách hàng (`KHACH_HANG`):** `/dat-ve/**`, `/lich-su/**`
- **Nhân viên bán vé & Quản trị (`NHAN_VIEN`, `QUAN_TRI`):** `/quay/**`, `/tra-cuu/**`
- **Quản trị (`QUAN_TRI`):** `/admin/thong-ke`, `/admin/phim/**`, `/admin/theloai/**`, `/admin/phong/**`, `/admin/suat/**`, `/admin/taikhoan/**`

---

## 5. Tài liệu tham khảo nội bộ (`docs/`)

- `docs/HUONG_DAN_CHAY_VA_TEST.md`: Hướng dẫn demo từng vai trò và kiểm thử đồng thời/TCP Socket.
- `docs/HUONG_DAN_DUNG_CHUNG.md`: Quy ước fragment dùng chung, interceptor và helper dùng chung.
- `docs/CAY_THU_MUC.txt`: Cây thư mục đầy đủ của toàn dự án.
- `docs/DANH_SACH_POSTER.txt`: Danh sách tệp ảnh poster và quy tắc nạp ảnh mẫu/upload.
