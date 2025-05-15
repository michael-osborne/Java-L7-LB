package com.utopiarealized.bugbounty.proxy.rules;

public class ParseRule {

    /*
    capture(#, method,url, header:value, formfield:value, cookie:value);cookie/header/form/const(name, store)
    send(method,url, header:value, formfield:value, cookie:value);cookie/header/form(name, value)
     */


    public Rule parseRule(final String rule) {
        final String[] rulebits = rule.split(";");
        if (rulebits.length != 2) {
            throw BadRuleException.getInstance();
        }

        if (rule.startsWith("capture")) {
            return new CaptureRule(new RuleSelectorModel(stripParens(rulebits[0])), new RuleActionModel(rulebits[1]));
        } else if (rule.startsWith("send")) {
            return new SendRule(new RuleSelectorModel(stripParens(rulebits[0])), new RuleActionModel(rulebits[1]));
        } else {
            throw BadRuleException.getInstance();
        }
    }

    private String stripParens(final String ruleBit) {
        final int beginParen = ruleBit.indexOf('(');
        final int endParen = ruleBit.indexOf(')', beginParen);
        if (beginParen == -1 || endParen == -1) {
            throw BadRuleException.getInstance();
        }
        return ruleBit.substring(beginParen+1, endParen);
    }
}
