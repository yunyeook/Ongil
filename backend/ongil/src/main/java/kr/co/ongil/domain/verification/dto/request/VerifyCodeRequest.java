package kr.co.ongil.domain.verification.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "인증번호 검증 요청")
public record VerifyCodeRequest(

    @Schema(description = "전화번호 (하이픈 없이)", example = "01012345678")
    @NotBlank(message = "전화번호는 필수입니다.")
    @Pattern(regexp = "^01[0-9]{8,9}$", message = "올바른 전화번호 형식이 아닙니다.")
    String phoneNumber,

    @Schema(description = "인증번호 (6자리)", example = "839201")
    @NotBlank(message = "인증번호는 필수입니다.")
    @Size(min = 6, max = 6, message = "인증번호는 6자리여야 합니다.")
    @Pattern(regexp = "^[0-9]{6}$", message = "인증번호는 숫자 6자리여야 합니다.")
    String verificationCode,

    @Schema(
            description = "토큰 사용 목적 (SELF: 본인 인증, RELATIONSHIP: 관계 연결 인증)",
            example = "SELF",
            allowableValues = {"SELF", "RELATIONSHIP"}
    )
    String grants
) {
}
