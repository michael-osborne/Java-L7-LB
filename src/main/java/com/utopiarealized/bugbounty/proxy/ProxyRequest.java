package com.utopiarealized.bugbounty.proxy;

import com.utopiarealized.bugbounty.proxy.model.HttpRequestModel;
import com.utopiarealized.bugbounty.proxy.parser.HttpRequestParser;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;


public class ProxyRequest implements Runnable {

    private Socket socket;

    private HttpForwarder forwarder;

    private final HttpSpy httpSpy;
    private final HttpRequestParser requestParser;


    public ProxyRequest(Socket socket, final HttpForwarder forwarder, final HttpSpy httpSpy) {
        this.httpSpy = httpSpy;
        this.requestParser = new HttpRequestParser();
        this.forwarder = forwarder;
        this.socket = socket;
        Thread t = new Thread(this);
        t.setDaemon(true);
        t.start();
    }


    public void run() {
        try {

            final HttpRequestModel requestModel = requestParser.parseRequest(socket.getInputStream());

            final String requestId = httpSpy.start(requestModel);
            forwarder.forwardRequestAndResponse(socket, requestId);

        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

}
