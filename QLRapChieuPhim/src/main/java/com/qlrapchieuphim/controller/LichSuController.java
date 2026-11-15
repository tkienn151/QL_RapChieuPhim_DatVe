package com.qlrapchieuphim.controller;

import com.qlrapchieuphim.config.PhienDangNhap;
import com.qlrapchieuphim.entity.DonDat;
import com.qlrapchieuphim.entity.NguoiDung;
import com.qlrapchieuphim.entity.enums.TrangThaiDon;
import com.qlrapchieuphim.entity.enums.VaiTro;
import com.qlrapchieuphim.service.DatVeService;
import com.qlrapchieuphim.service.VeService;
import jakarta.servlet.http.HttpSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@Controller
public class LichSuController {
    private static final int SO_DON_MOI_TRANG = 10;
    private final VeService veService;
    private final DatVeService datVeService;
    public LichSuController(VeService veService, DatVeService datVeService) {
        this.veService = veService;
        this.datVeService = datVeService;
    }
    @GetMapping("/lich-su")
    public String danhSach(@RequestParam(defaultValue = "0") int trang,
                           @RequestParam(required = false) String tuKhoa,
                           @RequestParam(required = false) String trangThai,
                           @RequestParam(defaultValue = "ngayDat") String sapXep,
                           @RequestParam(defaultValue = "false") boolean tang,
                           HttpSession session,
                           Model model) {
        NguoiDung nd = PhienDangNhap.layNguoiDung(session);
        if (nd == null) {
            return "redirect:/dang-nhap";
        }
        datVeService.donTatCaGheQuaHan();
        String tk = (tuKhoa == null || tuKhoa.isBlank()) ? null : tuKhoa.trim();
        TrangThaiDon tt = null;
        String ttTen = null;
        if (trangThai != null && !trangThai.isBlank()) {
            try {
                tt = TrangThaiDon.valueOf(trangThai.trim().toUpperCase());
                ttTen = tt.name();
            } catch (IllegalArgumentException ignored) {
                tt = null;
            }
        }
        String cot = "tongTien".equals(sapXep) ? "tongTien" : ("maDon".equals(sapXep) ? "maDon" : "ngayDat");
        Sort.Direction huong = tang ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sort = Sort.by(huong, cot).and(Sort.by(Sort.Direction.ASC, "id"));
        Pageable pageable = PageRequest.of(Math.max(0, trang), SO_DON_MOI_TRANG, sort);
        Page<DonDat> page = veService.timLichSuDatVe(nd.getId(), tk, tt, pageable);

        model.addAttribute("page", page);
        model.addAttribute("dsDonDat", page.getContent());
        model.addAttribute("tuKhoa", tk);
        model.addAttribute("trangThai", ttTen);
        model.addAttribute("sapXep", cot);
        model.addAttribute("tang", tang);
        model.addAttribute("duongDan", taoDuongDan(tk, ttTen, cot, tang));
        model.addAttribute("dsTrangThai", TrangThaiDon.values());
        return "lichsu/danh-sach";
    }

    private String taoDuongDan(String tuKhoa, String trangThai, String sapXep, boolean tang) {
        StringBuilder sb = new StringBuilder("/lich-su?sapXep=").append(sapXep)
                .append("&tang=").append(tang);
        if (tuKhoa != null) sb.append("&tuKhoa=").append(URLEncoder.encode(tuKhoa, StandardCharsets.UTF_8));
        if (trangThai != null) sb.append("&trangThai=").append(URLEncoder.encode(trangThai, StandardCharsets.UTF_8));
        return sb.toString();
    }
    @GetMapping("/lich-su/chi-tiet")
    public String chiTiet(@RequestParam(required = false) Long id,
                          @RequestParam(required = false) String maDon,
                          HttpSession session,
                          Model model,
                          RedirectAttributes ra) {
        return xuLyXemChiTiet(id, maDon, session, model, ra);
    }

