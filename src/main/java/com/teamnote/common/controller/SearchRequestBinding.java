package com.teamnote.common.controller;

import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

import com.teamnote.common.exception.InvalidRequestException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.DataBinder;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.annotation.ModelAttributeMethodProcessor;

public final class SearchRequestBinding extends ModelAttributeMethodProcessor {

    public SearchRequestBinding() {
        super(false);
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(SearchQuery.class);
    }

    @Override
    protected void constructAttribute(WebDataBinder binder, NativeWebRequest request) {
        HttpServletRequest servletRequest = request.getNativeRequest(HttpServletRequest.class);
        Assert.state(servletRequest != null, "HTTP servlet request is required");
        binder.construct(new QueryValues(parseQuery(servletRequest.getQueryString())));
    }

    @Override
    protected void bindRequestParameters(WebDataBinder binder, NativeWebRequest request) {
        // 검색 DTO는 URL 값으로 생성한 record이므로 Servlet 요청 값의 추가 바인딩을 생략한다.
    }

    private MultiValueMap<String, String> parseQuery(String query) {
        MultiValueMap<String, String> values = new LinkedMultiValueMap<>();
        if (query == null) {
            return values;
        }

        for (String pair : query.split("&")) {
            int separator = pair.indexOf('=');
            String name = separator < 0 ? pair : pair.substring(0, separator);
            if (!name.isEmpty()) {
                String value = separator < 0 ? "" : pair.substring(separator + 1);
                values.add(decode(name), decode(value));
            }
        }
        return values;
    }

    private String decode(String value) {
        try {
            // 이스케이프된 바이트를 복원하고 잘못된 UTF-8은 입력 오류로 처리한다.
            String octets = URLDecoder.decode(value, StandardCharsets.ISO_8859_1);
            ByteBuffer bytes = StandardCharsets.ISO_8859_1.newEncoder().encode(CharBuffer.wrap(octets));
            return StandardCharsets.UTF_8.newDecoder().decode(bytes).toString();
        } catch (IllegalArgumentException | CharacterCodingException exception) {
            throw new InvalidRequestException("검색 조건의 URL 인코딩이 올바르지 않습니다.");
        }
    }

    private record QueryValues(MultiValueMap<String, String> parameters) implements DataBinder.ValueResolver {

        @Override
        public Object resolveValue(String name, Class<?> type) {
            List<String> values = parameters.get(name);
            if (values == null) {
                return null;
            }
            return values.size() == 1 ? values.getFirst() : values.toArray(String[]::new);
        }

        @Override
        public Set<String> getNames() {
            return parameters.keySet();
        }
    }
}
