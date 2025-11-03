package kr.co.ongil.domain.verification.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import kr.co.ongil.domain.verification.dto.request.SendVerificationRequest;
import kr.co.ongil.domain.verification.dto.request.VerifyCodeRequest;
import kr.co.ongil.domain.verification.dto.response.VerificationResponse;
import kr.co.ongil.domain.verification.service.PhoneVerificationService;
import kr.co.ongil.global.common.response.ApiResponse;
import kr.co.ongil.global.common.response.ResponseMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 전화번호 인증 API
 *
 * WebMvcConfig에 의해 자동으로 /api/v1 prefix가 추가됩니다.
 * 실제 경로: /api/v1/phone-verifications
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/phone-verifications")
@RequiredArgsConstructor
@Tag(name = "Phone Verification API", description = "전화번호 인증 API")
public class PhoneVerificationController {

    private final PhoneVerificationService phoneVerificationService;

    /**
     * 인증번호 발송
     *
     * @param request        전화번호 요청
     * @param servletRequest HTTP 요청 (IP 추출용)
     * @return 성공 응답
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "인증번호 발송",
            description = "전화번호로 6자리 인증번호를 발송합니다. (개발 환경에서는 콘솔에 출력됩니다)"
    )
    public ApiResponse<String> sendVerificationCode(
            @Valid @RequestBody SendVerificationRequest request,
            HttpServletRequest servletRequest
    ) {
        String ipAddress = getClientIpAddress(servletRequest);

        phoneVerificationService.sendVerificationCode(request, ipAddress);

        return ApiResponse.success(
                ResponseMessage.PHONE_VERIFICATION_SENT);
    }

    /**
     * 인증번호 검증 및 1회용 토큰 발급
     *
     * @param request 인증번호 검증 요청
     * @return 인증 성공 응답 (1회용 토큰 포함)
     */
    @PostMapping("/verify")
    @Operation(
            summary = "인증번호 검증",
            description = "인증번호를 검증하고 지정된 목적(grant)에 사용할 1회용 verificationToken을 발급합니다."
    )
    public ApiResponse<VerificationResponse> verifyCode(
            @Valid @RequestBody VerifyCodeRequest request
    ) {
        VerificationResponse response = phoneVerificationService.verifyCode(request);

        return ApiResponse.success(
                ResponseMessage.PHONE_VERIFICATION_SUCCESS,response
        );
    }

    /**
     * 클라이언트 IP 주소 추출
     *
     * @param request HTTP 요청
     * @return IP 주소
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");

        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        // IPv6 루프백 주소를 IPv4로 변환
        if ("0:0:0:0:0:0:0:1".equals(ip)) {
            ip = "127.0.0.1";
        }

        return ip;
    }
}
