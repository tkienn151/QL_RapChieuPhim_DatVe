# Tài liệu tham chiếu hạ tầng & thành phần dùng chung

Tài liệu mô tả quy ước thiết kế các thành phần dùng chung trong module `QLRapChieuPhim` nhằm bảo đảm giao diện đồng nhất, kiểm soát phiên/phân quyền tập trung và thông báo lỗi thống nhất trên toàn bộ hệ thống.

---

## 1. Bố cục & Thymeleaf Fragments dùng chung (`templates/fragments/`)

Tất cả các trang Thymeleaf sử dụng chung cụm fragment trong thư mục `templates/fragments/`:

| Tệp & Cú pháp nhúng | Thành phần / Tham số | Mô tả |
|---|---|---|
| `header.html`<br>`th:replace="~{fragments/header :: head('Tiêu đề trang')}"` | `tieuDe` | Tạo `<head>` UTF-8, thẻ responsive `viewport`, tải Google Fonts `Be Vietnam Pro`, Bootstrap `5.3.3` CSS, Bootstrap Icons `1.11.3` và `/css/style.css?v=6`. |
| `navbar.html`<br>`th:replace="~{fragments/navbar :: navbar}"` | Không tham số (đọc `session.nguoiDung`) | Thanh điều hướng tối màu dính đỉnh (`sticky-top`), tự động ẩn/hiện mục menu theo `VaiTro` (`QUAN_TRI`, `NHAN_VIEN`, `KHACH_HANG` hoặc khách vãng lai), kèm menu hồ sơ và nút đăng xuất POST. |
| `footer.html`<br>`th:replace="~{fragments/footer :: footer}"` | Không tham số | Chân trang CineStar + nhúng `bootstrap.bundle.min.js` (`5.3.3`) và `/js/main.js?v=2`. |
| `phim-card.html`<br>`th:replace="~{fragments/phim-card :: card(phim)}"` | Đối tượng `Phim` | Hiển thị thẻ phim (poster, thể loại, thời lượng, nhãn trạng thái, nút xem chi tiết/đặt vé). |
| `phantrang.html`<br>`th:replace="~{fragments/phantrang :: phanTrang(page, duongDan)}"` | `Page<?>` và chuỗi URL nền `duongDan` | Sinh thanh phân trang Bootstrap chuẩn (tự nối `?trang=` hoặc `&trang=` tùy `duongDan` đã có tham số hay chưa). |
| `phieuve.html`<br>`th:replace="~{fragments/phieuve :: phieuVe(don)}"` | Đối tượng `DonDat` (`don`) | Mẫu phiếu vé in (`d-print-block`) hiển thị mã đơn, suất chiếu, tên khách, từng vé và mã vé để in trực tiếp từ trình duyệt. |
| `loi-truong.html`<br>`th:replace="~{fragments/loi-truong :: loiTruong('tenTruong')}"` | Tên trường (`String`) trong bản đồ `loiTruong` | Hiển thị thông báo lỗi kiểm tra hợp lệ màu đỏ ngay dưới ô nhập liệu tương ứng. |

---

## 2. CSS, JavaScript & Quy tắc hiển thị Poster

### A. Giao diện & JS dùng chung
- **`static/css/style.css`:** Quy định bảng màu thương hiệu (`--mau-chinh: #e50914`, nền trang `--nen-trang: #f4f6f9`, navbar/footer tối màu `--mau-toi: #0f1219`), thẻ phim, lưới sơ đồ ghế (`THUONG` / `VIP` / `DA_CHON` / `DA_DAT`), bảng dữ liệu và khối định dạng in phiếu vé (`@media print` / `@page`), kết hợp hệ lưới Bootstrap để tương thích màn hình di động (`390x844`).
- **`static/js/main.js`:**
  - Tự động gắn lớp `active` cho liên kết `.navbar [data-menu]` khớp đường dẫn URL hiện tại (`/` hoặc tiền tố `/phim`, `/lich-su`, `/quay`, `/tra-cuu`, `/admin`).
  - Tự động đóng thông báo `.alert-success` sau `5` giây (`5000ms`).
  - Bắt sự kiện xác nhận hộp thoại `confirm` cho các form/nút có `data-xac-nhan` hoặc `data-confirm`.
  - Hỗ trợ kiểm tra khớp mật khẩu nhập lại (`data-trung-voi`), nút bật/tắt ẩn hiện mật khẩu (`data-hien-mat-khau`), đồng bộ trạng thái `:user-invalid` và tự động thay ảnh `img.poster` bị lỗi tải sang `/images/no-poster.svg`.

### B. Ánh xạ & hiển thị ảnh Poster (`WebConfig`)
- Trong `WebConfig.addResourceHandlers(...)`, đường dẫn URL `/images/posters/**` được cấu hình tra cứu theo thứ tự ưu tiên:
  1. Thư mục ngoài chạy runtime: `file:uploads/posters/` (chứa ảnh poster do quản trị viên tải lên qua `AdminPhimController` / `PhimService`).
  2. Tài nguyên tích hợp sẵn trong classpath: `classpath:/static/images/posters/` (chứa `phim01.jpg`..`phim20.jpg`, `default-poster.jpg`, `hero-banner.jpg`).
