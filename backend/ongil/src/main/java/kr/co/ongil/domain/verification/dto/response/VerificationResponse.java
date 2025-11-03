package kr.co.ongil.domain.verification.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "인증 성공 응답")
public record VerificationResponse(

    @Schema(description = "인증 성공 여부", example = "true")
    boolean verified,

    @Schema(description = "1회용 목적제한 토큰 (JWT)", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    String verificationToken
) {

    public static VerificationResponse of(boolean verified, String verificationToken) {
        return new VerificationResponse(verified, verificationToken);
    }
}
