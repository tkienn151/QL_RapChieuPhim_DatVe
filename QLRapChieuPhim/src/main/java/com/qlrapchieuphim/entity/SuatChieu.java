package com.qlrapchieuphim.entity;

import com.qlrapchieuphim.entity.enums.LoaiGhe;
import com.qlrapchieuphim.entity.enums.TrangThaiSuat;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "SuatChieu")
public class SuatChieu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Long id;

    @Column(name = "GioBatDau", nullable = false)
    private LocalDateTime gioBatDau;

    @Column(name = "GioKetThuc", nullable = false)
    private LocalDateTime gioKetThuc;

    @Column(name = "GiaThuong", nullable = false, precision = 12, scale = 0)
    private BigDecimal giaThuong;

    @Column(name = "GiaVip", nullable = false, precision = 12, scale = 0)
    private BigDecimal giaVip;

    @Enumerated(EnumType.STRING)
    @Column(name = "TrangThai", nullable = false, length = 20)
    private TrangThaiSuat trangThai;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "PhimId", nullable = false)
    private Phim phim;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "PhongChieuId", nullable = false)
    private PhongChieu phongChieu;

    @OneToMany(mappedBy = "suatChieu")
    private List<Ve> dsVe = new ArrayList<>();

    public Phim getPhim() { return phim; }
    public void setPhim(Phim phim) { this.phim = phim; }
    public PhongChieu getPhongChieu() { return phongChieu; }
    public void setPhongChieu(PhongChieu phongChieu) { this.phongChieu = phongChieu; }
    public List<Ve> getDsVe() { return dsVe; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDateTime getGioBatDau() { return gioBatDau; }
    public void setGioBatDau(LocalDateTime gioBatDau) { this.gioBatDau = gioBatDau; }

    public LocalDateTime getGioKetThuc() { return gioKetThuc; }
    public void setGioKetThuc(LocalDateTime gioKetThuc) { this.gioKetThuc = gioKetThuc; }

    public BigDecimal getGiaThuong() { return giaThuong; }
    public void setGiaThuong(BigDecimal giaThuong) { this.giaThuong = giaThuong; }

    public BigDecimal getGiaVip() { return giaVip; }
    public void setGiaVip(BigDecimal giaVip) { this.giaVip = giaVip; }

    public TrangThaiSuat getTrangThai() { return trangThai; }
    public void setTrangThai(TrangThaiSuat trangThai) { this.trangThai = trangThai; }

    public LocalDateTime tinhGioKetThuc(int thoiLuongPhut) { return gioBatDau.plusMinutes(thoiLuongPhut); }
    public BigDecimal layGia(LoaiGhe loai) { return loai == LoaiGhe.VIP ? giaVip : giaThuong; }
    public boolean conMoBan() { return trangThai == TrangThaiSuat.MO_BAN; }
}
