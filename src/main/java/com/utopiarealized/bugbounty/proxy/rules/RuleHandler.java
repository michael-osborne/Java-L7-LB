package com.utopiarealized.bugbounty.proxy.rules;

import com.utopiarealized.bugbounty.proxy.model.HttpRequestModel;
import com.utopiarealized.bugbounty.proxy.modify.HttpModifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class RuleHandler {

    private ParseRule parseRule = new ParseRule();

    private HttpModifier httpModifier = new HttpModifier();

    private Map<String, String> ruleContext = new HashMap<>();

    // Small race condition on this: Adding a rule while it's working will cause this list to be
    // updated concurrently. Not really relevant for my use case
    private List<Rule> rules = new ArrayList<>();

    private AtomicInteger requestNum = new AtomicInteger(1); // This is a little fragile as a selector, but...

    public void addRule(final String rule) {
        final Rule parsedRule = parseRule.parseRule(rule);
        rules.add(parsedRule);
    }

    public HttpRequestModel applyRules(final HttpRequestModel requestModel) {
        ruleContext.put("requestNum", requestNum.getAndAdd(1) + "");
        HttpRequestModel returnModel = requestModel;
        for (final Rule rule : rules) {
            returnModel = rule.applyRule(ruleContext, returnModel);
        }
        return returnModel;
    }

}
