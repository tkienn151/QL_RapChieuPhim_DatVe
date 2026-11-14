package com.qlrapchieuphim.interceptor;

import com.qlrapchieuphim.config.PhienDangNhap;
import com.qlrapchieuphim.entity.NguoiDung;
import com.qlrapchieuphim.repository.NguoiDungRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class DangNhapInterceptor implements HandlerInterceptor {

    private final NguoiDungRepository nguoiDungRepo;

    public DangNhapInterceptor(NguoiDungRepository nguoiDungRepo) {
        this.nguoiDungRepo = nguoiDungRepo;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        HttpSession session = request.getSession(false);
        if (PhienDangNhap.daDangNhap(session)) {
            NguoiDung hienTai = PhienDangNhap.layNguoiDung(session);
            NguoiDung moi = hienTai.getId() == null ? null : nguoiDungRepo.findById(hienTai.getId()).orElse(null);
            if (moi != null && moi.isHoatDong()) {
                session.setAttribute(PhienDangNhap.KEY, moi);
                return true;
            }
            session.invalidate();
            response.sendRedirect(request.getContextPath() + "/dang-nhap?canDangNhap=1");
            return false;
        }
        String url = request.getContextPath() + "/dang-nhap?canDangNhap=1";
        if ("GET".equalsIgnoreCase(request.getMethod())) {
            String duongDan = request.getRequestURI().substring(request.getContextPath().length());
            if (request.getQueryString() != null) {
                duongDan += "?" + request.getQueryString();
            }
            url += "&quayLai=" + URLEncoder.encode(duongDan, StandardCharsets.UTF_8);
        }
        response.sendRedirect(url);
        return false;
    }
}
