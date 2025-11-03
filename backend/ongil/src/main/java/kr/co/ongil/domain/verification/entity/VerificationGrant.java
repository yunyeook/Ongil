package kr.co.ongil.domain.verification.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 인증 토큰 사용 목적
 *
 * SELF: 본인의 전화번호 소유 증명 (회원가입, 비밀번호 재설정, 전화번호 변경 등)
 * RELATIONSHIP: 타인의 전화번호에 대한 동의 증명 (보호자-환자 관계 연결 등)
 */
@Getter
@RequiredArgsConstructor
public enum VerificationGrant {

    /**
     * 본인 인증 (회원가입, 비밀번호 재설정, 전화번호 변경)
     * - 인증번호를 받은 사람 = 요청한 사람 = 본인
     */
    SELF("본인 인증"),

    /**
     * 관계 연결 인증 (보호자-환자 연결)
     * - 인증번호를 받은 사람 = 연결 대상 (타인)
     * - 요청한 사람 = 연결 주체 (본인)
     * - 상대방의 동의를 증명하는 용도
     */
    RELATIONSHIP("관계 연결 인증");

    private final String description;

    public static VerificationGrant fromString(String value) {
        if (value == null || value.isBlank()) {
            return SELF; // 기본값: 본인 인증
        }

        try {
            return VerificationGrant.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return SELF; // 잘못된 값이면 기본값
        }
    }
}
