package com.qlrapchieuphim.controller;

import com.qlrapchieuphim.config.PhienDangNhap;
import com.qlrapchieuphim.entity.*;
import com.qlrapchieuphim.repository.*;
import com.qlrapchieuphim.entity.DonDat;
import com.qlrapchieuphim.entity.NguoiDung;
import com.qlrapchieuphim.entity.SuatChieu;
import com.qlrapchieuphim.entity.enums.TrangThaiDon;
import com.qlrapchieuphim.repository.DonDatRepository;
import com.qlrapchieuphim.repository.GheRepository;
import com.qlrapchieuphim.repository.SuatChieuRepository;
import com.qlrapchieuphim.repository.VeRepository;
import com.qlrapchieuphim.service.BanVeService;
import com.qlrapchieuphim.service.DatVeService;
import jakarta.servlet.http.HttpSession;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/quay")
public class QuayController {
    private final BanVeService banVeService;
    private final DatVeService datVeService;
    private final SuatChieuRepository suatChieuRepo;
    private final GheRepository gheRepo;
    private final VeRepository veRepo;
    private final DonDatRepository donDatRepo;

    public QuayController(BanVeService banVeService, DatVeService datVeService, SuatChieuRepository suatChieuRepo,
                          GheRepository gheRepo, VeRepository veRepo, DonDatRepository donDatRepo) {
        this.banVeService = banVeService;
        this.datVeService = datVeService;
        this.suatChieuRepo = suatChieuRepo;
        this.gheRepo = gheRepo;
        this.veRepo = veRepo;
        this.donDatRepo = donDatRepo;
    }

    @GetMapping("/ban-ve")
    public String manHinhBanVe(@RequestParam(required = false) Long suatId, Model model) {
        List<SuatChieu> dsSuat = new ArrayList<>(suatChieuRepo.findAll(Sort.by(Sort.Direction.ASC, "gioBatDau")));
        LocalDateTime bayGio = LocalDateTime.now();
        dsSuat.removeIf(s -> !s.conMoBan() || !s.getGioKetThuc().isAfter(bayGio));

        model.addAttribute("dsSuat", dsSuat);

        if (suatId != null) {
            SuatChieu suatChieu = suatChieuRepo.findById(suatId).orElse(null);
            if (suatChieu != null && suatChieu.conMoBan() && suatChieu.getGioKetThuc().isAfter(bayGio)) {
                datVeService.donGheQuaHanTheoSuat(suatId);
                model.addAttribute("suatChieu", suatChieu);
                model.addAttribute("dsGhe", gheRepo.findByPhongChieuIdOrderByHangAscSoGheAsc(suatChieu.getPhongChieu().getId()));
                model.addAttribute("dsGheDaDat", veRepo.findGheDaDat(suatId));
            }
        }
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
        DonDat donDat = donDatRepo.findByMaDon(maDon).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn đặt vé"));
        if (donDat.getTrangThai() != TrangThaiDon.DA_THANH_TOAN) {
            ra.addFlashAttribute("loi", "Đơn đặt vé chưa hoàn tất thanh toán.");
            return "redirect:/tra-cuu?maDon=" + java.net.URLEncoder.encode(donDat.getMaDon(), java.nio.charset.StandardCharsets.UTF_8);
        }
        model.addAttribute("donDat", donDat);
        return "quay/ket-qua";
    }
}
