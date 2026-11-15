package com.qlrapchieuphim.controller;

import com.qlrapchieuphim.entity.SuatChieu;
import com.qlrapchieuphim.entity.enums.TrangThaiSuat;
import com.qlrapchieuphim.service.LoiTruongException;
import com.qlrapchieuphim.service.SuatChieuService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.beans.PropertyEditorSupport;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/admin/suat")
public class AdminSuatController {

    private final SuatChieuService suatChieuService;

    public AdminSuatController(SuatChieuService suatChieuService) {
        this.suatChieuService = suatChieuService;
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(LocalDateTime.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) throws IllegalArgumentException {
                if (text == null || text.trim().isEmpty()) {
                    setValue(null);
                } else {
                    setValue(LocalDateTime.parse(text.trim()));
                }
            }

            @Override
            public String getAsText() {
                LocalDateTime val = (LocalDateTime) getValue();
                return val != null ? val.toString() : "";
            }
        });
    }

    @GetMapping({"", "/"})
    public String danhSach(@RequestParam(required = false) String tuKhoa,
                           @RequestParam(required = false) Long phimId,
                           @RequestParam(required = false) Long phongId,
                           @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ngay,
                           @RequestParam(required = false) TrangThaiSuat trangThai,
                           @RequestParam(defaultValue = "gioBatDau") String sapXep,
                           @RequestParam(defaultValue = "false") boolean tang,
                           @RequestParam(defaultValue = "0") int trang,
                           @RequestParam(defaultValue = "10") int kichThuoc,
                           Model model) {
        String tk = (tuKhoa == null || tuKhoa.isBlank()) ? null : tuKhoa.trim();
        String cot = "giaThuong".equals(sapXep) ? "giaThuong" : ("giaVip".equals(sapXep) ? "giaVip" : "gioBatDau");
        Sort.Direction huong = tang ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sort = Sort.by(huong, cot).and(Sort.by(Sort.Direction.ASC, "id"));
        Pageable pageable = PageRequest.of(Math.max(0, trang), kichThuoc, sort);
        Page<SuatChieu> page = suatChieuService.layDanhSachSuatChieu(tk, phimId, phongId, ngay, trangThai, pageable);

        model.addAttribute("page", page);
        model.addAttribute("dsSuat", page.getContent());
        model.addAttribute("dsPhim", suatChieuService.layTatCaPhimTheoTen());
        model.addAttribute("dsPhong", suatChieuService.layTatCaPhongTheoTen());
        model.addAttribute("dsTrangThai", TrangThaiSuat.values());
        model.addAttribute("tuKhoa", tk);
        model.addAttribute("phimId", phimId);
        model.addAttribute("phongId", phongId);
        model.addAttribute("ngay", ngay);
        model.addAttribute("trangThai", trangThai);
        model.addAttribute("sapXep", cot);
        model.addAttribute("tang", tang);
        model.addAttribute("duongDan", taoDuongDan(tk, phimId, phongId, ngay, trangThai, cot, tang));

        return "admin/suat-danh-sach";
    }

    @GetMapping("/them")
    public String formThem(Model model) {
        SuatChieu suatChieu = new SuatChieu();
        suatChieu.setGioBatDau(LocalDateTime.now().plusDays(1).withHour(9).withMinute(0).withSecond(0).withNano(0));
        suatChieu.setGiaThuong(BigDecimal.valueOf(75000));
        suatChieu.setGiaVip(BigDecimal.valueOf(95000));
        suatChieu.setTrangThai(TrangThaiSuat.MO_BAN);

        model.addAttribute("suatChieu", suatChieu);
        napDuLieuForm(model, true, null, null);

        return "admin/suat-form";
    }

    @PostMapping("/them")
    public String xuLyThem(@ModelAttribute("suatChieu") SuatChieu suatChieu,
                           @RequestParam(required = false) Long phimId,
                           @RequestParam(required = false) Long phongChieuId,
                           RedirectAttributes ra,
                           Model model) {
        try {
            suatChieuService.taoSuatChieu(suatChieu, phimId, phongChieuId);
            ra.addFlashAttribute("thanhCong", "Thêm suất chiếu thành công.");
            return "redirect:/admin/suat";
        } catch (Exception e) {
            LoiTruongException.dayVaoModel(e, model);
            napDuLieuForm(model, true, phimId, phongChieuId);
            return "admin/suat-form";
        }
    }

    @GetMapping({"/sua/{id}", "/sua"})
    public String formSua(@PathVariable(name = "id", required = false) Long pathId,
                          @RequestParam(name = "id", required = false) Long paramId,
                          RedirectAttributes ra,
                          Model model) {
        Long id = (pathId != null) ? pathId : paramId;
        if (id == null) {
            ra.addFlashAttribute("loi", "Thiếu mã suất chiếu cần chỉnh sửa.");
            return "redirect:/admin/suat";
        }

        try {
            SuatChieu sc = suatChieuService.layTheoId(id);
            model.addAttribute("suatChieu", sc);
            napDuLieuForm(model, false,
                    sc.getPhim() != null ? sc.getPhim().getId() : null,
                    sc.getPhongChieu() != null ? sc.getPhongChieu().getId() : null);
            return "admin/suat-form";
        } catch (Exception e) {
            ra.addFlashAttribute("loi", e.getMessage());
            return "redirect:/admin/suat";
        }
    }

    @PostMapping({"/sua/{id}", "/sua"})
    public String xuLySua(@PathVariable(name = "id", required = false) Long pathId,
                          @RequestParam(name = "id", required = false) Long paramId,
                          @ModelAttribute("suatChieu") SuatChieu suatChieu,
                          @RequestParam(required = false) Long phimId,
                          @RequestParam(required = false) Long phongChieuId,
                          RedirectAttributes ra,
                          Model model) {
        Long id = (pathId != null) ? pathId : paramId;
        if (id == null && suatChieu.getId() != null) {
            id = suatChieu.getId();
        }

        if (id == null) {
            model.addAttribute("loi", "Thiếu mã suất chiếu cần cập nhật.");
            napDuLieuForm(model, false, phimId, phongChieuId);
            return "admin/suat-form";
        }

        try {
            suatChieuService.capNhatSuatChieu(id, suatChieu, phimId, phongChieuId);
            ra.addFlashAttribute("thanhCong", "Cập nhật suất chiếu thành công.");
            return "redirect:/admin/suat";
        } catch (Exception e) {
            LoiTruongException.dayVaoModel(e, model);
            suatChieu.setId(id);
            model.addAttribute("suatChieu", suatChieu);
            napDuLieuForm(model, false, phimId, phongChieuId);
            return "admin/suat-form";
        }
    }

    private void napDuLieuForm(Model model, boolean laThemMoi, Long phimId, Long phongChieuId) {
        model.addAttribute("dsPhim", suatChieuService.layTatCaPhimTheoTen());
        model.addAttribute("dsPhong", suatChieuService.layTatCaPhongTheoTen());
        model.addAttribute("dsTrangThai", TrangThaiSuat.values());
        model.addAttribute("laThemMoi", laThemMoi);
        if (phimId != null) model.addAttribute("phimId", phimId);
        if (phongChieuId != null) model.addAttribute("phongChieuId", phongChieuId);
    }

    @PostMapping({"/xoa/{id}", "/xoa"})
    public String xoaSuatChieu(@PathVariable(name = "id", required = false) Long pathId,
                               @RequestParam(name = "id", required = false) Long paramId,
                               RedirectAttributes ra) {
        Long id = (pathId != null) ? pathId : paramId;
        if (id == null) {
            ra.addFlashAttribute("loi", "Thiếu mã suất chiếu cần xóa.");
            return "redirect:/admin/suat";
        }

        try {
            suatChieuService.xoaSuatChieu(id);
            ra.addFlashAttribute("thanhCong", "Xóa suất chiếu thành công.");
        } catch (Exception e) {
            ra.addFlashAttribute("loi", e.getMessage());
        }
        return "redirect:/admin/suat";
    }

    @PostMapping({"/huy/{id}", "/huy"})
    public String huySuatChieu(@PathVariable(name = "id", required = false) Long pathId,
                               @RequestParam(name = "id", required = false) Long paramId,
                               RedirectAttributes ra) {
        Long id = (pathId != null) ? pathId : paramId;
        if (id == null) {
            ra.addFlashAttribute("loi", "Thiếu mã suất chiếu cần hủy.");
            return "redirect:/admin/suat";
        }

        try {
            suatChieuService.huySuatChieu(id);
            ra.addFlashAttribute("thanhCong", "Hủy suất chiếu thành công.");
        } catch (Exception e) {
            ra.addFlashAttribute("loi", e.getMessage());
        }
        return "redirect:/admin/suat";
    }

    @PostMapping("/doi-trang-thai")
    public String doiTrangThai(@RequestParam Long id,
                               @RequestParam TrangThaiSuat trangThai,
                               RedirectAttributes ra) {
        try {
            suatChieuService.doiTrangThai(id, trangThai);
            ra.addFlashAttribute("thanhCong", "Đổi trạng thái suất chiếu thành công.");
        } catch (Exception e) {
            ra.addFlashAttribute("loi", e.getMessage());
        }
        return "redirect:/admin/suat";
    }

    private String taoDuongDan(String tuKhoa, Long phimId, Long phongId, LocalDate ngay, TrangThaiSuat trangThai, String sapXep, boolean tang) {
        StringBuilder sb = new StringBuilder("/admin/suat?");
        if (tuKhoa != null) sb.append("tuKhoa=").append(URLEncoder.encode(tuKhoa, StandardCharsets.UTF_8)).append("&");
        if (phimId != null) sb.append("phimId=").append(phimId).append("&");
        if (phongId != null) sb.append("phongId=").append(phongId).append("&");
        if (ngay != null) sb.append("ngay=").append(ngay).append("&");
        if (trangThai != null) sb.append("trangThai=").append(trangThai.name()).append("&");
        sb.append("sapXep=").append(sapXep).append("&tang=").append(tang);
        return sb.toString();
    }
}
