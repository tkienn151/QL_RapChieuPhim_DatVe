package com.qlrapchieuphim.controller;

import com.qlrapchieuphim.repository.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class KiemTraKetNoiController {
    private final NguoiDungRepository nguoiDung; private final TheLoaiRepository theLoai; private final PhimRepository phim;
    private final PhongChieuRepository phong; private final GheRepository ghe; private final SuatChieuRepository suat;
    private final DonDatRepository don; private final VeRepository ve;

    public KiemTraKetNoiController(NguoiDungRepository nguoiDung, TheLoaiRepository theLoai, PhimRepository phim, PhongChieuRepository phong,
                                   GheRepository ghe, SuatChieuRepository suat, DonDatRepository don, VeRepository ve) {
        this.nguoiDung = nguoiDung; this.theLoai = theLoai; this.phim = phim; this.phong = phong; this.ghe = ghe; this.suat = suat; this.don = don; this.ve = ve;
    }

    @GetMapping("/kiem-tra")
    @ResponseBody
    public String kiemTra() {
        return "Ket noi CSDL OK. So dong: NguoiDung=" + nguoiDung.count() + ", TheLoai=" + theLoai.count() + ", Phim=" + phim.count()
                + ", PhongChieu=" + phong.count() + ", Ghe=" + ghe.count() + ", SuatChieu=" + suat.count()
                + ", DonDat=" + don.count() + ", Ve=" + ve.count();
    }
}
