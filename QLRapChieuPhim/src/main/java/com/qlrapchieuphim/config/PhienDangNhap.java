package com.qlrapchieuphim.config;

import com.qlrapchieuphim.entity.NguoiDung;
import com.qlrapchieuphim.entity.enums.VaiTro;
import jakarta.servlet.http.HttpSession;

public final class PhienDangNhap {
    public static final String KEY = "nguoiDung";

    private PhienDangNhap() {}

    public static NguoiDung layNguoiDung(HttpSession session) {
        if (session == null) return null;
        Object o = session.getAttribute(KEY);
        return (o instanceof NguoiDung) ? (NguoiDung) o : null;
    }

    public static boolean daDangNhap(HttpSession session) {
        return layNguoiDung(session) != null;
    }

    public static boolean coVaiTro(VaiTro vaiTro, VaiTro... dsVaiTro) {
        if (vaiTro == null) return false;
        for (VaiTro v : dsVaiTro) {
            if (vaiTro == v) return true;
        }
        return false;
    }

    public static boolean coVaiTro(HttpSession session, VaiTro... dsVaiTro) {
        NguoiDung nd = layNguoiDung(session);
        return nd != null && coVaiTro(nd.getVaiTro(), dsVaiTro);
    }

    public static boolean duocPhepTruyCap(VaiTro vaiTro, String duongDan) {
        if (khopTienTo(duongDan, "/admin")) {
            return coVaiTro(vaiTro, VaiTro.QUAN_TRI);
        }
        if (khopTienTo(duongDan, "/quay") || khopTienTo(duongDan, "/tra-cuu")) {
            return coVaiTro(vaiTro, VaiTro.NHAN_VIEN, VaiTro.QUAN_TRI);
        }
        if (khopTienTo(duongDan, "/dat-ve") || khopTienTo(duongDan, "/lich-su")) {
            return coVaiTro(vaiTro, VaiTro.KHACH_HANG);
        }
        return true;
    }

    public static boolean duocPhepTruyCap(HttpSession session, String duongDan) {
        NguoiDung nd = layNguoiDung(session);
        return duocPhepTruyCap(nd == null ? null : nd.getVaiTro(), duongDan);
    }

    private static boolean khopTienTo(String duongDan, String tienTo) {
        return duongDan != null && (duongDan.equals(tienTo) || duongDan.startsWith(tienTo + "/"));
    }
}
