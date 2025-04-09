package com.shipproxy.client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

@SpringBootApplication
public class ClientApplication {

    public static void main(String[] args) throws IOException {
        SpringApplication.run(ClientApplication.class, args);
        ServerSocket clientProxySocket = new ServerSocket(8080);
        System.out.println("[CLIENT] SHIP Proxy running on port 8080");

        while (true) {
            Socket browserSocket = clientProxySocket.accept();
            new ProxyClientHandler(browserSocket).start();
        }
    }
}
