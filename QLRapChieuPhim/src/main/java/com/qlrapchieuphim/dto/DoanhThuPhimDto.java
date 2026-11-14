package com.qlrapchieuphim.dto;

import java.math.BigDecimal;

public interface DoanhThuPhimDto {
    Long getPhimId();
    default String getMaPhim() {
        return "";
    }
    String getTenPhim();
    BigDecimal getTongDoanhThu();
    default BigDecimal getDoanhThu() {
        return getTongDoanhThu();
    }
    Long getSoLuongVe();
    default Long getSoVe() {
        return getSoLuongVe();
    }
    static DoanhThuPhimDto of(Long phimId, String tenPhim, BigDecimal tongDoanhThu, Long soLuongVe) {
        return new DoanhThuPhimDto() {
            @Override
            public Long getPhimId() {
                return phimId;
            }
            @Override
            public String getTenPhim() {
                return tenPhim;
            }
            @Override
            public BigDecimal getTongDoanhThu() {
                return tongDoanhThu != null ? tongDoanhThu : BigDecimal.ZERO;
            }
            @Override
            public Long getSoLuongVe() {
                return soLuongVe != null ? soLuongVe : 0L;
            }
        };
    }
    static DoanhThuPhimDto of(Long phimId, String maPhim, String tenPhim, BigDecimal tongDoanhThu, Long soLuongVe) {
        return new DoanhThuPhimDto() {
            @Override
            public Long getPhimId() {
                return phimId;
            }
            @Override
            public String getMaPhim() {
                return maPhim != null ? maPhim : "";
            }
            @Override
            public String getTenPhim() {
                return tenPhim;
            }
            @Override
            public BigDecimal getTongDoanhThu() {
                return tongDoanhThu != null ? tongDoanhThu : BigDecimal.ZERO;
            }
            @Override
            public Long getSoLuongVe() {
                return soLuongVe != null ? soLuongVe : 0L;
            }
        };
    }
}
