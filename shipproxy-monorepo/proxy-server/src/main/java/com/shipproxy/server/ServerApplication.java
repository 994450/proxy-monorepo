package com.shipproxy.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

@SpringBootApplication
public class ServerApplication {
    public static void main(String[] args) throws IOException {
        SpringApplication.run(ServerApplication.class, args);
        ServerSocket serverSocket = new ServerSocket(9090);
        System.out.println("[SERVER] Offshore Proxy started on port 9090");

        while (true) {
            Socket clientSocket = serverSocket.accept();
            System.out.println("[SERVER] Connected to SHIP proxy: " + clientSocket.getInetAddress());
            new ProxyServerHandler(clientSocket).start();
        }
    }
}
