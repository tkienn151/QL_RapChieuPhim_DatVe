package com.qlrapchieuphim.entity;

import com.qlrapchieuphim.entity.enums.LoaiGhe;
import jakarta.persistence.*;

@Entity
@Table(name = "Ghe")
public class Ghe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Long id;

    @Column(name = "Hang", nullable = false, length = 2)
    private String hang;

    @Column(name = "SoGhe", nullable = false)
    private int soGhe;

    @Enumerated(EnumType.STRING)
    @Column(name = "LoaiGhe", nullable = false, length = 10)
    private LoaiGhe loaiGhe;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "PhongChieuId", nullable = false)
    private PhongChieu phongChieu;

    public PhongChieu getPhongChieu() { return phongChieu; }
    public void setPhongChieu(PhongChieu phongChieu) { this.phongChieu = phongChieu; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getHang() { return hang; }
    public void setHang(String hang) { this.hang = hang; }

    public int getSoGhe() { return soGhe; }
    public void setSoGhe(int soGhe) { this.soGhe = soGhe; }

    public LoaiGhe getLoaiGhe() { return loaiGhe; }
    public void setLoaiGhe(LoaiGhe loaiGhe) { this.loaiGhe = loaiGhe; }

    public boolean laVip() { return loaiGhe == LoaiGhe.VIP; }
    public String nhanDang() { return hang + soGhe; }
}
