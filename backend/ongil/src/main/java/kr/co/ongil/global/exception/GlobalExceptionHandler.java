package kr.co.ongil.global.exception;

import java.util.Optional;
import kr.co.ongil.global.common.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import jakarta.validation.ConstraintViolationException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 비즈니스 로직 예외
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<String>> handleBusinessException(BusinessException e) {
        log.warn("BusinessException: {}", e.getMessage());
        ErrorCode code = e.getErrorCode();
        return ResponseEntity
            .status(code.getStatus())
            .body(ApiResponse.fail(e.getErrorCode()));
    }

    // Validation 예외 (@Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<String>> handleValidationException(MethodArgumentNotValidException e) {
        log.warn("Validation failed: {}", e.getBindingResult().getAllErrors());
        return ResponseEntity
            .status(ErrorCode.INVALID_INPUT.getStatus())
            .body(ApiResponse.fail(ErrorCode.INVALID_INPUT));
    }

    // Constraint Violation 예외
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<String>> handleConstraintViolationException(ConstraintViolationException e) {
        log.warn("Constraint violation: {}", e.getMessage());
        return ResponseEntity
            .status(ErrorCode.INVALID_INPUT.getStatus())
            .body(ApiResponse.fail(ErrorCode.INVALID_INPUT));
    }

    // JSON 파싱 오류
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<String>> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        log.warn("JSON parsing error: {}", e.getMessage());
        return ResponseEntity
            .status(ErrorCode.JSON_PARSING_ERROR.getStatus())
            .body(ApiResponse.fail(ErrorCode.JSON_PARSING_ERROR));
    }

    // 타입 불일치 (PathVariable, RequestParam 등)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<String>> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        log.warn("Type mismatch: {}", e.getMessage());
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.fail(ErrorCode.INVALID_INPUT));
    }

    // 필수 파라미터 누락
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<String>> handleMissingServletRequestParameterException(MissingServletRequestParameterException e) {
        log.warn("Missing parameter: {}", e.getParameterName());
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.fail(ErrorCode.INVALID_REQUEST));
    }

    // 지원하지 않는 HTTP 메서드
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<String>> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        log.warn("Method not supported: {}", e.getMethod());
        return ResponseEntity
            .status(HttpStatus.METHOD_NOT_ALLOWED)
            .body(new ApiResponse<>("지원하지 않는 HTTP 메서드입니다.", ""));
    }

    // 지원하지 않는 Content-Type
    @ExceptionHandler(org.springframework.web.HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiResponse<String>> handleHttpMediaTypeNotSupportedException(
        org.springframework.web.HttpMediaTypeNotSupportedException e) {
        log.warn("Unsupported Media Type: {}", e.getContentType());
        return ResponseEntity
            .status(HttpStatus.UNSUPPORTED_MEDIA_TYPE) // 415
            .body(ApiResponse.fail(ErrorCode.UNSUPPORTED_MEDIA_TYPE));
    }
    // SSE 응답 중단 시 발생
    @ExceptionHandler(HttpMessageNotWritableException.class)
    public ResponseEntity<Void> handleSseWriteError(HttpMessageNotWritableException e) {
        if (Optional.ofNullable(e.getMessage()).orElse("")
            .toLowerCase().contains("text/event-stream")) {
            log.debug("SSE 연결 종료 중 HttpMessageNotWritableException 예외 발생");
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).build();
    }

    // 그 외 모든 예외
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleException(Exception e, jakarta.servlet.http.HttpServletRequest request) {

        String accept = Optional.ofNullable(request.getHeader("Accept")).orElse("");

        //  SSE 요청이라면 절대 JSON 응답 시도하지 않음
        if (accept.contains("text/event-stream")) {
            log.warn("SSE 요청 중 예외 발생 → JSON 응답 생략 (연결만 종료): {}", e.getMessage());
            return null; // 아무 응답도 하지 않음 → SSE 연결만 종료됨
        }

        //  일반 REST 요청은 기존대로 JSON 반환
        log.error("Unexpected error occurred: ", e);
        ErrorCode code = ErrorCode.INTERNAL_ERROR;

        return ResponseEntity
            .status(code.getStatus())
            .body(ApiResponse.fail(code));
    }
}