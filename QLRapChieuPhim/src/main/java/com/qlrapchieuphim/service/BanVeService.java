package com.qlrapchieuphim.service;

import com.qlrapchieuphim.entity.*;
import com.qlrapchieuphim.entity.enums.*;
import com.qlrapchieuphim.repository.*;
import com.qlrapchieuphim.entity.*;
import com.qlrapchieuphim.entity.enums.KenhDat;
import com.qlrapchieuphim.entity.enums.TrangThaiDon;
import com.qlrapchieuphim.entity.enums.TrangThaiVe;
import com.qlrapchieuphim.repository.*;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class BanVeService {
    private final SuatChieuRepository suatChieuRepo;
    private final GheRepository gheRepo;
    private final DonDatRepository donDatRepo;
    private final VeRepository veRepo;
    private final NguoiDungRepository nguoiDungRepo;
    private final DatVeService datVeService;

    public BanVeService(SuatChieuRepository suatChieuRepo, GheRepository gheRepo, DonDatRepository donDatRepo,
                        VeRepository veRepo, NguoiDungRepository nguoiDungRepo, DatVeService datVeService) {
        this.suatChieuRepo = suatChieuRepo;
        this.gheRepo = gheRepo;
        this.donDatRepo = donDatRepo;
        this.veRepo = veRepo;
        this.nguoiDungRepo = nguoiDungRepo;
        this.datVeService = datVeService;
    }

    @Transactional
    public DonDat banVeTaiQuay(Long suatChieuId, List<Long> dsGheId, Long nhanVienId, String hoTenKhach, String sdtKhach) {
        if (dsGheId == null || dsGheId.isEmpty()) throw new IllegalArgumentException("Vui lòng chọn ít nhất 1 ghế.");
        String tenKhach = null;
        if (hoTenKhach != null && !hoTenKhach.isBlank()) {
            tenKhach = hoTenKhach.trim();
            if (tenKhach.length() > 100) {
                throw new IllegalArgumentException("Tên khách hàng tối đa 100 ký tự.");
            }
        }
        String sdt = null;
        if (sdtKhach != null && !sdtKhach.isBlank()) {
            sdt = sdtKhach.trim().replaceAll("[\\s.-]+", "");
            if (sdt.length() > 15 || !NguoiDungService.soDienThoaiHopLe(sdt)) {
                throw new IllegalArgumentException("Số điện thoại khách hàng không đúng định dạng (10 số, bắt đầu bằng 0 hoặc +84).");
            }
        }

        SuatChieu suatChieu = suatChieuRepo.findByIdForUpdate(suatChieuId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy suất chiếu"));

        if (!suatChieu.conMoBan()) throw new IllegalArgumentException("Suất chiếu đã đóng.");
        if (!suatChieu.getGioKetThuc().isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("Suất chiếu đã kết thúc, không thể bán vé.");
        }

        NguoiDung nhanVien = nguoiDungRepo.findById(nhanVienId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản nhân viên"));

        datVeService.donGheQuaHanTheoSuat(suatChieuId);

        List<Long> dsGheDaDat = veRepo.findGheDaDat(suatChieuId);

        DonDat donDat = new DonDat();
        donDat.setMaDon("QUAY" + (System.currentTimeMillis() % 1000000000L) + UUID.randomUUID().toString().substring(0, 4).toUpperCase());
        donDat.setNguoiDung(nhanVien);
        if (tenKhach != null) donDat.setHoTenKhach(tenKhach);
        if (sdt != null) donDat.setSdtKhach(sdt);
        donDat.setNgayDat(LocalDateTime.now());
        donDat.setHanThanhToan(null);
        donDat.setTrangThai(TrangThaiDon.DA_THANH_TOAN);
        donDat.setKenhDat(KenhDat.QUAY);
        donDat.setTongTien(BigDecimal.ZERO);

        for (Long gheId : new LinkedHashSet<>(dsGheId)) {
            Ghe ghe = gheRepo.findById(gheId)
                    .orElseThrow(() -> new IllegalArgumentException("Ghế không tồn tại"));
            if (!ghe.getPhongChieu().getId().equals(suatChieu.getPhongChieu().getId())) {
                throw new IllegalArgumentException("Ghế " + ghe.nhanDang() + " không thuộc phòng chiếu của suất này.");
            }
            if (dsGheDaDat.contains(gheId)) {
                throw new IllegalArgumentException("Ghế " + ghe.nhanDang() + " đã bị mua lúc bạn đang thao tác. Vui lòng thử lại.");
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
        return donDatRepo.saveAndFlush(donDat);
    }

    @Transactional(readOnly = true)
    public List<SuatChieu> layDanhSachSuatMoBanTaiQuay() {
        List<SuatChieu> dsSuat = new ArrayList<>(suatChieuRepo.findAll(Sort.by(Sort.Direction.ASC, "gioBatDau")));
        LocalDateTime bayGio = LocalDateTime.now();
        dsSuat.removeIf(s -> !s.conMoBan() || !s.getGioKetThuc().isAfter(bayGio));
        for (SuatChieu s : dsSuat) {
            if (s.getPhim() != null) s.getPhim().getTenPhim();
            if (s.getPhongChieu() != null) s.getPhongChieu().getTenPhong();
        }
        return dsSuat;
    }

    @Transactional
    public Optional<SuatChieu> chuanBiBanVeChoSuat(Long suatId) {
        if (suatId == null) {
            return Optional.empty();
        }
        LocalDateTime bayGio = LocalDateTime.now();
        Optional<SuatChieu> opt = suatChieuRepo.findByIdWithDetails(suatId)
                .or(() -> suatChieuRepo.findById(suatId))
                .filter(s -> s.conMoBan() && s.getGioKetThuc().isAfter(bayGio));
        opt.ifPresent(s -> datVeService.donGheQuaHanTheoSuat(suatId));
        return opt;
    }

    @Transactional(readOnly = true)
    public List<Ghe> layDanhSachGheTheoPhong(Long phongChieuId) {
        return datVeService.layDanhSachGheTheoPhong(phongChieuId);
    }

    @Transactional(readOnly = true)
    public List<Long> layDanhSachGheDaDat(Long suatId) {
        return datVeService.layDanhSachGheDaDat(suatId);
    }

    @Transactional(readOnly = true)
    public DonDat layDonTheoMa(String maDon) {
        return donDatRepo.findByMaDon(maDon)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn đặt vé"));
    }
}
