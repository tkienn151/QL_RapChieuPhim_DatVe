# Hướng dẫn chạy, kiểm thử tự động & demo toàn hệ thống

Tài liệu hướng dẫn từng bước khởi tạo CSDL, chạy bộ kiểm thử tự động JUnit 5, kiểm thử thủ công tất cả vai trò trên Web và chạy Client TCP Socket soát vé.

---

## 1. Khởi tạo CSDL & Chạy ứng dụng Web + Socket Server

1. **Nạp CSDL SQL Server:** Mở SSMS và chạy toàn bộ tệp `QLRapChieuPhim/sql/QuanLyPhimVaDatVe.sql`. Tập lệnh tạo mới database `QuanLyPhimVaDatVe` (xóa sạch database cũ cùng tên nếu đã tồn tại) cùng đầy đủ dữ liệu mẫu ban đầu.
2. **Cấu hình cục bộ:** Đảm bảo có tệp `QLRapChieuPhim/src/main/resources/application.properties` (sao chép từ `application.properties.example`) với đúng username/password SQL Server của máy demo (`server.port=8080`, `soatve.socket.port=9999`, `soatve.server.enabled=true`).
3. **Chạy Server:**
   - Từ thư mục `QLRapChieuPhim/`, chạy `mvn spring-boot:run` (hoặc chạy lớp `QlRapChieuPhimApplication` trong IDE).
   - Xác nhận console in dòng log sau của máy chủ Socket:
     ```text
     SoatVeServer đã khởi động, lắng nghe trên cổng TCP: 9999
     ```
4. **Kiểm tra sức khỏe ứng dụng & CSDL:**
   - Truy cập `http://localhost:8080/kiem-tra`, kết quả chuẩn sau khi nạp SQL:
     `Ket noi CSDL OK. So dong: NguoiDung=25, TheLoai=25, Phim=25, PhongChieu=25, Ghe=1200, SuatChieu=45, DonDat=25, Ve=50`
   - Truy cập `http://localhost:8080/` để kiểm tra trang chủ CineStar hiển thị poster, lịch chiếu và bộ lọc phim.

---

## 2. Chạy bộ kiểm thử tự động JUnit 5 (9 ca kiểm thử)

Trong thư mục `QLRapChieuPhim/`, chạy:
```bash
mvn test
```
Toàn bộ kiểm thử chạy trên cấu hình `src/test/resources/application-test.properties` (CSDL H2 trong bộ nhớ), bao gồm **9 phương thức `@Test`** thuộc **3 lớp**:

| Lớp kiểm thử | Số ca | Phạm vi xác minh |
|---|---|---|
| `QlRapChieuPhimApplicationTests` | `1` | `contextLoads`: nạp đầy đủ Spring context khi tắt socket tự động. |
| `KiemThuChucNangWebMvcTest` | `4` | `kiemThuTatCaManHinhThymeleaf`, `kiemThuTraCuuQuayDonGiuGheVaHetHan`, `traCuuKhongKhopNhanVien_va_LichSuDonQuaHan`, `kiemThuTimKiemSapXepVaBaoLoiTruong`: kiểm thử toàn bộ màn hình Thymeleaf, phân quyền, tìm kiếm/phân trang/sắp xếp, lỗi trường nhập, đặt vé, bán tại quầy, tra cứu và lịch sử đơn quá hạn. |
| `KiemThuDongThoiVaMangTest` | `4` | `kiemThu50LuongCungDatMotGhe`, `kiemThuNhaGheQuaHan5Phut`, `kiemThu4ClientSocketCungSoatMotVe`, `kiemThuRangBuocNghiepVu`: kiểm chứng `50` luồng đồng thời cùng đặt một ghế (chỉ `1` luồng thành công), scheduler giải phóng ghế quá hạn `5` phút, `4` socket client cùng soát một vé và các ràng buộc xóa/trùng giờ. |

**Kết quả chuẩn:** `Tests run: 9, Failures: 0, Errors: 0, Skipped: 0`.

---

## 3. Danh sách tài khoản mẫu kiểm thử

Mật khẩu chung cho toàn bộ `25` tài khoản là **`123456`**:

| Vai trò | Tên đăng nhập | Họ và tên |
|---|---|---|
| Quản trị (`QUAN_TRI`) | `admin` | Quản Trị Hệ Thống |
| Nhân viên (`NHAN_VIEN`) | `nhanvien1` | Phạm Trung Kiên |
| Nhân viên (`NHAN_VIEN`) | `nhanvien2` | Phạm Trần Ngọc Nhân |
| Nhân viên (`NHAN_VIEN`) | `nhanvien3` | Phạm Quốc Đạt |
| Khách hàng (`KHACH_HANG`) | `khach01` | Nguyễn Trọng Nhân |
| Khách hàng (`KHACH_HANG`) | `khach02` | Lê Huy Quốc Bảo |
| Khách hàng (`KHACH_HANG`) | `khach03` | Nguyễn Thanh Huyền |
| Khách hàng (`KHACH_HANG`) | `khach04` .. `khach21` | `18` tài khoản khách hàng bổ sung khác |