- Khi dựng đường dẫn ảnh ở template (`phim-card.html` và các view chi tiết/admin):
  - Nếu `poster` rỗng/null -> dùng `/images/no-poster.svg`.
  - Nếu `poster` bắt đầu bằng `http` hoặc `/` -> dùng trực tiếp giá trị URL/đường dẫn đó.
  - Ngược lại (chỉ lưu tên tệp, ví dụ `phim01.jpg`) -> ghép thành `/images/posters/{poster}`.

---

## 3. Xử lý ngoại lệ & Kiểm tra hợp lệ đầu vào

### A. Lỗi từng trường biểu mẫu (`LoiTruongException`)
- Lớp `com.qlrapchieuphim.service.LoiTruongException` (kế thừa `IllegalArgumentException`) lưu tên trường `truong` hoặc bản đồ lỗi `dsLoi` (`getDsLoi()`) ánh xạ từng trường biểu mẫu (`tenDangNhap`, `matKhau`, `xacNhanMatKhau`, `hoTen`, `email`, `soDienThoai`, `tenPhim`, `gioBatDau`, ...).
- Controller gọi `LoiTruongException.dayVaoModel(e, model)` để đẩy thuộc tính `loi` và `loiTruong` về view (hiển thị ngay dưới ô nhập qua `fragments/loi-truong`).

### B. Xử lý lỗi toàn cục (`GlobalExceptionHandler`)
- Annotation `@ControllerAdvice`, chuyển mọi ngoại lệ chưa bắt về trang thân thiện **`templates/loi.html`** với `maLoi`, `tieuDe`, `thongBao`:
  - `IllegalArgumentException` / `IllegalStateException` -> HTTP `400` (*Yêu cầu không thực hiện được*).
  - `TypeMismatchException` -> HTTP `400` (*Tham số trên đường dẫn không hợp lệ*).
  - `404` -> *Không tìm thấy trang*.
  - Ngoại lệ hệ thống khác -> ghi `log.error(...)` phía server và trả HTTP `500` (*Hệ thống gặp sự cố*).

### C. Hàm kiểm tra & chuẩn hóa tĩnh trong `NguoiDungService`
- `NguoiDungService.tenDangNhapHopLe(String)`: kiểm tra quy tắc tên đăng nhập hợp lệ.
- `NguoiDungService.emailHopLe(String)`: kiểm tra định dạng email.
- `NguoiDungService.soDienThoaiHopLe(String)`: kiểm tra định dạng số điện thoại.
- `NguoiDungService.chuanHoaCotSapXep(String)`: lọc tên cột sắp xếp hợp lệ theo danh sách cho phép (`whitelist`), chống truyền chuỗi cột tùy ý gây lỗi truy vấn.

---

## 4. Phiên đăng nhập, Bảo mật & Phân quyền

- **Khóa phiên (`PhienDangNhap`):** Tên attribute trong `HttpSession` là `"nguoiDung"` (`PhienDangNhap.KEY`). Thời lượng phiên mặc định là **`30` phút** (`THOI_GIAN_PHIEN_GIAY = 30 * 60` thiết lập tại `AuthController`).
- **Mã hóa mật khẩu (`PasswordUtil`):** Hàm `PasswordUtil.bam(matKhau)` dùng thuật toán `SHA-256` tạo chuỗi hex `64` ký tự không dấu.
- **Tái xác thực & Bảo vệ đường dẫn (`DangNhapInterceptor`):**
  - Áp dụng cho `/dat-ve/**`, `/lich-su/**`, `/ho-so/**`, `/quay/**`, `/tra-cuu/**`, `/admin/**`.
  - Trên **mỗi request**, interceptor truy vấn lại bản ghi `NguoiDung` từ CSDL theo `id` đang lưu trong phiên: nếu tài khoản đã bị xóa hoặc bị khóa (`hoatDong == false`), phiên bị hủy ngay lập tức (`session.invalidate()`) và chuyển về `/dang-nhap`.
  - Với request `GET` chưa đăng nhập, interceptor lưu đường dẫn ban đầu vào `?quayLai=...` (kèm query string nếu có) để `AuthController` đưa người dùng quay lại đúng trang sau khi đăng nhập thành công (có kiểm tra an toàn chống Open Redirect).
- **Kiểm soát quyền truy cập (`PhanQuyenInterceptor` & `PhienDangNhap.duocPhepTruyCap`):**
  - `/admin/**` -> chỉ `QUAN_TRI`.
  - `/quay/**`, `/tra-cuu/**` -> `NHAN_VIEN`, `QUAN_TRI`.
  - `/dat-ve/**`, `/lich-su/**` -> chỉ `KHACH_HANG`.
  - Các trường hợp sai vai trò được chuyển hướng về `/khong-co-quyen`.
