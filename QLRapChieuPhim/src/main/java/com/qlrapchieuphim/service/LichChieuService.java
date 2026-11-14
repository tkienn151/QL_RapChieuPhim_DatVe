package com.qlrapchieuphim.service;

import com.qlrapchieuphim.entity.SuatChieu;
import com.qlrapchieuphim.entity.enums.TrangThaiSuat;
import com.qlrapchieuphim.repository.SuatChieuRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class LichChieuService {

    private static final String[] THU = {"Thứ Hai", "Thứ Ba", "Thứ Tư", "Thứ Năm", "Thứ Sáu", "Thứ Bảy", "Chủ Nhật"};

    private final SuatChieuRepository suatChieuRepo;

    public LichChieuService(SuatChieuRepository suatChieuRepo) {
        this.suatChieuRepo = suatChieuRepo;
    }

    public static class NgayChieu {
        private final LocalDate ngay;
        private final String nhan;
        private final List<SuatChieu> dsSuat;

        public NgayChieu(LocalDate ngay, String nhan, List<SuatChieu> dsSuat) {
            this.ngay = ngay;
            this.nhan = nhan;
            this.dsSuat = dsSuat;
        }
        public LocalDate getNgay() { return ngay; }
        public String getNhan() { return nhan; }
        public List<SuatChieu> getDsSuat() { return dsSuat; }
    }

    @Transactional(readOnly = true)
    public List<NgayChieu> layLichChieu(Long phimId) {
        LocalDateTime bayGio = LocalDateTime.now();
        List<SuatChieu> dsSuat = suatChieuRepo.timSuatSapChieuCuaPhim(phimId, TrangThaiSuat.MO_BAN, bayGio);

        Map<LocalDate, List<SuatChieu>> theoNgay = new LinkedHashMap<>();
        for (SuatChieu s : dsSuat) {
            theoNgay.computeIfAbsent(s.getGioBatDau().toLocalDate(), k -> new ArrayList<>()).add(s);
        }

        LocalDate homNay = bayGio.toLocalDate();
        List<NgayChieu> ketQua = new ArrayList<>();
        for (Map.Entry<LocalDate, List<SuatChieu>> e : theoNgay.entrySet()) {
            ketQua.add(new NgayChieu(e.getKey(), nhanNgay(e.getKey(), homNay), e.getValue()));
        }
        return ketQua;
    }

    private String nhanNgay(LocalDate ngay, LocalDate homNay) {
        String ddMM = String.format("%02d/%02d", ngay.getDayOfMonth(), ngay.getMonthValue());
        if (ngay.equals(homNay)) return "Hôm nay, " + ddMM;
        if (ngay.equals(homNay.plusDays(1))) return "Ngày mai, " + ddMM;
        return THU[ngay.getDayOfWeek().getValue() - 1] + ", " + ddMM;
    }
}
