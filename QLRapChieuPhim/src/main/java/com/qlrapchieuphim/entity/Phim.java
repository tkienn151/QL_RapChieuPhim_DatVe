package com.qlrapchieuphim.entity;

import com.qlrapchieuphim.entity.enums.TrangThaiPhim;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Phim")
public class Phim {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Long id;

    @Column(name = "MaPhim", nullable = false, unique = true, length = 20)
    private String maPhim;

    @Column(name = "TenPhim", nullable = false, length = 150)
    private String tenPhim;

    @Column(name = "MoTa", length = 1000)
    private String moTa;

    @Column(name = "ThoiLuong", nullable = false)
    private int thoiLuong;

    @Column(name = "NgayKhoiChieu")
    private LocalDate ngayKhoiChieu;

    @Column(name = "DaoDien", length = 100)
    private String daoDien;

    @Column(name = "DienVien", length = 255)
    private String dienVien;

    @Column(name = "Poster", length = 255)
    private String poster;

    @Enumerated(EnumType.STRING)
    @Column(name = "TrangThai", nullable = false, length = 20)
    private TrangThaiPhim trangThai;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "TheLoaiId", nullable = false)
    private TheLoai theLoai;

    @OneToMany(mappedBy = "phim")
    private List<SuatChieu> dsSuatChieu = new ArrayList<>();

    public TheLoai getTheLoai() { return theLoai; }
    public void setTheLoai(TheLoai theLoai) { this.theLoai = theLoai; }
    public List<SuatChieu> getDsSuatChieu() { return dsSuatChieu; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getMaPhim() { return maPhim; }
    public void setMaPhim(String maPhim) { this.maPhim = maPhim; }

    public String getTenPhim() { return tenPhim; }
    public void setTenPhim(String tenPhim) { this.tenPhim = tenPhim; }

    public String getMoTa() { return moTa; }
    public void setMoTa(String moTa) { this.moTa = moTa; }

    public int getThoiLuong() { return thoiLuong; }
    public void setThoiLuong(int thoiLuong) { this.thoiLuong = thoiLuong; }

    public LocalDate getNgayKhoiChieu() { return ngayKhoiChieu; }
    public void setNgayKhoiChieu(LocalDate ngayKhoiChieu) { this.ngayKhoiChieu = ngayKhoiChieu; }

    public String getDaoDien() { return daoDien; }
    public void setDaoDien(String daoDien) { this.daoDien = daoDien; }

    public String getDienVien() { return dienVien; }
    public void setDienVien(String dienVien) { this.dienVien = dienVien; }

    public String getPoster() { return poster; }
    public void setPoster(String poster) { this.poster = poster; }

    public TrangThaiPhim getTrangThai() { return trangThai; }
    public void setTrangThai(TrangThaiPhim trangThai) { this.trangThai = trangThai; }

    public boolean dangChieu() { return trangThai == TrangThaiPhim.DANG_CHIEU; }
    public void ngungChieu() { this.trangThai = TrangThaiPhim.NGUNG_CHIEU; }
}
