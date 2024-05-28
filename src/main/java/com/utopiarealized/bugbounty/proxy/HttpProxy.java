package com.utopiarealized.bugbounty.proxy;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class HttpProxy implements Runnable {

    private final int port;
    private final ServerSocket serverSocket;

    private boolean running = true;

    private Thread ourThread;

    public HttpProxy(int port) throws IOException {
        this.port = port;
        this.serverSocket = new ServerSocket(port);
        ourThread = new Thread(this);
        ourThread.start();
    }

    public void run() {
        while ( running) {
            try {
                final Socket httpRequest = serverSocket.accept();
            } catch (IOException ioe) {
                // TODO: What do I do with this?
                ioe.printStackTrace();
            }
        }
    }

}
