package com.qlrapchieuphim.controller;

import com.qlrapchieuphim.config.PasswordUtil;
import com.qlrapchieuphim.config.PhienDangNhap;
import com.qlrapchieuphim.entity.NguoiDung;
import com.qlrapchieuphim.entity.enums.VaiTro;
import com.qlrapchieuphim.repository.DonDatRepository;
import com.qlrapchieuphim.repository.NguoiDungRepository;
import com.qlrapchieuphim.service.NguoiDungService;
import jakarta.servlet.http.HttpSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
@RequestMapping("/admin/taikhoan")
public class AdminTaiKhoanController {

    private final NguoiDungRepository nguoiDungRepository;
    private final DonDatRepository donDatRepository;

    public AdminTaiKhoanController(NguoiDungRepository nguoiDungRepository,
                                   DonDatRepository donDatRepository) {
        this.nguoiDungRepository = nguoiDungRepository;
        this.donDatRepository = donDatRepository;
    }

    @GetMapping
    public String danhSachTaiKhoan(@RequestParam(required = false) String tuKhoa,
                                   @RequestParam(required = false) VaiTro vaiTro,
                                   @RequestParam(defaultValue = "id") String sapXep,
                                   @RequestParam(defaultValue = "false") boolean tang,
                                   @RequestParam(defaultValue = "0") int trang,
                                   Model model) {
        String tk = (tuKhoa == null || tuKhoa.isBlank()) ? null : tuKhoa.trim();
        String cot = "tenDangNhap".equals(sapXep) ? "tenDangNhap" : ("hoTen".equals(sapXep) ? "hoTen" : "id");
        Sort.Direction huong = tang ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sort = "id".equals(cot)
                ? Sort.by(huong, cot)
                : Sort.by(huong, cot).and(Sort.by(Sort.Direction.ASC, "id"));

        Page<NguoiDung> dsTaiKhoan = nguoiDungRepository.timKiemTaiKhoan(
                tk, vaiTro, PageRequest.of(Math.max(trang, 0), 10, sort));

        model.addAttribute("dsTaiKhoan", dsTaiKhoan);
        model.addAttribute("tuKhoa", tk);
        model.addAttribute("vaiTro", vaiTro);
        model.addAttribute("sapXep", cot);
        model.addAttribute("tang", tang);
        model.addAttribute("vaiTroList", VaiTro.values());
        model.addAttribute("duongDan", taoDuongDan(tk, vaiTro, cot, tang));
        return "admin/taikhoan-danh-sach";
    }

    @GetMapping("/them")
    public String hienThiFormThem(Model model) {
        model.addAttribute("nguoiDung", new NguoiDung());
        return "admin/taikhoan-form";
    }

    @PostMapping("/them")
    public String xuLyThemTaiKhoan(@RequestParam String tenDangNhap,
                                   @RequestParam String matKhau,
                                   @RequestParam String hoTen,
                                   @RequestParam(required = false) String email,
                                   @RequestParam(required = false) String soDienThoai,
                                   @RequestParam VaiTro vaiTro,
                                   Model model, RedirectAttributes ra) {
        String ten = tenDangNhap == null ? "" : tenDangNhap.trim();
        String em = (email == null || email.isBlank()) ? null : email.trim();
        String sdt = (soDienThoai == null || soDienThoai.isBlank()) ? null : soDienThoai.trim();
        java.util.Map<String, String> loiTruong = new java.util.LinkedHashMap<>();
        if (!NguoiDungService.tenDangNhapHopLe(ten)) loiTruong.put("tenDangNhap", "Tên đăng nhập gồm 3-50 ký tự, chỉ gồm chữ không dấu, số, dấu gạch dưới (_) và dấu chấm (.).");
        else if (nguoiDungRepository.existsByTenDangNhap(ten)) loiTruong.put("tenDangNhap", "Tên đăng nhập đã tồn tại!");
        if (matKhau == null || matKhau.length() < 6) loiTruong.put("matKhau", "Mật khẩu phải có ít nhất 6 ký tự.");
        else if (matKhau.length() > 50) loiTruong.put("matKhau", "Mật khẩu tối đa 50 ký tự.");
        if (hoTen == null || hoTen.isBlank()) loiTruong.put("hoTen", "Họ tên không được để trống.");
        else if (hoTen.trim().length() > 100) loiTruong.put("hoTen", "Họ tên tối đa 100 ký tự.");
        if (em != null) {
            if (em.length() > 100) loiTruong.put("email", "Email tối đa 100 ký tự.");
            else if (!NguoiDungService.emailHopLe(em)) loiTruong.put("email", "Email không đúng định dạng.");
        }
        if (sdt != null && !NguoiDungService.soDienThoaiHopLe(sdt)) loiTruong.put("soDienThoai", "Số điện thoại không đúng định dạng (10 số, bắt đầu bằng 0 hoặc +84).");
        if (vaiTro == VaiTro.KHACH_HANG) loiTruong.put("vaiTro", "Trang này chỉ tạo tài khoản nhân viên hoặc quản trị.");

        if (!loiTruong.isEmpty()) {
            return traVeFormThemLoi(model, ten, hoTen, em, sdt, vaiTro, loiTruong);
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
            nguoiDungRepository.save(nd);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            loiTruong.put("tenDangNhap", "Tên đăng nhập đã tồn tại!");
            return traVeFormThemLoi(model, ten, hoTen, em, sdt, vaiTro, loiTruong);
        }

        ra.addFlashAttribute("thanhCong", "Thêm tài khoản '" + ten + "' thành công!");
        return "redirect:/admin/taikhoan";
    }

