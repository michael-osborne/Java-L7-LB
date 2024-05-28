package com.utopiarealized.bugbounty.proxy.rules;

import com.utopiarealized.bugbounty.proxy.model.HttpRequestModel;

import java.util.List;
import java.util.Map;

public abstract class BaseRule implements Rule {

    protected final RuleActionModel ruleActionModel;

    protected final RuleSelectorModel ruleSelectorModel;

    public BaseRule(final RuleSelectorModel ruleSelectorModel, final RuleActionModel ruleActionModel) {
        this.ruleActionModel = ruleActionModel;
        this.ruleSelectorModel = ruleSelectorModel;
    }


    protected boolean matchSelector(final Map<String, String> ruleContext, final HttpRequestModel model) {
        boolean matchVal = match(ruleSelectorModel.getMethod(), model.getMethod()) &&
                match(ruleSelectorModel.getRequestNum(), ruleContext.get("requestNum")) &&
                match(ruleSelectorModel.getUrl(), model.getPath()) &&
                match(ruleSelectorModel.getCookieName(), ruleSelectorModel.getCookieValue(), model.getCookies()) &&
                match(ruleSelectorModel.getFormFieldName(), ruleSelectorModel.getFormFieldValue(), model.getFormParameters()) &&
                matchListMap(ruleSelectorModel.getHeaderName(), ruleSelectorModel.getHeaderValue(), model.getHeaders());

        return matchVal;
    }


    private boolean match(final String matchVal, final String modelVal) {
        if (matchVal == null || matchVal.trim().length() == 0) {
            return true;
        }

        return matchVal.toLowerCase().trim().matches(modelVal.toLowerCase().trim());
    }


    private boolean match(final String matchName, final String matchVal, final Map<String, String> map) {
        if (matchName == null || matchName.trim().length() == 0) {
            return true;
        }
        final String modelVal = map.get(matchName);
        if (modelVal == null) {
            return false;
        }
        if (matchVal == null || matchVal.trim().length() == 0) {
            return true;
        }

        return modelVal.toLowerCase().trim().matches(matchVal.toLowerCase().trim());
    }

    private boolean matchListMap(final String matchName, final String matchVal, final Map<String, List<String>> map) {
        if (matchName == null || matchName.trim().length() == 0) {
            return true;
        }
        final List<String> modelVal = map.get(matchName);
        if (modelVal == null) {
            return false;
        }
        if (matchVal == null || matchVal.trim().length() == 0) {
            return true;
        }

        for (final String listVal : modelVal) {
            if (matchVal.toLowerCase().trim().matches(listVal.toLowerCase().trim())) {
                return true;
            }
        }
        return false;
    }

    protected String getSelectorValue(final RuleActionModel ruleActionModel, final HttpRequestModel model) {

        if (ruleActionModel.getSelector().equals("const")) {
            return ruleActionModel.getName();
        }
        if (ruleActionModel.getSelector().equals("form")) {
            return model.getFormParameters().get(ruleActionModel.getName());
        }
        if (ruleActionModel.getSelector().equals("cookie")) {
            return model.getFormParameters().get(ruleActionModel.getName());
        }
        if (ruleActionModel.getSelector().equals("header")) {
            return model.getSingleHeader((ruleActionModel.getName()));
        }
        throw BadRuleException.getInstance();
    }


}
