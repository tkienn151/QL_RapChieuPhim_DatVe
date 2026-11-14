package com.qlrapchieuphim.repository;

import com.qlrapchieuphim.entity.*;
import com.qlrapchieuphim.entity.enums.*;
import com.qlrapchieuphim.entity.DonDat;
import com.qlrapchieuphim.entity.enums.TrangThaiDon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;

public interface DonDatRepository extends JpaRepository<DonDat, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select d from DonDat d where d.maDon = :maDon")
    Optional<DonDat> findByMaDonForUpdate(@Param("maDon") String maDon);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select d from DonDat d where d.id = :id")
    Optional<DonDat> findByIdForUpdate(@Param("id") Long id);

    Optional<DonDat> findByMaDon(String maDon);
    boolean existsByNguoiDungId(Long nguoiDungId);
    List<DonDat> findByTrangThaiAndHanThanhToanBefore(TrangThaiDon trangThai, LocalDateTime han);

    Page<DonDat> findByNguoiDungIdOrderByNgayDatDesc(Long nguoiDungId, Pageable pageable);

    Page<DonDat> findByNguoiDungIdAndTrangThaiOrderByNgayDatDesc(Long nguoiDungId, TrangThaiDon trangThai, Pageable pageable);
    @Query("select distinct d from DonDat d join d.dsVe v " +
            "where d.trangThai = com.qlrapchieuphim.entity.enums.TrangThaiDon.CHO_THANH_TOAN " +
            "and d.hanThanhToan < :now and v.suatChieu.id = :suatId")
    List<DonDat> findQuaHanTheoSuat(@Param("suatId") Long suatId, @Param("now") LocalDateTime now);

    @Query("select distinct d from DonDat d " +
           "left join fetch d.dsVe v " +
           "left join fetch v.suatChieu s " +
           "left join fetch s.phim " +
           "left join fetch s.phongChieu " +
           "left join fetch v.ghe " +
           "where d.id = :id")
    Optional<DonDat> findByIdWithChiTiet(@Param("id") Long id);

    @Query("select distinct d from DonDat d " +
           "left join fetch d.dsVe v " +
           "left join fetch v.suatChieu s " +
           "left join fetch s.phim " +
           "left join fetch s.phongChieu " +
           "left join fetch v.ghe " +
           "where d.maDon = :maDon")
    Optional<DonDat> findByMaDonWithChiTiet(@Param("maDon") String maDon);

    @Query("select distinct d from DonDat d left join d.nguoiDung nd where " +
           "(:tuKhoa is null or lower(d.maDon) like lower(concat('%', :tuKhoa, '%')) " +
           "or lower(d.sdtKhach) like lower(concat('%', :tuKhoa, '%')) " +
           "or lower(d.hoTenKhach) like lower(concat('%', :tuKhoa, '%')) " +
           "or (d.kenhDat = com.qlrapchieuphim.entity.enums.KenhDat.WEB and " +
           "(lower(nd.soDienThoai) like lower(concat('%', :tuKhoa, '%')) " +
           "or lower(nd.hoTen) like lower(concat('%', :tuKhoa, '%'))))) " +
           "order by d.ngayDat desc")
    List<DonDat> traCuuDon(@Param("tuKhoa") String tuKhoa);

    @Query("select distinct d from DonDat d left join d.nguoiDung nd where " +
           "(:tuKhoa is null or lower(d.maDon) like lower(concat('%', :tuKhoa, '%')) " +
           "or lower(d.sdtKhach) like lower(concat('%', :tuKhoa, '%')) " +
           "or lower(d.hoTenKhach) like lower(concat('%', :tuKhoa, '%')) " +
           "or (d.kenhDat = com.qlrapchieuphim.entity.enums.KenhDat.WEB and " +
           "(lower(nd.soDienThoai) like lower(concat('%', :tuKhoa, '%')) " +
           "or lower(nd.hoTen) like lower(concat('%', :tuKhoa, '%'))))) " +
           "order by d.ngayDat desc")
    Page<DonDat> traCuuDonPhanTrang(@Param("tuKhoa") String tuKhoa, Pageable pageable);

    @Query("select count(d.id) from DonDat d " +
           "where d.trangThai = com.qlrapchieuphim.entity.enums.TrangThaiDon.DA_THANH_TOAN " +
           "and (:tuNgay is null or d.ngayDat >= :tuNgay) " +
           "and (:denNgay is null or d.ngayDat <= :denNgay)")
    Long tinhTongSoDon(@Param("tuNgay") LocalDateTime tuNgay,
                       @Param("denNgay") LocalDateTime denNgay);
}
