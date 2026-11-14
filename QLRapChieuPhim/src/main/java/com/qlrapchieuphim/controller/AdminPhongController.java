package com.qlrapchieuphim.controller;

import com.qlrapchieuphim.entity.PhongChieu;
import com.qlrapchieuphim.service.PhongChieuService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
@Controller
@RequestMapping("/admin/phong")
public class AdminPhongController {

    private final PhongChieuService phongChieuService;

    public AdminPhongController(PhongChieuService phongChieuService) {
        this.phongChieuService = phongChieuService;
    }

    @GetMapping
    public String danhSach(@RequestParam(required = false) String tuKhoa,
                           @RequestParam(defaultValue = "maPhong") String sapXep,
                           @RequestParam(defaultValue = "true") boolean tang,
                           @RequestParam(defaultValue = "0") int trang,
                           Model model) {
        String tk = (tuKhoa == null || tuKhoa.isBlank()) ? null : tuKhoa.trim();
        String cot = switch (sapXep) {
            case "tenPhong", "soHang", "soCot" -> sapXep;
            default -> "maPhong";
        };
        model.addAttribute("dsPhong", phongChieuService.timKiem(tk, cot, tang, trang));
        model.addAttribute("tuKhoa", tk);
        model.addAttribute("sapXep", cot);
        model.addAttribute("tang", tang);
        model.addAttribute("duongDan", "/admin/phong?sapXep=" + cot + "&tang=" + tang
                + (tk != null ? "&tuKhoa=" + java.net.URLEncoder.encode(tk, java.nio.charset.StandardCharsets.UTF_8) : ""));
        return "admin/phong-danh-sach";
    }

    @GetMapping("/them")
    public String formThem(Model model) {

        model.addAttribute("phongChieu", new PhongChieu());

        return "admin/phong-form";
    }

    @PostMapping("/them")
    public String them(
            @ModelAttribute("phongChieu") PhongChieu phong,
            Model model,
            RedirectAttributes redirect) {

        try {

            phongChieuService.them(phong);

            redirect.addFlashAttribute(
                    "thanhCong", "Thêm phòng chiếu thành công.");

            return "redirect:/admin/phong";

        } catch (RuntimeException e) {

            com.qlrapchieuphim.service.LoiTruongException.dayVaoModel(e, model);

            return "admin/phong-form";
        }
    }

    @GetMapping("/sua/{id}")
    public String formSua(
            @PathVariable Long id,
            Model model) {

        model.addAttribute(
                "phongChieu", phongChieuService.layChiTiet(id));

        return "admin/phong-form";
    }

    @PostMapping("/sua/{id}")
    public String sua(
            @PathVariable Long id,
            @ModelAttribute("phongChieu") PhongChieu phong,
            Model model,
            RedirectAttributes redirect) {

        try {

            phongChieuService.capNhat(id, phong);

            redirect.addFlashAttribute(
                    "thanhCong", "Cập nhật phòng chiếu thành công.");

            return "redirect:/admin/phong";

        } catch (RuntimeException e) {

            phong.setId(id);
            com.qlrapchieuphim.service.LoiTruongException.dayVaoModel(e, model);

            return "admin/phong-form";
        }
    }

    @PostMapping("/xoa/{id}")
    public String xoa(
            @PathVariable Long id,
            RedirectAttributes redirect) {

        try {

            phongChieuService.xoa(id);

            redirect.addFlashAttribute(
                    "thanhCong", "Xóa phòng chiếu thành công.");

        } catch (RuntimeException e) {

            redirect.addFlashAttribute("loi", e.getMessage());
        }

        return "redirect:/admin/phong";
    }
}
