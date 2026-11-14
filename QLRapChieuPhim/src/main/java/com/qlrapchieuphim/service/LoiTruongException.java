package com.qlrapchieuphim.service;

import org.springframework.ui.Model;

import java.util.Map;

public class LoiTruongException extends IllegalArgumentException {
    private final String truong;

    public LoiTruongException(String truong, String thongBao) {
        super(thongBao);
        this.truong = truong;
    }

    public String getTruong() {
        return truong;
    }

    public static void dayVaoModel(Exception e, Model model) {
        model.addAttribute("loi", e.getMessage());
        if (e instanceof LoiTruongException lt) {
            model.addAttribute("loiTruong", Map.of(lt.getTruong(), lt.getMessage()));
        }
    }
}
