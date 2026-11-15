# QLRapChieuPhimClient - Chương trình Client TCP Socket Soát Vé

Chương trình Java Console độc lập dành cho nhân viên soát vé tại cửa phòng chiếu và kiểm thử khả năng phục vụ nhiều client đồng thời của `SoatVeServer` (mở cổng TCP `9999` bên trong module `QLRapChieuPhim`).

---

## 1. Yêu cầu môi trường

- **Java:** JDK **17** trở lên (`maven.compiler.source=17`, `maven.compiler.target=17`, mã hóa nguồn `UTF-8`).
- **Apache Maven:** `3.9+` (hoặc chạy trực tiếp trong IntelliJ IDEA / Eclipse).
- **Máy chủ đích:** Module `QLRapChieuPhim` phải đang chạy và lắng nghe thành công cổng TCP `9999` (`SoatVeServer đã khởi động, lắng nghe trên cổng TCP: 9999`).

---

## 2. Cấu trúc mã nguồn

| Tệp nguồn (`com.qlrapchieuphimclient`) | Chức năng |
|---|---|
| `SoatVeClient.java` | Client console tương tác trực tiếp với nhân viên soát vé tại cửa rạp (`127.0.0.1:9999` mặc định). |
| `KiemThuNhieuClient.java` | Chương trình mô phỏng **4 client** dùng `CountDownLatch` kết nối đồng thời tới `SoatVeServer` để cùng gửi `CHECKIN|<maVe>` vào một vé duy nhất nhằm kiểm chứng cơ chế chống soát trùng ở mức CSDL & Socket đa luồng. |

---

## 3. Hướng dẫn biên dịch và chạy

Mở terminal tại thư mục `QLRapChieuPhimClient/`:

### Bước 1: Biên dịch
```bash
mvn clean compile
```
*(Nếu chạy trên Command Prompt / PowerShell của Windows và muốn hiển thị dấu tiếng Việt rõ nét, có thể chạy trước lệnh `chcp 65001`.)*

### Bước 2: Chạy Client soát vé tương tác (`SoatVeClient`)
```bash
java -cp target/classes com.qlrapchieuphimclient.SoatVeClient
```
*(Mặc định kết nối tới `127.0.0.1:9999`.)*

Các lệnh nhập trực tiếp trong cửa sổ `SoatVeClient`:

| Lệnh người dùng nhập | Gói tin gửi sang Server | Ý nghĩa |
|---|---|---|
| `<maVe>` hoặc `CHECKIN\|<maVe>` | `CHECKIN\|<maVe>` | Thực hiện soát vé và đánh dấu vé `DA_SOAT` (chỉ hợp lệ 1 lần, trong khung giờ từ `60` phút trước `GioBatDau` đến `GioKetThuc`). |
| `INFO <maVe>` hoặc `INFO\|<maVe>` | `INFO\|<maVe>` | Tra cứu thông tin phim, phòng, ghế, giờ chiếu và trạng thái hiện tại của vé mà chưa làm đổi trạng thái. |
| `TEST [maVe]` | `INFO\|<maVe>` rồi `CHECKIN\|<maVe>` × `4` luồng | Kích hoạt trực tiếp `KiemThuNhieuClient` chạy **4 client đồng thời** cùng soát một mã vé (mặc định `VE000001`). |
| `QUIT` | `QUIT` | Đóng kết nối và thoát chương trình. |

### Bước 3: Chạy kiểm thử 4 Client đồng thời (`KiemThuNhieuClient`)
```bash
java -cp target/classes com.qlrapchieuphimclient.KiemThuNhieuClient
```
Hoặc truyền tham số `[host] [port] [maVe]` (mặc định là `127.0.0.1 9999 VE000001`; ví dụ dùng `VE000002` cùng suất chiếu đầu tiên sau khi đã soát `VE000001`):
```bash
java -cp target/classes com.qlrapchieuphimclient.KiemThuNhieuClient 127.0.0.1 9999 VE000002
```
**Kết quả mong đợi:** Với một mã vé hợp lệ chưa soát, đúng **1** client nhận phản hồi `OK|DA_SOAT|...` (`✅ SOÁT VÉ THÀNH CÔNG`), **3** client còn lại nhận `ERR|DA_SU_DUNG` (`❌ TỪ CHỐI: Vé này ĐÃ ĐƯỢC SỬ DỤNG.`).

---

## 4. Đặc tả giao thức trao đổi (`SoatVeProtocol`)

Trao đổi văn bản theo từng dòng (`\n`), bảng mã `UTF-8`:

| Chiều | Định dạng thông điệp | Ý nghĩa |
|---|---|---|
| Client → Server | `INFO\|<maVe>` | Truy vấn thông tin vé (`soatVeService.thongTinVe`). |
| Client → Server | `CHECKIN\|<maVe>` | Yêu cầu soát vé (`soatVeService.soatVe`). |
| Client → Server | `QUIT` | Yêu cầu kết thúc phiên Socket. |
| Server → Client | `OK\|INFO\|<TenPhim>\|<TenPhong>\|<Ghe>\|<HH:mm dd/MM/yyyy>\|<TrangThaiVe>` | Phản hồi thông tin chi tiết của vé. |
| Server → Client | `OK\|DA_SOAT\|<TenPhim>\|<TenPhong>\|<Ghe>` | Soát vé hợp lệ thành công. |
| Server → Client | `ERR\|KHONG_TON_TAI` | Không tìm thấy mã vé trên hệ thống. |
| Server → Client | `ERR\|DA_SU_DUNG` | Vé đã được soát trước đó (`DA_SOAT`). |
| Server → Client | `ERR\|DA_HUY` | Vé đã bị hủy hoặc thuộc đơn đã hủy / hết hạn. |
| Server → Client | `ERR\|CHUA_THANH_TOAN` | Đơn chứa vé vẫn đang ở trạng thái chưa thanh toán. |
| Server → Client | `ERR\|SAI_SUAT` | Suất chiếu không ở trạng thái `MO_BAN` hoặc ngoài khung giờ từ `60` phút trước `GioBatDau` đến `GioKetThuc`. |
| Server → Client | `ERR\|LENH_KHONG_HOP_LE` / `ERR\|LOI_HE_THONG` | Sai cú pháp lệnh hoặc lỗi hệ thống khi xử lý yêu cầu. |
