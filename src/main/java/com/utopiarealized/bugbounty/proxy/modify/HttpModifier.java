package com.utopiarealized.bugbounty.proxy.modify;

import com.utopiarealized.bugbounty.proxy.model.HttpRequestModel;
import com.utopiarealized.bugbounty.proxy.model.HttpResponseModel;
import com.utopiarealized.bugbounty.proxy.rules.Rule;

public class HttpModifier {

    public HttpRequestModel modify(final HttpRequestModel model) {
        final HttpRequestModel modified = new HttpRequestModel(model);

        modified.removeHeader("content-length");

        return modified;
    }

    public HttpResponseModel modify(final HttpResponseModel model) {
        final HttpResponseModel modified = new HttpResponseModel(model);

        modified.removeHeader("content-length");
        modified.removeHeader("transfer-encoding");
        modified.putHeader("content-length", model.getContentLength() + "");

        return modified;
    }


}
