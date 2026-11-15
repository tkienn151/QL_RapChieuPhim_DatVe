package com.qlrapchieuphim.controller;

import com.qlrapchieuphim.config.PhienDangNhap;
import com.qlrapchieuphim.entity.NguoiDung;
import com.qlrapchieuphim.entity.enums.VaiTro;
import com.qlrapchieuphim.service.LoiTruongException;
import com.qlrapchieuphim.service.NguoiDungService;
import jakarta.servlet.http.HttpSession;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
@RequestMapping("/admin/taikhoan")
public class AdminTaiKhoanController {

    private final NguoiDungService nguoiDungService;

    public AdminTaiKhoanController(NguoiDungService nguoiDungService) {
        this.nguoiDungService = nguoiDungService;
    }

    @GetMapping
    public String danhSachTaiKhoan(@RequestParam(required = false) String tuKhoa,
                                   @RequestParam(required = false) VaiTro vaiTro,
                                   @RequestParam(defaultValue = "id") String sapXep,
                                   @RequestParam(defaultValue = "false") boolean tang,
                                   @RequestParam(defaultValue = "0") int trang,
                                   Model model) {
        String tk = (tuKhoa == null || tuKhoa.isBlank()) ? null : tuKhoa.trim();
        String cot = NguoiDungService.chuanHoaCotSapXep(sapXep);
        Page<NguoiDung> dsTaiKhoan = nguoiDungService.timKiemTaiKhoan(tk, vaiTro, cot, tang, trang, 10);

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
        try {
            NguoiDung nd = nguoiDungService.themTaiKhoanNoiBo(ten, matKhau, hoTen, em, sdt, vaiTro);
            ra.addFlashAttribute("thanhCong", "Thêm tài khoản '" + nd.getTenDangNhap() + "' thành công!");
            return "redirect:/admin/taikhoan";
        } catch (LoiTruongException e) {
            return traVeFormThemLoi(model, ten, hoTen, em, sdt, vaiTro, e);
        }
    }

    private String traVeFormThemLoi(Model model, String ten, String hoTen, String em, String sdt,
                                    VaiTro vaiTro, LoiTruongException e) {
        NguoiDung nd = new NguoiDung();
        nd.setTenDangNhap(ten);
        nd.setHoTen(hoTen);
        nd.setEmail(em);
        nd.setSoDienThoai(sdt);
        nd.setVaiTro(vaiTro);
        model.addAttribute("nguoiDung", nd);
        LoiTruongException.dayVaoModel(e, model);
        return "admin/taikhoan-form";
    }

    @PostMapping("/khoa/{id}")
    public String khoaMoTaiKhoan(@PathVariable Long id, HttpSession session, RedirectAttributes ra) {
        NguoiDung dangNhap = PhienDangNhap.layNguoiDung(session);
        try {
            nguoiDungService.khoaMoTaiKhoan(id, dangNhap != null ? dangNhap.getId() : null)
                    .ifPresent(hoatDong -> ra.addFlashAttribute("thanhCong",
                            hoatDong ? "Đã mở khóa tài khoản!" : "Đã khóa tài khoản!"));
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("loi", e.getMessage());
        }
        return "redirect:/admin/taikhoan";
    }

    @PostMapping("/vai-tro/{id}")
    public String doiVaiTro(@PathVariable Long id,
                            @RequestParam VaiTro vaiTro,
                            HttpSession session,
                            RedirectAttributes ra) {
        NguoiDung dangNhap = PhienDangNhap.layNguoiDung(session);
        try {
            nguoiDungService.doiVaiTro(id, vaiTro, dangNhap != null ? dangNhap.getId() : null)
                    .ifPresent(nd -> ra.addFlashAttribute("thanhCong",
                            "Đã cập nhật vai trò tài khoản '" + nd.getTenDangNhap() + "' sang " + vaiTro));
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("loi", e.getMessage());
        }
        return "redirect:/admin/taikhoan";
    }

    @PostMapping("/xoa/{id}")
    public String xoaTaiKhoan(@PathVariable Long id, HttpSession session, RedirectAttributes ra) {
        NguoiDung dangNhap = PhienDangNhap.layNguoiDung(session);
        try {
            nguoiDungService.xoaTaiKhoan(id, dangNhap != null ? dangNhap.getId() : null)
                    .ifPresent(ten -> ra.addFlashAttribute("thanhCong",
                            "Đã xóa tài khoản '" + ten + "' thành công."));
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("loi", e.getMessage());
        }
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
