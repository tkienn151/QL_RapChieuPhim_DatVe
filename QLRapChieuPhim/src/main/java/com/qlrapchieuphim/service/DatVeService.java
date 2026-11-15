package com.qlrapchieuphim.service;

import com.qlrapchieuphim.entity.*;
import com.qlrapchieuphim.entity.enums.*;
import com.qlrapchieuphim.repository.*;
import com.qlrapchieuphim.entity.*;
import com.qlrapchieuphim.entity.enums.KenhDat;
import com.qlrapchieuphim.entity.enums.TrangThaiDon;
import com.qlrapchieuphim.entity.enums.TrangThaiVe;
import com.qlrapchieuphim.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;

@Service
public class DatVeService {
    private static final Logger log = LoggerFactory.getLogger(DatVeService.class);
    private final SuatChieuRepository suatChieuRepo;
    private final GheRepository gheRepo;
    private final DonDatRepository donDatRepo;
    private final VeRepository veRepo;
    private final NguoiDungRepository nguoiDungRepo;

    public DatVeService(SuatChieuRepository suatChieuRepo, GheRepository gheRepo, DonDatRepository donDatRepo, VeRepository veRepo, NguoiDungRepository nguoiDungRepo) {
        this.suatChieuRepo = suatChieuRepo;
        this.gheRepo = gheRepo;
        this.donDatRepo = donDatRepo;
        this.veRepo = veRepo;
        this.nguoiDungRepo = nguoiDungRepo;
    }