    private String traVeFormThemLoi(Model model, String ten, String hoTen, String em, String sdt,
                                    VaiTro vaiTro, java.util.Map<String, String> loiTruong) {
        NguoiDung nd = new NguoiDung();
        nd.setTenDangNhap(ten);
        nd.setHoTen(hoTen);
        nd.setEmail(em);
        nd.setSoDienThoai(sdt);
        nd.setVaiTro(vaiTro);
        model.addAttribute("nguoiDung", nd);
        model.addAttribute("loiTruong", loiTruong);
        model.addAttribute("loi", "Vui lòng kiểm tra lại các ô báo lỗi.");
        return "admin/taikhoan-form";
    }

    @PostMapping("/khoa/{id}")
    public String khoaMoTaiKhoan(@PathVariable Long id, HttpSession session, RedirectAttributes ra) {
        NguoiDung dangNhap = PhienDangNhap.layNguoiDung(session);
        if (dangNhap != null && dangNhap.getId().equals(id)) {
            ra.addFlashAttribute("loi", "Không thể tự khóa tài khoản đang đăng nhập.");
            return "redirect:/admin/taikhoan";
        }
        nguoiDungRepository.findById(id).ifPresent(nd -> {
            nd.setHoatDong(!nd.isHoatDong());
            nguoiDungRepository.save(nd);
            ra.addFlashAttribute("thanhCong", nd.isHoatDong() ? "Đã mở khóa tài khoản!" : "Đã khóa tài khoản!");
        });
        return "redirect:/admin/taikhoan";
    }

    @PostMapping("/vai-tro/{id}")
    public String doiVaiTro(@PathVariable Long id,
                            @RequestParam VaiTro vaiTro,
                            HttpSession session,
                            RedirectAttributes ra) {
        NguoiDung dangNhap = PhienDangNhap.layNguoiDung(session);
        if (dangNhap != null && dangNhap.getId().equals(id) && vaiTro != VaiTro.QUAN_TRI) {
            ra.addFlashAttribute("loi", "Không thể tự hạ quyền của tài khoản quản trị đang đăng nhập.");
            return "redirect:/admin/taikhoan";
        }
        nguoiDungRepository.findById(id).ifPresent(nd -> {
            nd.setVaiTro(vaiTro);
            nguoiDungRepository.save(nd);
            ra.addFlashAttribute("thanhCong", "Đã cập nhật vai trò tài khoản '" + nd.getTenDangNhap() + "' sang " + vaiTro);
        });
        return "redirect:/admin/taikhoan";
    }

    @PostMapping("/xoa/{id}")
    public String xoaTaiKhoan(@PathVariable Long id, HttpSession session, RedirectAttributes ra) {
        NguoiDung dangNhap = PhienDangNhap.layNguoiDung(session);
        if (dangNhap != null && dangNhap.getId().equals(id)) {
            ra.addFlashAttribute("loi", "Không thể tự xóa tài khoản đang đăng nhập.");
            return "redirect:/admin/taikhoan";
        }
        if (donDatRepository.existsByNguoiDungId(id)) {
            ra.addFlashAttribute("loi", "Không thể xóa tài khoản đã phát sinh đơn đặt vé. Hãy sử dụng chức năng Khóa tài khoản.");
            return "redirect:/admin/taikhoan";
        }
        nguoiDungRepository.findById(id).ifPresent(nd -> {
            nguoiDungRepository.delete(nd);
            ra.addFlashAttribute("thanhCong", "Đã xóa tài khoản '" + nd.getTenDangNhap() + "' thành công.");
        });
        return "redirect:/admin/taikhoan";
    }

    private String taoDuongDan(String tuKhoa, VaiTro vaiTro, String sapXep, boolean tang) {
        StringBuilder sb = new StringBuilder("/admin/taikhoan?sapXep=").append(sapXep)
                .append("&tang=").append(tang);
        if (tuKhoa != null) sb.append("&tuKhoa=").append(URLEncoder.encode(tuKhoa, StandardCharsets.UTF_8));
        if (vaiTro != null) sb.append("&vaiTro=").append(vaiTro.name());
        return sb.toString();
    }
}
