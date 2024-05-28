package com.utopiarealized.bugbounty.proxy;

import com.utopiarealized.bugbounty.proxy.model.HttpRequestModel;
import com.utopiarealized.bugbounty.proxy.model.HttpResponseModel;
import com.utopiarealized.bugbounty.proxy.model.SpyContext;
import com.utopiarealized.bugbounty.proxy.modify.HttpModifier;
import com.utopiarealized.bugbounty.proxy.rules.RuleHandler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class HttpSpy {

    final HttpModifier modifier;
    private Map<String, RequestResponseHolder> requestMap = new ConcurrentHashMap<>();

    private final SpyContext spyContext;

    private final String directory;

    private AtomicInteger atomicInteger = new AtomicInteger(0);

    private RuleHandler ruleHandler;


    public HttpSpy(final HttpModifier modifier, final SpyContext spyContext, final RuleHandler ruleHandler) throws IOException {
        this.modifier = modifier;
        this.spyContext = spyContext;
        this.ruleHandler = ruleHandler;
        this.directory = spyContext.getDirectory() +"/"+ spyContext.getBountyName() + "/sessions/" +
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(spyContext.getStartupTime()) + "/";

        // Format the date according to the specified pattern
        Path path = Paths.get(this.directory);
        Files.createDirectories(path);
    }

    public String start(HttpRequestModel requestModel) {
        final RequestResponseHolder requestResponseHolder = new RequestResponseHolder();
        requestResponseHolder.originalRequest = requestModel;
        HttpRequestModel modifiedRequest  = modifier.modify(requestModel);
        modifiedRequest = ruleHandler.applyRules(modifiedRequest);
        requestResponseHolder.modifiedRequest = modifiedRequest;

        final String uuid = UUID.randomUUID().toString();
        requestMap.put(uuid, requestResponseHolder);
        return uuid;
    }

    public HttpRequestModel getModifiedRequest(final String requestId) {
        return requestMap.get(requestId).modifiedRequest;
    }

    public HttpResponseModel getModifiedResponse(final String requestId) {
        return requestMap.get(requestId).modifiedResponse;
    }

    public void addResponse(final String requestId, HttpResponseModel responseModel) throws IOException {
        RequestResponseHolder requestResponseHolder = requestMap.get(requestId);
        requestResponseHolder.response = responseModel;
        requestResponseHolder.modifiedResponse = modifier.modify(responseModel);

        logRequestAndResponse(requestId, requestResponseHolder);
    }

    private void logRequestAndResponse(final String requestId, final RequestResponseHolder requestResponseHolder) throws IOException {
        final String originalRequest = dumpRequest(requestResponseHolder.originalRequest);
        final String modifiedRequest = dumpRequest(requestResponseHolder.modifiedRequest);
        final String originalResponse = dumpResponse(requestResponseHolder.response);
        final String modifiedResponse = dumpResponse(requestResponseHolder.modifiedResponse);

        printRequestAndResponse(requestResponseHolder);
        final int requestNum = atomicInteger.getAndIncrement();
        final String dir = this.directory + requestNum + "/";
        Path path = Paths.get(dir);
        Files.createDirectories(path);
        dumpToFile(originalRequest.getBytes(StandardCharsets.UTF_8), dir + "original-request.txt");
        dumpToFile(modifiedRequest.getBytes(StandardCharsets.UTF_8), dir + "modified-request.txt");
        dumpToFile(originalResponse.getBytes(StandardCharsets.UTF_8), dir + "original-response.txt");
        dumpToFile(modifiedResponse.getBytes(StandardCharsets.UTF_8), dir + "modified-response.txt");

        if (requestResponseHolder.response.getResponseBody() != null) {
            dumpToFile(requestResponseHolder.response.getResponseBody(), dir + "response-body.html");
        }
    }

    private void dumpToFile(final byte[] content, final String filePath) throws IOException {
        File file = new File(filePath);
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(content);
        fos.close();
    }

    private void printRequestAndResponse(final RequestResponseHolder requestResponseHolder) {
        final HttpRequestModel orignal = requestResponseHolder.originalRequest;
        System.out.println("Original:");
        System.out.println(dumpRequest(orignal));
        System.out.println("---");

        System.out.println("Modified:");
        final HttpRequestModel modified = requestResponseHolder.modifiedRequest;
        System.out.println(dumpRequest(modified));
        System.out.println("---");

        final HttpResponseModel response = requestResponseHolder.response;
        System.out.println("Response:");
        System.out.println(dumpResponse(response));

        System.out.println("---\n");
    }


    class RequestResponseHolder {
        HttpRequestModel originalRequest;
        HttpRequestModel modifiedRequest;

        HttpResponseModel response;

        HttpResponseModel modifiedResponse;
    }

    private String dumpRequest(final HttpRequestModel requestModel) {
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("https://").
                append(requestModel.getServer()).
                append(" ").
                append(requestModel.getMethod()).
                append(" ").append(requestModel.getPath()).
                append("\n");
        stringBuilder.append("Headers:\n");
        for (final String key : requestModel.getHeaders().keySet()) {
            stringBuilder.append("\t").append(key).append("\n");
            for (final String value : requestModel.getHeaders().get(key)) {
                stringBuilder.append("\t\t")
                        .append(value)
                        .append("\n");
            }
        }
        stringBuilder.append("Cookies:\n");
        for (final String key : requestModel.getCookies().keySet()) {
            stringBuilder.append("\t").append(key)
                    .append(" : ")
                    .append(requestModel.getCookies().get(key))
                    .append("\n");
        }
        stringBuilder.append("Form Parameters:\n");
        if (requestModel.getFormParameters() != null) {
            for (final String key : requestModel.getFormParameters().keySet()) {
                stringBuilder.append("\t").append(key)
                        .append(" : ")
                        .append(requestModel.getFormParameters().get(key))
                        .append("\n");
            }
        }
        return stringBuilder.toString();

    }

    private String dumpResponse(final HttpResponseModel response) {
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Response: ")
                .append(response.getStatus())
                .append(" ")
                .append(response.getProtoVersion())
                .append(" ")
                .append(response.getStatusMessage());
        stringBuilder.append("\n    Headers:\n");

        final Map<String, List<String>> headers = response.getHeaders();
        for (final String header : headers.keySet()) {
            stringBuilder.append("\t\t").append(header);
            final List<String> values = headers.get(header);
            for (final String value : values) {
                stringBuilder.append("\t\t\t").append(value).append("\n");
            }
        }
        return stringBuilder.toString();
    }
}
