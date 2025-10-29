package kr.co.ongil.global.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.validation.ConstraintViolationException;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Map<String, String>> handleBusinessException(BusinessException e) {
        ErrorCode code = e.getErrorCode();
        return ResponseEntity
            .status(code.getStatus())
            .body(Map.of("message", e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldError() != null
            ? e.getBindingResult().getFieldError().getDefaultMessage()
            : ErrorCode.INVALID_INPUT.getMessage();

        return ResponseEntity
            .status(ErrorCode.INVALID_INPUT.getStatus())
            .body(Map.of("message", message));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, String>> handleConstraintViolationException(ConstraintViolationException e) {
        String message = e.getConstraintViolations().stream()
            .findFirst()
            .map(violation -> violation.getMessage())
            .orElse(ErrorCode.INVALID_INPUT.getMessage());

        return ResponseEntity
            .status(ErrorCode.INVALID_INPUT.getStatus())
            .body(Map.of("message", message));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleException(Exception e) {
        e.printStackTrace();
        ErrorCode code = ErrorCode.INTERNAL_ERROR;
        return ResponseEntity
            .status(code.getStatus())
            .body(Map.of("message", code.getMessage()));
    }
}
