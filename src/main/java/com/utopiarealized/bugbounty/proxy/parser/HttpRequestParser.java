package com.utopiarealized.bugbounty.proxy.parser;

import com.utopiarealized.bugbounty.proxy.model.HttpRequestModel;

import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpRequestParser {

    // Parses the HTTP request from a string
    public HttpRequestModel parseRequest(final InputStream inputStream) throws IOException {

        // We don't need to read this 2x, but it's easy.
        byte[] headersAsBytes = getRequestHeaders(inputStream);
        BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(headersAsBytes)));

        final HttpRequestModel requestModel = parseStatusLine(reader.readLine());

        String line;
        // Parse headers
        while ((line = reader.readLine()) != null && !line.isEmpty()) {
            int colonPos = line.indexOf(":");
            if (colonPos == -1) {
                throw new IOException("Invalid Header Line: " + line);
            }
            String headerName = line.substring(0, colonPos).trim().toLowerCase();
            String headerValue = line.substring(colonPos + 1).trim();

            requestModel.addHeader(headerName, headerValue);
        }

        // If there's a content-length header, get and read the rest.
        final String contentLength = requestModel.getSingleHeader("content-length");
        byte[] body = null;
        if (contentLength != null) {
            int remainder = Integer.parseInt(contentLength);
            body = getRequestBody(inputStream, remainder);
            requestModel.setRawBody(body);
            //cludgy
            requestModel.setFormParameters(parseBody(body, requestModel));
        }
        return requestModel;
    }

    private Map<String, String> parseBody(final byte[] body, HttpRequestModel requestModel) throws IOException {
        final String contentType = requestModel.getSingleHeader("Content-Type");
        if ((contentType != null) && contentType.toLowerCase().startsWith("application/x-www-form-urlencoded")) {
            final String stringBody = new String(body);
            if (!stringBody.isEmpty()) {
                final Map<String, String> formParameters = new HashMap<>();
                String[] pairs = stringBody.split("&");
                for (String pair : pairs) {
                    int eq = pair.indexOf("=");
                    if (eq != -1) {
                        String key = URLDecoder.decode(pair.substring(0, eq), StandardCharsets.UTF_8);
                        String value = URLDecoder.decode(pair.substring(eq + 1), StandardCharsets.UTF_8);
                        formParameters.put(key, value);
                    }
                }
                return formParameters;
            }
            return null;
        }

        return null;
    }


    private byte[] getRequestBody(final InputStream inputStream, final int length) throws IOException {
        byte[] returnMe = new byte[length];
        int read = inputStream.read(returnMe);
        if (read == -1) {
            return null;
        }
        return returnMe;
    }

    private HttpRequestModel parseStatusLine(final String line) throws IOException {
        if (line == null) {
            throw new IOException("Request cannot be null");
        }

        // Parse the request line
        String[] requestLine = line.split(" ", 3);
        if (requestLine.length != 3) {
            throw new IOException("Invalid HTTP request line : " + requestLine);
        }

        final String method = requestLine[0];
        final String path = requestLine[1];
        final String version = requestLine[2];

        if (!"HTTP/1.1".equals(version)) {
            throw new IOException("Invalid HTTP version");
        }

        return new HttpRequestModel(method, path, version);
    }

    private byte[] getRequestHeaders(final InputStream inputStream) throws IOException {
        // The parser sucks and doesn't handle taking an input stream and reading it properly.
        // Dumb
        final StringBuilder toBody = new StringBuilder();

        //THis is inefficient but I don't care.
        int read = 0;
        boolean notDone = true;
        while (read != -1 && !toBody.toString().contains("\r\n\r\n")) {
            read = inputStream.read();
            toBody.append((char) read);
        }

        return toBody.toString().getBytes(StandardCharsets.UTF_8);
    }

}

