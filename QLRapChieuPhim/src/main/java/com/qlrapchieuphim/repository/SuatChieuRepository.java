package com.qlrapchieuphim.repository;

import com.qlrapchieuphim.entity.*;
import com.qlrapchieuphim.entity.enums.*;
import com.qlrapchieuphim.entity.SuatChieu;
import com.qlrapchieuphim.entity.enums.TrangThaiSuat;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SuatChieuRepository extends JpaRepository<SuatChieu, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from SuatChieu s where s.id = :id")
    Optional<SuatChieu> findByIdForUpdate(@Param("id") Long id);

    boolean existsByPhimId(Long phimId);

    @Query("select s from SuatChieu s join fetch s.phongChieu " +
           "where s.phim.id = :phimId and s.trangThai = :trangThai and s.gioBatDau > :tuLuc " +
           "order by s.gioBatDau")
    List<SuatChieu> timSuatSapChieuCuaPhim(@Param("phimId") Long phimId,
                                           @Param("trangThai") TrangThaiSuat trangThai,
                                           @Param("tuLuc") LocalDateTime tuLuc);

    @Query("select s from SuatChieu s " +
           "where s.phongChieu.id = :phongId " +
           "and s.trangThai <> :daHuy " +
           "and (:id is null or s.id <> :id) " +
           "and s.gioBatDau < :gioKetThuc " +
           "and s.gioKetThuc > :gioBatDau")
    List<SuatChieu> timSuatXungDot(@Param("phongId") Long phongId,
                                   @Param("gioBatDau") LocalDateTime gioBatDau,
                                   @Param("gioKetThuc") LocalDateTime gioKetThuc,
                                   @Param("id") Long id,
                                   @Param("daHuy") TrangThaiSuat daHuy);

    @Query(value = "select s from SuatChieu s " +
                   "left join fetch s.phim " +
                   "left join fetch s.phongChieu " +
                   "where (:phimId is null or s.phim.id = :phimId) " +
                   "and (:phongId is null or s.phongChieu.id = :phongId) " +
                   "and (:ngayBatDau is null or s.gioBatDau >= :ngayBatDau) " +
                   "and (:ngayKetThuc is null or s.gioBatDau < :ngayKetThuc) " +
                   "and (:trangThai is null or s.trangThai = :trangThai)",
           countQuery = "select count(s) from SuatChieu s " +
                        "where (:phimId is null or s.phim.id = :phimId) " +
                        "and (:phongId is null or s.phongChieu.id = :phongId) " +
                        "and (:ngayBatDau is null or s.gioBatDau >= :ngayBatDau) " +
                        "and (:ngayKetThuc is null or s.gioBatDau < :ngayKetThuc) " +
                        "and (:trangThai is null or s.trangThai = :trangThai)")
    Page<SuatChieu> timKiemSuatChieu(@Param("phimId") Long phimId,
                                    @Param("phongId") Long phongId,
                                    @Param("ngayBatDau") LocalDateTime ngayBatDau,
                                    @Param("ngayKetThuc") LocalDateTime ngayKetThuc,
                                    @Param("trangThai") TrangThaiSuat trangThai,
                                    Pageable pageable);

    @Query("select s from SuatChieu s " +
           "join fetch s.phongChieu " +
           "join fetch s.phim " +
           "where s.phim.id = :phimId " +
           "and s.trangThai = :trangThai " +
           "and s.gioBatDau >= :tuLuc and s.gioBatDau < :denLuc " +
           "order by s.gioBatDau asc")
    List<SuatChieu> timSuatHopLeTheoPhimVaKhoangThoiGian(@Param("phimId") Long phimId,
                                                        @Param("tuLuc") LocalDateTime tuLuc,
                                                        @Param("denLuc") LocalDateTime denLuc,
                                                        @Param("trangThai") TrangThaiSuat trangThai);

    @Query("select s from SuatChieu s " +
           "join fetch s.phongChieu " +
           "join fetch s.phim " +
           "where s.phim.id = :phimId " +
           "and s.trangThai = :trangThai " +
           "and s.gioBatDau > :tuLuc " +
           "order by s.gioBatDau asc")
    List<SuatChieu> timSuatHopLeTheoPhim(@Param("phimId") Long phimId,
                                        @Param("tuLuc") LocalDateTime tuLuc,
                                        @Param("trangThai") TrangThaiSuat trangThai);

    @Query("select s from SuatChieu s " +
           "left join fetch s.phim " +
           "left join fetch s.phongChieu " +
           "where s.id = :id")
    Optional<SuatChieu> findByIdWithDetails(@Param("id") Long id);

    boolean existsByPhongChieuId(Long phongChieuId);
}
