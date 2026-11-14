package com.qlrapchieuphim;

import com.qlrapchieuphim.config.PasswordUtil;
import com.qlrapchieuphim.config.PhienDangNhap;
import com.qlrapchieuphim.entity.DonDat;
import com.qlrapchieuphim.entity.Ghe;
import com.qlrapchieuphim.entity.NguoiDung;
import com.qlrapchieuphim.entity.Phim;
import com.qlrapchieuphim.entity.PhongChieu;
import com.qlrapchieuphim.entity.SuatChieu;
import com.qlrapchieuphim.entity.TheLoai;
import com.qlrapchieuphim.entity.enums.LoaiGhe;
import com.qlrapchieuphim.entity.enums.TrangThaiDon;
import com.qlrapchieuphim.entity.enums.TrangThaiPhim;
import com.qlrapchieuphim.entity.enums.TrangThaiSuat;
import com.qlrapchieuphim.entity.enums.VaiTro;
import com.qlrapchieuphim.repository.DonDatRepository;
import com.qlrapchieuphim.repository.GheRepository;
import com.qlrapchieuphim.repository.NguoiDungRepository;
import com.qlrapchieuphim.repository.PhimRepository;
import com.qlrapchieuphim.repository.PhongChieuRepository;
import com.qlrapchieuphim.repository.SuatChieuRepository;
import com.qlrapchieuphim.repository.TheLoaiRepository;
import com.qlrapchieuphim.repository.VeRepository;
import com.qlrapchieuphim.service.BanVeService;
import com.qlrapchieuphim.service.DatVeService;
import com.qlrapchieuphim.service.VeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class KiemThuChucNangWebMvcTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private DatVeService datVeService;
    @Autowired private BanVeService banVeService;
    @Autowired private VeService veService;
    @Autowired private NguoiDungRepository nguoiDungRepository;
    @Autowired private TheLoaiRepository theLoaiRepository;
    @Autowired private PhimRepository phimRepository;
    @Autowired private PhongChieuRepository phongChieuRepository;
    @Autowired private GheRepository gheRepository;
    @Autowired private SuatChieuRepository suatChieuRepository;
    @Autowired private DonDatRepository donDatRepository;
    @Autowired private VeRepository veRepository;

    private NguoiDung admin;
    private NguoiDung nhanVien;
    private NguoiDung khach;
    private Phim phim;
    private PhongChieu phong;
    private SuatChieu suat;
    private Ghe gheA1;

    @BeforeEach
    void setUp() {
        veRepository.deleteAll();
        donDatRepository.deleteAll();
        suatChieuRepository.deleteAll();
        gheRepository.deleteAll();
        phongChieuRepository.deleteAll();
        phimRepository.deleteAll();
        theLoaiRepository.deleteAll();
        nguoiDungRepository.deleteAll();

        admin = taoNguoiDung("admin01", "Quản Trị Viên", VaiTro.QUAN_TRI);
        nhanVien = taoNguoiDung("nv01", "Nhân Viên Quầy", VaiTro.NHAN_VIEN);
        khach = taoNguoiDung("khach01", "Khách Hàng A", VaiTro.KHACH_HANG);

        TheLoai tl = new TheLoai();
        tl.setTenTheLoai("Tâm lý");
        tl.setMoTa("Phim tâm lý xã hội");
        tl = theLoaiRepository.save(tl);

        phim = new Phim();
        phim.setMaPhim("P001");
        phim.setTenPhim("Mai");
        phim.setMoTa("Phim tâm lý tình cảm Việt Nam");
        phim.setThoiLuong(131);
        phim.setNgayKhoiChieu(LocalDate.now());
        phim.setDaoDien("Trấn Thành");
        phim.setDienVien("Phương Anh Đào, Tuấn Trần");
        phim.setTrangThai(TrangThaiPhim.DANG_CHIEU);
        phim.setTheLoai(tl);
        phim = phimRepository.save(phim);

        phong = new PhongChieu();
        phong.setMaPhong("PC01");
        phong.setTenPhong("Phòng chiếu 01");
        phong.setSoHang(6);
        phong.setSoCot(8);
        phong = phongChieuRepository.save(phong);

        gheA1 = new Ghe();
        gheA1.setPhongChieu(phong);
        gheA1.setHang("A");
        gheA1.setSoGhe(1);
        gheA1.setLoaiGhe(LoaiGhe.THUONG);
        gheA1 = gheRepository.save(gheA1);

        suat = new SuatChieu();
        suat.setPhim(phim);
        suat.setPhongChieu(phong);
        suat.setGioBatDau(LocalDateTime.now().plusHours(3));
        suat.setGioKetThuc(LocalDateTime.now().plusHours(5));
        suat.setGiaThuong(new BigDecimal("75000"));
        suat.setGiaVip(new BigDecimal("95000"));
        suat.setTrangThai(TrangThaiSuat.MO_BAN);
        suat = suatChieuRepository.save(suat);
    }

    private NguoiDung taoNguoiDung(String u, String ten, VaiTro vt) {
        NguoiDung nd = new NguoiDung();
        nd.setTenDangNhap(u);
        nd.setMatKhau(PasswordUtil.bam("123456"));
        nd.setHoTen(ten);
        nd.setEmail(u + "@cinema.vn");
        nd.setSoDienThoai("0901234567");
        nd.setVaiTro(vt);
        nd.setHoatDong(true);
        return nguoiDungRepository.save(nd);
    }

    private MockHttpSession taoSession(NguoiDung nd) {
        MockHttpSession s = new MockHttpSession();
        s.setAttribute(PhienDangNhap.KEY, nd);
        return s;
    }

    @Test
    @DisplayName("Kiểm thử giao diện Khách hàng, Nhân viên và Quản trị viên hoạt động và phân quyền đúng")
    void kiemThuTatCaManHinhThymeleaf() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Mai")));

        mockMvc.perform(get("/phim"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Mai")));

        mockMvc.perform(get("/phim/chi-tiet").param("id", String.valueOf(phim.getId())))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Trấn Thành")));

        mockMvc.perform(get("/dang-nhap")).andExpect(status().isOk());
        mockMvc.perform(get("/dang-ky")).andExpect(status().isOk());

        mockMvc.perform(get("/dat-ve/chon-ghe").param("suatId", String.valueOf(suat.getId())))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dang-nhap?canDangNhap=1&quayLai=%2Fdat-ve%2Fchon-ghe"));

        MockHttpSession sessionKhach = taoSession(khach);
        mockMvc.perform(get("/dat-ve/chon-ghe").param("suatId", String.valueOf(suat.getId())).session(sessionKhach))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("A1")));

        DonDat don = datVeService.giuGhe(suat.getId(), Collections.singletonList(gheA1.getId()), khach.getId());
        mockMvc.perform(get("/dat-ve/thanh-toan").param("maDon", don.getMaDon()).session(sessionKhach))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(don.getMaDon())));

        mockMvc.perform(post("/dat-ve/thanh-toan").param("maDon", don.getMaDon()).session(sessionKhach))
                .andExpect(status().is3xxRedirection());

        mockMvc.perform(get("/dat-ve/ket-qua").param("maDon", don.getMaDon()).session(sessionKhach))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(don.getMaDon())))
                .andExpect(content().string(containsString("In vé")))
                .andExpect(content().string(containsString("phieu-in-ve")))
                .andExpect(content().string(containsString("MÃ SOÁT VÉ")));

        mockMvc.perform(get("/lich-su").session(sessionKhach)).andExpect(status().isOk());
        mockMvc.perform(get("/lich-su/chi-tiet").param("maDon", don.getMaDon()).session(sessionKhach))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("In vé")))
                .andExpect(content().string(containsString("phieu-in-ve")))
                .andExpect(content().string(containsString("VÉ XEM PHIM")));
        mockMvc.perform(get("/ho-so").session(sessionKhach)).andExpect(status().isOk());

        mockMvc.perform(get("/admin/phim").session(sessionKhach))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/khong-co-quyen"));

        mockMvc.perform(get("/khong-co-quyen").session(sessionKhach))
                .andExpect(status().isForbidden())
                .andExpect(content().string(containsString("Không có quyền truy cập")));

        MockHttpSession sessionNV = taoSession(nhanVien);
        mockMvc.perform(get("/quay/ban-ve").param("suatId", String.valueOf(suat.getId())).session(sessionNV))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Chọn Suất Chiếu Đang Mở Bán")));

        mockMvc.perform(get("/tra-cuu").param("maDon", don.getMaDon()).session(sessionNV))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(don.getMaDon())))
                .andExpect(content().string(containsString("window.print()")))
                .andExpect(content().string(containsString("In vé")))
                .andExpect(content().string(containsString("phieu-in-ve")))
                .andExpect(content().string(containsString("VÉ XEM PHIM")))
                .andExpect(content().string(containsString("MÃ SOÁT VÉ")))
                .andExpect(content().string(containsString("Hủy đơn")))
                .andExpect(content().string(not(containsString("Xác nhận thu tiền"))))
                .andExpect(content().string(not(containsString("Hủy giữ ghế"))));

        MockHttpSession sessionAdmin = taoSession(admin);
        mockMvc.perform(get("/admin/thong-ke").session(sessionAdmin)).andExpect(status().isOk());
        mockMvc.perform(get("/admin/phim").session(sessionAdmin)).andExpect(status().isOk());
        mockMvc.perform(get("/admin/phim/them").session(sessionAdmin)).andExpect(status().isOk());
        mockMvc.perform(get("/admin/phim/sua/" + phim.getId()).session(sessionAdmin)).andExpect(status().isOk());
        mockMvc.perform(get("/admin/theloai").session(sessionAdmin)).andExpect(status().isOk());
        mockMvc.perform(get("/admin/theloai/them").session(sessionAdmin)).andExpect(status().isOk());
        mockMvc.perform(get("/admin/phong").session(sessionAdmin)).andExpect(status().isOk());
        mockMvc.perform(get("/admin/phong/them").session(sessionAdmin)).andExpect(status().isOk());
        mockMvc.perform(get("/admin/phong/sua/" + phong.getId()).session(sessionAdmin)).andExpect(status().isOk());
        mockMvc.perform(get("/admin/suat").session(sessionAdmin)).andExpect(status().isOk());
        mockMvc.perform(get("/admin/suat/them").session(sessionAdmin)).andExpect(status().isOk());
        mockMvc.perform(get("/admin/suat/sua/" + suat.getId()).session(sessionAdmin)).andExpect(status().isOk());
        mockMvc.perform(get("/admin/taikhoan").session(sessionAdmin)).andExpect(status().isOk());
        mockMvc.perform(get("/admin/taikhoan/them").session(sessionAdmin)).andExpect(status().isOk());
    }

    @Test
    @DisplayName("Tra cứu quầy: chỉ hiện In vé khi đã thanh toán và cho hủy giữ ghế tạm bất kể quy tắc 60 phút")
    void kiemThuTraCuuQuayDonGiuGheVaHetHan() throws Exception {
        MockHttpSession sessionNV = taoSession(nhanVien);

        SuatChieu suatGan = new SuatChieu();
        suatGan.setPhim(phim);
        suatGan.setPhongChieu(phong);
        suatGan.setGioBatDau(LocalDateTime.now().plusMinutes(30));
        suatGan.setGioKetThuc(LocalDateTime.now().plusMinutes(90));
        suatGan.setGiaThuong(new BigDecimal("75000"));
        suatGan.setGiaVip(new BigDecimal("95000"));
        suatGan.setTrangThai(TrangThaiSuat.MO_BAN);
        suatGan = suatChieuRepository.save(suatGan);

        DonDat donTam = datVeService.giuGhe(suatGan.getId(), Collections.singletonList(gheA1.getId()), khach.getId());
        mockMvc.perform(get("/tra-cuu").param("maDon", donTam.getMaDon()).session(sessionNV))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(donTam.getMaDon())))
                .andExpect(content().string(containsString("Xác nhận thu tiền")))
                .andExpect(content().string(containsString("Hủy giữ ghế")))
                .andExpect(content().string(containsString("action=\"/tra-cuu/huy-giu-ghe\"")))
                .andExpect(content().string(not(containsString("window.print()"))))
                .andExpect(content().string(not(containsString("In vé"))))
                .andExpect(content().string(not(containsString("phieu-in-ve"))))
                .andExpect(content().string(not(containsString("Hủy đơn"))))
                .andExpect(content().string(not(containsString("action=\"/tra-cuu/huy\""))));

        mockMvc.perform(post("/tra-cuu/huy-giu-ghe").param("maDon", donTam.getMaDon()).session(sessionNV))
                .andExpect(status().is3xxRedirection());

        DonDat donSauHuy = donDatRepository.findById(donTam.getId()).orElseThrow();
        assertEquals(TrangThaiDon.DA_HUY, donSauHuy.getTrangThai());
        assertTrue(veRepository.findGheDaDat(suatGan.getId()).isEmpty());

        mockMvc.perform(get("/tra-cuu").param("maDon", donTam.getMaDon()).session(sessionNV))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("DA_HUY")))
                .andExpect(content().string(not(containsString("window.print()"))))
                .andExpect(content().string(not(containsString("In vé"))))
                .andExpect(content().string(not(containsString("phieu-in-ve"))))
                .andExpect(content().string(not(containsString("Xác nhận thu tiền"))))
                .andExpect(content().string(not(containsString("Hủy giữ ghế"))))
                .andExpect(content().string(not(containsString("Hủy đơn"))));

        DonDat donQuaHan = datVeService.giuGhe(suatGan.getId(), Collections.singletonList(gheA1.getId()), khach.getId());
        donQuaHan.setHanThanhToan(LocalDateTime.now().minusSeconds(5));
        donDatRepository.save(donQuaHan);

        mockMvc.perform(get("/tra-cuu").param("maDon", donQuaHan.getMaDon()).session(sessionNV))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("HET_HAN")))
                .andExpect(content().string(not(containsString("window.print()"))))
                .andExpect(content().string(not(containsString("In vé"))))
                .andExpect(content().string(not(containsString("phieu-in-ve"))))
                .andExpect(content().string(not(containsString("Xác nhận thu tiền"))))
                .andExpect(content().string(not(containsString("Hủy giữ ghế"))))
                .andExpect(content().string(not(containsString("Hủy đơn"))));

        DonDat donSauHetHan = donDatRepository.findById(donQuaHan.getId()).orElseThrow();
        assertEquals(TrangThaiDon.HET_HAN, donSauHetHan.getTrangThai());
        assertTrue(veRepository.findGheDaDat(suatGan.getId()).isEmpty());

        String ketQuaUrl = mockMvc.perform(post("/quay/ban-ve")
                        .param("suatId", String.valueOf(suatGan.getId()))
                        .param("dsGheId", String.valueOf(gheA1.getId()))
                        .session(sessionNV))
                .andExpect(status().is3xxRedirection())
                .andReturn()
                .getResponse()
                .getRedirectedUrl();

        assertTrue(ketQuaUrl != null && ketQuaUrl.startsWith("/quay/ket-qua?maDon="));
        mockMvc.perform(get(ketQuaUrl).session(sessionNV))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("phieu-in-ve")))
                .andExpect(content().string(containsString("VÉ XEM PHIM")))
                .andExpect(content().string(containsString("MÃ SOÁT VÉ")))
                .andExpect(content().string(containsString("Khách vãng lai")));
    }

    @Test
    @DisplayName("Kiểm thử bổ sung: tra cứu quầy không khớp nhầm NV, lịch sử dọn đơn quá hạn và không in đơn chưa thanh toán")
    void traCuuKhongKhopNhanVien_va_LichSuDonQuaHan() throws Exception {
        nhanVien.setSoDienThoai("0988777666");
        nguoiDungRepository.save(nhanVien);

        Ghe gheA2 = new Ghe();
        gheA2.setPhongChieu(phong);
        gheA2.setHang("A");
        gheA2.setSoGhe(2);
        gheA2.setLoaiGhe(LoaiGhe.THUONG);
        gheA2 = gheRepository.save(gheA2);

        Ghe gheA3 = new Ghe();
        gheA3.setPhongChieu(phong);
        gheA3.setHang("A");
        gheA3.setSoGhe(3);
        gheA3.setLoaiGhe(LoaiGhe.THUONG);
        gheA3 = gheRepository.save(gheA3);

        DonDat donQuayCoTen = banVeService.banVeTaiQuay(
                suat.getId(), Collections.singletonList(gheA1.getId()), nhanVien.getId(), "Trần Thị B", "0912345678");
        DonDat donQuayVangLai = banVeService.banVeTaiQuay(
                suat.getId(), Collections.singletonList(gheA2.getId()), nhanVien.getId(), null, null);
        DonDat donWebTam = datVeService.giuGhe(suat.getId(), Collections.singletonList(gheA3.getId()), khach.getId());

        MockHttpSession sessionNV = taoSession(nhanVien);
        MockHttpSession sessionKhach = taoSession(khach);

        mockMvc.perform(get("/tra-cuu").param("tuKhoa", "Nhân Viên Quầy").session(sessionNV))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString(donQuayCoTen.getMaDon()))))
                .andExpect(content().string(not(containsString(donQuayVangLai.getMaDon()))));

        mockMvc.perform(get("/tra-cuu").param("tuKhoa", "0988777666").session(sessionNV))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString(donQuayCoTen.getMaDon()))))
                .andExpect(content().string(not(containsString(donQuayVangLai.getMaDon()))));

        mockMvc.perform(get("/tra-cuu").param("tuKhoa", "Trần Thị B").session(sessionNV))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(donQuayCoTen.getMaDon())))
                .andExpect(content().string(not(containsString(donQuayVangLai.getMaDon()))));

        mockMvc.perform(get("/tra-cuu").param("tuKhoa", donQuayVangLai.getMaDon()).session(sessionNV))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(donQuayVangLai.getMaDon())));

        mockMvc.perform(get("/tra-cuu").param("tuKhoa", "Khách Hàng A").session(sessionNV))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(donWebTam.getMaDon())))
                .andExpect(content().string(not(containsString(donQuayCoTen.getMaDon()))));

        mockMvc.perform(get("/lich-su/chi-tiet").param("maDon", donWebTam.getMaDon()).session(sessionKhach))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("In vé"))))
                .andExpect(content().string(not(containsString("phieu-in-ve"))));

        donWebTam.setHanThanhToan(LocalDateTime.now().minusSeconds(5));
        donWebTam = donDatRepository.save(donWebTam);

        DonDat donQuaHanNapLai = veService.layChiTietDon(donWebTam.getMaDon(), khach.getId());
        assertFalse(veService.coTheHuy(donQuaHanNapLai));
        assertThrows(IllegalStateException.class, () -> veService.huyDonDat(donQuaHanNapLai.getId(), khach.getId()));
        assertEquals(TrangThaiDon.HET_HAN, donDatRepository.findById(donWebTam.getId()).orElseThrow().getTrangThai());
        assertFalse(veRepository.findGheDaDat(suat.getId()).contains(gheA3.getId()));

        DonDat donQuaHanQuaLichSu = datVeService.giuGhe(suat.getId(), Collections.singletonList(gheA3.getId()), khach.getId());
        donQuaHanQuaLichSu.setHanThanhToan(LocalDateTime.now().minusSeconds(5));
        donQuaHanQuaLichSu = donDatRepository.save(donQuaHanQuaLichSu);

        mockMvc.perform(get("/lich-su/chi-tiet").param("maDon", donQuaHanQuaLichSu.getMaDon()).session(sessionKhach))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Hết hạn")))
                .andExpect(content().string(not(containsString("Xác nhận hủy đơn vé"))))
                .andExpect(content().string(not(containsString("In vé"))))
                .andExpect(content().string(not(containsString("phieu-in-ve"))));

        assertEquals(TrangThaiDon.HET_HAN, donDatRepository.findById(donQuaHanQuaLichSu.getId()).orElseThrow().getTrangThai());
        assertFalse(veRepository.findGheDaDat(suat.getId()).contains(gheA3.getId()));
    }
}
