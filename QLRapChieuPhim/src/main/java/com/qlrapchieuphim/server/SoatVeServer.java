package com.qlrapchieuphim.server;

import com.qlrapchieuphim.service.SoatVeService;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class SoatVeServer {
    private static final Logger log = LoggerFactory.getLogger(SoatVeServer.class);

    public static final int PORT = 9999;

    @Value("${soatve.socket.port:9999}")
    private int port = PORT;

    @Value("${soatve.server.enabled:true}")
    private boolean enabled = true;

    private ServerSocket serverSocket;
    private ExecutorService threadPool;
    private volatile boolean isRunning = false;

    @Autowired
    private SoatVeService soatVeService;

    @EventListener(ApplicationReadyEvent.class)
    public void startServer() {
        if (!enabled || isRunning) {
            return;
        }
        try {
            serverSocket = new ServerSocket(port);
            this.port = serverSocket.getLocalPort();
            AtomicInteger demLuong = new AtomicInteger(1);
            threadPool = Executors.newFixedThreadPool(10, r -> {
                Thread t = new Thread(r, "soat-ve-worker-" + demLuong.getAndIncrement());
                t.setDaemon(true);
                return t;
            });
            isRunning = true;
            log.info("SoatVeServer đã khởi động, lắng nghe trên cổng TCP: {}", this.port);

            Thread acceptor = new Thread(() -> {
                try {
                    while (isRunning && serverSocket != null && !serverSocket.isClosed()) {
                        Socket clientSocket = serverSocket.accept();
                        clientSocket.setSoTimeout(60000);
                        threadPool.execute(new XuLyClientTask(clientSocket, soatVeService));
                    }
                } catch (IOException e) {
                    if (isRunning) {
                        log.warn("Lỗi SoatVeServer: {}", e.getMessage());
                    }
                }
            }, "soat-ve-acceptor");
            acceptor.setDaemon(true);
            acceptor.start();
        } catch (IOException e) {
            log.error("Không thể mở cổng SoatVeServer {}: {}", port, e.getMessage());
        }
    }

    public int getPort() {
        return port;
    }

    @PreDestroy
    public void stopServer() {
        isRunning = false;
        try {
            if (threadPool != null) threadPool.shutdown();
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            log.warn("Lỗi khi đóng SoatVeServer: {}", e.getMessage());
        }
    }
}
