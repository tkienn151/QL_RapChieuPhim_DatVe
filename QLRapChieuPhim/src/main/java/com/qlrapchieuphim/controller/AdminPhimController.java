package com.qlrapchieuphim.controller;

import com.qlrapchieuphim.entity.Phim;
import com.qlrapchieuphim.entity.TheLoai;
import com.qlrapchieuphim.entity.enums.TrangThaiPhim;
import com.qlrapchieuphim.service.PhimService;
import com.qlrapchieuphim.service.TheLoaiService;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;
@Controller
public class AdminPhimController {

    private static final Set<String> DUOI_ANH_HOP_LE =
            Set.of(".jpg", ".jpeg", ".png", ".webp", ".gif", ".svg");

    private final PhimService phimService;
    private final TheLoaiService theLoaiService;

    public AdminPhimController(
            PhimService phimService,
            TheLoaiService theLoaiService) {

        this.phimService = phimService;
        this.theLoaiService = theLoaiService;
    }

    @GetMapping("/admin/phim")
    public String danhSachPhim(
            @RequestParam(required = false) String tuKhoa,
            @RequestParam(required = false) Long theLoaiId,
            @RequestParam(required = false) TrangThaiPhim trangThai,
            @RequestParam(defaultValue = "tenPhim") String sapXep,
            @RequestParam(defaultValue = "true") boolean tang,
            @RequestParam(defaultValue = "0") int trang,
            @RequestParam(defaultValue = "10") int kichThuoc,
            Model model) {

        Page<Phim> dsPhim = phimService.timPhim(
                tuKhoa, theLoaiId, trangThai,
                sapXep, tang, trang, kichThuoc);

        model.addAttribute("dsPhim", dsPhim);
        model.addAttribute("dsTheLoai",
                phimService.layDanhSachTheLoai());
        model.addAttribute("trangThaiList",
                TrangThaiPhim.values());

        model.addAttribute("tuKhoa", tuKhoa);
        model.addAttribute("theLoaiId", theLoaiId);
        model.addAttribute("trangThai", trangThai);
        model.addAttribute("sapXep", sapXep);
        model.addAttribute("tang", tang);

        return "admin/phim-danh-sach";
    }

    @GetMapping("/admin/phim/them")
    public String formThemPhim(Model model) {

        model.addAttribute("phim", new Phim());

        napDuLieuFormPhim(model);

        return "admin/phim-form";
    }

    @PostMapping("/admin/phim/them")
    public String themPhim(
            @ModelAttribute("phim") Phim phim,
            @RequestParam(required = false) Long theLoaiId,
            @RequestParam(required = false) MultipartFile posterFile,
            Model model,
            RedirectAttributes redirect) {

        return luuPhimKemPoster(null, phim, theLoaiId, posterFile, model, redirect);
    }

    @GetMapping("/admin/phim/sua/{id}")
    public String formSuaPhim(
            @PathVariable Long id,
            Model model) {

        model.addAttribute(
                "phim", phimService.layChiTiet(id));

        napDuLieuFormPhim(model);

        return "admin/phim-form";
    }

    @PostMapping("/admin/phim/sua/{id}")
    public String suaPhim(
            @PathVariable Long id,
            @ModelAttribute("phim") Phim phim,
            @RequestParam(required = false) Long theLoaiId,
            @RequestParam(required = false) MultipartFile posterFile,
            Model model,
            RedirectAttributes redirect) {

        return luuPhimKemPoster(id, phim, theLoaiId, posterFile, model, redirect);
    }

    private String luuPhimKemPoster(
            Long id,
            Phim phim,
            Long theLoaiId,
            MultipartFile posterFile,
            Model model,
            RedirectAttributes redirect) {
        String posterTruocKhiUpload = phim.getPoster();
        Path fileMoiLuu = null;
        try {
            fileMoiLuu = luuFilePosterNeuCo(phim, posterFile);

            if (id == null) {
                phimService.them(phim, theLoaiId);
                redirect.addFlashAttribute("thanhCong", "Thêm phim thành công.");
            } else {
                phimService.capNhat(id, phim, theLoaiId);
                redirect.addFlashAttribute("thanhCong", "Cập nhật phim thành công.");
            }

            return "redirect:/admin/phim";
        } catch (RuntimeException e) {
            xoaFileTamNeuCo(fileMoiLuu);
            phim.setPoster(posterTruocKhiUpload);
            if (id != null) {
                phim.setId(id);
            }
            if (theLoaiId != null) {
                TheLoai tl = new TheLoai();
                tl.setId(theLoaiId);
                phim.setTheLoai(tl);
            }
            com.qlrapchieuphim.service.LoiTruongException.dayVaoModel(e, model);
            napDuLieuFormPhim(model);
            return "admin/phim-form";
        }
    }

