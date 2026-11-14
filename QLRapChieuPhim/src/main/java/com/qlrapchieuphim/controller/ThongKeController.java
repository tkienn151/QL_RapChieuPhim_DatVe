package com.qlrapchieuphim.controller;

import com.qlrapchieuphim.dto.DoanhThuNgayDto;
import com.qlrapchieuphim.dto.DoanhThuPhimDto;
import com.qlrapchieuphim.service.ThongKeService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@Controller
public class ThongKeController {
    private final ThongKeService thongKeService;
    public ThongKeController(ThongKeService thongKeService) {
        this.thongKeService = thongKeService;
    }

    @GetMapping({"/admin", "/admin/", "/admin/thong-ke", "/admin/thong-ke/", "/admin/thongke"})
    public String thongKe(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate tuNgay,
                          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate denNgay,
                          Model model) {
        if (tuNgay == null && denNgay == null) {
            denNgay = LocalDate.now();
            tuNgay = denNgay.minusDays(30);
        } else if (tuNgay == null) {
            tuNgay = denNgay.minusDays(30);
        } else if (denNgay == null) {
            denNgay = LocalDate.now();
        }
        BigDecimal tongDoanhThu;
        long tongVe;
        long tongDon;
        List<DoanhThuNgayDto> dsDoanhThuNgay;
        List<ThongKeService.DoanhThuThang> dsDoanhThuThang;
        List<DoanhThuPhimDto> dsDoanhThuPhim;
        ThongKeService.TongQuanThongKe tongQuan;
        if (tuNgay.isAfter(denNgay)) {
            model.addAttribute("loi", "Ngày bắt đầu không được lớn hơn ngày kết thúc.");
            tongDoanhThu = BigDecimal.ZERO;
            tongVe = 0L;
            tongDon = 0L;
            dsDoanhThuNgay = Collections.emptyList();
            dsDoanhThuThang = Collections.emptyList();
            dsDoanhThuPhim = Collections.emptyList();
            tongQuan = new ThongKeService.TongQuanThongKe(BigDecimal.ZERO, 0L, 0L);
        } else {
            tongDoanhThu = thongKeService.tinhTongDoanhThu(tuNgay, denNgay);
            tongVe = thongKeService.tinhTongVeDaBan(tuNgay, denNgay);
            tongDon = thongKeService.tinhTongDonHang(tuNgay, denNgay);
            dsDoanhThuNgay = thongKeService.thongKeDoanhThuTheoNgay(tuNgay, denNgay);
            dsDoanhThuThang = thongKeService.thongKeDoanhThuTheoThang(tuNgay, denNgay);
            dsDoanhThuPhim = thongKeService.thongKeDoanhThuTheoPhim(tuNgay, denNgay);
            tongQuan = thongKeService.layTongQuan(tuNgay, denNgay);
        }
        model.addAttribute("tuNgay", tuNgay);
        model.addAttribute("denNgay", denNgay);
        model.addAttribute("tongDoanhThu", tongDoanhThu);
        model.addAttribute("tongVe", tongVe);
        model.addAttribute("tongSoVe", tongVe);
        model.addAttribute("tongDon", tongDon);
        model.addAttribute("tongSoDon", tongDon);
        model.addAttribute("dsDoanhThuNgay", dsDoanhThuNgay);
        model.addAttribute("dsDoanhThuThang", dsDoanhThuThang);
        model.addAttribute("dsDoanhThuPhim", dsDoanhThuPhim);
        model.addAttribute("tongQuan", tongQuan);
        return "admin/thongke";
    }
}
