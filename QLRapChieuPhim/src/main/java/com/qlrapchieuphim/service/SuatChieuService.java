package com.qlrapchieuphim.service;

import com.qlrapchieuphim.entity.Phim;
import com.qlrapchieuphim.entity.PhongChieu;
import com.qlrapchieuphim.entity.SuatChieu;
import com.qlrapchieuphim.entity.enums.TrangThaiPhim;
import com.qlrapchieuphim.entity.enums.TrangThaiSuat;
import com.qlrapchieuphim.repository.PhimRepository;
import com.qlrapchieuphim.repository.PhongChieuRepository;
import com.qlrapchieuphim.repository.SuatChieuRepository;
import com.qlrapchieuphim.repository.VeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class SuatChieuService {

    private final SuatChieuRepository suatChieuRepo;
    private final PhimRepository phimRepo;
    private final PhongChieuRepository phongChieuRepo;
    private final VeRepository veRepo;

    public SuatChieuService(SuatChieuRepository suatChieuRepo,
                            PhimRepository phimRepo,
                            PhongChieuRepository phongChieuRepo,
                            VeRepository veRepo) {
        this.suatChieuRepo = suatChieuRepo;
        this.phimRepo = phimRepo;
        this.phongChieuRepo = phongChieuRepo;
        this.veRepo = veRepo;
    }

    @Transactional
    public SuatChieu taoSuatChieu(SuatChieu suatChieu, Long phimId, Long phongChieuId) {
        if (suatChieu == null) {
            throw new IllegalArgumentException("Thông tin suất chiếu không được để trống.");
        }
        if (phimId == null) {
            throw new IllegalArgumentException("Vui lòng chọn phim.");
        }
        if (phongChieuId == null) {
            throw new IllegalArgumentException("Vui lòng chọn phòng chiếu.");
        }
        if (suatChieu.getGioBatDau() == null) {
            throw new LoiTruongException("gioBatDau","Giờ bắt đầu không được để trống.");
        }
        Phim phim = phimRepo.findById(phimId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phim với mã: " + phimId));
        if (phim.getThoiLuong() <= 0) {
            throw new IllegalArgumentException("Thời lượng phim phải lớn hơn 0 phút.");
        }
        if (phim.getTrangThai() == TrangThaiPhim.NGUNG_CHIEU) {
            throw new IllegalArgumentException("Phim đã ngừng chiếu, không thể tạo suất.");
        }
        if (suatChieu.getGioBatDau().isBefore(LocalDateTime.now())) {
            throw new LoiTruongException("gioBatDau","Giờ bắt đầu phải ở tương lai.");
        }
        PhongChieu phongChieu = phongChieuRepo.findById(phongChieuId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phòng chiếu với mã: " + phongChieuId));
        LocalDateTime gioKetThuc = suatChieu.tinhGioKetThuc(phim.getThoiLuong());
        suatChieu.setGioKetThuc(gioKetThuc);

        kiemTraGiaVe(suatChieu.getGiaThuong(), suatChieu.getGiaVip());
        kiemTraXungDot(phongChieuId, suatChieu.getGioBatDau(), gioKetThuc, null);
        if (suatChieu.getTrangThai() == null) {
            suatChieu.setTrangThai(TrangThaiSuat.MO_BAN);
        }

        suatChieu.setPhim(phim);
        suatChieu.setPhongChieu(phongChieu);
        return suatChieuRepo.save(suatChieu);
    }
    @Transactional
    public SuatChieu them(SuatChieu suatChieu, Long phimId, Long phongChieuId) {
        return taoSuatChieu(suatChieu, phimId, phongChieuId);
    }
    @Transactional
    public SuatChieu capNhatSuatChieu(Long id, SuatChieu suatMoi, Long phimId, Long phongChieuId) {
        if (id == null) {
            throw new IllegalArgumentException("Mã suất chiếu không được để trống.");
        }
        if (suatMoi == null) {
            throw new IllegalArgumentException("Thông tin suất chiếu không được để trống.");
        }
        if (phimId == null) {
            throw new IllegalArgumentException("Vui lòng chọn phim.");
        }
        if (phongChieuId == null) {
            throw new IllegalArgumentException("Vui lòng chọn phòng chiếu.");
        }
        if (suatMoi.getGioBatDau() == null) {
            throw new LoiTruongException("gioBatDau","Giờ bắt đầu không được để trống.");
        }
        SuatChieu hienTai = suatChieuRepo.findByIdForUpdate(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy suất chiếu với mã: " + id));
        if (suatMoi.getTrangThai() != null) {
            if (hienTai.getTrangThai() == TrangThaiSuat.DA_HUY && suatMoi.getTrangThai() != TrangThaiSuat.DA_HUY) {
                throw new IllegalStateException("Suất đã hủy, không thể mở lại.");
            }
            if (suatMoi.getTrangThai() == TrangThaiSuat.DA_HUY && hienTai.getTrangThai() != TrangThaiSuat.DA_HUY) {
                return huySuatChieu(id);
            }
        }
        boolean coVeHieuLuc = !veRepo.findBySuatChieuIdAndTrangThaiNot(id,
                com.qlrapchieuphim.entity.enums.TrangThaiVe.DA_HUY).isEmpty();
        if (coVeHieuLuc
                && (!hienTai.getPhim().getId().equals(phimId)
                || !hienTai.getPhongChieu().getId().equals(phongChieuId)
                || !hienTai.getGioBatDau().truncatedTo(java.time.temporal.ChronoUnit.MINUTES)
                        .equals(suatMoi.getGioBatDau().truncatedTo(java.time.temporal.ChronoUnit.MINUTES)))) {
            throw new IllegalStateException("Suất đã có vé, không thể đổi phim/phòng/giờ chiếu.");
        }
        Phim phim = phimRepo.findById(phimId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phim với mã: " + phimId));
        if (phim.getThoiLuong() <= 0) {
            throw new IllegalArgumentException("Thời lượng phim phải lớn hơn 0 phút.");
        }
        if (!hienTai.getPhim().getId().equals(phimId) && phim.getTrangThai() == TrangThaiPhim.NGUNG_CHIEU) {
            throw new IllegalArgumentException("Phim đã ngừng chiếu, không thể chọn cho suất chiếu.");
        }
        boolean gioBatDauThayDoi = !hienTai.getGioBatDau().truncatedTo(ChronoUnit.MINUTES)
                .equals(suatMoi.getGioBatDau().truncatedTo(ChronoUnit.MINUTES));
        if (gioBatDauThayDoi && suatMoi.getGioBatDau().isBefore(LocalDateTime.now())) {
            throw new LoiTruongException("gioBatDau", "Giờ bắt đầu phải ở tương lai.");
        }
        PhongChieu phongChieu = phongChieuRepo.findById(phongChieuId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phòng chiếu với mã: " + phongChieuId));
        LocalDateTime gioKetThuc = suatMoi.getGioBatDau().plusMinutes(phim.getThoiLuong());
        kiemTraGiaVe(suatMoi.getGiaThuong(), suatMoi.getGiaVip());
        kiemTraXungDot(phongChieuId, suatMoi.getGioBatDau(), gioKetThuc, id);
        hienTai.setPhim(phim);
        hienTai.setPhongChieu(phongChieu);
        hienTai.setGioBatDau(suatMoi.getGioBatDau());
        hienTai.setGioKetThuc(gioKetThuc);
        hienTai.setGiaThuong(suatMoi.getGiaThuong());
        hienTai.setGiaVip(suatMoi.getGiaVip());
        if (suatMoi.getTrangThai() != null) {
            hienTai.setTrangThai(suatMoi.getTrangThai());
        }
        return suatChieuRepo.save(hienTai);
    }
    @Transactional
    public SuatChieu capNhat(Long id, SuatChieu suatMoi, Long phimId, Long phongChieuId) {
        return capNhatSuatChieu(id, suatMoi, phimId, phongChieuId);
    }
    @Transactional
    public void xoaSuatChieu(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Mã suất chiếu không được để trống.");
        }
        SuatChieu suat = suatChieuRepo.findByIdForUpdate(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy suất chiếu với mã: " + id));
        if (veRepo.existsBySuatChieuId(id)) {
            throw new IllegalStateException("Không thể xóa suất chiếu đã có vé, vui lòng hủy suất chiếu");
        }

        suatChieuRepo.delete(suat);
    }
    @Transactional
    public void xoa(Long id) {
        xoaSuatChieu(id);
    }
    @Transactional
    public SuatChieu huySuatChieu(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Mã suất chiếu không được để trống.");
        }
        SuatChieu suat = suatChieuRepo.findByIdForUpdate(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy suất chiếu với mã: " + id));
        suat.setTrangThai(TrangThaiSuat.DA_HUY);
        java.util.Set<com.qlrapchieuphim.entity.DonDat> dsDon = new java.util.LinkedHashSet<>();
        for (com.qlrapchieuphim.entity.Ve v : veRepo.findBySuatChieuIdAndTrangThaiNot(id,
                com.qlrapchieuphim.entity.enums.TrangThaiVe.DA_HUY)) {
            v.huy();
            dsDon.add(v.getDonDat());
        }
        for (com.qlrapchieuphim.entity.DonDat d : dsDon) {
            boolean tatCaDaHuy = d.getDsVe().stream()
                    .allMatch(x -> x.getTrangThai() == com.qlrapchieuphim.entity.enums.TrangThaiVe.DA_HUY);
            if (tatCaDaHuy) d.huy();
        }
        return suatChieuRepo.save(suat);
    }
    @Transactional
    public SuatChieu doiTrangThai(Long id, TrangThaiSuat trangThai) {
        if (id == null) {
            throw new IllegalArgumentException("Mã suất chiếu không được để trống.");
        }
        if (trangThai == null) {
            throw new IllegalArgumentException("Trạng thái không được để trống.");
        }
        if (trangThai == TrangThaiSuat.DA_HUY) {
            return huySuatChieu(id);
        }
        SuatChieu suat = suatChieuRepo.findByIdForUpdate(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy suất chiếu với mã: " + id));
        if (suat.getTrangThai() == TrangThaiSuat.DA_HUY) {
            throw new IllegalStateException("Suất đã hủy, không thể mở lại.");
        }
        suat.setTrangThai(trangThai);
        return suatChieuRepo.save(suat);
    }
    public SuatChieu layTheoId(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Mã suất chiếu không được để trống.");
        }
        return suatChieuRepo.findByIdWithDetails(id)
                .or(() -> suatChieuRepo.findById(id))
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy suất chiếu với mã: " + id));
    }
    public SuatChieu timTheoId(Long id) {
        return layTheoId(id);
    }
    public SuatChieu layChiTiet(Long id) {
        return layTheoId(id);
    }

    public Page<SuatChieu> layDanhSachSuatChieu(Long phimId, Long phongId, LocalDate ngay, Pageable pageable) {
        return layDanhSachSuatChieu(phimId, phongId, ngay, null, pageable);
    }
    public Page<SuatChieu> layDanhSachSuatChieu(Long phimId, Long phongId, LocalDate ngay, TrangThaiSuat trangThai, Pageable pageable) {
        LocalDateTime ngayBatDau = (ngay != null) ? ngay.atStartOfDay() : null;
        LocalDateTime ngayKetThuc = (ngay != null) ? ngay.plusDays(1).atStartOfDay() : null;
        return suatChieuRepo.timKiemSuatChieu(phimId, phongId, ngayBatDau, ngayKetThuc, trangThai, pageable);
    }
    public Page<SuatChieu> timSuatChieu(Long phimId, Long phongId, LocalDate ngay, Pageable pageable) {
        return layDanhSachSuatChieu(phimId, phongId, ngay, pageable);
    }

    public List<SuatChieu> laySuatChieuTheoPhimVaNgay(Long phimId, LocalDate ngay) {
        if (phimId == null) {
            return Collections.emptyList();
        }
        if (ngay != null) {
            LocalDateTime tuLuc = ngay.isEqual(LocalDate.now()) ? LocalDateTime.now() : ngay.atStartOfDay();
            LocalDateTime denLuc = ngay.plusDays(1).atStartOfDay();
            if (tuLuc.isAfter(denLuc)) {
                return Collections.emptyList();
            }
            return suatChieuRepo.timSuatHopLeTheoPhimVaKhoangThoiGian(phimId, tuLuc, denLuc, TrangThaiSuat.MO_BAN);
        } else {
            return suatChieuRepo.timSuatHopLeTheoPhim(phimId, LocalDateTime.now(), TrangThaiSuat.MO_BAN);
        }
    }
    public List<SuatChieu> laySuatHopLeTheoPhimVaNgay(Long phimId, LocalDate ngay) {
        return laySuatChieuTheoPhimVaNgay(phimId, ngay);
    }

    public LocalDateTime tinhGioKetThuc(LocalDateTime gioBatDau, int thoiLuongPhut) {
        if (gioBatDau == null) {
            throw new LoiTruongException("gioBatDau","Giờ bắt đầu không được để trống.");
        }
        if (thoiLuongPhut <= 0) {
            throw new IllegalArgumentException("Thời lượng phim phải lớn hơn 0 phút.");
        }
        return gioBatDau.plusMinutes(thoiLuongPhut);
    }
    public boolean kiemTraTrungGio(LocalDateTime s1Start, LocalDateTime s1End, LocalDateTime s2Start, LocalDateTime s2End) {
        if (s1Start == null || s1End == null || s2Start == null || s2End == null) {
            return false;
        }
        return s1Start.isBefore(s2End) && s1End.isAfter(s2Start);
    }
    public void xacThucGiaVe(BigDecimal giaThuong, BigDecimal giaVip) {
        kiemTraGiaVe(giaThuong, giaVip);
    }
    private void kiemTraGiaVe(BigDecimal giaThuong, BigDecimal giaVip) {

        if (giaThuong == null || giaThuong.compareTo(BigDecimal.ZERO) < 0) {
            throw new LoiTruongException("giaThuong","Giá vé thường phải lớn hơn hoặc bằng 0.");
        }
        if (giaVip == null || giaVip.compareTo(BigDecimal.ZERO) < 0) {
            throw new LoiTruongException("giaVip","Giá vé VIP phải lớn hơn hoặc bằng 0.");
        }
        if (giaVip.compareTo(giaThuong) < 0) {
            throw new LoiTruongException("giaVip","Giá vé VIP phải lớn hơn hoặc bằng giá vé thường.");
        }
    }
    private void kiemTraXungDot(Long phongId, LocalDateTime batDau, LocalDateTime ketThuc, Long suatId) {
        List<SuatChieu> xungDot = suatChieuRepo.timSuatXungDot(phongId, batDau, ketThuc, suatId, TrangThaiSuat.DA_HUY);
        if (!xungDot.isEmpty()) {
            SuatChieu trung = xungDot.get(0);
            throw new LoiTruongException("gioBatDau","Phòng chiếu đã có suất chiếu khác trong khoảng thời gian này ("
                    + trung.getGioBatDau() + " - " + trung.getGioKetThuc() + ").");
        }
    }
}
