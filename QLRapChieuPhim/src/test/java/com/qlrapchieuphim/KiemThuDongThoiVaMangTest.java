package com.qlrapchieuphim;

import com.qlrapchieuphim.config.PasswordUtil;
import com.qlrapchieuphim.entity.DonDat;
import com.qlrapchieuphim.entity.Ghe;
import com.qlrapchieuphim.entity.NguoiDung;
import com.qlrapchieuphim.entity.Phim;
import com.qlrapchieuphim.entity.PhongChieu;
import com.qlrapchieuphim.entity.SuatChieu;
import com.qlrapchieuphim.entity.TheLoai;
import com.qlrapchieuphim.entity.Ve;
import com.qlrapchieuphim.entity.enums.LoaiGhe;
import com.qlrapchieuphim.entity.enums.TrangThaiDon;
import com.qlrapchieuphim.entity.enums.TrangThaiPhim;
import com.qlrapchieuphim.entity.enums.TrangThaiSuat;
import com.qlrapchieuphim.entity.enums.TrangThaiVe;
import com.qlrapchieuphim.entity.enums.VaiTro;
import com.qlrapchieuphim.repository.DonDatRepository;
import com.qlrapchieuphim.repository.GheRepository;
import com.qlrapchieuphim.repository.NguoiDungRepository;
import com.qlrapchieuphim.repository.PhimRepository;
import com.qlrapchieuphim.repository.PhongChieuRepository;
import com.qlrapchieuphim.repository.SuatChieuRepository;
import com.qlrapchieuphim.repository.TheLoaiRepository;
import com.qlrapchieuphim.repository.VeRepository;
import com.qlrapchieuphim.scheduler.NhaGheScheduler;
import com.qlrapchieuphim.server.SoatVeServer;
import com.qlrapchieuphim.service.DatVeService;
import com.qlrapchieuphim.service.PhimService;
import com.qlrapchieuphim.service.PhongChieuService;
import com.qlrapchieuphim.service.SoatVeService;
import com.qlrapchieuphim.service.SuatChieuService;
import com.qlrapchieuphim.service.VeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class KiemThuDongThoiVaMangTest {
    private static final Logger log = LoggerFactory.getLogger(KiemThuDongThoiVaMangTest.class);

    @Autowired private DatVeService datVeService;
    @Autowired private SoatVeService soatVeService;
    @Autowired private VeService veService;
    @Autowired private SuatChieuService suatChieuService;
    @Autowired private PhongChieuService phongChieuService;
    @Autowired private PhimService phimService;
    @Autowired private NhaGheScheduler nhaGheScheduler;
    @Autowired private SoatVeServer soatVeServer;

    @Autowired private NguoiDungRepository nguoiDungRepository;
    @Autowired private TheLoaiRepository theLoaiRepository;
    @Autowired private PhimRepository phimRepository;
    @Autowired private PhongChieuRepository phongChieuRepository;
    @Autowired private GheRepository gheRepository;
    @Autowired private SuatChieuRepository suatChieuRepository;
    @Autowired private DonDatRepository donDatRepository;
    @Autowired private VeRepository veRepository;

    private NguoiDung khachHang;
    private SuatChieu suatChieu;
    private Ghe gheMucTieu;

    @BeforeEach
    void chuanBiDuLieu() {
        veRepository.deleteAll();
        donDatRepository.deleteAll();
        suatChieuRepository.deleteAll();
        gheRepository.deleteAll();
        phongChieuRepository.deleteAll();
        phimRepository.deleteAll();
        theLoaiRepository.deleteAll();
        nguoiDungRepository.deleteAll();

        NguoiDung nd = new NguoiDung();
        nd.setTenDangNhap("khach_test");
        nd.setMatKhau(PasswordUtil.bam("123456"));
        nd.setHoTen("Khách Hàng Kiểm Thử");
        nd.setEmail("khach@test.vn");
        nd.setSoDienThoai("0901234567");
        nd.setVaiTro(VaiTro.KHACH_HANG);
        nd.setHoatDong(true);
        khachHang = nguoiDungRepository.save(nd);

        TheLoai tl = new TheLoai();
        tl.setTenTheLoai("Hành động");
        tl.setMoTa("Phim hành động kịch tính");
        tl = theLoaiRepository.save(tl);

        Phim phim = new Phim();
        phim.setMaPhim("P_TEST_01");
        phim.setTenPhim("Lật Mặt 7: Một Điều Ước");
        phim.setThoiLuong(120);
        phim.setNgayKhoiChieu(LocalDate.now());
        phim.setTrangThai(TrangThaiPhim.DANG_CHIEU);
        phim.setTheLoai(tl);
        phim = phimRepository.save(phim);

        PhongChieu pc = new PhongChieu();
        pc.setMaPhong("PC_TEST");
        pc.setTenPhong("Phòng chiếu số 1");
        pc.setSoHang(6);
        pc.setSoCot(8);
        pc = phongChieuRepository.save(pc);

        Ghe g = new Ghe();
        g.setPhongChieu(pc);
        g.setHang("A");
        g.setSoGhe(5);
        g.setLoaiGhe(LoaiGhe.THUONG);
        gheMucTieu = gheRepository.save(g);

        SuatChieu sc = new SuatChieu();
        sc.setPhim(phim);
        sc.setPhongChieu(pc);
        sc.setGioBatDau(LocalDateTime.now().plusMinutes(30));
        sc.setGioKetThuc(LocalDateTime.now().plusMinutes(150));
        sc.setGiaThuong(new BigDecimal("75000"));
        sc.setGiaVip(new BigDecimal("95000"));
        sc.setTrangThai(TrangThaiSuat.MO_BAN);
        suatChieu = suatChieuRepository.save(sc);
    }

    @Test
    @Order(1)
    @DisplayName("Kiểm thử 50 luồng cùng đặt 1 ghế tại cùng 1 thời điểm (ExecutorService + CountDownLatch + PESSIMISTIC_WRITE)")
    void kiemThu50LuongCungDatMotGhe() throws InterruptedException {
        int soLuong = 50;
        ExecutorService executor = Executors.newFixedThreadPool(soLuong);
        CountDownLatch sanSang = new CountDownLatch(soLuong);
        CountDownLatch xuatPhat = new CountDownLatch(1);
        CountDownLatch ketThuc = new CountDownLatch(soLuong);

        AtomicInteger soThanhCong = new AtomicInteger(0);
        AtomicInteger soBiTuChoi = new AtomicInteger(0);

        for (int i = 1; i <= soLuong; i++) {
            final int thuTu = i;
            executor.submit(() -> {
                Thread.currentThread().setName("Luong-DatVe-" + String.format("%02d", thuTu));
                try {
                    sanSang.countDown();
                    xuatPhat.await();

                    DonDat don = datVeService.giuGhe(
                            suatChieu.getId(),
                            Collections.singletonList(gheMucTieu.getId()),
                            khachHang.getId());
                    soThanhCong.incrementAndGet();
                    log.info("[KET QUA] {} -> DAT THANH CONG (maDon={})", Thread.currentThread().getName(), don.getMaDon());
                } catch (Exception e) {
                    soBiTuChoi.incrementAndGet();
                    log.info("[KET QUA] {} -> BI TU CHOI ({})", Thread.currentThread().getName(), e.getMessage());
                } finally {
                    ketThuc.countDown();
                }
            });
        }

        assertTrue(sanSang.await(5, TimeUnit.SECONDS));
        xuatPhat.countDown();
        assertTrue(ketThuc.await(20, TimeUnit.SECONDS));
        executor.shutdown();

        List<Long> dsGheDaDat = veRepository.findGheDaDat(suatChieu.getId());
        log.info("==================================================================");
        log.info("TONG KET KIEM THU 50 LUONG DONG THOI CUNG DAT GHE A5:");
        log.info("- So luong dat ghe thanh cong : {}", soThanhCong.get());
        log.info("- So luong bi tu choi         : {}", soBiTuChoi.get());
        log.info("- So ve con hieu luc trong DB : {}", dsGheDaDat.size());
        log.info("==================================================================");

        assertEquals(1, soThanhCong.get(), "Chỉ đúng 1 luồng được phép giữ ghế thành công!");
        assertEquals(49, soBiTuChoi.get(), "49 luồng còn lại phải bị từ chối!");
        assertEquals(1, dsGheDaDat.size(), "Trong CSDL chỉ được có duy nhất 1 vé hợp lệ cho ghế A5!");
    }

    @Test
    @Order(2)
    @DisplayName("Kiểm thử tác vụ định kỳ @Scheduled tự động giải phóng đơn giữ tạm quá hạn 5 phút")
    void kiemThuNhaGheQuaHan5Phut() {
        DonDat donTam = datVeService.giuGhe(
                suatChieu.getId(),
                Collections.singletonList(gheMucTieu.getId()),
                khachHang.getId());
        assertEquals(TrangThaiDon.CHO_THANH_TOAN, donTam.getTrangThai());

        donTam.setHanThanhToan(LocalDateTime.now().minusMinutes(1));
        donDatRepository.saveAndFlush(donTam);

        nhaGheScheduler.nhaGheQuaHan();

        DonDat donSauKhiQuet = donDatRepository.findByMaDon(donTam.getMaDon()).orElseThrow();
        List<Ve> dsVe = veRepository.findByDonDatId(donSauKhiQuet.getId());
        assertEquals(TrangThaiDon.HET_HAN, donSauKhiQuet.getTrangThai(), "Đơn quá hạn phải chuyển sang HET_HAN");
        assertEquals(TrangThaiVe.DA_HUY, dsVe.get(0).getTrangThai(), "Vé của đơn quá hạn phải chuyển sang DA_HUY");

        DonDat donMoi = datVeService.giuGhe(
                suatChieu.getId(),
                Collections.singletonList(gheMucTieu.getId()),
                khachHang.getId());
        assertEquals(TrangThaiDon.CHO_THANH_TOAN, donMoi.getTrangThai());
    }

    @Test
    @Order(3)
    @DisplayName("Kiểm thử đồng thời 4 Client kết nối TCP Socket cùng lúc và cùng soát 1 mã vé")
    void kiemThu4ClientSocketCungSoatMotVe() throws Exception {
        DonDat don = datVeService.giuGhe(
                suatChieu.getId(),
                Collections.singletonList(gheMucTieu.getId()),
                khachHang.getId());
        datVeService.thanhToan(don.getMaDon());
        List<Ve> dsVe = veRepository.findByDonDatId(don.getId());
        String maVe = dsVe.get(0).getMaVe();

        int congSocket = soatVeServer.getPort();
        int soClient = 4;
        ExecutorService pool = Executors.newFixedThreadPool(soClient);
        CountDownLatch sanSang = new CountDownLatch(soClient);
        CountDownLatch xuatPhat = new CountDownLatch(1);
        CountDownLatch ketThuc = new CountDownLatch(soClient);

        List<String> phanHoiInfo = Collections.synchronizedList(new ArrayList<>());
        List<String> phanHoiCheckin = Collections.synchronizedList(new ArrayList<>());

        for (int i = 1; i <= soClient; i++) {
            final int idClient = i;
            pool.submit(() -> {
                Thread.currentThread().setName("SocketClient-" + idClient);
                try (Socket socket = new Socket("localhost", congSocket);
                     BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                     PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8)), true)) {

                    sanSang.countDown();
                    xuatPhat.await();

                    out.println("INFO|" + maVe);
                    String info = in.readLine();
                    phanHoiInfo.add(info);

                    out.println("CHECKIN|" + maVe);
                    String checkin = in.readLine();
                    phanHoiCheckin.add(checkin);
                    log.info("[{}] CHECKIN|{} -> {}", Thread.currentThread().getName(), maVe, checkin);

                    out.println("QUIT");
                } catch (Exception e) {
                    log.error("Loi client socket: " + e.getMessage(), e);
                } finally {
                    ketThuc.countDown();
                }
            });
        }

        assertTrue(sanSang.await(5, TimeUnit.SECONDS));
        xuatPhat.countDown();
        assertTrue(ketThuc.await(10, TimeUnit.SECONDS));
        pool.shutdown();

        long soCheckinThanhCong = phanHoiCheckin.stream().filter(s -> s.startsWith("OK|DA_SOAT|")).count();
        long soCheckinDaSuDung = phanHoiCheckin.stream().filter("ERR|DA_SU_DUNG"::equals).count();

        assertEquals(4, phanHoiInfo.size());
        assertEquals(1, soCheckinThanhCong, "Chỉ đúng 1 client soát vé thành công (OK|DA_SOAT)!");
        assertEquals(3, soCheckinDaSuDung, "3 client còn lại phải nhận ERR|DA_SU_DUNG!");

        Ve veSauSoat = veRepository.findByMaVe(maVe).orElseThrow();
        assertEquals(TrangThaiVe.DA_SOAT, veSauSoat.getTrangThai());
    }

    @Test
    @Order(4)
    @DisplayName("Kiểm thử ràng buộc nghiệp vụ: chặn trùng giờ suất chiếu, chặn xóa bản ghi đang tham chiếu, chặn hủy vé sát giờ")
    void kiemThuRangBuocNghiepVu() {
        SuatChieu suatTrung = new SuatChieu();
        suatTrung.setGioBatDau(suatChieu.getGioBatDau().plusMinutes(30));
        suatTrung.setGiaThuong(new BigDecimal("75000"));
        suatTrung.setGiaVip(new BigDecimal("95000"));
        assertThrows(RuntimeException.class, () -> suatChieuService.taoSuatChieu(
                        suatTrung,
                        suatChieu.getPhim().getId(),
                        suatChieu.getPhongChieu().getId()),
                "Phải chặn lập suất chiếu trùng giờ trong cùng một phòng!");

        assertThrows(RuntimeException.class, () -> phimService.xoa(suatChieu.getPhim().getId()),
                "Phải chặn xóa bộ phim đã có suất chiếu!");

        PhongChieu formPhong = new PhongChieu();
        formPhong.setMaPhong("PC_MOI");
        formPhong.setTenPhong("Phòng Mới Kiểm Thử");
        formPhong.setSoHang(4);
        formPhong.setSoCot(5);
        PhongChieu phongMoi = phongChieuService.them(formPhong);
        assertEquals(20, gheRepository.countByPhongChieuId(phongMoi.getId()));
        phongChieuService.xoa(phongMoi.getId());
        assertEquals(0, gheRepository.countByPhongChieuId(phongMoi.getId()));

        DonDat don = datVeService.giuGhe(
                suatChieu.getId(),
                Collections.singletonList(gheMucTieu.getId()),
                khachHang.getId());
        datVeService.thanhToan(don.getMaDon());
        assertThrows(RuntimeException.class, () -> veService.huyDon(don.getMaDon(), khachHang.getId(), false),
                "Phải chặn hủy vé khi còn ít hơn 60 phút trước giờ chiếu!");
    }
}
