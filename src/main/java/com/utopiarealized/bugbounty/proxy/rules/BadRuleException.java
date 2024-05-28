package com.utopiarealized.bugbounty.proxy.rules;

public class BadRuleException extends RuntimeException {

    private static final String EXCEPTION = "Rule not in format 'capture/store(#, method,url, header:value, formfield:value, cookie:value);cookie/header/form/const(name, store)')";

    public BadRuleException(final String exception) {
        super(exception);
    }

    public static BadRuleException getInstance() {
        return new BadRuleException(EXCEPTION);
    }
}