---

## 4. Kịch bản kiểm thử thủ công trên Web

### A. Khách vãng lai & Khách hàng (`KHACH_HANG`)
1. **Trang chủ & danh sách phim:** Vào `/` và `/phim`, lọc theo từ khóa, thể loại, trạng thái (`DANG_CHIEU`, `SAP_CHIEU`), sắp xếp và chuyển trang (`?trang=`).
2. **Chi tiết & Đặt vé giữ ghế:** Chọn một phim có suất `MO_BAN` (hoặc tạo suất sắp chiếu) -> bấm đặt vé vào `/dat-ve/chon-ghe?suatId=...` (nếu chưa đăng nhập sẽ tự chuyển tới `/dang-nhap?quayLai=...` và điều hướng lại đúng suất sau khi đăng nhập bằng `khach01`).
3. **Kiểm tra xung đột giữ ghế (đa luồng/đa phiên):** Mở thêm trình duyệt ẩn danh đăng nhập `khach02`, cùng chọn chính ghế vừa giữ -> người đến sau nhận thông báo từ chối giữ ghế trùng.
4. **Thanh toán đếm ngược 5 phút & Kết quả:** Trên cửa sổ `khach01`, kiểm tra đồng hồ đếm ngược 5 phút tại `/dat-ve/thanh-toan` -> bấm **Xác nhận thanh toán** -> chuyển sang `/dat-ve/ket-qua` hiển thị phiếu vé và danh sách mã vé `VE...`.
5. **Tự động nhả ghế quá hạn (`5` phút):** Thử giữ một ghế khác nhưng không thanh toán; sau `5` phút (khi `NhaGheScheduler` chạy theo chu kỳ `60` giây), console server ghi log giải phóng ghế và trạng thái đơn chuyển thành `HET_HAN`, ghế trống trở lại trên sơ đồ.
6. **Lịch sử & Hủy vé:** Vào `/lich-su` và `/lich-su/chi-tiet/{id}`. Với đơn đã thanh toán còn trước giờ chiếu ít nhất `60` phút và chưa có vé soát, khách có thể hủy đơn; với đơn sát giờ chiếu (`< 60` phút), đã soát hoặc hết hạn, hệ thống từ chối hủy theo quy tắc nghiệp vụ.
7. **Kiểm tra Responsive trên di động (`390x844`):** Bật DevTools trình duyệt (`Ctrl+Shift+M`), chọn kích thước `390x844` trên trang chủ `/` và trang chọn ghế `/dat-ve/chon-ghe` để xác nhận bố cục cột, navbar thu gọn và sơ đồ ghế cuộn/hiển thị rõ nét không vỡ khung.

### B. Nhân viên bán vé (`NHAN_VIEN`)
1. Đăng nhập bằng `nhanvien1` (hoặc `nhanvien2`, `nhanvien3`): hệ thống chuyển vào `/quay/ban-ve`.
2. **Bán vé tại quầy:** Chọn suất chiếu, nhập thông tin khách (hoặc bỏ trống đối với khách vãng lai), chọn ghế -> bấm **Xác nhận thu tiền mặt** -> xem mã đơn `DD...` và mã vé `VE...` tại `/quay/ket-qua`.
3. **Tra cứu & xử lý đơn tại quầy (`/tra-cuu`):** Tìm theo mã đơn (`DD0007`), mã vé hoặc số điện thoại; thử chức năng **Xác nhận thu tiền** (với đơn `CHO_THANH_TOAN` chưa quá hạn), xem phiếu in vé và **Hủy đơn** (khi còn hợp lệ).
4. **Kiểm tra chặn vượt quyền:** Thử truy cập `/admin/phim` hoặc `/dat-ve/chon-ghe?suatId=1` khi đang đăng nhập `NHAN_VIEN` -> bị chặn và chuyển tới `/khong-co-quyen`.

