package com.teamnote.common.config;

import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.teamnote.common.response.ApiError;
import io.swagger.v3.core.util.Json31;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.BooleanSchema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Configuration(proxyBeanMethods = false)
public class OpenApiConfiguration {

    private static final String RESPONSE_COMPONENT = "#/components/responses/";
    public static final String RESOURCE_NOT_FOUND_REF = RESPONSE_COMPONENT + "ResourceNotFound";
    public static final String CONFLICT_REF = RESPONSE_COMPONENT + "Conflict";

    @Bean
    OpenAPI teamnoteOpenApi() {
        Components components = new Components()
                .addSchemas("ApiErrorResponse", new ObjectSchema()
                        .required(List.of("success", "data", "error"))
                        .addProperty("success", new BooleanSchema()._const(false))
                        .addProperty("data", new Schema<>().types(Set.of("null")))
                        .addProperty("error", new Schema<>().$ref("#/components/schemas/ApiError")))
                .addResponses("InvalidRequest", errorResponse(
                        "요청 값이 유효하지 않거나 입력 조건을 충족하지 않습니다.",
                        "INVALID_REQUEST", "요청 값이 올바르지 않습니다."))
                .addResponses("ResourceNotFound", errorResponse(
                        "요청한 프로젝트나 항목을 찾을 수 없거나 프로젝트 소속이 일치하지 않습니다.",
                        "RESOURCE_NOT_FOUND", "프로젝트를 찾을 수 없습니다."))
                .addResponses("Conflict", errorResponse(
                        "업무 또는 회의가 남아 있는 프로젝트는 삭제할 수 없습니다.",
                        "CONFLICT", "업무 또는 회의가 남아 있는 프로젝트는 삭제할 수 없습니다."))
                .addResponses("UnsupportedMediaType", errorResponse(
                        "지원하지 않는 요청 본문 형식입니다.",
                        "UNSUPPORTED_MEDIA_TYPE", "지원하지 않는 요청 형식입니다."))
                .addResponses("InternalServerError", errorResponse(
                        "예상하지 못한 서버 오류가 발생했습니다.",
                        "INTERNAL_SERVER_ERROR", "서버 내부 오류가 발생했습니다."));
        return new OpenAPI().components(components);
    }

    @Bean
    OperationCustomizer commonApiErrorResponses() {
        return (operation, handlerMethod) -> {
            var responses = operation.getResponses();
            if (operation.getRequestBody() != null || operation.getParameters() != null) {
                responses.putIfAbsent("400", new ApiResponse().$ref(RESPONSE_COMPONENT + "InvalidRequest"));
            }
            if (operation.getRequestBody() != null) {
                responses.putIfAbsent("415", new ApiResponse().$ref(RESPONSE_COMPONENT + "UnsupportedMediaType"));
            }
            responses.putIfAbsent("500", new ApiResponse().$ref(RESPONSE_COMPONENT + "InternalServerError"));
            return operation;
        };
    }

    private ApiResponse errorResponse(String description, String code, String message) {
        ApiError error = new ApiError(code, message, List.of());
        ObjectNode example = Json31.mapper().valueToTree(com.teamnote.common.response.ApiResponse.failure(error));
        // Swagger 직렬화에서는 기본적으로 null 필드를 생략하므로 실제 응답의 data를 명시한다.
        example.putNull("data");
        MediaType json = new MediaType()
                .schema(new Schema<>().$ref("#/components/schemas/ApiErrorResponse"))
                .example(example);
        return new ApiResponse()
                .description(description)
                .content(new Content().addMediaType(APPLICATION_JSON_VALUE, json));
    }
}
