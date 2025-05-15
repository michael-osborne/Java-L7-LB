package com.utopiarealized.bugbounty.proxy.rules;

import com.utopiarealized.bugbounty.proxy.model.HttpRequestModel;

import java.util.Map;

public class SendRule extends BaseRule {


    public SendRule(final RuleSelectorModel ruleSelectorModel, final RuleActionModel ruleActionModel) {
        super(ruleSelectorModel, ruleActionModel);
    }


    public HttpRequestModel applyRule(final Map<String, String> ruleContext, final HttpRequestModel model) {

        boolean matchVal = super.matchSelector(ruleContext, model);
        if (matchVal) {
            System.err.println("Send matched :" + ruleSelectorModel);
            final String setValue = ruleContext.get(ruleActionModel.getValue());
            if (setValue == null) {
                throw BadRuleException.getInstance();
            }
            final String name = ruleActionModel.getName();
            final HttpRequestModel updatedModel = new HttpRequestModel(model);

            if (ruleActionModel.getSelector().equals("form")) {
                //cookie/header/form
                updatedModel.getFormParameters().put(name, setValue);
            } else if (ruleActionModel.getSelector().equals("cookie")) {
                updatedModel.getCookies().put(name, setValue);
            } else {
                updatedModel.addHeader(name, setValue);
            }
            System.err.println("Action: " + ruleActionModel +" = " + setValue);
            return updatedModel;
        }
        return model;
    }
}
