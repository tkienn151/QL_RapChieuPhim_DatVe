package com.qlrapchieuphim.service;

import com.qlrapchieuphim.config.PasswordUtil;
import com.qlrapchieuphim.entity.NguoiDung;
import com.qlrapchieuphim.entity.enums.VaiTro;
import com.qlrapchieuphim.repository.NguoiDungRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.regex.Pattern;

@Service
public class NguoiDungService {

    private static final Pattern TEN_DANG_NHAP = Pattern.compile("^[A-Za-z0-9_.]{3,50}$");
    private static final Pattern EMAIL = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern SO_DIEN_THOAI = Pattern.compile("^(0|\\+84)[0-9]{9}$");
    public static final int MAT_KHAU_TOI_THIEU = 6;

    private final NguoiDungRepository nguoiDungRepo;

    public NguoiDungService(NguoiDungRepository nguoiDungRepo) {
        this.nguoiDungRepo = nguoiDungRepo;
    }

    public static boolean tenDangNhapHopLe(String ten) {
        return ten != null && TEN_DANG_NHAP.matcher(ten).matches();
    }

    public static boolean emailHopLe(String email) {
        return email != null && email.length() <= 100 && EMAIL.matcher(email).matches();
    }

    public static boolean soDienThoaiHopLe(String sdt) {
        return sdt != null && SO_DIEN_THOAI.matcher(sdt).matches();
    }

    @Transactional
    public NguoiDung dangKy(String tenDangNhap, String matKhau, String xacNhanMatKhau,
                            String hoTen, String email, String soDienThoai) {
        String ten = chuanHoa(tenDangNhap);
        String ht = chuanHoa(hoTen);
        String em = chuanHoa(email);
        String sdt = chuanHoa(soDienThoai);

        if (!tenDangNhapHopLe(ten))
            throw new LoiTruongException("tenDangNhap","Tên đăng nhập gồm 3-50 ký tự, chỉ gồm chữ không dấu, số, dấu gạch dưới (_) và dấu chấm (.).");
        kiemTraMatKhauMoi(matKhau, xacNhanMatKhau);
        kiemTraThongTinChung(ht, em, sdt);
        if (nguoiDungRepo.existsByTenDangNhap(ten))
            throw new LoiTruongException("tenDangNhap","Tên đăng nhập đã tồn tại, vui lòng chọn tên khác.");

        NguoiDung nd = new NguoiDung();
        nd.setTenDangNhap(ten);
        nd.setMatKhau(PasswordUtil.bam(matKhau));
        nd.setHoTen(ht);
        nd.setEmail(em);
        nd.setSoDienThoai(sdt);
        nd.setVaiTro(VaiTro.KHACH_HANG);
        nd.setHoatDong(true);
        try {
            return nguoiDungRepo.save(nd);
        } catch (DataIntegrityViolationException e) {
            throw new LoiTruongException("tenDangNhap","Tên đăng nhập đã tồn tại, vui lòng chọn tên khác.");
        }
    }

    @Transactional(readOnly = true)
    public NguoiDung dangNhap(String tenDangNhap, String matKhau) {
        String ten = chuanHoa(tenDangNhap);
        if (ten == null || matKhau == null || matKhau.isEmpty())
            throw new IllegalArgumentException("Vui lòng nhập tên đăng nhập và mật khẩu.");
        NguoiDung nd = nguoiDungRepo.findByTenDangNhap(ten)
                .orElseThrow(() -> new IllegalArgumentException("Sai tên đăng nhập hoặc mật khẩu."));
        if (!nd.kiemTraMatKhau(PasswordUtil.bam(matKhau)))
            throw new IllegalArgumentException("Sai tên đăng nhập hoặc mật khẩu.");
        if (!nd.isHoatDong())
            throw new IllegalArgumentException("Tài khoản đã bị khóa, vui lòng liên hệ quản trị viên.");
        return nd;
    }

    @Transactional(readOnly = true)
    public NguoiDung layTheoId(Long id) {
        return nguoiDungRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản."));
    }

    @Transactional
    public NguoiDung capNhatHoSo(Long id, String hoTen, String email, String soDienThoai) {
        String ht = chuanHoa(hoTen);
        String em = chuanHoa(email);
        String sdt = chuanHoa(soDienThoai);
        kiemTraThongTinChung(ht, em, sdt);

        NguoiDung nd = layTheoId(id);
        nd.setHoTen(ht);
        nd.setEmail(em);
        nd.setSoDienThoai(sdt);
        return nguoiDungRepo.save(nd);
    }

    @Transactional
    public void doiMatKhau(Long id, String matKhauCu, String matKhauMoi, String xacNhanMatKhauMoi) {
        NguoiDung nd = layTheoId(id);
        if (matKhauCu == null || !nd.kiemTraMatKhau(PasswordUtil.bam(matKhauCu)))
            throw new LoiTruongException("matKhauCu","Mật khẩu hiện tại không đúng.");
        kiemTraMatKhauMoi(matKhauMoi, xacNhanMatKhauMoi);
        if (matKhauMoi.equals(matKhauCu))
            throw new LoiTruongException("matKhauMoi","Mật khẩu mới phải khác mật khẩu hiện tại.");
        nd.setMatKhau(PasswordUtil.bam(matKhauMoi));
        nguoiDungRepo.save(nd);
    }

    private String chuanHoa(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private void kiemTraMatKhauMoi(String matKhau, String xacNhan) {
        if (matKhau == null || matKhau.length() < MAT_KHAU_TOI_THIEU)
            throw new LoiTruongException("matKhau","Mật khẩu phải có ít nhất " + MAT_KHAU_TOI_THIEU + " ký tự.");
        if (matKhau.length() > 50)
            throw new LoiTruongException("matKhau","Mật khẩu tối đa 50 ký tự.");
        if (!matKhau.equals(xacNhan))
            throw new LoiTruongException("xacNhanMatKhau","Mật khẩu xác nhận không khớp.");
    }

    private void kiemTraThongTinChung(String hoTen, String email, String soDienThoai) {
        if (hoTen == null)
            throw new LoiTruongException("hoTen","Họ tên không được để trống.");
        if (hoTen.length() > 100)
            throw new LoiTruongException("hoTen","Họ tên tối đa 100 ký tự.");
        if (email != null && !emailHopLe(email))
            throw new LoiTruongException("email","Email không đúng định dạng.");
        if (soDienThoai != null && !soDienThoaiHopLe(soDienThoai))
            throw new LoiTruongException("soDienThoai","Số điện thoại không đúng định dạng (10 số, bắt đầu bằng 0 hoặc +84).");
    }
}
