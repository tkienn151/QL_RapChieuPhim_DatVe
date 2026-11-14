package com.qlrapchieuphim.scheduler;

import com.qlrapchieuphim.entity.DonDat;
import com.qlrapchieuphim.entity.enums.TrangThaiDon;
import com.qlrapchieuphim.repository.DonDatRepository;
import com.qlrapchieuphim.service.DatVeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class NhaGheScheduler {
    private static final Logger log = LoggerFactory.getLogger(NhaGheScheduler.class);
    private final DonDatRepository donDatRepo;
    private final DatVeService datVeService;

    public NhaGheScheduler(DonDatRepository donDatRepo, DatVeService datVeService) {
        this.donDatRepo = donDatRepo;
        this.datVeService = datVeService;
    }

    @Scheduled(fixedRate = 60000)
    public void nhaGheQuaHan() {
        String tenLuong = Thread.currentThread().getName();
        List<DonDat> dsQuaHan = donDatRepo.findByTrangThaiAndHanThanhToanBefore(
                TrangThaiDon.CHO_THANH_TOAN, LocalDateTime.now());
        if (dsQuaHan.isEmpty()) {
            log.debug("[Scheduler - Luong: {}] Quet don tam qua han: 0 don", tenLuong);
            return;
        }
        log.info("[Scheduler - Luong: {}] Tìm thấy {} đơn đặt vé quá hạn 5 phút. Tiến hành giải phóng ghế...",
                tenLuong, dsQuaHan.size());
        for (DonDat donDat : dsQuaHan) {
            try {
                if (datVeService.nhaGheDonQuaHan(donDat.getId())) {
                    log.info("[Scheduler - Luong: {}] Đã hết hạn đơn {} và nhả ghế thành công.", tenLuong, donDat.getMaDon());
                }
            } catch (Exception e) {
                log.error("[Scheduler - Luong: {}] Lỗi khi nhả ghế đơn {}: {}", tenLuong, donDat.getMaDon(), e.getMessage());
            }
        }
    }
}
