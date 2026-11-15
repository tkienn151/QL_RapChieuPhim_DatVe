package com.qlrapchieuphim.controller;

import com.qlrapchieuphim.config.PhienDangNhap;
import com.qlrapchieuphim.entity.DonDat;
import com.qlrapchieuphim.entity.NguoiDung;
import com.qlrapchieuphim.entity.SuatChieu;
import com.qlrapchieuphim.entity.enums.TrangThaiDon;
import com.qlrapchieuphim.service.DatVeService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/dat-ve")
public class DatVeController {
    private final DatVeService datVeService;

    public DatVeController(DatVeService datVeService) {
        this.datVeService = datVeService;
    }

    @GetMapping("/chon-ghe")
    public String chonGhe(@RequestParam Long suatId, Model model) {
        SuatChieu suatChieu = datVeService.chuanBiChonGhe(suatId);
        model.addAttribute("suatChieu", suatChieu);
        model.addAttribute("dsGhe", datVeService.layDanhSachGheTheoPhong(suatChieu.getPhongChieu().getId()));
        model.addAttribute("dsGheDaDat", datVeService.layDanhSachGheDaDat(suatId));
        return "datve/chon-ghe";
    }

    @PostMapping("/giu-ghe")
    public String giuGhe(@RequestParam Long suatId, @RequestParam(required = false) List<Long> dsGheId,
                         HttpSession session, RedirectAttributes ra) {
        if (dsGheId == null || dsGheId.isEmpty()) {
            ra.addFlashAttribute("loi", "Vui lòng chọn ít nhất 1 ghế.");
            return "redirect:/dat-ve/chon-ghe?suatId=" + suatId;
        }
        NguoiDung nd = PhienDangNhap.layNguoiDung(session);
        try {
            DonDat don = datVeService.giuGhe(suatId, dsGheId, nd.getId());
            return "redirect:/dat-ve/thanh-toan?maDon=" + don.getMaDon();
        } catch (IllegalArgumentException | org.springframework.dao.DataIntegrityViolationException e) {
            String msg = (e instanceof IllegalArgumentException) ? e.getMessage()
                    : "Ghế vừa bị người khác đặt, vui lòng chọn ghế khác.";
            ra.addFlashAttribute("loi", msg);
            return "redirect:/dat-ve/chon-ghe?suatId=" + suatId;
        }
    }

    @GetMapping("/thanh-toan")
    public String trangThanhToan(@RequestParam String maDon, HttpSession session, Model model, RedirectAttributes ra) {
        NguoiDung nguoiDungSession = PhienDangNhap.layNguoiDung(session);
        DonDat donDat = datVeService.layDonCuaNguoiDung(
                maDon,
                nguoiDungSession != null ? nguoiDungSession.getId() : null,
                "Bạn không có quyền truy cập đơn hàng này.");

        if (donDat.getTrangThai() != TrangThaiDon.CHO_THANH_TOAN) {
            ra.addFlashAttribute("loi", "Đơn đặt vé đã được xử lý hoặc hết hạn.");
            return "redirect:/lich-su";
        }
        if (donDat.daHetHan(LocalDateTime.now())) {
            datVeService.nhaGheDonQuaHan(donDat.getId());
            ra.addFlashAttribute("loi", "Đơn đặt vé đã quá hạn 5 phút và được giải phóng ghế.");
            return "redirect:/lich-su";
        }

        model.addAttribute("donDat", donDat);
        model.addAttribute("hanThanhToanStr", donDat.getHanThanhToan() != null ? donDat.getHanThanhToan().toString() : "");

        return "datve/thanh-toan";
    }

    @PostMapping("/thanh-toan")
    public String xuLyThanhToan(@RequestParam String maDon, HttpSession session, RedirectAttributes ra) {
        NguoiDung nguoiDungSession = PhienDangNhap.layNguoiDung(session);
        datVeService.layDonCuaNguoiDung(
                maDon,
                nguoiDungSession != null ? nguoiDungSession.getId() : null,
                "Bạn không có quyền thao tác.");
        try {
            datVeService.thanhToan(maDon);
            ra.addFlashAttribute("thanhCong", "Thanh toán thành công! Mã vé của bạn đã được xuất.");
            return "redirect:/dat-ve/ket-qua?maDon=" + maDon;
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("loi", e.getMessage());
            return "redirect:/lich-su";
        }
    }

    @PostMapping("/huy-giu-ghe")
    public String huyGiuGhe(@RequestParam String maDon, HttpSession session, RedirectAttributes ra) {
        NguoiDung nguoiDungSession = PhienDangNhap.layNguoiDung(session);
        try {
            Long suatId = datVeService.huyDonGiuGhe(maDon, nguoiDungSession != null ? nguoiDungSession.getId() : null);
            ra.addFlashAttribute("thanhCong", "Đã hủy giữ ghế cho đơn " + maDon.trim() + ". Các ghế đã được trả về trạng thái trống.");
            if (datVeService.suatConMoBan(suatId)) {
                return "redirect:/dat-ve/chon-ghe?suatId=" + suatId;
            }
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("loi", e.getMessage());
        }
        return "redirect:/lich-su";
    }

    @GetMapping("/ket-qua")
    public String ketQua(@RequestParam String maDon, HttpSession session, Model model, RedirectAttributes ra) {
        NguoiDung nguoiDungSession = PhienDangNhap.layNguoiDung(session);
        DonDat donDat = datVeService.layDonCuaNguoiDung(
                maDon,
                nguoiDungSession != null ? nguoiDungSession.getId() : null,
                "Không có quyền xem đơn này.");
        if (donDat.getTrangThai() != TrangThaiDon.DA_THANH_TOAN) {
            ra.addFlashAttribute("loi", "Đơn đặt vé chưa hoàn tất thanh toán.");
            String maMa = URLEncoder.encode(donDat.getMaDon(), StandardCharsets.UTF_8);
            return donDat.getTrangThai() == TrangThaiDon.CHO_THANH_TOAN
                    ? "redirect:/dat-ve/thanh-toan?maDon=" + maMa
                    : "redirect:/lich-su";
        }
        model.addAttribute("donDat", donDat);
        return "datve/ket-qua";
    }
}
