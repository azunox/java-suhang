package com.teamnote.common.exception;

import java.util.List;

import com.teamnote.common.response.ApiError;
import com.teamnote.common.response.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidRequest(InvalidRequestException exception) {
        return domainErrorResponse(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFound(ResourceNotFoundException exception) {
        return domainErrorResponse(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiResponse<Void>> handleConflict(ConflictException exception) {
        return domainErrorResponse(HttpStatus.CONFLICT, exception.getMessage());
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            Exception exception,
            Object body,
            HttpHeaders headers,
            HttpStatusCode statusCode,
            WebRequest request
    ) {
        List<ApiError.InvalidField> fields = List.of();
        if (exception instanceof MethodArgumentNotValidException validationException) {
            fields = validationException.getBindingResult().getFieldErrors().stream()
                    .map(error -> new ApiError.InvalidField(error.getField(), error.getDefaultMessage()))
                    .toList();
        }

        ApiError error = new ApiError(errorCode(statusCode.value()), errorMessage(statusCode), fields);
        return super.handleExceptionInternal(exception, ApiResponse.failure(error), headers, statusCode, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpectedException(Exception exception) {
        log.error("처리하지 못한 요청 오류가 발생했습니다.", exception);
        ApiError error = new ApiError("INTERNAL_SERVER_ERROR", "서버 내부 오류가 발생했습니다.", List.of());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.failure(error));
    }

    private ResponseEntity<ApiResponse<Void>> domainErrorResponse(HttpStatus status, String message) {
        ApiError error = new ApiError(errorCode(status.value()), message, List.of());
        return ResponseEntity.status(status).body(ApiResponse.failure(error));
    }

    private String errorCode(int status) {
        return switch (status) {
            case 400 -> "INVALID_REQUEST";
            case 404 -> "RESOURCE_NOT_FOUND";
            case 405 -> "METHOD_NOT_ALLOWED";
            case 409 -> "CONFLICT";
            case 415 -> "UNSUPPORTED_MEDIA_TYPE";
            default -> "HTTP_" + status;
        };
    }

    private String errorMessage(HttpStatusCode statusCode) {
        return switch (statusCode.value()) {
            case 400 -> "요청 값이 올바르지 않습니다.";
            case 404 -> "요청한 대상을 찾을 수 없습니다.";
            case 405 -> "지원하지 않는 HTTP 메서드입니다.";
            case 409 -> "현재 데이터 상태와 요청이 충돌합니다.";
            case 415 -> "지원하지 않는 요청 형식입니다.";
            default -> "요청을 처리할 수 없습니다. HTTP 상태: " + statusCode.value();
        };
    }
}
