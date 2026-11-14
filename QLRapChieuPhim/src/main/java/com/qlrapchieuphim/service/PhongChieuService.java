package com.qlrapchieuphim.service;

import com.qlrapchieuphim.entity.Ghe;
import com.qlrapchieuphim.entity.PhongChieu;
import com.qlrapchieuphim.entity.enums.LoaiGhe;
import com.qlrapchieuphim.repository.GheRepository;
import com.qlrapchieuphim.repository.PhongChieuRepository;
import com.qlrapchieuphim.repository.SuatChieuRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PhongChieuService {

    private final PhongChieuRepository phongChieuRepository;
    private final GheRepository gheRepository;
    private final SuatChieuRepository suatChieuRepository;

    public PhongChieuService(
            PhongChieuRepository phongChieuRepository,
            GheRepository gheRepository,
            SuatChieuRepository suatChieuRepository) {

        this.phongChieuRepository = phongChieuRepository;
        this.gheRepository = gheRepository;
        this.suatChieuRepository = suatChieuRepository;
    }

    @Transactional(readOnly = true)
    public List<PhongChieu> layDanhSach() {
        return phongChieuRepository.findAll(
                org.springframework.data.domain.Sort.by("maPhong"));
    }

    @Transactional(readOnly = true)
    public long demSoPhong() {
        return phongChieuRepository.count();
    }

    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<PhongChieu> timKiem(String tuKhoa, String cot, boolean tang, int trang) {
        org.springframework.data.domain.Sort.Direction huong = tang
                ? org.springframework.data.domain.Sort.Direction.ASC : org.springframework.data.domain.Sort.Direction.DESC;
        org.springframework.data.domain.Sort sort = org.springframework.data.domain.Sort.by(huong, cot)
                .and(org.springframework.data.domain.Sort.by("id"));
        return phongChieuRepository.timKiem(tuKhoa,
                org.springframework.data.domain.PageRequest.of(Math.max(trang, 0), 10, sort));
    }

    @Transactional(readOnly = true)
    public PhongChieu layChiTiet(Long id) {
        return phongChieuRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Không tìm thấy phòng chiếu."));
    }

    @Transactional
    public PhongChieu them(PhongChieu phongChieu) {

        kiemTraThongTin(phongChieu);

        String ma = phongChieu.getMaPhong().trim();

        if (phongChieuRepository.existsByMaPhong(ma)) {
            throw new LoiTruongException("maPhong","Mã phòng đã tồn tại.");
        }

        phongChieu.setMaPhong(ma);
        phongChieu.setTenPhong(phongChieu.getTenPhong().trim());

        PhongChieu daLuu = phongChieuRepository.saveAndFlush(phongChieu);

        taoDanhSachGhe(daLuu);

        return daLuu;
    }

    @Transactional
    public PhongChieu capNhat(Long id, PhongChieu moi) {

        PhongChieu cu = layChiTiet(id);

        kiemTraThongTin(moi);

        String ma = moi.getMaPhong().trim();

        if (phongChieuRepository.existsByMaPhongAndIdNot(ma, id)) {
            throw new LoiTruongException("maPhong","Mã phòng đã được sử dụng.");
        }

        boolean doiSoDo = cu.getSoHang() != moi.getSoHang() || cu.getSoCot() != moi.getSoCot();
        if (doiSoDo && suatChieuRepository.existsByPhongChieuId(id)) {
            throw new IllegalStateException(
                    "Không thể thay đổi số hàng/số cột của phòng đã có suất chiếu.");
        }

        cu.setMaPhong(ma);
        cu.setTenPhong(moi.getTenPhong().trim());

        if (doiSoDo) {
            cu.setSoHang(moi.getSoHang());
            cu.setSoCot(moi.getSoCot());
            gheRepository.deleteByPhongChieuId(id);
            phongChieuRepository.flush();
            taoDanhSachGhe(cu);
        }

        return phongChieuRepository.save(cu);
    }

    @Transactional
    public void xoa(Long id) {

        PhongChieu phong = layChiTiet(id);

        if (suatChieuRepository.existsByPhongChieuId(id)) {
            throw new IllegalStateException(
                    "Không thể xóa phòng đã có suất chiếu.");
        }

        gheRepository.deleteByPhongChieuId(id);
        gheRepository.flush();

        phongChieuRepository.delete(phong);
    }

    private void taoDanhSachGhe(PhongChieu phong) {

        for (int h = 0; h < phong.getSoHang(); h++) {

            String tenHang = tenHang(h);

            LoaiGhe loai = (h == phong.getSoHang() - 1)
                    ? LoaiGhe.VIP
                    : LoaiGhe.THUONG;

            for (int cot = 1; cot <= phong.getSoCot(); cot++) {

                Ghe ghe = new Ghe();

                ghe.setPhongChieu(phong);
                ghe.setHang(tenHang);
                ghe.setSoGhe(cot);
                ghe.setLoaiGhe(loai);

                gheRepository.save(ghe);
            }
        }
    }

    private String tenHang(int index) {

        if (index >= 26 * 26) {
            throw new LoiTruongException("soHang","Số hàng tối đa là 676.");
        }

        StringBuilder s = new StringBuilder();

        int n = index + 1;

        while (n > 0) {
            n--;
            s.insert(0, (char) ('A' + n % 26));
            n /= 26;
        }

        return s.toString();
    }

    private void kiemTraThongTin(PhongChieu p) {

        if (p.getMaPhong() == null || p.getMaPhong().isBlank()) {
            throw new LoiTruongException("maPhong","Mã phòng không được để trống.");
        }

        if (p.getTenPhong() == null || p.getTenPhong().isBlank()) {
            throw new LoiTruongException("tenPhong","Tên phòng không được để trống.");
        }

        if (p.getMaPhong().trim().length() > 10) {
            throw new LoiTruongException("maPhong","Mã phòng tối đa 10 ký tự.");
        }

        if (p.getTenPhong().trim().length() > 50) {
            throw new LoiTruongException("tenPhong","Tên phòng tối đa 50 ký tự.");
        }

        if (p.getSoHang() <= 0 || p.getSoHang() > 676) {
            throw new LoiTruongException("soHang","Số hàng phải từ 1 đến 676.");
        }

        if (p.getSoCot() <= 0) {
            throw new LoiTruongException("soCot","Số cột phải lớn hơn 0.");
        }

        if ((long) p.getSoHang() * p.getSoCot() > 10000) {
            throw new IllegalArgumentException(
                    "Tổng số ghế không được vượt quá 10.000.");
        }
    }
}
