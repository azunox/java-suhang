package com.teamnote.common.controller;

import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.servlet.support.ExtendedServletRequestDataBinder;

public final class SearchRequestBinding {

    private SearchRequestBinding() {
    }

    public static void useQueryParameters(WebDataBinder binder) {
        binder.setFieldDefaultPrefix(null);
        binder.setFieldMarkerPrefix(null);
        if (binder instanceof ExtendedServletRequestDataBinder servletBinder) {
            servletBinder.addHeaderPredicate(headerName -> false);
        }
    }
}
