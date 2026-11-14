package com.qlrapchieuphim.entity;

import com.qlrapchieuphim.entity.enums.VaiTro;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "NguoiDung")
public class NguoiDung {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Long id;

    @Column(name = "TenDangNhap", nullable = false, unique = true, length = 50)
    private String tenDangNhap;

    @Column(name = "MatKhau", nullable = false, length = 64)
    private String matKhau;

    @Column(name = "HoTen", nullable = false, length = 100)
    private String hoTen;

    @Column(name = "Email", length = 100)
    private String email;

    @Column(name = "SoDienThoai", length = 15)
    private String soDienThoai;

    @Enumerated(EnumType.STRING)
    @Column(name = "VaiTro", nullable = false, length = 20)
    private VaiTro vaiTro;

    @Column(name = "HoatDong", nullable = false)
    private boolean hoatDong;
    @OneToMany(mappedBy = "nguoiDung")
    private List<DonDat> dsDonDat = new ArrayList<>();
    public List<DonDat> getDsDonDat() { return dsDonDat; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTenDangNhap() { return tenDangNhap; }
    public void setTenDangNhap(String tenDangNhap) { this.tenDangNhap = tenDangNhap; }

    public String getMatKhau() { return matKhau; }
    public void setMatKhau(String matKhau) { this.matKhau = matKhau; }

    public String getHoTen() { return hoTen; }
    public void setHoTen(String hoTen) { this.hoTen = hoTen; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getSoDienThoai() { return soDienThoai; }
    public void setSoDienThoai(String soDienThoai) { this.soDienThoai = soDienThoai; }

    public VaiTro getVaiTro() { return vaiTro; }
    public void setVaiTro(VaiTro vaiTro) { this.vaiTro = vaiTro; }

    public boolean isHoatDong() { return hoatDong; }
    public void setHoatDong(boolean hoatDong) { this.hoatDong = hoatDong; }

    public boolean kiemTraMatKhau(String matKhauDaBam) { return matKhau != null && matKhau.equalsIgnoreCase(matKhauDaBam); }
    public boolean coVaiTro(VaiTro v) { return vaiTro == v; }
}