    private Path luuFilePosterNeuCo(Phim phim, MultipartFile posterFile) {
        if (posterFile == null || posterFile.isEmpty()) {
            return null;
        }
        String tenGoc = posterFile.getOriginalFilename();
        String duoi = ".jpg";
        if (tenGoc != null && tenGoc.contains(".")) {
            duoi = tenGoc.substring(tenGoc.lastIndexOf('.')).toLowerCase();
        }
        if (!DUOI_ANH_HOP_LE.contains(duoi)) {
            throw new IllegalArgumentException("Tệp poster phải là ảnh (.jpg, .jpeg, .png, .webp, .gif, .svg).");
        }
        String tenLuu = "poster-" + UUID.randomUUID().toString().substring(0, 8) + duoi;
        try {
            Path thuMuc = Paths.get("uploads", "posters");
            Files.createDirectories(thuMuc);
            Path dich = thuMuc.resolve(tenLuu);
            Files.copy(posterFile.getInputStream(), dich, StandardCopyOption.REPLACE_EXISTING);
            phim.setPoster(tenLuu);
            return dich;
        } catch (IOException ex) {
            throw new IllegalStateException("Không thể lưu file poster: " + ex.getMessage(), ex);
        }
    }

    private void xoaFileTamNeuCo(Path fileMoiLuu) {
        if (fileMoiLuu == null) {
            return;
        }
        try {
            Files.deleteIfExists(fileMoiLuu);
        } catch (IOException ignored) {
        }
    }

    @PostMapping("/admin/phim/xoa/{id}")
    public String xoaPhim(
            @PathVariable Long id,
            RedirectAttributes redirect) {

        try {

            phimService.xoa(id);

            redirect.addFlashAttribute(
                    "thanhCong", "Xóa phim thành công.");

        } catch (RuntimeException e) {

            redirect.addFlashAttribute("loi", e.getMessage());
        }

        return "redirect:/admin/phim";
    }

    @PostMapping("/admin/phim/doi-trang-thai/{id}")
    public String doiTrangThai(
            @PathVariable Long id,
            @RequestParam TrangThaiPhim trangThai,
            RedirectAttributes redirect) {

        try {

            phimService.doiTrangThai(id, trangThai);

            redirect.addFlashAttribute(
                    "thanhCong", "Đổi trạng thái phim thành công.");

        } catch (RuntimeException e) {

            redirect.addFlashAttribute("loi", e.getMessage());
        }

        return "redirect:/admin/phim";
    }

    private void napDuLieuFormPhim(Model model) {

        model.addAttribute("dsTheLoai",
                phimService.layDanhSachTheLoai());

        model.addAttribute("trangThaiList",
                TrangThaiPhim.values());
    }

    @GetMapping("/admin/theloai")
    public String danhSachTheLoai(@RequestParam(required = false) String tuKhoa,
                                  @RequestParam(defaultValue = "tenTheLoai") String sapXep,
                                  @RequestParam(defaultValue = "true") boolean tang,
                                  @RequestParam(defaultValue = "0") int trang,
                                  Model model) {
        String tk = (tuKhoa == null || tuKhoa.isBlank()) ? null : tuKhoa.trim();
        String cot = "moTa".equals(sapXep) ? "moTa" : "tenTheLoai";
        model.addAttribute("dsTheLoai", theLoaiService.timKiem(tk, cot, tang, trang));
        model.addAttribute("tuKhoa", tk);
        model.addAttribute("sapXep", cot);
        model.addAttribute("tang", tang);
        model.addAttribute("duongDan", "/admin/theloai?sapXep=" + cot + "&tang=" + tang
                + (tk != null ? "&tuKhoa=" + java.net.URLEncoder.encode(tk, java.nio.charset.StandardCharsets.UTF_8) : ""));

        return "admin/theloai-danh-sach";
    }

    @GetMapping("/admin/theloai/them")
    public String formThemTheLoai(Model model) {

        model.addAttribute("theLoai", new TheLoai());

        return "admin/theloai-form";
    }

    @PostMapping("/admin/theloai/them")
    public String themTheLoai(
            @ModelAttribute("theLoai") TheLoai theLoai,
            Model model,
            RedirectAttributes redirect) {

        try {

            theLoaiService.them(theLoai);

            redirect.addFlashAttribute(
                    "thanhCong", "Thêm thể loại thành công.");

            return "redirect:/admin/theloai";

        } catch (RuntimeException e) {

            com.qlrapchieuphim.service.LoiTruongException.dayVaoModel(e, model);

            return "admin/theloai-form";
        }
    }

    @GetMapping("/admin/theloai/sua/{id}")
    public String formSuaTheLoai(
            @PathVariable Long id,
            Model model) {

        model.addAttribute(
                "theLoai", theLoaiService.layChiTiet(id));

        return "admin/theloai-form";
    }

    @PostMapping("/admin/theloai/sua/{id}")
    public String suaTheLoai(
            @PathVariable Long id,
            @ModelAttribute("theLoai") TheLoai theLoai,
            Model model,
            RedirectAttributes redirect) {

        try {

            theLoaiService.capNhat(id, theLoai);

            redirect.addFlashAttribute(
                    "thanhCong", "Cập nhật thể loại thành công.");

            return "redirect:/admin/theloai";

        } catch (RuntimeException e) {

            theLoai.setId(id);

            com.qlrapchieuphim.service.LoiTruongException.dayVaoModel(e, model);

            return "admin/theloai-form";
        }
    }

    @PostMapping("/admin/theloai/xoa/{id}")
    public String xoaTheLoai(
            @PathVariable Long id,
            RedirectAttributes redirect) {

        try {

            theLoaiService.xoa(id);

            redirect.addFlashAttribute(
                    "thanhCong", "Xóa thể loại thành công.");

        } catch (RuntimeException e) {

            redirect.addFlashAttribute("loi", e.getMessage());
        }

        return "redirect:/admin/theloai";
    }
}
