package com.utopiarealized.bugbounty.proxy.rules;

public class RuleActionModel {
    // This handles cookie/header/form(name, store) part

    private final String selector;
    private final String name;
    private final String value;


    public RuleActionModel(final String action) {

        if (action.startsWith("cookie")) {
            selector = "cookie";
        } else if (action.startsWith("header")) {
            selector = "header";
        } else if (action.startsWith("form")) {
            selector = "form";
        } else if (action.startsWith("const")) {
            selector = "const";
        } else {
            throw new RuntimeException("Action needs to be in the form of 'cookie/header/form/const(name, store)'");
        }
        final int beginParen = action.indexOf('(');
        final int endParen = action.indexOf(')', beginParen);
        if ((beginParen == -1) || (endParen == -1)) {
            throw new RuntimeException("Action needs to be in the form of 'cookie/header/form(name, store)'");
        }

        final String nameVal[] = action.substring(beginParen + 1, endParen).split(",", 2);
        if (nameVal.length != 2) {
            throw new RuntimeException("Action needs to be in the form of 'cookie/header/form(name, store)'");
        }
        name = nameVal[0].trim();
        value = nameVal[1].trim();

    }

    public String getSelector() {
        return selector;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public String toString() {
        return new StringBuilder()
                .append("Action: ")
                .append(selector)
                .append("(")
                .append(name)
                .append(", ")
                .append(value)
                .append(")")
                .toString();
    }
}
