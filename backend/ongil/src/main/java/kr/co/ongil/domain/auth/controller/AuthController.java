package kr.co.ongil.domain.auth.controller;

import kr.co.ongil.domain.auth.service.AuthService;
import kr.co.ongil.domain.auth.dto.request.RegisterRequest;
import kr.co.ongil.global.common.response.ApiResponse;
import kr.co.ongil.global.common.response.ResponseMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@Validated
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<String>> register(@Valid @RequestBody RegisterRequest request) {
        log.info("회원가입 요청: {}", request.getPhoneNumber());

        try {
            authService.register(request);
            return ResponseEntity.ok(ApiResponse.success(ResponseMessage.SIGNUP_SUCCESS.getMessage()));
        } catch (IllegalArgumentException e) {
            log.error("유효성 검증 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .body(ApiResponse.success(ResponseMessage.INVALID_INPUT.getMessage()));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("이미 가입된")) {
                log.error("중복 가입 시도: {}", e.getMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.success(ResponseMessage.DUPLICATE_USER.getMessage()));
            }
            log.error("회원가입 처리 오류: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.success(ResponseMessage.INTERNAL_SERVER_ERROR.getMessage()));
        } catch (Exception e) {
            log.error("예상치 못한 오류: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.success(ResponseMessage.INTERNAL_SERVER_ERROR.getMessage()));
        }
    }
}