    @GetMapping("/lich-su/chi-tiet/{id}")
    public String chiTietTheoId(@PathVariable Long id,
                                HttpSession session,
                                Model model,
                                RedirectAttributes ra) {
        return xuLyXemChiTiet(id, null, session, model, ra);
    }
    @PostMapping("/lich-su/huy")
    public String huyDon(@RequestParam(required = false) Long id,
                         @RequestParam(required = false) String maDon,
                         HttpSession session,
                         RedirectAttributes ra) {
        return xuLyHuyDon(id, maDon, session, ra);
    }
    @PostMapping("/lich-su/huy/{id}")
    public String huyDonTheoId(@PathVariable Long id,
                               HttpSession session,
                               RedirectAttributes ra) {
        return xuLyHuyDon(id, null, session, ra);
    }
    private String xuLyXemChiTiet(Long id, String maDon, HttpSession session, Model model, RedirectAttributes ra) {
        NguoiDung nd = PhienDangNhap.layNguoiDung(session);
        if (nd == null) {
            return "redirect:/dang-nhap";
        }
        datVeService.donTatCaGheQuaHan();
        try {
            DonDat don;
            if (id != null) {
                don = veService.layChiTietDonDat(id, nd.getId());
            } else if (maDon != null && !maDon.isBlank()) {
                don = veService.layChiTietDon(maDon.trim(), nd.getId());
            } else {
                ra.addFlashAttribute("loi", "Mã đơn hàng hoặc ID không được để trống.");
                return "redirect:/lich-su";
            }
            boolean laChuDon = don.getNguoiDung() != null && don.getNguoiDung().getId().equals(nd.getId());
            boolean coTheThanhToan = laChuDon
                    && don.getTrangThai() == TrangThaiDon.CHO_THANH_TOAN
                    && !don.daHetHan(LocalDateTime.now());
            model.addAttribute("don", don);
            model.addAttribute("coTheThanhToan", coTheThanhToan);
            model.addAttribute("coTheHuy", veService.coTheHuy(don));
            return "lichsu/chi-tiet";
        } catch (IllegalArgumentException | IllegalStateException e) {
            ra.addFlashAttribute("loi", e.getMessage());
            return "redirect:/lich-su";
        }
    }
    private String xuLyHuyDon(Long id, String maDon, HttpSession session, RedirectAttributes ra) {
        NguoiDung nd = PhienDangNhap.layNguoiDung(session);
        if (nd == null) {
            return "redirect:/dang-nhap";
        }
        boolean laNhanVienHoacQuanTri = (nd.getVaiTro() == VaiTro.NHAN_VIEN || nd.getVaiTro() == VaiTro.QUAN_TRI);

        try {
            if (id != null) {
                veService.huyDonDat(id, nd.getId(), laNhanVienHoacQuanTri);
                ra.addFlashAttribute("thanhCong", "Hủy đơn đặt vé thành công! Toàn bộ ghế đã được hoàn trả.");
                return "redirect:/lich-su/chi-tiet?id=" + id;
            } else if (maDon != null && !maDon.isBlank()) {
                veService.huyDon(maDon.trim(), nd.getId(), laNhanVienHoacQuanTri);
                ra.addFlashAttribute("thanhCong", "Hủy đơn đặt vé " + maDon.trim() + " thành công! Toàn bộ ghế đã được hoàn trả.");
                return "redirect:/lich-su/chi-tiet?maDon=" + URLEncoder.encode(maDon.trim(), StandardCharsets.UTF_8);
            } else {
                ra.addFlashAttribute("loi", "Thông tin đơn vé cần hủy không hợp lệ.");
                return "redirect:/lich-su";
            }
        } catch (IllegalArgumentException | IllegalStateException e) {
            ra.addFlashAttribute("loi", e.getMessage());
            if (id != null) {
                return "redirect:/lich-su/chi-tiet?id=" + id;
            } else if (maDon != null && !maDon.isBlank()) {
                return "redirect:/lich-su/chi-tiet?maDon=" + URLEncoder.encode(maDon.trim(), StandardCharsets.UTF_8);
            }
            return "redirect:/lich-su";
        }
    }
}
