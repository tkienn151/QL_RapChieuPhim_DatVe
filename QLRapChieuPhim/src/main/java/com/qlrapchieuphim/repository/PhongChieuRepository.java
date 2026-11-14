package com.qlrapchieuphim.repository;

import com.qlrapchieuphim.entity.*;
import com.qlrapchieuphim.entity.enums.*;
import com.qlrapchieuphim.entity.PhongChieu;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PhongChieuRepository extends JpaRepository<PhongChieu, Long> {
    boolean existsByMaPhong(String maPhong);
    boolean existsByMaPhongAndIdNot(String maPhong, Long id);

    @Query("select p from PhongChieu p where (:tuKhoa is null or :tuKhoa = '' " +
           "or lower(p.maPhong) like lower(concat('%', :tuKhoa, '%')) " +
           "or lower(p.tenPhong) like lower(concat('%', :tuKhoa, '%')))")
    Page<PhongChieu> timKiem(@Param("tuKhoa") String tuKhoa, Pageable pageable);
}
