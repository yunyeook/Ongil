package kr.co.ongil.domain.password.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.co.ongil.domain.password.dto.request.PasswordResetRequest;
import kr.co.ongil.domain.password.service.PasswordService;
import kr.co.ongil.global.common.response.ApiResponse;
import kr.co.ongil.global.common.response.ResponseMessage;
import kr.co.ongil.global.security.userdetails.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/password")
@Tag(name = "Password API", description = "비밀번호 관련 API")
public class PasswordController {

    private final PasswordService passwordService;
    
    @PatchMapping("/reset")
    @Operation(summary = "비밀번호 변경", description = "현재 로그인한 사용자의 비밀번호를 변경합니다.")
    public ApiResponse<String> resetPassword(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody PasswordResetRequest request) {

        passwordService.resetPassword(userDetails.getUserId(), request);
        return ApiResponse.success(ResponseMessage.PASSWORD_RESET_SUCCESS);
    }
}
