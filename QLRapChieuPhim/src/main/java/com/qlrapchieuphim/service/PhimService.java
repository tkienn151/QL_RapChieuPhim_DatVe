package com.qlrapchieuphim.service;

import com.qlrapchieuphim.entity.Phim;
import com.qlrapchieuphim.entity.TheLoai;
import com.qlrapchieuphim.entity.enums.TrangThaiPhim;
import com.qlrapchieuphim.repository.PhimRepository;
import com.qlrapchieuphim.repository.SuatChieuRepository;
import com.qlrapchieuphim.repository.TheLoaiRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.text.Normalizer;
import java.util.regex.Pattern;
import org.springframework.data.domain.PageImpl;

import java.util.List;

@Service
public class PhimService {

    private final PhimRepository phimRepo;
    private final TheLoaiRepository theLoaiRepo;
    private final SuatChieuRepository suatChieuRepo;

    public PhimService(PhimRepository phimRepo, TheLoaiRepository theLoaiRepo,
                       SuatChieuRepository suatChieuRepo) {
        this.phimRepo = phimRepo;
        this.theLoaiRepo = theLoaiRepo;
        this.suatChieuRepo = suatChieuRepo;
    }

    @Transactional(readOnly = true)
    public Page<Phim> timPhim(String tuKhoa, Long theLoaiId, TrangThaiPhim trangThai,
                              String sapXep, boolean tang, int trang, int kichThuoc) {
        String tk = (tuKhoa == null || tuKhoa.isBlank()) ? null : tuKhoa.trim();
        Sort.Direction huong = tang ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sort = Sort.by(huong, cotSapXep(sapXep)).and(Sort.by(Sort.Direction.ASC, "id"));
        Pageable pageable = PageRequest.of(Math.max(trang, 0), Math.max(kichThuoc, 1), sort);

        if (tk == null) {
            return phimRepo.timPhim(null, theLoaiId, trangThai, pageable);
        }

        String tkChuanHoa = boDau(tk);
        Pageable layHet = PageRequest.of(0, 1000, sort);
        List<Phim> tatCaPhim = phimRepo.timPhim(null, theLoaiId, trangThai, layHet).getContent();

        List<Phim> ketQua = tatCaPhim.stream()
                .filter(p -> p.getTenPhim() != null && boDau(p.getTenPhim()).contains(tkChuanHoa))
                .toList();

        int batDau = Math.min((int) pageable.getOffset(), ketQua.size());
        int ketThuc = Math.min(batDau + pageable.getPageSize(), ketQua.size());
        List<Phim> danhSachTrang = ketQua.subList(batDau, ketThuc);

        return new PageImpl<>(danhSachTrang, pageable, ketQua.size());
    }

    private String boDau(String chuoi) {
        if (chuoi == null) return "";
        String normalized = Normalizer.normalize(chuoi, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(normalized)
                .replaceAll("")
                .replace('đ', 'd')
                .replace('Đ', 'd')
                .toLowerCase()
                .trim();
    }

    private String cotSapXep(String sapXep) {
        if ("ngayKhoiChieu".equals(sapXep)) return "ngayKhoiChieu";
        if ("thoiLuong".equals(sapXep)) return "thoiLuong";
        return "tenPhim";
    }

    @Transactional(readOnly = true)
    public Phim layChiTiet(Long id) {
        return phimRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phim."));
    }

    @Transactional(readOnly = true)
    public List<TheLoai> layDanhSachTheLoai() {
        return theLoaiRepo.findAll(Sort.by("tenTheLoai"));
    }

    @Transactional
    public Phim them(Phim phim, Long theLoaiId) {
        kiemTra(phim);
        if (phimRepo.existsByMaPhim(phim.getMaPhim().trim())) {
            throw new LoiTruongException("maPhim","Mã phim đã tồn tại.");
        }
        phim.setMaPhim(phim.getMaPhim().trim());
        phim.setTenPhim(phim.getTenPhim().trim());
        if (phim.getPoster() != null) {
            phim.setPoster(phim.getPoster().isBlank() ? null : phim.getPoster().trim());
        }
        phim.setTheLoai(layTheLoai(theLoaiId));
        if (phim.getTrangThai() == null) phim.setTrangThai(TrangThaiPhim.SAP_CHIEU);
        return phimRepo.save(phim);
    }

    @Transactional
    public Phim capNhat(Long id, Phim moi, Long theLoaiId) {
        kiemTra(moi);
        Phim p = layChiTiet(id);
        if (phimRepo.existsByMaPhimAndIdNot(moi.getMaPhim().trim(), id)) {
            throw new LoiTruongException("maPhim","Mã phim đã tồn tại.");
        }
        p.setMaPhim(moi.getMaPhim().trim());
        p.setTenPhim(moi.getTenPhim().trim());
        p.setMoTa(moi.getMoTa());
        p.setThoiLuong(moi.getThoiLuong());
        p.setNgayKhoiChieu(moi.getNgayKhoiChieu());
        p.setDaoDien(moi.getDaoDien());
        p.setDienVien(moi.getDienVien());
        if (moi.getPoster() != null && !moi.getPoster().isBlank()) p.setPoster(moi.getPoster().trim());
        if (moi.getTrangThai() != null) p.setTrangThai(moi.getTrangThai());
        p.setTheLoai(layTheLoai(theLoaiId));
        return phimRepo.save(p);
    }

    @Transactional
    public void xoa(Long id) {
        Phim p = layChiTiet(id);
        if (suatChieuRepo.existsByPhimId(id)) {
            throw new IllegalStateException("Phim đã có suất chiếu, không thể xóa. Hãy chuyển sang trạng thái Ngừng chiếu.");
        }
        phimRepo.delete(p);
    }

    @Transactional
    public void doiTrangThai(Long id, TrangThaiPhim trangThai) {
        layChiTiet(id).setTrangThai(trangThai);
    }

    private TheLoai layTheLoai(Long theLoaiId) {
        if (theLoaiId == null) {
            throw new IllegalArgumentException("Vui lòng chọn thể loại.");
        }
        return theLoaiRepo.findById(theLoaiId)
                .orElseThrow(() -> new IllegalArgumentException("Thể loại không tồn tại."));
    }

    private void kiemTra(Phim p) {
        if (p.getMaPhim() == null || p.getMaPhim().isBlank())
            throw new LoiTruongException("maPhim","Mã phim không được để trống.");
        if (p.getMaPhim().trim().length() > 20)
            throw new LoiTruongException("maPhim","Mã phim tối đa 20 ký tự.");
        if (p.getTenPhim() == null || p.getTenPhim().isBlank())
            throw new LoiTruongException("tenPhim","Tên phim không được để trống.");
        if (p.getTenPhim().trim().length() > 150)
            throw new LoiTruongException("tenPhim","Tên phim tối đa 150 ký tự.");
        if (p.getThoiLuong() <= 0)
            throw new LoiTruongException("thoiLuong","Thời lượng phải lớn hơn 0.");
    }
}
