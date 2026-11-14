package com.qlrapchieuphim.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "PhongChieu")
public class PhongChieu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Long id;

    @Column(name = "MaPhong", nullable = false, unique = true, length = 10)
    private String maPhong;

    @Column(name = "TenPhong", nullable = false, length = 50)
    private String tenPhong;

    @Column(name = "SoHang", nullable = false)
    private int soHang;

    @Column(name = "SoCot", nullable = false)
    private int soCot;
    @OneToMany(mappedBy = "phongChieu")
    private List<Ghe> dsGhe = new ArrayList<>();

    @OneToMany(mappedBy = "phongChieu")
    private List<SuatChieu> dsSuatChieu = new ArrayList<>();

    public List<Ghe> getDsGhe() { return dsGhe; }
    public List<SuatChieu> getDsSuatChieu() { return dsSuatChieu; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getMaPhong() { return maPhong; }
    public void setMaPhong(String maPhong) { this.maPhong = maPhong; }

    public String getTenPhong() { return tenPhong; }
    public void setTenPhong(String tenPhong) { this.tenPhong = tenPhong; }

    public int getSoHang() { return soHang; }
    public void setSoHang(int soHang) { this.soHang = soHang; }

    public int getSoCot() { return soCot; }
    public void setSoCot(int soCot) { this.soCot = soCot; }

    public int tongSoGhe() { return soHang * soCot; }
}
