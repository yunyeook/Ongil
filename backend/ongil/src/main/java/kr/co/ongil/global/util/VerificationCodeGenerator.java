package kr.co.ongil.global.util;

import java.security.SecureRandom;

/**
 * 인증번호 생성 유틸리티
 *
 * 전화번호 인증, 이메일 인증 등 다양한 인증 시나리오에서 사용 가능한 랜덤 코드 생성기
 */
public class VerificationCodeGenerator {

    private static final SecureRandom random = new SecureRandom();

    /**
     * 6자리 숫자 인증번호 생성
     *
     * @return 6자리 숫자 문자열 (예: "839201")
     */
    public static String generate() {
        return generate(6);
    }

    /**
     * 지정된 자릿수의 숫자 인증번호 생성
     *
     * @param length 생성할 코드의 자릿수 (예: 4, 6, 8)
     * @return 지정된 자릿수의 숫자 문자열
     */
    public static String generate(int length) {
        if (length <= 0 || length > 10) {
            throw new IllegalArgumentException("코드 길이는 1~10 사이여야 합니다.");
        }

        int max = (int) Math.pow(10, length);
        int code = random.nextInt(max);
        return String.format("%0" + length + "d", code);
    }

    /**
     * 영숫자 혼합 랜덤 코드 생성 (대문자 + 숫자)
     *
     * @param length 생성할 코드의 길이
     * @return 영숫자 혼합 문자열 (예: "A3K9M2")
     */
    public static String generateAlphanumeric(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder code = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            code.append(chars.charAt(random.nextInt(chars.length())));
        }

        return code.toString();
    }
}
