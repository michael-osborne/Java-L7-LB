package com.utopiarealized.bugbounty.proxy.model;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpResponseModel {

    private int status;
    private String protoVersion;
    private String statusMessage;

    private final Map<String, List<String>> headers = new HashMap<>();

    private byte[] responseBody;


    public HttpResponseModel(final HttpResponseModel toCopy) {
        this.status = toCopy.status;
        this.protoVersion = toCopy.protoVersion;
        this.statusMessage = toCopy.statusMessage;
        this.headers.putAll(toCopy.headers);
        this.responseBody = toCopy.responseBody;
    }

    public HttpResponseModel() {
    }


    public static HttpResponseModel fromCloseableHttpResponse(final CloseableHttpResponse response) throws IOException {
        final HttpResponseModel model = new HttpResponseModel();
        model.status = response.getStatusLine().getStatusCode();
        model.protoVersion = response.getStatusLine().getProtocolVersion().toString();
        model.statusMessage = response.getStatusLine().getReasonPhrase();

        for (final Header header : response.getAllHeaders()) {
            model.putHeader(header.getName(), header.getValue());
        }


        if (response.getEntity() != null) {
            File file = File.createTempFile("responseFile", ".output");

            FileOutputStream fileOutputStream = new FileOutputStream(file);
            response.getEntity().writeTo(fileOutputStream);

            fileOutputStream.close();

            FileInputStream fileInputStream = new FileInputStream(file);
            model.responseBody = fileInputStream.readAllBytes();
        }

        return model;
    }

    public int getStatus() {
        return status;
    }

    public String getProtoVersion() {
        return protoVersion;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    public byte[] getResponseBody() {
        return responseBody;
    }

    public void removeHeader(final String header) {
        headers.remove(header.toLowerCase());
    }

    public void putHeader(final String header, final String headerValue) {
        List<String> headerVals = headers.computeIfAbsent(header.toLowerCase(), k -> new ArrayList<>());
        headerVals.add(headerValue);
    }

    public int getContentLength() {
        if (responseBody == null) {
            return 0;
        }
        return responseBody.length;
    }
}
