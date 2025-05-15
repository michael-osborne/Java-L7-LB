package com.utopiarealized.bugbounty.proxy.rules;

import com.utopiarealized.bugbounty.proxy.model.HttpRequestModel;

import java.util.List;
import java.util.Map;

public class CaptureRule extends BaseRule {

    public CaptureRule(final RuleSelectorModel ruleSelectorModel, final RuleActionModel ruleActionModel) {
        super(ruleSelectorModel, ruleActionModel);
    }

    public HttpRequestModel applyRule(final Map<String, String> ruleContext, final HttpRequestModel model) {

        boolean matchVal = super.matchSelector(ruleContext, model);
        if (matchVal) {
            final String selectorValue = super.getSelectorValue(ruleActionModel, model);
            //Naming makes this confusing. It should be calleds something else.
            System.err.println("Capture matched :" + ruleSelectorModel);
            System.err.println("Action: " + selectorValue + " = " + ruleActionModel);

            if (selectorValue != null) {

                ruleContext.put(ruleActionModel.getValue(), selectorValue);
            }
        }
        return model;
    }

}
