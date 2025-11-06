package kr.co.ongil.domain.auth.controller;

import kr.co.ongil.domain.auth.service.AuthService;
import kr.co.ongil.domain.auth.dto.request.RegisterRequest;
import kr.co.ongil.domain.auth.dto.request.LoginRequest;
import kr.co.ongil.domain.auth.dto.request.RefreshRequest;
import kr.co.ongil.domain.auth.dto.response.LoginResponse;
import kr.co.ongil.domain.auth.dto.response.RefreshResponse;
import kr.co.ongil.global.common.response.ApiResponse;
import kr.co.ongil.global.common.response.ResponseMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
@Validated
@Tag(name = "Auth API", description = "인증 관련 API")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "회원가입", description = "새로운 사용자를 등록합니다.")
    public ApiResponse<String> register(@Valid @ModelAttribute RegisterRequest request) {

        authService.register(request);
        return ApiResponse.success(ResponseMessage.SIGNUP_SUCCESS);
    }

    @PostMapping("/refresh")
    @Operation(summary = "토큰 재발급", description = "리프레시 토큰을 사용하여 새로운 액세스 토큰과 리프레시 토큰을 발급받습니다.")
    public ApiResponse<RefreshResponse> refresh(@Valid @RequestBody RefreshRequest request) {

        RefreshResponse refreshResponse = authService.refresh(request);
        return ApiResponse.success(ResponseMessage.TOKEN_REISSUE_SUCCESS, refreshResponse);
    }

    @PostMapping("/logout")
    @Operation(summary = "로그아웃", description = "액세스 토큰을 블랙리스트에 추가하고 리프레시 토큰을 삭제하여 로그아웃합니다.")
    public ApiResponse<String> logout(@RequestHeader("Authorization") String authorizationHeader) {

        // "Bearer " 접두사 제거
        String accessToken = authorizationHeader.replace("Bearer ", "");

        authService.logout(accessToken);
        return ApiResponse.success(ResponseMessage.LOGOUT_SUCCESS);
    }
}
