package com.qlrapchieuphim.repository;

import com.qlrapchieuphim.entity.*;
import com.qlrapchieuphim.entity.enums.*;
import com.qlrapchieuphim.entity.Ve;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import com.qlrapchieuphim.dto.DoanhThuNgayDto;
import com.qlrapchieuphim.dto.DoanhThuPhimDto;

public interface VeRepository extends JpaRepository<Ve, Long> {
    Optional<Ve> findByMaVe(String maVe);

    @Query("select v from Ve v " +
           "join fetch v.donDat d " +
           "join fetch v.ghe g " +
           "join fetch v.suatChieu s " +
           "join fetch s.phim p " +
           "join fetch s.phongChieu pc " +
           "where v.maVe = :maVe")
    Optional<Ve> findChiTietByMaVe(@Param("maVe") String maVe);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select v from Ve v " +
           "join fetch v.donDat d " +
           "join fetch v.ghe g " +
           "join fetch v.suatChieu s " +
           "join fetch s.phim p " +
           "join fetch s.phongChieu pc " +
           "where v.maVe = :maVe")
    Optional<Ve> findByMaVeForUpdate(@Param("maVe") String maVe);
    @Query("select v.ghe.id from Ve v where v.suatChieu.id = :suatId and v.trangThai <> com.qlrapchieuphim.entity.enums.TrangThaiVe.DA_HUY")
    List<Long> findGheDaDat(@Param("suatId") Long suatId);

    boolean existsBySuatChieuId(Long suatChieuId);

    List<Ve> findBySuatChieuIdAndTrangThaiNot(Long suatChieuId, TrangThaiVe trangThai);

    List<Ve> findByDonDatId(Long donDatId);
    List<Ve> findByDonDatMaDon(String maDon);

    @Query("select coalesce(sum(v.giaVe), 0) from Ve v join v.donDat d " +
           "where d.trangThai = com.qlrapchieuphim.entity.enums.TrangThaiDon.DA_THANH_TOAN " +
           "and v.trangThai <> com.qlrapchieuphim.entity.enums.TrangThaiVe.DA_HUY " +
           "and (:tuNgay is null or d.ngayDat >= :tuNgay) " +
           "and (:denNgay is null or d.ngayDat <= :denNgay)")
    BigDecimal tinhTongDoanhThu(@Param("tuNgay") LocalDateTime tuNgay,
                                @Param("denNgay") LocalDateTime denNgay);

    @Query("select count(v.id) from Ve v join v.donDat d " +
           "where d.trangThai = com.qlrapchieuphim.entity.enums.TrangThaiDon.DA_THANH_TOAN " +
           "and v.trangThai <> com.qlrapchieuphim.entity.enums.TrangThaiVe.DA_HUY " +
           "and (:tuNgay is null or d.ngayDat >= :tuNgay) " +
           "and (:denNgay is null or d.ngayDat <= :denNgay)")
    Long tinhTongSoVe(@Param("tuNgay") LocalDateTime tuNgay,
                      @Param("denNgay") LocalDateTime denNgay);

    @Query("select p.id as phimId, p.maPhim as maPhim, p.tenPhim as tenPhim, " +
           "coalesce(sum(v.giaVe), 0) as tongDoanhThu, count(v.id) as soLuongVe " +
           "from Ve v join v.suatChieu s join s.phim p join v.donDat d " +
           "where d.trangThai = com.qlrapchieuphim.entity.enums.TrangThaiDon.DA_THANH_TOAN " +
           "and v.trangThai <> com.qlrapchieuphim.entity.enums.TrangThaiVe.DA_HUY " +
           "and (:tuNgay is null or d.ngayDat >= :tuNgay) " +
           "and (:denNgay is null or d.ngayDat <= :denNgay) " +
           "group by p.id, p.maPhim, p.tenPhim " +
           "order by coalesce(sum(v.giaVe), 0) desc")
    List<DoanhThuPhimDto> thongKeDoanhThuTheoPhim(@Param("tuNgay") LocalDateTime tuNgay,
                                                 @Param("denNgay") LocalDateTime denNgay);

    @Query("select cast(d.ngayDat as LocalDate) as ngay, " +
           "count(distinct d.id) as soLuongDon, " +
           "count(v.id) as soLuongVe, " +
           "coalesce(sum(v.giaVe), 0) as tongDoanhThu " +
           "from Ve v join v.donDat d " +
           "where d.trangThai = com.qlrapchieuphim.entity.enums.TrangThaiDon.DA_THANH_TOAN " +
           "and v.trangThai <> com.qlrapchieuphim.entity.enums.TrangThaiVe.DA_HUY " +
           "and (:tuNgay is null or d.ngayDat >= :tuNgay) " +
           "and (:denNgay is null or d.ngayDat <= :denNgay) " +
           "group by cast(d.ngayDat as LocalDate) " +
           "order by cast(d.ngayDat as LocalDate) desc")
    List<DoanhThuNgayDto> thongKeDoanhThuTheoNgay(@Param("tuNgay") LocalDateTime tuNgay,
                                                 @Param("denNgay") LocalDateTime denNgay);
}
