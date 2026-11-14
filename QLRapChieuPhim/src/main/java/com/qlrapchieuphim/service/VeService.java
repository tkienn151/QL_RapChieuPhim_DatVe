package com.qlrapchieuphim.service;

import com.qlrapchieuphim.entity.DonDat;
import com.qlrapchieuphim.entity.Ve;
import com.qlrapchieuphim.entity.enums.TrangThaiDon;
import com.qlrapchieuphim.entity.enums.TrangThaiVe;
import com.qlrapchieuphim.entity.enums.VaiTro;
import com.qlrapchieuphim.repository.DonDatRepository;
import com.qlrapchieuphim.repository.NguoiDungRepository;
import com.qlrapchieuphim.repository.VeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class VeService {
    private final DonDatRepository donDatRepo;
    private final VeRepository veRepo;
    private final NguoiDungRepository nguoiDungRepo;
    public VeService(DonDatRepository donDatRepo, VeRepository veRepo, NguoiDungRepository nguoiDungRepo) {
        this.donDatRepo = donDatRepo;
        this.veRepo = veRepo;
        this.nguoiDungRepo = nguoiDungRepo;
    }
    @Transactional(readOnly = true)
    public Page<DonDat> layLichSuDatVe(Long nguoiDungId, Pageable pageable) {
        if (nguoiDungId == null) {
            throw new IllegalArgumentException("Mã người dùng không được để trống");
        }
        return donDatRepo.findByNguoiDungIdOrderByNgayDatDesc(nguoiDungId, pageable);
    }
    @Transactional(readOnly = true)
    public Page<DonDat> layLichSuDatVeTheoTrangThai(Long nguoiDungId, TrangThaiDon trangThai, Pageable pageable) {
        if (nguoiDungId == null) {
            throw new IllegalArgumentException("Mã người dùng không được để trống");
        }
        if (trangThai == null) {
            return donDatRepo.findByNguoiDungIdOrderByNgayDatDesc(nguoiDungId, pageable);
        }
        return donDatRepo.findByNguoiDungIdAndTrangThaiOrderByNgayDatDesc(nguoiDungId, trangThai, pageable);
    }
    @Transactional(readOnly = true)
    public DonDat layChiTietDonDat(Long donDatId, Long nguoiDungId) {
        if (donDatId == null) {
            throw new IllegalArgumentException("Mã đơn đặt vé không được để trống");
        }
        DonDat don = donDatRepo.findByIdWithChiTiet(donDatId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn đặt vé với ID: " + donDatId));
        kiemTraQuyenXemDon(don, nguoiDungId);
        return don;
    }
    @Transactional(readOnly = true)
    public DonDat layChiTietDonDat(Long donDatId) {
        if (donDatId == null) {
            throw new IllegalArgumentException("Mã đơn đặt vé không được để trống");
        }
        return donDatRepo.findByIdWithChiTiet(donDatId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn đặt vé với ID: " + donDatId));
    }
    @Transactional(readOnly = true)
    public DonDat layChiTietDon(String maDon, Long nguoiDungId) {
        if (maDon == null || maDon.isBlank()) {
            throw new IllegalArgumentException("Mã đơn đặt vé không được để trống");
        }
        DonDat don = donDatRepo.findByMaDonWithChiTiet(maDon)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn đặt vé: " + maDon));
        kiemTraQuyenXemDon(don, nguoiDungId);
        return don;
    }
    @Transactional(readOnly = true)
    public DonDat layChiTietDon(String maDon) {
        if (maDon == null || maDon.isBlank()) {
            throw new IllegalArgumentException("Mã đơn đặt vé không được để trống");
        }
        return donDatRepo.findByMaDonWithChiTiet(maDon)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn đặt vé: " + maDon));
    }
    public boolean coTheHuy(DonDat don) {
        return coTheHuy(don, LocalDateTime.now());
    }
    public boolean coTheHuy(DonDat don, LocalDateTime thoiDiemHuy) {
        if (don == null || thoiDiemHuy == null) {
            return false;
        }
        if (don.getTrangThai() == TrangThaiDon.DA_HUY || don.getTrangThai() == TrangThaiDon.HET_HAN) {
            return false;
        }
        if (don.daHetHan(thoiDiemHuy)) {
            return false;
        }
        if (don.getDsVe() == null || don.getDsVe().isEmpty()) {
            return false;
        }
        boolean coVeDaSoat = don.getDsVe().stream()
                .anyMatch(v -> v.getTrangThai() == TrangThaiVe.DA_SOAT);
        if (coVeDaSoat) {
            return false;
        }
        LocalDateTime gioChieu = layGioChieuSomNhat(don);
        if (gioChieu == null) {
            return false;
        }
        LocalDateTime hanHuy = gioChieu.minusMinutes(60);
        return !thoiDiemHuy.isAfter(hanHuy);
    }
    @Transactional(noRollbackFor = IllegalStateException.class)
    public void huyDonDat(Long donDatId, Long nguoiDungId) {
        if (donDatId == null) {
            throw new IllegalArgumentException("Đơn đặt vé không tồn tại");
        }
        donDatRepo.findByIdForUpdate(donDatId)
                .orElseThrow(() -> new IllegalArgumentException("Đơn đặt vé không tồn tại"));
        DonDat don = donDatRepo.findByIdWithChiTiet(donDatId)
                .orElseThrow(() -> new IllegalArgumentException("Đơn đặt vé không tồn tại"));
        boolean laAdmin = laNhanVienHoacAdmin(nguoiDungId);
        thucHienHuy(don, nguoiDungId, laAdmin, LocalDateTime.now());
    }
    @Transactional(noRollbackFor = IllegalStateException.class)
    public void huyDonDat(Long donDatId, Long nguoiDungId, boolean laAdmin) {
        if (donDatId == null) {
            throw new IllegalArgumentException("Đơn đặt vé không tồn tại");
        }
        donDatRepo.findByIdForUpdate(donDatId)
                .orElseThrow(() -> new IllegalArgumentException("Đơn đặt vé không tồn tại"));
        DonDat don = donDatRepo.findByIdWithChiTiet(donDatId)
                .orElseThrow(() -> new IllegalArgumentException("Đơn đặt vé không tồn tại"));
        thucHienHuy(don, nguoiDungId, laAdmin, LocalDateTime.now());
    }
    @Transactional(noRollbackFor = IllegalStateException.class)
    public void huyDon(String maDon, Long nguoiDungId, boolean laNhanVienHoacQuanTri) {
        huyDon(maDon, nguoiDungId, laNhanVienHoacQuanTri, LocalDateTime.now());
    }
    @Transactional(noRollbackFor = IllegalStateException.class)
    public void huyDon(String maDon, Long nguoiDungId, boolean laNhanVienHoacQuanTri, LocalDateTime thoiDiemHuy) {
        if (maDon == null || maDon.isBlank()) {
            throw new IllegalArgumentException("Đơn đặt vé không tồn tại");
        }
        String ma = maDon.trim();
        donDatRepo.findByMaDonForUpdate(ma)
                .orElseThrow(() -> new IllegalArgumentException("Đơn đặt vé không tồn tại"));
        DonDat don = donDatRepo.findByMaDonWithChiTiet(ma)
                .orElseThrow(() -> new IllegalArgumentException("Đơn đặt vé không tồn tại"));
        thucHienHuy(don, nguoiDungId, laNhanVienHoacQuanTri, thoiDiemHuy);
    }
    private void thucHienHuy(DonDat don, Long nguoiDungId, boolean laAdmin, LocalDateTime thoiDiemHuy) {
        if (don == null) {
            throw new IllegalArgumentException("Đơn đặt vé không tồn tại");
        }
        if (!laAdmin && (don.getNguoiDung() == null || !don.getNguoiDung().getId().equals(nguoiDungId))) {
            throw new IllegalArgumentException("Bạn không có quyền thao tác trên đơn hàng này");
        }
        if (don.getTrangThai() == TrangThaiDon.DA_HUY) {
            throw new IllegalStateException("Đơn đặt vé này đã được hủy trước đó");
        }
        if (don.getTrangThai() == TrangThaiDon.HET_HAN) {
            throw new IllegalStateException("Đơn đặt vé này đã hết hạn");
        }
        if (don.daHetHan(thoiDiemHuy)) {
            don.setTrangThai(TrangThaiDon.HET_HAN);
            if (don.getDsVe() != null) {
                for (Ve v : don.getDsVe()) {
                    v.huy();
                }
                veRepo.saveAll(don.getDsVe());
            }
            donDatRepo.save(don);
            throw new IllegalStateException("Đơn đặt vé này đã hết hạn giữ ghế 5 phút");
        }
        if (don.getDsVe() == null || don.getDsVe().isEmpty()) {
            throw new IllegalStateException("Đơn hàng không chứa vé hợp lệ để hủy");
        }
        boolean coVeDaSoat = don.getDsVe().stream()
                .anyMatch(v -> v.getTrangThai() == TrangThaiVe.DA_SOAT);
        if (coVeDaSoat) {
            throw new IllegalStateException("Không thể hủy đơn vé vì đã có vé được soát vào phòng chiếu");
        }
        LocalDateTime gioChieu = layGioChieuSomNhat(don);
        if (gioChieu == null) {
            throw new IllegalStateException("Thông tin suất chiếu của đơn hàng không hợp lệ");
        }
        if (thoiDiemHuy.isAfter(gioChieu.minusMinutes(60))) {
            throw new IllegalStateException("Chỉ được hủy vé trước giờ chiếu ít nhất 60 phút");
        }
        don.huy();
        for (Ve v : don.getDsVe()) {
            v.huy();
        }
        donDatRepo.save(don);
        veRepo.saveAll(don.getDsVe());
    }
    @Transactional(readOnly = true)
    public List<DonDat> traCuuDon(String tuKhoa) {
        if (tuKhoa == null || tuKhoa.isBlank()) {
            return donDatRepo.findAll(Sort.by(Sort.Direction.DESC, "ngayDat"));
        }
        return donDatRepo.traCuuDon(tuKhoa.trim());
    }
    @Transactional(readOnly = true)
    public Page<DonDat> traCuuDon(String tuKhoa, Pageable pageable) {
        if (tuKhoa == null || tuKhoa.isBlank()) {
            return donDatRepo.findAll(pageable);
        }
        return donDatRepo.traCuuDonPhanTrang(tuKhoa.trim(), pageable);
    }
    private void kiemTraQuyenXemDon(DonDat don, Long nguoiDungId) {
        if (don == null) {
            throw new IllegalArgumentException("Đơn đặt vé không tồn tại");
        }
        if (nguoiDungId == null) {
            throw new IllegalArgumentException("Bạn không có quyền thao tác trên đơn hàng này");
        }
        boolean laChuDon = don.getNguoiDung() != null && nguoiDungId.equals(don.getNguoiDung().getId());
        if (!laChuDon && !laNhanVienHoacAdmin(nguoiDungId)) {
            throw new IllegalArgumentException("Bạn không có quyền thao tác trên đơn hàng này");
        }
    }

    private boolean laNhanVienHoacAdmin(Long nguoiDungId) {
        if (nguoiDungId == null) {
            return false;
        }
        return nguoiDungRepo.findById(nguoiDungId)
                .map(nd -> nd.getVaiTro() == VaiTro.NHAN_VIEN || nd.getVaiTro() == VaiTro.QUAN_TRI)
                .orElse(false);
    }

    private LocalDateTime layGioChieuSomNhat(DonDat don) {
        if (don == null || don.getDsVe() == null || don.getDsVe().isEmpty()) {
            return null;
        }
        return don.getDsVe().stream()
                .map(v -> v.getSuatChieu() != null ? v.getSuatChieu().getGioBatDau() : null)
                .filter(Objects::nonNull)
                .min(LocalDateTime::compareTo)
                .orElse(null);
    }
}
