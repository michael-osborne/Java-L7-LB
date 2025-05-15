package com.utopiarealized.bugbounty.proxy;

import com.utopiarealized.bugbounty.proxy.model.HttpRequestModel;

import com.utopiarealized.bugbounty.proxy.model.HttpResponseModel;
import org.apache.http.*;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HttpForwarder {

    private final CachedDnsResolver cachedDnsResolver;

    private final HttpClientBuilder httpClientBuilder;


    private final HttpSpy httpSpy;

    public HttpForwarder(final CachedDnsResolver cachedDnsResolver, final HttpSpy httpSpy) {
        this.cachedDnsResolver = cachedDnsResolver;
        this.httpSpy = httpSpy;

        // This is insanely complicated for the base use case.
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(
                RegistryBuilder.<ConnectionSocketFactory>create()
                        .register("http", PlainConnectionSocketFactory.getSocketFactory())
                        .register("https", SSLConnectionSocketFactory.getSocketFactory())
                        .build(), cachedDnsResolver);

        RequestConfig config = RequestConfig.custom()
                .setRedirectsEnabled(false) // Disable redirects
                .build();

        httpClientBuilder = HttpClients.custom().
                setDefaultRequestConfig(config).setConnectionManager(connectionManager);
    }

    public void forwardRequestAndResponse(final Socket socket, final String requestId) throws IOException {

        final HttpRequestModel requestModel = httpSpy.getModifiedRequest(requestId);

        final String method = requestModel.getMethod();
        final String url = requestModel.getPath();

        final HttpUriRequest uriRequest = getUriRequest(method, "https://" +
                requestModel.getServer() +
                url);

        final HttpResponseModel responseModel = proxyRequest(uriRequest, requestModel);

        httpSpy.addResponse(requestId, responseModel);

        final HttpResponseModel modifiedResponse = httpSpy.getModifiedResponse(requestId);
        proxyResponse(modifiedResponse, socket.getOutputStream());
        socket.getOutputStream().close();
        socket.close();
    }


    private HttpResponseModel proxyRequest(final HttpUriRequest uriRequest, final HttpRequestModel requestModel) throws IOException {

        setHeaders(uriRequest, requestModel);
        setFormDataIfExists(uriRequest, requestModel);
        CloseableHttpClient httpClient = httpClientBuilder.build();
        try {

            final CloseableHttpResponse responseFromServer = httpClient.execute(uriRequest);
            final HttpResponseModel responseModel = HttpResponseModel.fromCloseableHttpResponse(responseFromServer);

            return responseModel;
        } finally {
            //
        }
    }

    private void proxyResponse(final HttpResponseModel responseModel, final OutputStream outputStream) throws IOException {
        final String statulLine = (responseModel.getProtoVersion() +
                " " + responseModel.getStatus() +
                " " + responseModel.getStatusMessage() + "\r\n");

        outputStream.write(statulLine.getBytes(StandardCharsets.UTF_8));

        final Map<String, List<String>> headers = responseModel.getHeaders();
        for (final String header : headers.keySet()) {
            final List<String> values = headers.get(header);
            for (final String value : values) {
                final String headerLine = header + ": " + value + "\r\n";
                outputStream.write(headerLine.getBytes(StandardCharsets.UTF_8));
            }
        }
        outputStream.write("\r\n".getBytes(StandardCharsets.UTF_8));

        final byte[] entity = responseModel.getResponseBody();
        if (entity != null) {
            outputStream.write(entity);
        }

    }

    private void setHeaders(final HttpUriRequest uriRequest, final HttpRequestModel requestModel) {
        final Map<String, List<String>> headerMap = requestModel.getHeaders();
        for (final String headerName : headerMap.keySet()) {
            final List<String> headerVals = headerMap.get(headerName);
            for (final String headerVal : headerVals) {
                uriRequest.setHeader(headerName, headerVal);
            }
        }
    }

    private void setFormDataIfExists(final HttpUriRequest uriRequest, final HttpRequestModel requestModel) {
        final Map<String, String> formData = requestModel.getFormParameters();
        if (formData != null) {
            List<NameValuePair> form = new ArrayList<>();
            for (final String name : formData.keySet()) {
                form.add(new BasicNameValuePair(name, formData.get(name)));
            }

            try {

                // Create UrlEncodedFormEntity with the form data
                UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(form, "UTF-8");

                ((HttpEntityEnclosingRequestBase) uriRequest).setEntity(formEntity);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private HttpUriRequest getUriRequest(final String method, final String url) {
        switch (method) {
            case "POST":
                return new HttpPost(url);
            case "GET":
                return new HttpGet(url);
            case "PUT":
                return new HttpPut(url);
            case "DELETE":
                return new HttpDelete(url);
            case "HEAD":
                return new HttpHead(url);
            case "PATCH":
                return new HttpPatch(url);
            default:
                return null;
        }
    }
}
