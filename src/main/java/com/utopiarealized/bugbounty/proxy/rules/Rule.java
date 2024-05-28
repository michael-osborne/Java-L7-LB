package com.utopiarealized.bugbounty.proxy.rules;

import com.utopiarealized.bugbounty.proxy.model.HttpRequestModel;

import java.util.Map;

public interface Rule {

    HttpRequestModel applyRule(final Map<String, String> ruleContext, final HttpRequestModel model);
}
