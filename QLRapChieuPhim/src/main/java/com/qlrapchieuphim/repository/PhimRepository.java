package com.qlrapchieuphim.repository;

import com.qlrapchieuphim.entity.*;
import com.qlrapchieuphim.entity.enums.*;
import com.qlrapchieuphim.entity.Phim;
import com.qlrapchieuphim.entity.enums.TrangThaiPhim;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PhimRepository extends JpaRepository<Phim, Long> {
    Page<Phim> findByTenPhimContainingIgnoreCase(String tuKhoa, Pageable pageable);

    @Query("select p from Phim p where " +
           "(:tuKhoa is null or lower(p.tenPhim) like lower(concat('%', :tuKhoa, '%'))) and " +
           "(:theLoaiId is null or p.theLoai.id = :theLoaiId) and " +
           "(:trangThai is null or p.trangThai = :trangThai)")
    Page<Phim> timPhim(@Param("tuKhoa") String tuKhoa,
                       @Param("theLoaiId") Long theLoaiId,
                       @Param("trangThai") TrangThaiPhim trangThai,
                       Pageable pageable);

    boolean existsByMaPhim(String maPhim);
    boolean existsByMaPhimAndIdNot(String maPhim, Long id);
    boolean existsByTheLoaiId(Long theLoaiId);
}
