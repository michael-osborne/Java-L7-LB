package com.utopiarealized.bugbounty.proxy.rules;

public class RuleSelectorModel {

    //TODO: Escaping could be an issue.
    //    capture(#, method,url, header:value, formfield:value, cookie:value);cookie/header/form(name, store)

    // This class handles the (#, method,url, header:value, formfield:value, cookie:value) part
    private final String requestNum;
    private final String method;
    private final String url;
    private String headerName;
    private String headerValue;
    private String formFieldName;
    private String formFieldValue;
    private String cookieName;
    private String cookieValue;

    public RuleSelectorModel(final String buildRule) {
        //Consider using a different splitter
        final String[] splitString = buildRule.split(",");

        if (splitString.length != 6) {
            throw new RuntimeException("Rule reqires '(#, method,url, header:value, formfield:value, cookie:value)'");
        }
        requestNum = splitString[0].trim();
        method = splitString[1].trim();
        url = splitString[2].trim();

        if (splitString[3].trim().length() > 0) {
            String[] nameValue = splitString[3].split(":", 2);
            headerName = nameValue[0];
            headerValue = nameValue[1];

        }

        if (splitString[3].trim().length() > 0) {
            String[] nameValue = splitString[4].split(":", 2);
            formFieldName = nameValue[0];
            formFieldValue = nameValue[1];
        }

        if (splitString[3].trim().length() > 0) {
            String[] nameValue = splitString[5].split(":", 2);

            cookieName = nameValue[0];
            cookieValue = nameValue[1];
        }
    }


    public String getRequestNum() {
        return requestNum;
    }

    public String getMethod() {
        return method;
    }

    public String getUrl() {
        return url;
    }

    public String getHeaderName() {
        return headerName;
    }

    public String getHeaderValue() {
        return headerValue;
    }

    public String getFormFieldName() {
        return formFieldName;
    }

    public String getFormFieldValue() {
        return formFieldValue;
    }

    public String getCookieName() {
        return cookieName;
    }

    public String getCookieValue() {
        return cookieValue;
    }

    public String toString(){

        return new StringBuilder().append("Selector: ")
                .append( requestNum).append(", ")
                .append(method).append(", ")
                .append(url).append(", ")
                .append(headerName).append(":").append(headerValue).append(", ")
                .append(formFieldName).append(":").append(formFieldValue).append(", ")
                .append(cookieName).append(":").append(cookieValue)
                .toString();

    }
}
