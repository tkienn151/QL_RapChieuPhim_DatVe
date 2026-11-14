package com.qlrapchieuphim.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface DoanhThuNgayDto {
    LocalDate getNgay();
    BigDecimal getTongDoanhThu();
    default BigDecimal getDoanhThu() {
        return getTongDoanhThu();
    }
    Long getSoLuongDon();
    default Long getSoDon() {
        return getSoLuongDon();
    }
    Long getSoLuongVe();
    default Long getSoVe() {
        return getSoLuongVe();
    }
    static DoanhThuNgayDto of(LocalDate ngay, BigDecimal tongDoanhThu, Long soLuongDon, Long soLuongVe) {
        return new DoanhThuNgayDto() {
            @Override
            public LocalDate getNgay() {
                return ngay;
            }
            @Override
            public BigDecimal getTongDoanhThu() {
                return tongDoanhThu != null ? tongDoanhThu : BigDecimal.ZERO;
            }
            @Override
            public Long getSoLuongDon() {
                return soLuongDon != null ? soLuongDon : 0L;
            }
            @Override
            public Long getSoLuongVe() {
                return soLuongVe != null ? soLuongVe : 0L;
            }
        };
    }
}
