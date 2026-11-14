package com.qlrapchieuphimclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class SoatVeClient {
    private static final String SERVER_IP = "127.0.0.1";
    private static final int SERVER_PORT = 9999;

    public static void main(String[] args) {
        try (
                Socket socket = new Socket(SERVER_IP, SERVER_PORT);
                PrintWriter out = new PrintWriter(new java.io.OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
                Scanner scanner = new Scanner(System.in)
        ) {
            System.out.println("=== HỆ THỐNG SOÁT VÉ ===");
            System.out.println("Kết nối tới máy chủ thành công!");
            System.out.println("Nhập mã vé để soát. Gõ 'INFO <mã vé>' để xem thông tin vé. Gõ 'TEST <mã vé>' để kiểm thử 4 client đồng thời. Gõ 'QUIT' để thoát.");

            while (true) {
                System.out.print("\nNhập hoặc quét mã vé: ");
                String maVe = scanner.nextLine().trim();

                if (maVe.equalsIgnoreCase("QUIT")) {
                    out.println("QUIT");
                    break;
                }

                if (maVe.isEmpty()) continue;

                if (maVe.toUpperCase().startsWith("TEST")) {
                    String maVeTest = maVe.length() > 4 ? maVe.substring(4).trim() : "";
                    if (maVeTest.isEmpty()) maVeTest = "VE000001";
                    KiemThuNhieuClient.chayKiemThuDongThoi(SERVER_IP, SERVER_PORT, maVeTest);
                    continue;
                }

                if (maVe.toUpperCase().startsWith("INFO ")) {
                    out.println("INFO|" + maVe.substring(5).trim());
                } else if (maVe.toUpperCase().startsWith("INFO|") || maVe.toUpperCase().startsWith("CHECKIN|")) {
                    out.println(maVe);
                } else {
                    out.println("CHECKIN|" + maVe);
                }

                String response = in.readLine();
                if (response == null) {
                    System.out.println("Mất kết nối với máy chủ!");
                    break;
                }

                hienThiKetQua(response);
            }
        } catch (IOException e) {
            System.err.println("Lỗi: Không thể kết nối tới máy chủ soát vé (cổng 9999). Hãy chạy QLRapChieuPhim trước. Chi tiết: " + e.getMessage());
        }
    }

    private static void hienThiKetQua(String response) {
        String[] parts = response.split("\\|");
        if (parts[0].equals("OK") && parts.length >= 7 && parts[1].equals("INFO")) {
            System.out.println("THÔNG TIN VÉ (chưa soát)");
            System.out.println("Phim: " + parts[2]);
            System.out.println("Phòng: " + parts[3] + " - Ghế: " + parts[4]);
            System.out.println("Giờ chiếu: " + parts[5] + " - Trạng thái: " + parts[6]);
        } else if (parts[0].equals("OK")) {
            System.out.println("✅ SOÁT VÉ THÀNH CÔNG (HỢP LỆ)");
            System.out.println("Phim: " + parts[2]);
            System.out.println("Phòng: " + parts[3]);
            System.out.println("Ghế: " + parts[4]);
        } else if (parts[0].equals("ERR")) {
            System.out.print("❌ TỪ CHỐI: ");
            switch (parts[1]) {
                case "KHONG_TON_TAI": System.out.println("Mã vé không tồn tại trên hệ thống."); break;
                case "DA_SU_DUNG": System.out.println("Vé này ĐÃ ĐƯỢC SỬ DỤNG."); break;
                case "DA_HUY": System.out.println("Vé này ĐÃ BỊ HỦY."); break;
                case "CHUA_THANH_TOAN": System.out.println("Đơn đặt vé này CHƯA THANH TOÁN."); break;
                case "SAI_SUAT": System.out.println("Vé không hợp lệ cho thời điểm hiện tại (Sai suất chiếu hoặc quá giờ)."); break;
                case "LOI_HE_THONG": System.out.println("Máy chủ gặp lỗi, vui lòng thử lại."); break;
                default: System.out.println("Lỗi không xác định."); break;
            }
        }
    }
}
