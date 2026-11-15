package com.qlrapchieuphim.controller;

import com.qlrapchieuphim.config.PhienDangNhap;
import com.qlrapchieuphim.entity.DonDat;
import com.qlrapchieuphim.entity.NguoiDung;
import com.qlrapchieuphim.entity.enums.TrangThaiDon;
import com.qlrapchieuphim.service.DatVeService;
import com.qlrapchieuphim.service.VeService;
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
import java.time.LocalDateTime;

@Controller
@RequestMapping("/tra-cuu")
public class TraCuuVeController {

    private final VeService veService;
    private final DatVeService datVeService;

    public TraCuuVeController(VeService veService, DatVeService datVeService) {
        this.veService = veService;
        this.datVeService = datVeService;
    }

    @GetMapping
    public String traCuu(@RequestParam(required = false) String tuKhoa,
                         @RequestParam(required = false) String maDon,
                         @RequestParam(defaultValue = "ngayDat") String sapXep,
                         @RequestParam(defaultValue = "false") boolean tang,
                         @RequestParam(defaultValue = "0") int trang,
                         Model model) {
        datVeService.donTatCaGheQuaHan();

        String tk = tuKhoa == null ? "" : tuKhoa.trim();
        String cot = "tongTien".equals(sapXep) ? "tongTien" : ("maDon".equals(sapXep) ? "maDon" : "ngayDat");
        Sort.Direction huong = tang ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sort = Sort.by(huong, cot).and(Sort.by(Sort.Direction.ASC, "id"));
        PageRequest pageable = PageRequest.of(Math.max(trang, 0), 10, sort);

        Page<DonDat> page = veService.traCuuDon(tk, pageable);
        model.addAttribute("page", page);
        model.addAttribute("tuKhoa", tk);
        model.addAttribute("sapXep", cot);
        model.addAttribute("tang", tang);
        model.addAttribute("duongDan", taoDuongDan(tk, cot, tang));

        if (maDon != null && !maDon.isBlank()) {
            try {
                DonDat don = veService.layChiTietDon(maDon.trim());
                boolean coTheThanhToan = don.getTrangThai() == TrangThaiDon.CHO_THANH_TOAN
                        && !don.daHetHan(LocalDateTime.now());
                boolean coTheHuy = don.getTrangThai() == TrangThaiDon.DA_THANH_TOAN
                        && veService.coTheHuy(don);
                model.addAttribute("don", don);
                model.addAttribute("coTheThanhToan", coTheThanhToan);
                model.addAttribute("coTheHuy", coTheHuy);
            } catch (IllegalArgumentException e) {
                model.addAttribute("loi", e.getMessage());
            }
        }
        return "tracuu/tim-ve";
    }

    @PostMapping("/xac-nhan")
    public String xacNhanThanhToan(@RequestParam String maDon, RedirectAttributes ra) {
        String ma = maDon.trim();
        String maMa = URLEncoder.encode(ma, StandardCharsets.UTF_8);
        try {
            datVeService.thanhToan(ma);
            ra.addFlashAttribute("thanhCong", "Đã xác nhận thu tiền và xuất vé cho đơn " + ma + ".");
        } catch (IllegalArgumentException | IllegalStateException e) {
            ra.addFlashAttribute("loi", e.getMessage());
        }
        return "redirect:/tra-cuu?maDon=" + maMa;
    }

    @PostMapping("/huy-giu-ghe")
    public String huyGiuGhe(@RequestParam String maDon, HttpSession session, RedirectAttributes ra) {
        NguoiDung nv = PhienDangNhap.layNguoiDung(session);
        String ma = maDon.trim();
        String maMa = URLEncoder.encode(ma, StandardCharsets.UTF_8);
        try {
            datVeService.huyDonGiuGhe(ma, nv.getId(), true);
            ra.addFlashAttribute("thanhCong", "Đã hủy giữ ghế cho đơn " + ma + ". Ghế đã được giải phóng.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            ra.addFlashAttribute("loi", e.getMessage());
        }
        return "redirect:/tra-cuu?maDon=" + maMa;
    }

    @PostMapping("/huy")
    public String huyDon(@RequestParam String maDon, HttpSession session, RedirectAttributes ra) {
        NguoiDung nv = PhienDangNhap.layNguoiDung(session);
        String ma = maDon.trim();
        String maMa = URLEncoder.encode(ma, StandardCharsets.UTF_8);
        try {
            veService.huyDon(ma, nv.getId(), true);
            ra.addFlashAttribute("thanhCong", "Đã hủy đơn " + ma + ". Ghế đã được hoàn trả.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            ra.addFlashAttribute("loi", e.getMessage());
        }
        return "redirect:/tra-cuu?maDon=" + maMa;
    }

    private String taoDuongDan(String tuKhoa, String sapXep, boolean tang) {
        StringBuilder sb = new StringBuilder("/tra-cuu?sapXep=").append(sapXep)
                .append("&tang=").append(tang);
        if (tuKhoa != null && !tuKhoa.isEmpty()) {
            sb.append("&tuKhoa=").append(URLEncoder.encode(tuKhoa, StandardCharsets.UTF_8));
        }
        return sb.toString();
    }
}
