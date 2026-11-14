package com.qlrapchieuphim.controller;

import com.qlrapchieuphim.config.PhienDangNhap;
import com.qlrapchieuphim.entity.NguoiDung;
import com.qlrapchieuphim.entity.enums.VaiTro;
import com.qlrapchieuphim.service.NguoiDungService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private static final int THOI_GIAN_PHIEN_GIAY = 30 * 60;

    private final NguoiDungService nguoiDungService;

    public AuthController(NguoiDungService nguoiDungService) {
        this.nguoiDungService = nguoiDungService;
    }

    @GetMapping("/dang-nhap")
    public String formDangNhap(@RequestParam(required = false) String quayLai,
                               @RequestParam(required = false) String canDangNhap,
                               HttpSession session, Model model) {
        if (PhienDangNhap.daDangNhap(session)) {
            return "redirect:" + trangChuTheoVaiTro(PhienDangNhap.layNguoiDung(session));
        }
        if (canDangNhap != null) model.addAttribute("loi", "Vui lòng đăng nhập để tiếp tục.");
        model.addAttribute("quayLai", quayLai);
        return "auth/dang-nhap";
    }

    @PostMapping("/dang-nhap")
    public String dangNhap(@RequestParam String tenDangNhap,
                           @RequestParam String matKhau,
                           @RequestParam(required = false) String quayLai,
                           HttpServletRequest request, Model model) {
        try {
            NguoiDung nd = nguoiDungService.dangNhap(tenDangNhap, matKhau);
            HttpSession cu = request.getSession(false);
            if (cu != null) cu.invalidate();
            HttpSession session = request.getSession(true);
            session.setMaxInactiveInterval(THOI_GIAN_PHIEN_GIAY);
            session.setAttribute(PhienDangNhap.KEY, nd);
            return "redirect:" + diaChiSauDangNhap(nd, quayLai);
        } catch (IllegalArgumentException e) {
            com.qlrapchieuphim.service.LoiTruongException.dayVaoModel(e, model);
            model.addAttribute("tenDangNhap", tenDangNhap);
            model.addAttribute("quayLai", quayLai);
            return "auth/dang-nhap";
        }
    }

    @PostMapping("/dang-xuat")
    public String dangXuat(HttpSession session, RedirectAttributes ra) {
        NguoiDung nd = PhienDangNhap.layNguoiDung(session);
        boolean laNoiBo = nd != null && nd.getVaiTro() != VaiTro.KHACH_HANG;
        session.invalidate();
        ra.addFlashAttribute("thanhCong", "Bạn đã đăng xuất.");
        return laNoiBo ? "redirect:/dang-nhap" : "redirect:/";
    }

    private String diaChiSauDangNhap(NguoiDung nd, String quayLai) {
        if (quayLai != null && quayLai.startsWith("/") && !quayLai.startsWith("//")
                && !quayLai.contains("\\") && !quayLai.contains("\r") && !quayLai.contains("\n")) {
            int viTriHoi = quayLai.indexOf('?');
            String duongDan = viTriHoi >= 0 ? quayLai.substring(0, viTriHoi) : quayLai;
            if (!duongDan.startsWith("/dang-nhap") && !duongDan.startsWith("/dang-ky")
                    && !duongDan.startsWith("/dang-xuat") && !duongDan.startsWith("/khong-co-quyen")
                    && PhienDangNhap.duocPhepTruyCap(nd.getVaiTro(), duongDan)) {
                return quayLai;
            }
        }
        return trangChuTheoVaiTro(nd);
    }

    private String trangChuTheoVaiTro(NguoiDung nd) {
        if (nd == null || nd.getVaiTro() == null) return "/";
        return switch (nd.getVaiTro()) {
            case QUAN_TRI -> "/admin/thong-ke";
            case NHAN_VIEN -> "/quay/ban-ve";
            case KHACH_HANG -> "/";
        };
    }

    @GetMapping("/dang-ky")
    public String formDangKy(HttpSession session) {
        if (PhienDangNhap.daDangNhap(session)) {
            return "redirect:" + trangChuTheoVaiTro(PhienDangNhap.layNguoiDung(session));
        }
        return "auth/dang-ky";
    }

    @PostMapping("/dang-ky")
    public String dangKy(@RequestParam String tenDangNhap,
                         @RequestParam String matKhau,
                         @RequestParam String xacNhanMatKhau,
                         @RequestParam String hoTen,
                         @RequestParam(required = false) String email,
                         @RequestParam(required = false) String soDienThoai,
                         RedirectAttributes ra, Model model) {
        try {
            nguoiDungService.dangKy(tenDangNhap, matKhau, xacNhanMatKhau, hoTen, email, soDienThoai);
            ra.addFlashAttribute("thanhCong", "Đăng ký thành công! Hãy đăng nhập bằng tài khoản vừa tạo.");
            return "redirect:/dang-nhap";
        } catch (IllegalArgumentException e) {
            com.qlrapchieuphim.service.LoiTruongException.dayVaoModel(e, model);
            model.addAttribute("tenDangNhap", tenDangNhap);
            model.addAttribute("hoTen", hoTen);
            model.addAttribute("email", email);
            model.addAttribute("soDienThoai", soDienThoai);
            return "auth/dang-ky";
        }
    }

    @GetMapping("/ho-so")
    public String hoSo(HttpSession session, Model model) {
        napHoSo(model, layNguoiDungMoi(session));
        return "auth/ho-so";
    }

    @PostMapping("/ho-so/cap-nhat")
    public String capNhatHoSo(@RequestParam String hoTen,
                              @RequestParam(required = false) String email,
                              @RequestParam(required = false) String soDienThoai,
                              HttpSession session, RedirectAttributes ra, Model model) {
        NguoiDung hienTai = PhienDangNhap.layNguoiDung(session);
        try {
            NguoiDung moi = nguoiDungService.capNhatHoSo(hienTai.getId(), hoTen, email, soDienThoai);
            session.setAttribute(PhienDangNhap.KEY, moi);
            ra.addFlashAttribute("thanhCong", "Đã cập nhật hồ sơ.");
            return "redirect:/ho-so";
        } catch (IllegalArgumentException e) {
            napHoSo(model, layNguoiDungMoi(session));
            model.addAttribute("hoTen", hoTen);
            model.addAttribute("email", email);
            model.addAttribute("soDienThoai", soDienThoai);
            com.qlrapchieuphim.service.LoiTruongException.dayVaoModel(e, model);
            return "auth/ho-so";
        }
    }

    @PostMapping("/ho-so/doi-mat-khau")
    public String doiMatKhau(@RequestParam String matKhauCu,
                             @RequestParam String matKhauMoi,
                             @RequestParam String xacNhanMatKhauMoi,
                             HttpSession session, RedirectAttributes ra, Model model) {
        NguoiDung hienTai = PhienDangNhap.layNguoiDung(session);
        try {
            nguoiDungService.doiMatKhau(hienTai.getId(), matKhauCu, matKhauMoi, xacNhanMatKhauMoi);
            ra.addFlashAttribute("thanhCong", "Đổi mật khẩu thành công.");
            return "redirect:/ho-so";
        } catch (IllegalArgumentException e) {
            napHoSo(model, layNguoiDungMoi(session));
            model.addAttribute("loi", e.getMessage());
            if (e instanceof com.qlrapchieuphim.service.LoiTruongException lt) {
                String o = switch (lt.getTruong()) {
                    case "matKhau" -> "matKhauMoi";
                    case "xacNhanMatKhau" -> "xacNhanMatKhauMoi";
                    default -> lt.getTruong();
                };
                model.addAttribute("loiTruong", java.util.Map.of(o, lt.getMessage()));
            }
            return "auth/ho-so";
        }
    }

    @GetMapping("/khong-co-quyen")
    public String khongCoQuyen(HttpServletResponse response, Model model) {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        model.addAttribute("maLoi", 403);
        model.addAttribute("tieuDe", "Không có quyền truy cập");
        model.addAttribute("thongBao", "Tài khoản của bạn không có quyền sử dụng chức năng này.");
        return "loi";
    }

    private NguoiDung layNguoiDungMoi(HttpSession session) {
        return nguoiDungService.layTheoId(PhienDangNhap.layNguoiDung(session).getId());
    }

    private void napHoSo(Model model, NguoiDung nd) {
        model.addAttribute("nd", nd);
        model.addAttribute("hoTen", nd.getHoTen());
        model.addAttribute("email", nd.getEmail());
        model.addAttribute("soDienThoai", nd.getSoDienThoai());
    }
}
