package com.qlrapchieuphim.server;

import com.qlrapchieuphim.service.SoatVeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class XuLyClientTask implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(XuLyClientTask.class);

    private final Socket socket;
    private final SoatVeService soatVeService;

    public XuLyClientTask(Socket socket, SoatVeService soatVeService) {
        this.socket = socket;
        this.soatVeService = soatVeService;
    }

    @Override
    public void run() {
        String tenLuong = Thread.currentThread().getName();
        String diaChiClient = socket.getRemoteSocketAddress() != null
                ? socket.getRemoteSocketAddress().toString() : "client";
        log.info("[SoatVeServer - Luong: {}] Client ket noi: {}", tenLuong, diaChiClient);

        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true)
        ) {
            String line;
            while ((line = in.readLine()) != null) {
                line = line.trim();
                log.info("[SoatVeServer - Luong: {}] Nhan lenh tu {}: {}", tenLuong, diaChiClient, line);
                if (line.equalsIgnoreCase(SoatVeProtocol.CMD_QUIT)) {
                    log.info("[SoatVeServer - Luong: {}] Client {} gui QUIT, dong ket noi", tenLuong, diaChiClient);
                    break;
                }

                try {
                    String phanHoi;
                    if (line.startsWith(SoatVeProtocol.PREFIX_CHECKIN)) {
                        String maVe = line.substring(SoatVeProtocol.PREFIX_CHECKIN.length()).trim();
                        phanHoi = soatVeService.soatVe(maVe);
                    } else if (line.startsWith(SoatVeProtocol.PREFIX_INFO)) {
                        String maVe = line.substring(SoatVeProtocol.PREFIX_INFO.length()).trim();
                        phanHoi = soatVeService.thongTinVe(maVe);
                    } else {
                        phanHoi = SoatVeProtocol.ERR_INVALID_CMD;
                    }
                    out.println(phanHoi);
                    log.info("[SoatVeServer - Luong: {}] Phan hoi {}: {}", tenLuong, diaChiClient, phanHoi);
                } catch (Exception e) {
                    log.error("[SoatVeServer - Luong: {}] Lỗi xử lý lệnh '{}': {}", tenLuong, line, e.getMessage());
                    out.println(SoatVeProtocol.ERR_SYSTEM);
                }
            }
        } catch (IOException e) {
            log.warn("[SoatVeServer - Luong: {}] Kết nối tới Client {} đã đóng: {}", tenLuong, diaChiClient, e.getMessage());
        } finally {
            try {
                if (!socket.isClosed()) socket.close();
            } catch (IOException e) {
                log.warn("Lỗi đóng socket: {}", e.getMessage());
            }
        }
    }
}