### C. Quản trị viên (`QUAN_TRI`)
1. Đăng nhập `admin` -> vào trang `/admin/thong-ke` xem doanh thu tổng quan theo ngày, tháng và phim.
2. **Quản lý Thể loại & Phim (`/admin/theloai`, `/admin/phim`):** Thêm, sửa, tìm kiếm, upload poster (xem trước ngay trên form `phim-form.html`), kiểm tra thông báo lỗi ngay dưới từng ô nhập khi để trống/sai định dạng (`loiTruong`), và kiểm tra ràng buộc **không thể xóa** phim/thể loại đang có bản ghi tham chiếu.
3. **Quản lý Phòng chiếu (`/admin/phong`):** Thêm/sửa phòng và kiểm tra hệ thống tự động sinh đủ sơ đồ ghế (`SoHang × SoCot`, hai hàng cuối mặc định `VIP`).
4. **Quản lý Suất chiếu (`/admin/suat`):** Thử tạo suất chiếu trùng phòng và giao thời gian (bao gồm khoảng đệm dọn rạp) -> hệ thống báo lỗi không cho lưu.
5. **Quản lý Tài khoản (`/admin/taikhoan`):** Tạo/khóa/mở khóa/đổi vai trò tài khoản, kiểm tra chặn tự khóa chính tài khoản quản trị đang đăng nhập; thử khóa một tài khoản đang đăng nhập ở tab khác -> `DangNhapInterceptor` tự ngắt phiên ở request kế tiếp.

---

## 5. Kiểm thử Chương trình Client TCP Socket (`QLRapChieuPhimClient`)

Mở terminal mới tại `QLRapChieuPhimClient/` (trong khi Server `QLRapChieuPhim` đang chạy trên cổng `9999`):

```bash
mvn clean compile
```

### A. Soát vé tương tác (`SoatVeClient`)
```bash
java -cp target/classes com.qlrapchieuphimclient.SoatVeClient
```
Thử tuần tự các trường hợp (đối chiếu với dữ liệu mẫu `DD0001..DD0025` trong `QuanLyPhimVaDatVe.sql`):
1. `INFO VE000001`: Xem thông tin vé `VE000001` (thuộc suất chiếu số 1 gần thời điểm khởi tạo SQL: `DATEADD(MINUTE, 30, SYSDATETIME())`) khi vẫn đang ở trạng thái `HOP_LE`.
2. `CHECKIN|VE000001` (hoặc gõ `VE000001`): Lần 1 thành công (`✅ SOÁT VÉ THÀNH CÔNG (HỢP LỆ)`).
3. Nhập lại `VE000001` lần 2: Bị từ chối (`❌ TỪ CHỐI: Vé này ĐÃ ĐƯỢC SỬ DỤNG.`).
4. Nhập `VE000013` (đơn `DD0007` trạng thái `CHO_THANH_TOAN`): Bị từ chối (`❌ TỪ CHỐI: Đơn đặt vé này CHƯA THANH TOÁN.`).
5. Nhập `VE000017` (đơn `DD0009` trạng thái `DA_HUY`): Bị từ chối (`❌ TỪ CHỐI: Vé này ĐÃ BỊ HỦY.`).
6. Nhập `VE000003` (thuộc suất chiếu ngày hôm sau, ngoài khung `60` phút trước `GioBatDau`): Bị từ chối (`❌ TỪ CHỐI: Vé không hợp lệ cho thời điểm hiện tại (Sai suất chiếu hoặc quá giờ).`).
7. Nhập `VE999999`: Bị từ chối (`❌ TỪ CHỐI: Mã vé không tồn tại trên hệ thống.`).
8. Gõ `QUIT` để đóng kết nối và thoát.

### B. Soát vé đồng thời nhiều client (`KiemThuNhieuClient`)
Để thử `4` client kết nối cùng lúc soát vé hợp lệ (ví dụ dùng `VE000002` thuộc cùng suất 1 nếu `VE000001` đã được soát trước đó, hoặc chạy lại file SQL để làm mới trạng thái):
```bash
java -cp target/classes com.qlrapchieuphimclient.KiemThuNhieuClient 127.0.0.1 9999 VE000002
```
**Quan sát:**
- Phía Client: đúng `1` trong `4` luồng nhận `OK|DA_SOAT|...`, `3` luồng còn lại nhận `ERR|DA_SU_DUNG`.
- Phía Server Console: log `SoatVeServer` hiển thị tên các luồng xử lý song song (`soat-ve-worker-1`, `soat-ve-worker-2`, ...).

---

## 6. Đặt lại dữ liệu trước khi thuyết trình / demo

Sau khi thử soát `VE000001` / `VE000002` hoặc thêm/sửa/xóa dữ liệu thử nghiệm, cần chạy lại **`QLRapChieuPhim/sql/QuanLyPhimVaDatVe.sql`** trong SSMS để đưa `8` bảng và trạng thái các vé (`VE000001`, `VE000002` cùng `25` đơn đặt mẫu) về dữ liệu ban đầu trước buổi demo.
