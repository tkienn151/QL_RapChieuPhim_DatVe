package com.qlrapchieuphim.controller;

import com.qlrapchieuphim.config.PhienDangNhap;
import com.qlrapchieuphim.entity.DonDat;
import com.qlrapchieuphim.entity.NguoiDung;
import com.qlrapchieuphim.entity.SuatChieu;
import com.qlrapchieuphim.entity.enums.TrangThaiDon;
import com.qlrapchieuphim.service.BanVeService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Controller
@RequestMapping("/quay")
public class QuayController {
    private final BanVeService banVeService;

    public QuayController(BanVeService banVeService) {
        this.banVeService = banVeService;
    }

    @GetMapping("/ban-ve")
    public String manHinhBanVe(@RequestParam(required = false) Long suatId, Model model) {
        List<SuatChieu> dsSuat = banVeService.layDanhSachSuatMoBanTaiQuay();
        model.addAttribute("dsSuat", dsSuat);

        banVeService.chuanBiBanVeChoSuat(suatId).ifPresent(suatChieu -> {
            model.addAttribute("suatChieu", suatChieu);
            model.addAttribute("dsGhe", banVeService.layDanhSachGheTheoPhong(suatChieu.getPhongChieu().getId()));
            model.addAttribute("dsGheDaDat", banVeService.layDanhSachGheDaDat(suatId));
        });
        return "quay/ban-ve";
    }

    @PostMapping("/ban-ve")
    public String xuatVeTaiQuay(@RequestParam Long suatId,
                                @RequestParam(required = false) List<Long> dsGheId,
                                @RequestParam(required = false) String hoTenKhach,
                                @RequestParam(required = false) String sdtKhach,
                                HttpSession session, RedirectAttributes ra) {
        if (dsGheId == null || dsGheId.isEmpty()) {
            ra.addFlashAttribute("loi", "Vui lòng chọn ghế trên màn hình.");
            return "redirect:/quay/ban-ve?suatId=" + suatId;
        }
        try {
            NguoiDung nv = PhienDangNhap.layNguoiDung(session);
            DonDat don = banVeService.banVeTaiQuay(suatId, dsGheId, nv.getId(), hoTenKhach, sdtKhach);
            ra.addFlashAttribute("thanhCong", "Xuất vé thành công!");
            return "redirect:/quay/ket-qua?maDon=" + don.getMaDon();
        } catch (IllegalArgumentException | org.springframework.dao.DataIntegrityViolationException e) {
            String msg = (e instanceof IllegalArgumentException) ? e.getMessage()
                    : "Ghế vừa được giao dịch bởi quầy/khách khác, vui lòng thử lại.";
            ra.addFlashAttribute("loi", msg);
            return "redirect:/quay/ban-ve?suatId=" + suatId;
        }
    }

    @GetMapping("/ket-qua")
    public String ketQua(@RequestParam String maDon, Model model, RedirectAttributes ra) {
        DonDat donDat = banVeService.layDonTheoMa(maDon);
        if (donDat.getTrangThai() != TrangThaiDon.DA_THANH_TOAN) {
            ra.addFlashAttribute("loi", "Đơn đặt vé chưa hoàn tất thanh toán.");
            return "redirect:/tra-cuu?maDon=" + URLEncoder.encode(donDat.getMaDon(), StandardCharsets.UTF_8);
        }
        model.addAttribute("donDat", donDat);
        return "quay/ket-qua";
    }
}
