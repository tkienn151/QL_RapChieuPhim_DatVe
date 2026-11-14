package com.qlrapchieuphim.service;

import com.qlrapchieuphim.entity.TheLoai;
import com.qlrapchieuphim.repository.PhimRepository;
import com.qlrapchieuphim.repository.TheLoaiRepository;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TheLoaiService {

    private final TheLoaiRepository theLoaiRepository;
    private final PhimRepository phimRepository;

    public TheLoaiService(
            TheLoaiRepository theLoaiRepository,
            PhimRepository phimRepository) {

        this.theLoaiRepository = theLoaiRepository;
        this.phimRepository = phimRepository;
    }

    @Transactional(readOnly = true)
    public List<TheLoai> layDanhSach() {
        return theLoaiRepository.findAll(Sort.by("tenTheLoai"));
    }

    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<TheLoai> timKiem(String tuKhoa, String cot, boolean tang, int trang) {
        Sort sort = Sort.by(tang ? Sort.Direction.ASC : Sort.Direction.DESC, cot).and(Sort.by("id"));
        return theLoaiRepository.timKiem(tuKhoa,
                org.springframework.data.domain.PageRequest.of(Math.max(trang, 0), 10, sort));
    }

    @Transactional(readOnly = true)
    public TheLoai layChiTiet(Long id) {
        return theLoaiRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Không tìm thấy thể loại."));
    }

    @Transactional
    public TheLoai them(TheLoai theLoai) {

        kiemTra(theLoai);

        String ten = theLoai.getTenTheLoai().trim();

        if (theLoaiRepository.existsByTenTheLoaiIgnoreCase(ten)) {
            throw new LoiTruongException("tenTheLoai","Tên thể loại đã tồn tại.");
        }

        theLoai.setTenTheLoai(ten);

        if (theLoai.getMoTa() != null) {
            theLoai.setMoTa(theLoai.getMoTa().trim());
        }

        return theLoaiRepository.save(theLoai);
    }

    @Transactional
    public TheLoai capNhat(Long id, TheLoai moi) {

        TheLoai cu = layChiTiet(id);

        kiemTra(moi);

        String ten = moi.getTenTheLoai().trim();

        if (theLoaiRepository.existsByTenTheLoaiIgnoreCaseAndIdNot(ten, id)) {
            throw new LoiTruongException("tenTheLoai","Tên thể loại đã được sử dụng.");
        }

        cu.setTenTheLoai(ten);

        cu.setMoTa(
                moi.getMoTa() == null ? null : moi.getMoTa().trim());

        return theLoaiRepository.save(cu);
    }

    @Transactional
    public void xoa(Long id) {

        TheLoai theLoai = layChiTiet(id);

        if (phimRepository.existsByTheLoaiId(id)) {
            throw new IllegalStateException(
                    "Không thể xóa thể loại đang có phim.");
        }

        theLoaiRepository.delete(theLoai);
    }

    private void kiemTra(TheLoai t) {

        if (t.getTenTheLoai() == null || t.getTenTheLoai().isBlank()) {
            throw new LoiTruongException("tenTheLoai",
                    "Tên thể loại không được để trống.");
        }

        if (t.getTenTheLoai().trim().length() > 50) {
            throw new LoiTruongException("tenTheLoai",
                    "Tên thể loại tối đa 50 ký tự.");
        }

        if (t.getMoTa() != null && t.getMoTa().trim().length() > 255) {
            throw new LoiTruongException("moTa",
                    "Mô tả tối đa 255 ký tự.");
        }
    }
}
