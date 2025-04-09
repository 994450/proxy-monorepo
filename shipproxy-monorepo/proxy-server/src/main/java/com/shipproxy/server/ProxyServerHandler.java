package com.shipproxy.server;

import java.io.*;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.InetSocketAddress;

public class ProxyServerHandler extends Thread {

    private final Socket shipSocket;

    public ProxyServerHandler(Socket shipSocket) {
        this.shipSocket = shipSocket;
    }

    @Override
    public void run() {
        try (
                InputStream shipIn = shipSocket.getInputStream();
                OutputStream shipOut = shipSocket.getOutputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(shipIn))
        ) {
            // Read the initial request line (CONNECT host:port HTTP/1.1)
            String requestLine = reader.readLine();
            if (requestLine == null || !requestLine.startsWith("CONNECT")) {
                System.err.println("[SERVER] Unsupported method or invalid request.");
                return;
            }

            String[] parts = requestLine.split(" ");
            if (parts.length < 2) {
                System.err.println("[SERVER] Malformed CONNECT request.");
                return;
            }

            String[] target = parts[1].split(":");
            String targetHost = target[0];
            int targetPort = (target.length == 2) ? Integer.parseInt(target[1]) : 443;

            System.out.println("[SERVER] CONNECT to " + targetHost + ":" + targetPort);

            // Consume the rest of the HTTP headers
            String line;
            while (!(line = reader.readLine()).isEmpty()) {
                // Optionally log headers
            }

            // Connect to the target server (INTERNET)
            Socket targetSocket = new Socket();
            targetSocket.connect(new InetSocketAddress(targetHost, targetPort), 5000);

            // Notify the SHIP proxy that the tunnel is established
            shipOut.write("HTTP/1.1 200 Connection Established\r\n\r\n".getBytes());
            shipOut.flush();

            // Start bi-directional piping
            InputStream targetIn = targetSocket.getInputStream();
            OutputStream targetOut = targetSocket.getOutputStream();

            Thread toTarget = new Thread(() -> pipeData(shipIn, targetOut));
            Thread fromTarget = new Thread(() -> pipeData(targetIn, shipOut));

            toTarget.start();
            fromTarget.start();

            toTarget.join();
            fromTarget.join();

        } catch (Exception e) {
            System.err.println("[SERVER] Error: " + e.getMessage());
        }
    }

    private void pipeData(InputStream in, OutputStream out) {
        byte[] buffer = new byte[8192];
        int read;
        try {
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
                out.flush();
            }
        } catch (IOException e) {
            // Client disconnected or error occurred
        }
    }
}