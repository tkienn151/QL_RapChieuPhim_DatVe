package com.qlrapchieuphim.service;

import com.qlrapchieuphim.config.PasswordUtil;
import com.qlrapchieuphim.entity.NguoiDung;
import com.qlrapchieuphim.entity.enums.VaiTro;
import com.qlrapchieuphim.repository.DonDatRepository;
import com.qlrapchieuphim.repository.NguoiDungRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class NguoiDungService {

    private static final Pattern TEN_DANG_NHAP = Pattern.compile("^[A-Za-z0-9_.]{3,50}$");
    private static final Pattern EMAIL = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern SO_DIEN_THOAI = Pattern.compile("^(0|\\+84)[0-9]{9}$");
    public static final int MAT_KHAU_TOI_THIEU = 6;

    private final NguoiDungRepository nguoiDungRepo;
    private final DonDatRepository donDatRepo;

    public NguoiDungService(NguoiDungRepository nguoiDungRepo, DonDatRepository donDatRepo) {
        this.nguoiDungRepo = nguoiDungRepo;
        this.donDatRepo = donDatRepo;
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

    public static String chuanHoaCotSapXep(String sapXep) {
        return "tenDangNhap".equals(sapXep) ? "tenDangNhap" : ("hoTen".equals(sapXep) ? "hoTen" : "id");
    }

    @Transactional(readOnly = true)
    public Page<NguoiDung> timKiemTaiKhoan(String tuKhoa, VaiTro vaiTro, String sapXep, boolean tang, int trang, int kichThuoc) {
        String tk = chuanHoa(tuKhoa);
        String cot = chuanHoaCotSapXep(sapXep);
        Sort.Direction huong = tang ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sort = "id".equals(cot)
                ? Sort.by(huong, cot)
                : Sort.by(huong, cot).and(Sort.by(Sort.Direction.ASC, "id"));
        return nguoiDungRepo.timKiemTaiKhoan(tk, vaiTro, PageRequest.of(Math.max(trang, 0), Math.max(kichThuoc, 1), sort));
    }

    @Transactional
    public NguoiDung themTaiKhoanNoiBo(String tenDangNhap, String matKhau, String hoTen,
                                       String email, String soDienThoai, VaiTro vaiTro) {
        String ten = tenDangNhap == null ? "" : tenDangNhap.trim();
        String em = chuanHoa(email);
        String sdt = chuanHoa(soDienThoai);
        Map<String, String> loiTruong = new LinkedHashMap<>();

        if (!tenDangNhapHopLe(ten)) {
            loiTruong.put("tenDangNhap", "Tên đăng nhập gồm 3-50 ký tự, chỉ gồm chữ không dấu, số, dấu gạch dưới (_) và dấu chấm (.).");
        } else if (nguoiDungRepo.existsByTenDangNhap(ten)) {
            loiTruong.put("tenDangNhap", "Tên đăng nhập đã tồn tại!");
        }
        if (matKhau == null || matKhau.length() < MAT_KHAU_TOI_THIEU) {
            loiTruong.put("matKhau", "Mật khẩu phải có ít nhất 6 ký tự.");
        } else if (matKhau.length() > 50) {
            loiTruong.put("matKhau", "Mật khẩu tối đa 50 ký tự.");
        }
        if (hoTen == null || hoTen.isBlank()) {
            loiTruong.put("hoTen", "Họ tên không được để trống.");
        } else if (hoTen.trim().length() > 100) {
            loiTruong.put("hoTen", "Họ tên tối đa 100 ký tự.");
        }
        if (em != null) {
            if (em.length() > 100) {
                loiTruong.put("email", "Email tối đa 100 ký tự.");
            } else if (!emailHopLe(em)) {
                loiTruong.put("email", "Email không đúng định dạng.");
            }
        }
        if (sdt != null && !soDienThoaiHopLe(sdt)) {
            loiTruong.put("soDienThoai", "Số điện thoại không đúng định dạng (10 số, bắt đầu bằng 0 hoặc +84).");
        }
        if (vaiTro == null || vaiTro == VaiTro.KHACH_HANG) {
            loiTruong.put("vaiTro", "Trang này chỉ tạo tài khoản nhân viên hoặc quản trị.");
        }

        if (!loiTruong.isEmpty()) {
            throw new LoiTruongException(loiTruong, "Vui lòng kiểm tra lại các ô báo lỗi.");
        }

        NguoiDung nd = new NguoiDung();
        nd.setTenDangNhap(ten);
        nd.setMatKhau(PasswordUtil.bam(matKhau));
        nd.setHoTen(hoTen.trim());
        nd.setEmail(em);
        nd.setSoDienThoai(sdt);
        nd.setVaiTro(vaiTro);
        nd.setHoatDong(true);
        try {
            return nguoiDungRepo.save(nd);
        } catch (DataIntegrityViolationException e) {
            throw new LoiTruongException(Map.of("tenDangNhap", "Tên đăng nhập đã tồn tại!"), "Vui lòng kiểm tra lại các ô báo lỗi.");
        }
    }

    @Transactional
    public Optional<Boolean> khoaMoTaiKhoan(Long id, Long nguoiThucHienId) {
        if (nguoiThucHienId != null && nguoiThucHienId.equals(id)) {
            throw new IllegalStateException("Không thể tự khóa tài khoản đang đăng nhập.");
        }
        return nguoiDungRepo.findById(id).map(nd -> {
            nd.setHoatDong(!nd.isHoatDong());
            nguoiDungRepo.save(nd);
            return nd.isHoatDong();
        });
    }

    @Transactional
    public Optional<NguoiDung> doiVaiTro(Long id, VaiTro vaiTro, Long nguoiThucHienId) {
        if (nguoiThucHienId != null && nguoiThucHienId.equals(id) && vaiTro != VaiTro.QUAN_TRI) {
            throw new IllegalStateException("Không thể tự hạ quyền của tài khoản quản trị đang đăng nhập.");
        }
        return nguoiDungRepo.findById(id).map(nd -> {
            nd.setVaiTro(vaiTro);
            return nguoiDungRepo.save(nd);
        });
    }

    @Transactional
    public Optional<String> xoaTaiKhoan(Long id, Long nguoiThucHienId) {
        if (nguoiThucHienId != null && nguoiThucHienId.equals(id)) {
            throw new IllegalStateException("Không thể tự xóa tài khoản đang đăng nhập.");
        }
        if (donDatRepo.existsByNguoiDungId(id)) {
            throw new IllegalStateException("Không thể xóa tài khoản đã phát sinh đơn đặt vé. Hãy sử dụng chức năng Khóa tài khoản.");
        }
        return nguoiDungRepo.findById(id).map(nd -> {
            String ten = nd.getTenDangNhap();
            nguoiDungRepo.delete(nd);
            return ten;
        });
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
