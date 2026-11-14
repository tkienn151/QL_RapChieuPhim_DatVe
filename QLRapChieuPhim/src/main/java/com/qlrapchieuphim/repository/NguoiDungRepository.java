package com.qlrapchieuphim.repository;

import com.qlrapchieuphim.entity.*;
import com.qlrapchieuphim.entity.enums.*;
import com.qlrapchieuphim.entity.NguoiDung;
import com.qlrapchieuphim.entity.enums.VaiTro;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface NguoiDungRepository extends JpaRepository<NguoiDung, Long> {
    Optional<NguoiDung> findByTenDangNhap(String tenDangNhap);
    boolean existsByTenDangNhap(String tenDangNhap);

    @Query("select n from NguoiDung n " +
           "where (:tuKhoa is null or :tuKhoa = '' " +
           "       or lower(n.tenDangNhap) like lower(concat('%', :tuKhoa, '%')) " +
           "       or lower(n.hoTen) like lower(concat('%', :tuKhoa, '%')) " +
           "       or lower(coalesce(n.email, '')) like lower(concat('%', :tuKhoa, '%')) " +
           "       or lower(coalesce(n.soDienThoai, '')) like lower(concat('%', :tuKhoa, '%'))) " +
           "  and (:vaiTro is null or n.vaiTro = :vaiTro)")
    Page<NguoiDung> timKiemTaiKhoan(@Param("tuKhoa") String tuKhoa,
                                    @Param("vaiTro") VaiTro vaiTro,
                                    Pageable pageable);
}
