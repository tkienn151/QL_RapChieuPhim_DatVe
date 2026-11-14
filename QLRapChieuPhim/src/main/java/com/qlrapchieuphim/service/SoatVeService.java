package com.qlrapchieuphim.service;

import com.qlrapchieuphim.entity.Ve;
import com.qlrapchieuphim.entity.enums.TrangThaiDon;
import com.qlrapchieuphim.entity.enums.TrangThaiSuat;
import com.qlrapchieuphim.entity.enums.TrangThaiVe;
import com.qlrapchieuphim.repository.VeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class SoatVeService {
    private static final Logger log = LoggerFactory.getLogger(SoatVeService.class);

    @Autowired
    private VeRepository veRepository;

    @Transactional
    public String soatVe(String maVe) {
        String tenLuong = Thread.currentThread().getName();
        if (maVe == null || maVe.isBlank()) {
            return "ERR|KHONG_TON_TAI";
        }
        Optional<Ve> optVe = veRepository.findByMaVeForUpdate(maVe.trim());
        if (optVe.isEmpty()) {
            log.info("[SoatVe - Luong: {}] Ma ve {}: KHONG_TON_TAI", tenLuong, maVe);
            return "ERR|KHONG_TON_TAI";
        }

        Ve ve = optVe.get();

        if (ve.getTrangThai() == TrangThaiVe.DA_SOAT) {
            log.info("[SoatVe - Luong: {}] Ma ve {}: DA_SU_DUNG", tenLuong, maVe);
            return "ERR|DA_SU_DUNG";
        }
        if (ve.getTrangThai() == TrangThaiVe.DA_HUY
                || (ve.getDonDat() != null && (ve.getDonDat().getTrangThai() == TrangThaiDon.DA_HUY
                || ve.getDonDat().getTrangThai() == TrangThaiDon.HET_HAN))) {
            log.info("[SoatVe - Luong: {}] Ma ve {}: DA_HUY", tenLuong, maVe);
            return "ERR|DA_HUY";
        }
        if (ve.getDonDat() == null || ve.getDonDat().getTrangThai() != TrangThaiDon.DA_THANH_TOAN) {
            log.info("[SoatVe - Luong: {}] Ma ve {}: CHUA_THANH_TOAN", tenLuong, maVe);
            return "ERR|CHUA_THANH_TOAN";
        }

        LocalDateTime bayGio = LocalDateTime.now();
        LocalDateTime gioBatDau = ve.getSuatChieu().getGioBatDau();
        LocalDateTime gioKetThuc = ve.getSuatChieu().getGioKetThuc();

        if (ve.getSuatChieu().getTrangThai() != TrangThaiSuat.MO_BAN ||
                bayGio.isBefore(gioBatDau.minusMinutes(60)) ||
                bayGio.isAfter(gioKetThuc)) {
            log.info("[SoatVe - Luong: {}] Ma ve {}: SAI_SUAT", tenLuong, maVe);
            return "ERR|SAI_SUAT";
        }

        ve.danhDauDaSoat();
        veRepository.saveAndFlush(ve);

        String tenPhim = ve.getSuatChieu().getPhim().getTenPhim();
        String phong = ve.getSuatChieu().getPhongChieu().getTenPhong();
        String ghe = ve.getGhe().nhanDang();
        log.info("[SoatVe - Luong: {}] Ma ve {} -> THANH CONG (DA_SOAT, phong {}, ghe {})", tenLuong, maVe, phong, ghe);
        return "OK|DA_SOAT|" + tenPhim + "|" + phong + "|" + ghe;
    }

    @Transactional(readOnly = true)
    public String thongTinVe(String maVe) {
        if (maVe == null || maVe.isBlank()) {
            return "ERR|KHONG_TON_TAI";
        }
        Optional<Ve> optVe = veRepository.findChiTietByMaVe(maVe.trim());
        if (optVe.isEmpty()) {
            return "ERR|KHONG_TON_TAI";
        }
        Ve ve = optVe.get();
        return "OK|INFO|" + ve.getSuatChieu().getPhim().getTenPhim()
                + "|" + ve.getSuatChieu().getPhongChieu().getTenPhong()
                + "|" + ve.getGhe().nhanDang()
                + "|" + ve.getSuatChieu().getGioBatDau().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy"))
                + "|" + ve.getTrangThai();
    }
}
