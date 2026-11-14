package com.qlrapchieuphim.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.ui.Model;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public String loiNghiepVu(RuntimeException ex, HttpServletResponse response, Model model) {
        return hienThi(response, model, HttpStatus.BAD_REQUEST, "Yêu cầu không thực hiện được", ex.getMessage());
    }

    @ExceptionHandler(TypeMismatchException.class)
    public String saiThamSo(HttpServletResponse response, Model model) {
        return hienThi(response, model, HttpStatus.BAD_REQUEST, "Yêu cầu không hợp lệ", "Tham số trên đường dẫn không hợp lệ.");
    }

    @ExceptionHandler(Exception.class)
    public String loiKhac(Exception ex, HttpServletResponse response, Model model) {
        HttpStatusCode ma = (ex instanceof ErrorResponse er) ? er.getStatusCode() : HttpStatus.INTERNAL_SERVER_ERROR;
        if (ma.value() == 404) {
            return hienThi(response, model, ma, "Không tìm thấy trang", "Trang bạn tìm không tồn tại hoặc đã bị di chuyển.");
        }
        if (ma.is4xxClientError()) {
            return hienThi(response, model, ma, "Yêu cầu không hợp lệ", "Yêu cầu gửi lên không hợp lệ.");
        }
        log.error("Lỗi không mong muốn", ex);
        return hienThi(response, model, ma, "Hệ thống gặp sự cố", "Đã có lỗi xảy ra, vui lòng thử lại sau.");
    }

    private String hienThi(HttpServletResponse response, Model model, HttpStatusCode ma, String tieuDe, String thongBao) {
        response.setStatus(ma.value());
        model.addAttribute("maLoi", ma.value());
        model.addAttribute("tieuDe", tieuDe);
        model.addAttribute("thongBao", thongBao);
        return "loi";
    }
}
