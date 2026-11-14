package com.qlrapchieuphim.interceptor;

import com.qlrapchieuphim.config.PhienDangNhap;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

@Component
public class PhanQuyenInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        String duongDan = request.getRequestURI().substring(request.getContextPath().length());
        if (!PhienDangNhap.duocPhepTruyCap(request.getSession(false), duongDan)) {
            response.sendRedirect(request.getContextPath() + "/khong-co-quyen");
            return false;
        }
        return true;
    }
}
