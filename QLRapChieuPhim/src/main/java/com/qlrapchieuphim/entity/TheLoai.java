package com.qlrapchieuphim.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "TheLoai")
public class TheLoai {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Long id;

    @Column(name = "TenTheLoai", nullable = false, unique = true, length = 50)
    private String tenTheLoai;

    @Column(name = "MoTa", length = 255)
    private String moTa;
    @OneToMany(mappedBy = "theLoai")
    private List<Phim> dsPhim = new ArrayList<>();
    public List<Phim> getDsPhim() { return dsPhim; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTenTheLoai() { return tenTheLoai; }
    public void setTenTheLoai(String tenTheLoai) { this.tenTheLoai = tenTheLoai; }

    public String getMoTa() { return moTa; }
    public void setMoTa(String moTa) { this.moTa = moTa; }

    public int soPhim() { return dsPhim.size(); }
}
