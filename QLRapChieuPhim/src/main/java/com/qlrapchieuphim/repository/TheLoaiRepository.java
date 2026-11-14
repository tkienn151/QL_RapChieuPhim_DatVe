package com.qlrapchieuphim.repository;

import com.qlrapchieuphim.entity.*;
import com.qlrapchieuphim.entity.enums.*;
import com.qlrapchieuphim.entity.TheLoai;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TheLoaiRepository extends JpaRepository<TheLoai, Long> {
    boolean existsByTenTheLoaiIgnoreCase(String tenTheLoai);
    boolean existsByTenTheLoaiIgnoreCaseAndIdNot(String tenTheLoai, Long id);

    @Query("select t from TheLoai t where (:tuKhoa is null or :tuKhoa = '' " +
           "or lower(t.tenTheLoai) like lower(concat('%', :tuKhoa, '%')) " +
           "or lower(coalesce(t.moTa, '')) like lower(concat('%', :tuKhoa, '%')))")
    Page<TheLoai> timKiem(@Param("tuKhoa") String tuKhoa, Pageable pageable);
}
