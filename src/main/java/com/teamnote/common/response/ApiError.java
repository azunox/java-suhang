package com.teamnote.common.response;

import java.util.List;

public record ApiError(String code, String message, List<InvalidField> fields) {

    public ApiError {
        fields = List.copyOf(fields);
    }

    public record InvalidField(String field, String message) {
    }
}
