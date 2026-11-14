# Tài liệu cấu trúc dùng chung của hệ thống

Tài liệu mô tả cấu trúc giao diện Thymeleaf, quản lý phiên đăng nhập, phân quyền và phân trang dùng chung trong toàn bộ ứng dụng.

## 1. Khung một trang Thymeleaf
```html
<!DOCTYPE html>
<html lang="vi" xmlns:th="http://www.thymeleaf.org">
<head th:replace="~{fragments/header :: head('Tiêu đề trang')}"></head>
<body>
<th:block th:replace="~{fragments/navbar :: navbar}"></th:block>

<main class="container py-4">
    ... nội dung trang ...
</main>

<th:block th:replace="~{fragments/footer :: footer}"></th:block>
</body>
</html>
```
Bootstrap 5 + Bootstrap Icons (`<i class="bi bi-..."></i>`) đã có sẵn (class `btn`, `table`, `card`, `form-control`, `row`, `col-md-6`...).

## 2. Lấy người đang đăng nhập
Session lưu entity `NguoiDung` với khóa `"nguoiDung"` (hằng `PhienDangNhap.KEY`).
```java
@PostMapping("/dat-ve/giu-ghe")
public String giuGhe(..., HttpSession session) {
    NguoiDung nd = PhienDangNhap.layNguoiDung(session);
    ...
}
```
Trong Thymeleaf: `${session.nguoiDung.hoTen}`, `${session.nguoiDung.id}`.

## 3. Phân quyền (tự động, không cần code thêm)
| Đường dẫn | Ai vào được |
|---|---|
| `/dat-ve/**`, `/lich-su/**` | `KHACH_HANG` |
| `/ho-so/**` | Đã đăng nhập (mọi vai trò) |
| `/quay/**`, `/tra-cuu/**` | `NHAN_VIEN`, `QUAN_TRI` |
| `/admin/**` | `QUAN_TRI` |
| `/`, `/phim/**`, `/dang-nhap`, `/dang-ky`, `/kiem-tra`, `/khong-co-quyen` | Công khai |

Chưa đăng nhập -> chuyển `/dang-nhap` rồi quay lại đúng trang. Sai quyền -> trang `/khong-co-quyen` (mã 403).
Muốn thêm đường dẫn mới cần bảo vệ: sửa `config/WebConfig.java` (cập nhật cấu hình).

## 4. Thông báo thành công / lỗi
Không cần viết `alert` trong trang. Chỉ cần đặt attribute, navbar tự hiển thị:
```java
ra.addFlashAttribute("thanhCong", "Đã lưu phim.");
model.addAttribute("loi", e.getMessage());
```
Lỗi `IllegalArgumentException` / `IllegalStateException` mà controller không bắt sẽ do `GlobalExceptionHandler` hiển thị ở trang `loi.html` (thông báo lấy từ `getMessage()`).

## 5. Phân trang
Controller truyền `page` (kiểu `Page<...>`) và `duongDan` (đường dẫn kèm bộ lọc, KHÔNG có `trang`):
```java
model.addAttribute("page", page);
model.addAttribute("duongDan", "/admin/phim?tuKhoa=" + URLEncoder.encode(tuKhoa, StandardCharsets.UTF_8));
```
```html
<nav th:replace="~{fragments/phantrang :: phanTrang(${page}, ${duongDan})}"></nav>
```
Tham số số trang trên URL tên là `trang` (bắt đầu từ 0).

## 6. Tiện ích JS/HTML có sẵn (main.js)
- Hộp xác nhận: `<form data-xac-nhan="Bạn chắc chắn muốn xóa?" ...>`
- Ô nhập lại mật khẩu: `<input data-trung-voi="idOMatKhau">`
- Nút ẩn/hiện mật khẩu: `<button type="button" data-hien-mat-khau="idOMatKhau"><i class="bi bi-eye"></i></button>`
- Thông báo thành công tự tắt sau 5 giây.

## 7. Poster phim và định dạng tiền
- Poster đọc từ `/images/posters/{tên file}`. Phục vụ từ 2 nơi: `src/main/resources/static/images/posters/` và thư mục `uploads/posters/` (cạnh `pom.xml`, dành cho ảnh admin tải lên lúc chạy). Thiếu file thì hiện `/images/no-poster.svg`.
- Hiện thẻ phim: `~{fragments/phim-card :: card(${phim})}`.
- Tiền: `${#numbers.formatDecimal(giaVe, 0, 'POINT', 0, 'COMMA')}` -> `90.000`. Ngày giờ: `${#temporals.format(gio, 'dd/MM/yyyy HH:mm')}`.

## 8. Quy ước đường dẫn điều hướng trong hệ thống
- Link từ trang chi tiết phim: `/dat-ve/chon-ghe?suatId={id suất chiếu}` đối với khách hàng/chưa đăng nhập, hoặc `/quay/ban-ve?suatId={id suất chiếu}` nếu đang đăng nhập bằng tài khoản `NHAN_VIEN`/`QUAN_TRI`.
- Menu "Bán vé tại quầy" -> `/quay/ban-ve`; "Tra cứu vé" -> `/tra-cuu`; "Vé của tôi" -> `/lich-su`; menu Quản trị -> `/admin/phim`, `/admin/theloai`, `/admin/phong`, `/admin/suat`, `/admin/taikhoan`, `/admin/thong-ke`.
- Chi tiết phim lấy lịch chiếu qua `LichChieuService` (đọc suất MO_BAN chưa chiếu).
- `NguoiDungService.emailHopLe(...)`, `soDienThoaiHopLe(...)` là phương thức `static` dùng chung để kiểm tra định dạng dữ liệu tài khoản.
