package com.qlrapchieuphimclient;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class KiemThuNhieuClient {

    public static void main(String[] args) {
        String host = args.length >= 1 ? args[0] : "127.0.0.1";
        int port = args.length >= 2 ? Integer.parseInt(args[1]) : 9999;
        String maVe = args.length >= 3 ? args[2] : "VE000001";
        chayKiemThuDongThoi(host, port, maVe);
    }

    public static void chayKiemThuDongThoi(String host, int port, String maVeMucTieu) {
        int soClient = 4;
        System.out.println("\n================ KIỂM THỬ ĐỒNG THỜI " + soClient + " CLIENT SOCKET ================");
        System.out.println("Mục tiêu: " + soClient + " client cùng kết nối đến " + host + ":" + port
                + " và cùng gửi lệnh INFO + CHECKIN cho mã vé " + maVeMucTieu);

        ExecutorService pool = Executors.newFixedThreadPool(soClient);
        CountDownLatch sanSang = new CountDownLatch(soClient);
        CountDownLatch batDau = new CountDownLatch(1);
        CountDownLatch hoanTat = new CountDownLatch(soClient);
        List<String> ketQuaCheckin = Collections.synchronizedList(new ArrayList<>());

        for (int i = 1; i <= soClient; i++) {
            final int idClient = i;
            pool.submit(() -> {
                String tenClient = "Client-" + idClient;
                try (Socket socket = new Socket(host, port);
                     BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                     PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8)), true)) {

                    sanSang.countDown();
                    batDau.await();

                    out.println("INFO|" + maVeMucTieu);
                    String kqInfo = in.readLine();
                    System.out.printf("[%s - %s] INFO|%s -> %s%n",
                            tenClient, Thread.currentThread().getName(), maVeMucTieu, kqInfo);

                    out.println("CHECKIN|" + maVeMucTieu);
                    String kqCheckin = in.readLine();
                    ketQuaCheckin.add(kqCheckin);
                    System.out.printf("[%s - %s] CHECKIN|%s -> %s%n",
                            tenClient, Thread.currentThread().getName(), maVeMucTieu, kqCheckin);

                    out.println("QUIT");
                } catch (Exception e) {
                    System.out.printf("[%s] Lỗi: %s%n", tenClient, e.getMessage());
                } finally {
                    hoanTat.countDown();
                }
            });
        }

        try {
            sanSang.await(5, TimeUnit.SECONDS);
            batDau.countDown();
            hoanTat.await(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            pool.shutdown();
        }

        long soThanhCong = ketQuaCheckin.stream().filter(s -> s != null && s.startsWith("OK|DA_SOAT")).count();
        long soBiTuChoi = ketQuaCheckin.size() - soThanhCong;
        System.out.println("--------------------------------------------------------------------");
        System.out.println("TỔNG KẾT KIỂM THỬ " + soClient + " CLIENT ĐỒNG THỜI:");
        System.out.println("- Tổng số phản hồi CHECKIN nhận được: " + ketQuaCheckin.size());
        System.out.println("- Số client CHECKIN thành công (OK|DA_SOAT): " + soThanhCong);
        System.out.println("- Số client bị từ chối (ERR|DA_SU_DUNG / ERR|...): " + soBiTuChoi);
        System.out.println("====================================================================\n");
    }
}
