package com.qlrapchieuphim.entity;

import com.qlrapchieuphim.entity.enums.KenhDat;
import com.qlrapchieuphim.entity.enums.TrangThaiDon;
import com.qlrapchieuphim.entity.enums.TrangThaiVe;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "DonDat")
public class DonDat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Long id;

    @Column(name = "MaDon", nullable = false, unique = true, length = 20)
    private String maDon;

    @Column(name = "HoTenKhach", length = 100)
    private String hoTenKhach;

    @Column(name = "SdtKhach", length = 15)
    private String sdtKhach;

    @Column(name = "NgayDat", nullable = false)
    private LocalDateTime ngayDat;

    @Column(name = "HanThanhToan")
    private LocalDateTime hanThanhToan;

    @Column(name = "TongTien", nullable = false, precision = 12, scale = 0)
    private BigDecimal tongTien;

    @Enumerated(EnumType.STRING)
    @Column(name = "TrangThai", nullable = false, length = 20)
    private TrangThaiDon trangThai;

    @Enumerated(EnumType.STRING)
    @Column(name = "KenhDat", nullable = false, length = 10)
    private KenhDat kenhDat;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "NguoiDungId", nullable = false)
    private NguoiDung nguoiDung;

    @OneToMany(mappedBy = "donDat", cascade = CascadeType.ALL)
    private List<Ve> dsVe = new ArrayList<>();

    public NguoiDung getNguoiDung() { return nguoiDung; }
    public void setNguoiDung(NguoiDung nguoiDung) { this.nguoiDung = nguoiDung; }
    public List<Ve> getDsVe() { return dsVe; }
    public void themVe(Ve v) { v.setDonDat(this); dsVe.add(v); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getMaDon() { return maDon; }
    public void setMaDon(String maDon) { this.maDon = maDon; }

    public String getHoTenKhach() { return hoTenKhach; }
    public void setHoTenKhach(String hoTenKhach) { this.hoTenKhach = hoTenKhach; }

    public String getSdtKhach() { return sdtKhach; }
    public void setSdtKhach(String sdtKhach) { this.sdtKhach = sdtKhach; }

    public LocalDateTime getNgayDat() { return ngayDat; }
    public void setNgayDat(LocalDateTime ngayDat) { this.ngayDat = ngayDat; }

    public LocalDateTime getHanThanhToan() { return hanThanhToan; }
    public void setHanThanhToan(LocalDateTime hanThanhToan) { this.hanThanhToan = hanThanhToan; }

    public BigDecimal getTongTien() { return tongTien; }
    public void setTongTien(BigDecimal tongTien) { this.tongTien = tongTien; }

    public TrangThaiDon getTrangThai() { return trangThai; }
    public void setTrangThai(TrangThaiDon trangThai) { this.trangThai = trangThai; }

    public KenhDat getKenhDat() { return kenhDat; }
    public void setKenhDat(KenhDat kenhDat) { this.kenhDat = kenhDat; }

    public BigDecimal tinhTongTien() {
        BigDecimal t = BigDecimal.ZERO;
        for (Ve v : dsVe) if (v.getTrangThai() != TrangThaiVe.DA_HUY) t = t.add(v.getGiaVe());
        return t;
    }
    public boolean daHetHan(LocalDateTime bayGio) { return hanThanhToan != null && bayGio.isAfter(hanThanhToan); }
    public void danhDauDaThanhToan() { this.trangThai = TrangThaiDon.DA_THANH_TOAN; }
    public void huy() { this.trangThai = TrangThaiDon.DA_HUY; }
}
