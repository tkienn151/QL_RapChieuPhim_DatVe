package com.qlrapchieuphim.repository;

import com.qlrapchieuphim.entity.*;
import com.qlrapchieuphim.entity.enums.*;
import com.qlrapchieuphim.entity.Ghe;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GheRepository extends JpaRepository<Ghe, Long> {
    List<Ghe> findByPhongChieuIdOrderByHangAscSoGheAsc(Long phongChieuId);
    long countByPhongChieuId(Long phongChieuId);
    void deleteByPhongChieuId(Long phongChieuId);
}
