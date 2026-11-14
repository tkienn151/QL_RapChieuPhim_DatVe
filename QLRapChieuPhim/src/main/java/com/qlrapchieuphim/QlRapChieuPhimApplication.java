package com.qlrapchieuphim;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class QlRapChieuPhimApplication {
    public static void main(String[] args) {
        SpringApplication.run(QlRapChieuPhimApplication.class, args);
    }
}
