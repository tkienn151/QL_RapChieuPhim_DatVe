package com.qlrapchieuphim.service;

import com.qlrapchieuphim.dto.DoanhThuNgayDto;
import com.qlrapchieuphim.dto.DoanhThuPhimDto;
import com.qlrapchieuphim.repository.DonDatRepository;
import com.qlrapchieuphim.repository.VeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ThongKeService {
    private final VeRepository veRepo;
    private final DonDatRepository donDatRepo;

    public ThongKeService(VeRepository veRepo, DonDatRepository donDatRepo) {
        this.veRepo = veRepo;
        this.donDatRepo = donDatRepo;
    }
    public static class TongQuanThongKe {
        private final BigDecimal tongDoanhThu;
        private final long tongSoVe;
        private final long tongSoDon;
        public TongQuanThongKe(BigDecimal tongDoanhThu, long tongSoVe, long tongSoDon) {
            this.tongDoanhThu = (tongDoanhThu != null) ? tongDoanhThu : BigDecimal.ZERO;
            this.tongSoVe = tongSoVe;
            this.tongSoDon = tongSoDon;
        }
        public BigDecimal getTongDoanhThu() {
            return tongDoanhThu;
        }
        public long getTongSoVe() {
            return tongSoVe;
        }
        public long getTongVe() {
            return tongSoVe;
        }
        public long getTongSoDon() {
            return tongSoDon;
        }
        public long getTongDon() {
            return tongSoDon;
        }
    }

    public static class DoanhThuThang {
        private final String thangNam;
        private final long soLuongDon;
        private final long soLuongVe;
        private final BigDecimal tongDoanhThu;
        public DoanhThuThang(String thangNam, long soLuongDon, long soLuongVe, BigDecimal tongDoanhThu) {
            this.thangNam = thangNam;
            this.soLuongDon = soLuongDon;
            this.soLuongVe = soLuongVe;
            this.tongDoanhThu = tongDoanhThu != null ? tongDoanhThu : BigDecimal.ZERO;
        }

        public String getThangNam() { return thangNam; }
        public long getSoLuongDon() { return soLuongDon; }
        public long getSoLuongVe() { return soLuongVe; }
        public BigDecimal getTongDoanhThu() { return tongDoanhThu; }
    }

    public List<DoanhThuThang> thongKeDoanhThuTheoThang(LocalDate tuNgay, LocalDate denNgay) {
        List<DoanhThuNgayDto> theoNgay = thongKeDoanhThuTheoNgay(tuNgay, denNgay);
        java.util.Map<String, long[]> dem = new java.util.LinkedHashMap<>();
        java.util.Map<String, BigDecimal> tien = new java.util.LinkedHashMap<>();
        java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern("MM/yyyy");
        for (DoanhThuNgayDto d : theoNgay) {
            if (d.getNgay() == null) continue;
            String key = d.getNgay().format(fmt);
            long[] arr = dem.computeIfAbsent(key, k -> new long[]{0L, 0L});
            arr[0] += d.getSoLuongDon() != null ? d.getSoLuongDon() : 0L;
            arr[1] += d.getSoLuongVe() != null ? d.getSoLuongVe() : 0L;
            BigDecimal dt = d.getTongDoanhThu() != null ? d.getTongDoanhThu() : BigDecimal.ZERO;
            tien.put(key, tien.getOrDefault(key, BigDecimal.ZERO).add(dt));
        }
        List<DoanhThuThang> kq = new java.util.ArrayList<>();
        for (java.util.Map.Entry<String, long[]> e : dem.entrySet()) {
            String k = e.getKey();
            kq.add(new DoanhThuThang(k, e.getValue()[0], e.getValue()[1], tien.get(k)));
        }
        return kq;
    }
    public List<DoanhThuNgayDto> thongKeDoanhThuTheoNgay(LocalDate tuNgay, LocalDate denNgay) {
        if (tuNgay != null && denNgay != null && tuNgay.isAfter(denNgay)) {
            return Collections.emptyList();
        }
        LocalDateTime tuLuc = (tuNgay != null) ? tuNgay.atStartOfDay() : null;
        LocalDateTime denLuc = (denNgay != null) ? denNgay.atTime(LocalTime.MAX) : null;
        List<DoanhThuNgayDto> ketQua = veRepo.thongKeDoanhThuTheoNgay(tuLuc, denLuc);
        return ketQua != null ? ketQua : Collections.emptyList();
    }
    public List<DoanhThuNgayDto> layDoanhThuTheoNgay(LocalDate tuNgay, LocalDate denNgay) {
        return thongKeDoanhThuTheoNgay(tuNgay, denNgay);
    }
    public List<DoanhThuPhimDto> thongKeDoanhThuTheoPhim(LocalDate tuNgay, LocalDate denNgay) {
        if (tuNgay != null && denNgay != null && tuNgay.isAfter(denNgay)) {
            return Collections.emptyList();
        }
        LocalDateTime tuLuc = (tuNgay != null) ? tuNgay.atStartOfDay() : null;
        LocalDateTime denLuc = (denNgay != null) ? denNgay.atTime(LocalTime.MAX) : null;
        List<DoanhThuPhimDto> ketQua = veRepo.thongKeDoanhThuTheoPhim(tuLuc, denLuc);
        return ketQua != null ? ketQua : Collections.emptyList();
    }

    public List<DoanhThuPhimDto> layDoanhThuTheoPhim(LocalDate tuNgay, LocalDate denNgay) {
        return thongKeDoanhThuTheoPhim(tuNgay, denNgay);
    }
    public BigDecimal tinhTongDoanhThu(LocalDate tuNgay, LocalDate denNgay) {
        if (tuNgay != null && denNgay != null && tuNgay.isAfter(denNgay)) {
            return BigDecimal.ZERO;
        }
        LocalDateTime tuLuc = (tuNgay != null) ? tuNgay.atStartOfDay() : null;
        LocalDateTime denLuc = (denNgay != null) ? denNgay.atTime(LocalTime.MAX) : null;
        BigDecimal tong = veRepo.tinhTongDoanhThu(tuLuc, denLuc);
        return tong != null ? tong : BigDecimal.ZERO;
    }
    public long tinhTongVeDaBan(LocalDate tuNgay, LocalDate denNgay) {
        if (tuNgay != null && denNgay != null && tuNgay.isAfter(denNgay)) {
            return 0L;
        }
        LocalDateTime tuLuc = (tuNgay != null) ? tuNgay.atStartOfDay() : null;
        LocalDateTime denLuc = (denNgay != null) ? denNgay.atTime(LocalTime.MAX) : null;
        Long tong = veRepo.tinhTongSoVe(tuLuc, denLuc);
        return tong != null ? tong : 0L;
    }
    public long tinhTongSoVe(LocalDate tuNgay, LocalDate denNgay) {
        return tinhTongVeDaBan(tuNgay, denNgay);
    }

    public long tinhTongDonHang(LocalDate tuNgay, LocalDate denNgay) {
        if (tuNgay != null && denNgay != null && tuNgay.isAfter(denNgay)) {
            return 0L;
        }
        LocalDateTime tuLuc = (tuNgay != null) ? tuNgay.atStartOfDay() : null;
        LocalDateTime denLuc = (denNgay != null) ? denNgay.atTime(LocalTime.MAX) : null;
        Long tong = donDatRepo.tinhTongSoDon(tuLuc, denLuc);
        return tong != null ? tong : 0L;
    }
    public long tinhTongSoDon(LocalDate tuNgay, LocalDate denNgay) {
        return tinhTongDonHang(tuNgay, denNgay);
    }
    public TongQuanThongKe layTongQuan(LocalDate tuNgay, LocalDate denNgay) {
        BigDecimal doanhThu = tinhTongDoanhThu(tuNgay, denNgay);
        long soVe = tinhTongVeDaBan(tuNgay, denNgay);
        long soDon = tinhTongDonHang(tuNgay, denNgay);
        return new TongQuanThongKe(doanhThu, soVe, soDon);
    }
}
