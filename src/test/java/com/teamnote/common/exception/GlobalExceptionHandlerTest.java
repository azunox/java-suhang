package com.teamnote.common.exception;

import com.teamnote.common.response.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ValidationTestController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void returnsUnifiedJsonAndFieldDetailsForInvalidRequest() throws Exception {
        mockMvc.perform(post("/test/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.error.fields[0].field").value("title"));
    }

    @Test
    void preservesMethodNotAllowedStatusAndAllowHeader() throws Exception {
        mockMvc.perform(get("/test/messages"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(header().string("Allow", "POST"))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("METHOD_NOT_ALLOWED"));
    }

    @ParameterizedTest
    @CsvSource({
            "invalid, 400, INVALID_REQUEST, 시작일은 종료일보다 늦을 수 없습니다.",
            "missing, 404, RESOURCE_NOT_FOUND, 프로젝트를 찾을 수 없습니다.",
            "conflict, 409, CONFLICT, 하위 데이터가 있는 프로젝트는 삭제할 수 없습니다."
    })
    void mapsDomainExceptionsToUnifiedJson(String type, int statusCode, String code, String message)
            throws Exception {
        mockMvc.perform(get("/test/errors/{type}", type))
                .andExpect(status().is(statusCode))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data").value(nullValue()))
                .andExpect(jsonPath("$.error.code").value(code))
                .andExpect(jsonPath("$.error.message").value(message))
                .andExpect(jsonPath("$.error.fields").isArray())
                .andExpect(jsonPath("$.error.fields").isEmpty());
    }

    @Test
    void preservesUnsupportedMediaTypeStatus() throws Exception {
        mockMvc.perform(post("/test/messages")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("title"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("UNSUPPORTED_MEDIA_TYPE"));
    }

    @Test
    void preservesNotFoundStatusForUnknownPath() throws Exception {
        mockMvc.perform(get("/test/unknown"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    void hidesInternalDetailsForUnexpectedException() throws Exception {
        mockMvc.perform(get("/test/errors/unexpected"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data").value(nullValue()))
                .andExpect(jsonPath("$.error.code").value("INTERNAL_SERVER_ERROR"))
                .andExpect(jsonPath("$.error.message").value("서버 내부 오류가 발생했습니다."))
                .andExpect(jsonPath("$.error.fields").isEmpty());
    }

    @RestController
    static class ValidationTestController {

        @PostMapping("/test/messages")
        ApiResponse<String> create(@Valid @RequestBody MessageRequest request) {
            return ApiResponse.success(request.title());
        }

        @GetMapping("/test/errors/{type}")
        ApiResponse<Void> fail(@PathVariable String type) {
            throw switch (type) {
                case "invalid" -> new InvalidRequestException("시작일은 종료일보다 늦을 수 없습니다.");
                case "missing" -> new ResourceNotFoundException("프로젝트를 찾을 수 없습니다.");
                case "conflict" -> new ConflictException("하위 데이터가 있는 프로젝트는 삭제할 수 없습니다.");
                default -> new IllegalStateException("외부에 노출하면 안 되는 내부 오류");
            };
        }
    }

    record MessageRequest(@NotBlank String title) {
    }
}