    @Transactional
    public DonDat giuGhe(Long suatChieuId, List<Long> dsGheId, Long nguoiDungId) {
        String tenLuong = Thread.currentThread().getName();
        if (dsGheId == null || dsGheId.isEmpty()) {
            throw new IllegalArgumentException("Vui lòng chọn ít nhất 1 ghế.");
        }
        SuatChieu suatChieu = suatChieuRepo.findByIdForUpdate(suatChieuId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy suất chiếu"));

        if (!suatChieu.conMoBan()) {
            throw new IllegalArgumentException("Suất chiếu đã đóng hoặc bị hủy.");
        }
        if (!suatChieu.getGioBatDau().isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("Suất chiếu đã bắt đầu, không thể đặt vé online.");
        }

        donGheQuaHanTheoSuat(suatChieuId);

        NguoiDung nguoiDung = nguoiDungRepo.findById(nguoiDungId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản người dùng"));
        List<Long> dsGheDaDat = veRepo.findGheDaDat(suatChieuId);

        DonDat donDat = new DonDat();
        donDat.setMaDon("WEB" + (System.currentTimeMillis() % 1000000000L) + UUID.randomUUID().toString().substring(0, 4).toUpperCase());
        donDat.setNguoiDung(nguoiDung);
        donDat.setHoTenKhach(nguoiDung.getHoTen());
        donDat.setSdtKhach(nguoiDung.getSoDienThoai());
        donDat.setNgayDat(LocalDateTime.now());
        donDat.setHanThanhToan(LocalDateTime.now().plusMinutes(5));
        donDat.setTrangThai(TrangThaiDon.CHO_THANH_TOAN);
        donDat.setKenhDat(KenhDat.WEB);
        donDat.setTongTien(BigDecimal.ZERO);

        for (Long gheId : new LinkedHashSet<>(dsGheId)) {
            Ghe ghe = gheRepo.findById(gheId)
                    .orElseThrow(() -> new IllegalArgumentException("Ghế không tồn tại"));
            if (!ghe.getPhongChieu().getId().equals(suatChieu.getPhongChieu().getId())) {
                throw new IllegalArgumentException("Ghế " + ghe.nhanDang() + " không thuộc phòng chiếu của suất này.");
            }
            if (dsGheDaDat.contains(gheId)) {
                log.info("[Luong: {}] Tu choi giu ghe {} cua suat #{}: ghe da co nguoi dat truoc",
                        tenLuong, ghe.nhanDang(), suatChieuId);
                throw new IllegalArgumentException("Ghế " + ghe.nhanDang() + " đã có người nhanh tay đặt trước. Vui lòng chọn ghế khác.");
            }
            Ve ve = new Ve();
            ve.setMaVe("VE" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase());
            ve.setSuatChieu(suatChieu);
            ve.setGhe(ghe);
            ve.setGiaVe(suatChieu.layGia(ghe.getLoaiGhe()));
            ve.setTrangThai(TrangThaiVe.HOP_LE);
            donDat.themVe(ve);
        }
        donDat.setTongTien(donDat.tinhTongTien());
        DonDat daLuu = donDatRepo.saveAndFlush(donDat);
        log.info("[Luong: {}] Giu ghe THANH CONG: maDon={}, suatId={}, soGhe={}",
                tenLuong, daLuu.getMaDon(), suatChieuId, daLuu.getDsVe().size());
        return daLuu;
    }

    @Transactional(noRollbackFor = IllegalArgumentException.class)
    public void thanhToan(String maDon) {
        DonDat donDat = donDatRepo.findByMaDonForUpdate(maDon).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn đặt"));
        if (donDat.getTrangThai() != TrangThaiDon.CHO_THANH_TOAN) {
            throw new IllegalArgumentException("Đơn không ở trạng thái chờ thanh toán");
        }
        if (donDat.daHetHan(LocalDateTime.now())) {
            huyGiuGhe(donDat, TrangThaiDon.HET_HAN);
            throw new IllegalArgumentException("Đơn đặt đã quá hạn 5 phút và bị huỷ ghế.");
        }
        donDat.danhDauDaThanhToan();
        donDatRepo.save(donDat);
    }

    @Transactional
    public void huyGiuGhe(DonDat donDat, TrangThaiDon trangThaiMoi) {
        donDat.setTrangThai(trangThaiMoi);
        for (Ve ve : donDat.getDsVe()) {
            ve.huy();
        }
        donDatRepo.save(donDat);
    }

    @Transactional(noRollbackFor = IllegalArgumentException.class)
    public Long huyDonGiuGhe(String maDon, Long nguoiDungId) {
        return huyDonGiuGhe(maDon, nguoiDungId, false);
    }

    @Transactional(noRollbackFor = IllegalArgumentException.class)
    public Long huyDonGiuGhe(String maDon, Long nguoiDungId, boolean laNhanVienHoacQuanTri) {
        String ma = (maDon == null) ? "" : maDon.trim();
        DonDat donDat = donDatRepo.findByMaDonForUpdate(ma)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn đặt"));
        if (!laNhanVienHoacQuanTri
                && (donDat.getNguoiDung() == null || nguoiDungId == null || !donDat.getNguoiDung().getId().equals(nguoiDungId))) {
            throw new IllegalArgumentException("Bạn không có quyền thao tác đơn hàng này.");
        }
        if (donDat.getTrangThai() != TrangThaiDon.CHO_THANH_TOAN) {
            throw new IllegalArgumentException("Đơn không ở trạng thái chờ thanh toán.");
        }
        Long suatId = donDat.getDsVe().isEmpty() ? null : donDat.getDsVe().get(0).getSuatChieu().getId();
        if (donDat.daHetHan(LocalDateTime.now())) {
            huyGiuGhe(donDat, TrangThaiDon.HET_HAN);
            throw new IllegalArgumentException("Đơn đặt đã quá hạn 5 phút và được tự động giải phóng ghế.");
        }
        huyGiuGhe(donDat, TrangThaiDon.DA_HUY);
        return suatId;
    }

    @Transactional
    public void donTatCaGheQuaHan() {
        LocalDateTime bayGio = LocalDateTime.now();
        List<DonDat> dsQuaHan = donDatRepo.findByTrangThaiAndHanThanhToanBefore(
                TrangThaiDon.CHO_THANH_TOAN, bayGio);
        if (dsQuaHan.isEmpty()) {
            return;
        }
        dsQuaHan.sort(java.util.Comparator.comparing(DonDat::getId));
        for (DonDat d : dsQuaHan) {
            thuNhaGheDonQuaHan(d.getId(), bayGio);
        }
        donDatRepo.flush();
    }

    @Transactional
    public void donGheQuaHanTheoSuat(Long suatChieuId) {
        if (suatChieuId == null) {
            return;
        }
        LocalDateTime bayGio = LocalDateTime.now();
        List<DonDat> dsQuaHan = donDatRepo.findQuaHanTheoSuat(suatChieuId, bayGio);
        if (dsQuaHan.isEmpty()) {
            return;
        }
        for (DonDat d : dsQuaHan) {
            thuNhaGheDonQuaHan(d.getId(), bayGio);
        }
        donDatRepo.flush();
    }

    @Transactional
    public boolean nhaGheDonQuaHan(Long donDatId) {
        return thuNhaGheDonQuaHan(donDatId, LocalDateTime.now());
    }

    private boolean thuNhaGheDonQuaHan(Long donDatId, LocalDateTime bayGio) {
        DonDat donDat = donDatRepo.findByIdForUpdate(donDatId).orElse(null);
        if (donDat == null
                || donDat.getTrangThai() != TrangThaiDon.CHO_THANH_TOAN
                || !donDat.daHetHan(bayGio)) {
            return false;
        }
        huyGiuGhe(donDat, TrangThaiDon.HET_HAN);
        return true;
    }

    @Transactional
    public SuatChieu chuanBiChonGhe(Long suatId) {
        SuatChieu suatChieu = suatChieuRepo.findByIdWithDetails(suatId)
                .or(() -> suatChieuRepo.findById(suatId))
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy suất chiếu"));
        if (!suatChieu.conMoBan() || !suatChieu.getGioBatDau().isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("Suất chiếu đã đóng hoặc đã bắt đầu.");
        }
        donGheQuaHanTheoSuat(suatId);
        return suatChieu;
    }

    @Transactional(readOnly = true)
    public List<Ghe> layDanhSachGheTheoPhong(Long phongChieuId) {
        return gheRepo.findByPhongChieuIdOrderByHangAscSoGheAsc(phongChieuId);
    }

    @Transactional(readOnly = true)
    public List<Long> layDanhSachGheDaDat(Long suatId) {
        return veRepo.findGheDaDat(suatId);
    }

    @Transactional(readOnly = true)
    public DonDat layDonCuaNguoiDung(String maDon, Long nguoiDungId, String thongBaoKhongCoQuyen) {
        DonDat donDat = donDatRepo.findByMaDon(maDon)
                .orElseThrow(() -> new IllegalArgumentException("Đơn không tồn tại"));
        if (donDat.getNguoiDung() == null || nguoiDungId == null || !donDat.getNguoiDung().getId().equals(nguoiDungId)) {
            throw new IllegalArgumentException(thongBaoKhongCoQuyen);
        }
        return donDat;
    }

    @Transactional(readOnly = true)
    public boolean suatConMoBan(Long suatId) {
        if (suatId == null) {
            return false;
        }
        return suatChieuRepo.findById(suatId)
                .map(sc -> sc.conMoBan() && sc.getGioBatDau().isAfter(LocalDateTime.now()))
                .orElse(false);
    }
}
