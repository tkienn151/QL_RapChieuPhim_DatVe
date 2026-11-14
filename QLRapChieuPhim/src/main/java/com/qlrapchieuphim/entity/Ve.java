package com.qlrapchieuphim.entity;

import com.qlrapchieuphim.entity.enums.TrangThaiDon;
import com.qlrapchieuphim.entity.enums.TrangThaiVe;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "Ve")
public class Ve {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Long id;

    @Column(name = "MaVe", nullable = false, unique = true, length = 20)
    private String maVe;

    @Column(name = "GiaVe", nullable = false, precision = 12, scale = 0)
    private BigDecimal giaVe;

    @Enumerated(EnumType.STRING)
    @Column(name = "TrangThai", nullable = false, length = 20)
    private TrangThaiVe trangThai;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "DonDatId", nullable = false)
    private DonDat donDat;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "SuatChieuId", nullable = false)
    private SuatChieu suatChieu;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "GheId", nullable = false)
    private Ghe ghe;

    public DonDat getDonDat() { return donDat; }
    public void setDonDat(DonDat donDat) { this.donDat = donDat; }
    public SuatChieu getSuatChieu() { return suatChieu; }
    public void setSuatChieu(SuatChieu suatChieu) { this.suatChieu = suatChieu; }
    public Ghe getGhe() { return ghe; }
    public void setGhe(Ghe ghe) { this.ghe = ghe; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getMaVe() { return maVe; }
    public void setMaVe(String maVe) { this.maVe = maVe; }

    public BigDecimal getGiaVe() { return giaVe; }
    public void setGiaVe(BigDecimal giaVe) { this.giaVe = giaVe; }

    public TrangThaiVe getTrangThai() { return trangThai; }
    public void setTrangThai(TrangThaiVe trangThai) { this.trangThai = trangThai; }

    public boolean hopLe() { return trangThai == TrangThaiVe.HOP_LE && donDat != null && donDat.getTrangThai() == TrangThaiDon.DA_THANH_TOAN; }
    public void danhDauDaSoat() { this.trangThai = TrangThaiVe.DA_SOAT; }
    public void huy() { this.trangThai = TrangThaiVe.DA_HUY; }
}
