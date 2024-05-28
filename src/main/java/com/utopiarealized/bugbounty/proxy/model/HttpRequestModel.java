package com.utopiarealized.bugbounty.proxy.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpRequestModel {

    private final String method;
    private final String path;
    private final String version;

    private byte[] rawBody;

    private Map<String, String> formParameters;
    private final Map<String, List<String>> headers = new HashMap<>();

    //Cookies are handled kind of weird.
    private Map<String, String> cookies = new HashMap<>();


    public HttpRequestModel(final HttpRequestModel model) {
        this.method = model.method;
        this.path = model.path;
        this.version = model.version;
        this.cookies = new HashMap<>(model.cookies);

        if (model.rawBody != null) {
            this.rawBody = new byte[model.rawBody.length];
            System.arraycopy(model.rawBody, 0, this.rawBody, 0, model.rawBody.length);
        }
        if (model.formParameters != null) {
            this.formParameters = new HashMap<>(model.formParameters);
        }
        for (final String key : model.headers.keySet()) {
            headers.put(key, new ArrayList<>(model.headers.get(key)));
        }


    }

    public HttpRequestModel(final String method, final String path, final String version) {
        this.method = method;
        this.path = path;
        this.version = version;
    }

    public Map<String, String> getFormParameters() {
        return formParameters;
    }

    public byte[] getRawBody() {
        return rawBody;
    }

    public void setRawBody(byte[] rawBody) {
        this.rawBody = rawBody;
    }

    public void setFormParameters(Map<String, String> formParameters) {
        this.formParameters = formParameters;
    }

    // Getters for request components
    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public String getVersion() {
        return version;
    }

    // Cookies get a little strange. We're going to add them one at a time here.
    public Map<String, List<String>> getHeaders() {
        Map<String, List<String>> returnMe = new HashMap<>(headers);
        if (!cookies.isEmpty()) {
            final List<String> cookieHeaders = new ArrayList<>();
            returnMe.put("cookie", cookieHeaders);
            final StringBuilder cookieHeader = new StringBuilder();
            for (final String cookieName : cookies.keySet()) {
                cookieHeader.append(cookieName)
                        .append("=")
                        .append(cookies.get(cookieName))
                        .append("; ");
            }
            cookieHeader.setLength(cookieHeader.length()-1);
            cookieHeaders.add(cookieHeader.toString());

        }
        return returnMe;
    }

    public String getSingleHeader(final String header) {
        List<String> headerList = headers.get(header.toLowerCase());
        return headerList == null ? null : headerList.get(0);
    }

    public void addHeader(final String name, final String value) {
        if ("cookie".equalsIgnoreCase(name)) {
            cookies.putAll(parseCookiesFromHeader(value));
        } else {
            List<String> headersNamed = headers.computeIfAbsent(name, k -> new ArrayList<>());
            headersNamed.add(value);
        }
    }

    public void removeHeader(final String header) {
        headers.remove(header);
    }

    public String getServer() {
        return headers.get("host").get(0);
    }

    public Map<String, String> getCookies() {
        return cookies;
    }


    private Map<String, String> parseCookiesFromHeader(final String cookieLine) {
        final Map<String, String> returnMe = new HashMap<>();
        final String[] allCookiesInSingleHeader = cookieLine.split(";");
        for (final String cookieNv : allCookiesInSingleHeader) {
            final String[] split = cookieNv.split("=", 2);
            returnMe.put(split[0].trim(), split[1].trim());
        }
        return returnMe;
    }
}
