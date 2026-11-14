package com.qlrapchieuphim.config;

import com.qlrapchieuphim.interceptor.DangNhapInterceptor;
import com.qlrapchieuphim.interceptor.PhanQuyenInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final DangNhapInterceptor dangNhapInterceptor;
    private final PhanQuyenInterceptor phanQuyenInterceptor;

    public WebConfig(DangNhapInterceptor dangNhapInterceptor, PhanQuyenInterceptor phanQuyenInterceptor) {
        this.dangNhapInterceptor = dangNhapInterceptor;
        this.phanQuyenInterceptor = phanQuyenInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(dangNhapInterceptor)
                .order(1)
                .addPathPatterns("/dat-ve/**", "/lich-su/**", "/ho-so/**", "/quay/**", "/tra-cuu/**", "/admin/**");

        registry.addInterceptor(phanQuyenInterceptor)
                .order(2)
                .addPathPatterns("/admin/**", "/quay/**", "/tra-cuu/**", "/dat-ve/**", "/lich-su/**");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/images/posters/**")
                .addResourceLocations("file:uploads/posters/", "classpath:/static/images/posters/");
    }
}
