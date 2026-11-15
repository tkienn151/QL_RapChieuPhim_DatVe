package com.qlrapchieuphim.service;

import org.springframework.ui.Model;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class LoiTruongException extends IllegalArgumentException {
    private final String truong;
    private final Map<String, String> dsLoi;

    public LoiTruongException(String truong, String thongBao) {
        super(thongBao);
        this.truong = truong;
        this.dsLoi = null;
    }

    public LoiTruongException(Map<String, String> dsLoi, String thongBao) {
        super(thongBao);
        this.truong = null;
        this.dsLoi = dsLoi == null ? Collections.emptyMap() : Collections.unmodifiableMap(new LinkedHashMap<>(dsLoi));
    }

    public String getTruong() {
        return truong;
    }

    public Map<String, String> getDsLoi() {
        return dsLoi == null
                ? (truong == null ? Collections.emptyMap() : Map.of(truong, getMessage()))
                : dsLoi;
    }

    public static void dayVaoModel(Exception e, Model model) {
        model.addAttribute("loi", e.getMessage());
        if (e instanceof LoiTruongException lt) {
            Map<String, String> loiTheoTruong = lt.getDsLoi();
            if (!loiTheoTruong.isEmpty()) {
                model.addAttribute("loiTruong", loiTheoTruong);
            }
        }
    }
}
