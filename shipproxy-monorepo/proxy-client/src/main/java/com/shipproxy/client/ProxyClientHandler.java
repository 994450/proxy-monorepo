package com.shipproxy.client;

import java.io.*;
import java.net.*;

public class ProxyClientHandler extends Thread {
    private final Socket browserSocket;

    // Static variables 
    private static Socket offshoreSocket;
    private static OutputStream offshoreOut;
    private static InputStream offshoreIn;

    // Initialize the offshore connection once
    static {
        try {
            offshoreSocket = new Socket("localhost", 9090); // hardcoded offshore
            offshoreOut = offshoreSocket.getOutputStream();
            offshoreIn = offshoreSocket.getInputStream();
        } catch (IOException e) {
            System.err.println("[OFFSHORE ERROR] Failed to connect to offshore server: " + e.getMessage());
        }
    }

    public ProxyClientHandler(Socket browserSocket) {
        this.browserSocket = browserSocket;
    }

    public void run() {
        try (
                InputStream browserIn = browserSocket.getInputStream();
                OutputStream browserOut = browserSocket.getOutputStream();
                BufferedReader browserReader = new BufferedReader(new InputStreamReader(browserIn))
        ) {
            String requestLine = browserReader.readLine();
            if (requestLine == null) return;
            System.out.println("[CLIENT] Received: " + requestLine);
            offshoreOut.write((requestLine + "\r\n").getBytes());

            String line;
            while (!(line = browserReader.readLine()).isEmpty()) {
                offshoreOut.write((line + "\r\n").getBytes());
            }
            offshoreOut.write("\r\n".getBytes());
            offshoreOut.flush();

            if (requestLine.startsWith("CONNECT")) {
                browserOut.write("HTTP/1.1 200 Connection Established\r\n\r\n".getBytes());
                browserOut.flush();

                Thread t1 = new Thread(() -> pipe(browserIn, offshoreOut));
                Thread t2 = new Thread(() -> pipe(offshoreIn, browserOut));
                t1.start();
                t2.start();
                t1.join();
                t2.join();
            } else {
                BufferedReader offshoreReader = new BufferedReader(new InputStreamReader(offshoreIn));
                BufferedWriter browserWriter = new BufferedWriter(new OutputStreamWriter(browserOut));
                String responseLine;
                while ((responseLine = offshoreReader.readLine()) != null) {
                    browserWriter.write(responseLine + "\r\n");
                    browserWriter.flush();
                }
            }

        } catch (Exception e) {
            System.err.println("[CLIENT ERROR] " + e.getMessage());
        }
    }

    private void pipe(InputStream in, OutputStream out) {
        try {
            byte[] buffer = new byte[8192];
            int len;
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
                out.flush();
            }
        } catch (IOException ignored) {}
    }
}
