# QLRapChieuPhim - Quản lý rạp chiếu phim và đặt vé xem phim

## Cách chạy lần đầu
- **Môi trường:** **JDK 17** và **Microsoft SQL Server 2025** (script tạo CSDL tương thích từ SQL Server 2012 trở lên).
1. Chạy `sql/QuanLyPhimVaDatVe_tao_CSDL.sql` trong SQL Server Management Studio (4 câu kiểm tra cuối file đều phải bằng 0).
2. Mở thư mục này trong IntelliJ (File > Open > chọn `pom.xml` > Open as Project), chọn SDK **JDK 17**, đợi Maven tải thư viện.
3. Sao chép `src/main/resources/application.properties.example` thành `application.properties`, rồi sửa username, password SQL Server (tệp `application.properties` không được đưa lên Git).
4. Chạy `QlRapChieuPhimApplication`, mở http://localhost:8080/kiem-tra: thấy "Ket noi CSDL OK. So dong: ..." là thành công.

## Thành phần nền tảng dùng chung
- `entity/` và `entity/enums/`: 8 entity + 7 enum khớp đúng CSDL SQL Server
- `repository/`: 8 interface JpaRepository
- `config/PasswordUtil`: băm mật khẩu SHA-256 (mật khẩu mẫu `123456`)

## Phân công nhiệm vụ
| Nhóm | Thành viên | Công việc | Thành phần mã nguồn |
|---|---|---|---|
| 1 | Phạm Trần Ngọc Nhân | Đặt vé online (giữ ghế, thanh toán, nhả ghế) và bán vé tại quầy: KH03, KH04, NV01 | service/DatVeService, BanVeService; controller/DatVeController, QuayController; scheduler/NhaGheScheduler; templates/datve, quay |
| 2 | Nguyễn Trọng Nhân | Soát vé Socket (server + client), tra cứu vé, quản lý tài khoản: NV03, NV02, QT04 | package server/*; service/SoatVeService; controller/TraCuuVeController, AdminTaiKhoanController; templates/tracuu, admin/taikhoan-*; project QLRapChieuPhimClient |
| 3 | Phạm Trung Kiên | Đăng nhập, đăng ký, hồ sơ, phân quyền, giao diện chung, xem phim: C01, C02, KH01, KH02 | service/NguoiDungService; controller/AuthController, PhimController; config/WebConfig; interceptor/*; templates/fragments, auth, phim, trang-chu, loi; static/* |
| 4 | Nguyễn Thanh Huyền | Quản lý phim, thể loại, phòng chiếu, ghế: QT01, QT02 | service/PhimService, TheLoaiService, PhongChieuService; controller/AdminPhimController, AdminPhongController; templates/admin/phim-*, theloai-*, phong-* |
| 5 | Lê Huy Quốc Bảo | Quản lý suất chiếu, lịch sử và hủy vé, thống kê: QT03, KH05, QT05 | service/SuatChieuService, VeService, ThongKeService; controller/AdminSuatController, LichSuController, ThongKeController; templates/admin/suat-*, thongke, lichsu/* |

## Kiến trúc & Quy tắc nghiệp vụ chính
- Kiến trúc phân tầng: Controller tiếp nhận request, gọi Service, trả View; Service xử lý nghiệp vụ và `@Transactional`; Repository truy vấn dữ liệu.
- Danh sách hỗ trợ tìm kiếm, phân trang, sắp xếp (`Pageable`, `Sort`).
- Giữ ghế: `@Transactional` kết hợp khóa dòng `SuatChieuRepository.findByIdForUpdate` và chỉ mục duy nhất `UX_Ve_Suat_Ghe`.
- Thời gian giữ ghế 5 phút (`HanThanhToan = NgayDat + 5 phút`); hủy vé khi còn ít nhất 60 phút trước giờ chiếu.
- Soát vé (TCP Socket cổng 9999): giao thức `INFO|maVe`, `CHECKIN|maVe`, `QUIT`; kiểm tra vé `HOP_LE`, đơn `DA_THANH_TOAN` và khung thời gian soát vé.
- Xóa phòng chiếu: xóa ghế của phòng trước (`GheRepository.deleteByPhongChieuId`) rồi xóa phòng trong cùng một `@Transactional`.
