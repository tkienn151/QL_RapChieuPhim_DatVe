package com.qlrapchieuphim.controller;

import com.qlrapchieuphim.entity.Phim;
import com.qlrapchieuphim.entity.enums.TrangThaiPhim;
import com.qlrapchieuphim.service.LichChieuService;
import com.qlrapchieuphim.service.PhimService;
import com.qlrapchieuphim.service.PhongChieuService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
public class PhimController {

    private static final int SO_PHIM_MOI_TRANG = 8;

    private final PhimService phimService;
    private final LichChieuService lichChieuService;
    private final PhongChieuService phongChieuService;

    public PhimController(PhimService phimService, LichChieuService lichChieuService, PhongChieuService phongChieuService) {
        this.phimService = phimService;
        this.lichChieuService = lichChieuService;
        this.phongChieuService = phongChieuService;
    }

    @GetMapping("/")
    public String trangChu(Model model) {
        model.addAttribute("dsDangChieu",
                phimService.timPhim(null, null, TrangThaiPhim.DANG_CHIEU, "ngayKhoiChieu", false, 0, 8).getContent());
        model.addAttribute("dsSapChieu",
                phimService.timPhim(null, null, TrangThaiPhim.SAP_CHIEU, "ngayKhoiChieu", true, 0, 4).getContent());
        model.addAttribute("soPhongChieu", phongChieuService.demSoPhong());
        return "trang-chu";
    }

    @GetMapping("/phim")
    public String danhSach(@RequestParam(required = false) String tuKhoa,
                           @RequestParam(required = false) Long theLoaiId,
                           @RequestParam(defaultValue = "DANG_CHIEU") String trangThai,
                           @RequestParam(defaultValue = "ngayKhoiChieu") String sapXep,
                           @RequestParam(defaultValue = "giam") String huong,
                           @RequestParam(defaultValue = "0") int trang,
                           Model model) {
        TrangThaiPhim tt = "SAP_CHIEU".equals(trangThai) ? TrangThaiPhim.SAP_CHIEU : TrangThaiPhim.DANG_CHIEU;
        boolean tang = "tang".equals(huong);
        String tk = (tuKhoa == null || tuKhoa.isBlank()) ? null : tuKhoa.trim();

        Page<Phim> page = phimService.timPhim(tk, theLoaiId, tt, sapXep, tang, trang, SO_PHIM_MOI_TRANG);

        model.addAttribute("page", page);
        model.addAttribute("dsPhim", page.getContent());
        model.addAttribute("dsTheLoai", phimService.layDanhSachTheLoai());
        model.addAttribute("tuKhoa", tk);
        model.addAttribute("theLoaiId", theLoaiId);
        model.addAttribute("trangThai", tt.name());
        model.addAttribute("sapXep", sapXep);
        model.addAttribute("huong", tang ? "tang" : "giam");
        model.addAttribute("duongDan", taoDuongDan(tk, theLoaiId, tt, sapXep, tang));
        return "phim/danh-sach";
    }

    @GetMapping("/phim/chi-tiet")
    public String chiTiet(@RequestParam Long id, Model model) {
        Phim phim = phimService.layChiTiet(id);
        model.addAttribute("phim", phim);
        model.addAttribute("dsNgayChieu", lichChieuService.layLichChieu(id));
        return "phim/chi-tiet";
    }

    @GetMapping("/phim/{id:\\d+}")
    public String chiTietTheoPath(@PathVariable Long id, Model model) {
        return chiTiet(id, model);
    }

    private String taoDuongDan(String tuKhoa, Long theLoaiId, TrangThaiPhim tt, String sapXep, boolean tang) {
        StringBuilder sb = new StringBuilder("/phim?trangThai=").append(tt.name());
        if (tuKhoa != null) sb.append("&tuKhoa=").append(URLEncoder.encode(tuKhoa, StandardCharsets.UTF_8));
        if (theLoaiId != null) sb.append("&theLoaiId=").append(theLoaiId);
        sb.append("&sapXep=").append(URLEncoder.encode(sapXep, StandardCharsets.UTF_8));
        sb.append("&huong=").append(tang ? "tang" : "giam");
        return sb.toString();
    }
}
