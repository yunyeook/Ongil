package kr.co.ongil.global.sms;

/**
 * SMS 전송 서비스 인터페이스
 *
 * 다양한 SMS 서비스 제공자를 지원할 수 있도록 추상화
 * 구현체: CoolSmsService, MockSmsService 등
 */
public interface SmsService {

    /**
     * 인증번호 SMS 전송
     *
     * @param phoneNumber 수신자 전화번호 (예: "01012345678")
     * @param verificationCode 6자리 인증번호
     */
    void sendVerificationCode(String phoneNumber, String verificationCode);

    /**
     * 일반 메시지 SMS 전송
     *
     * @param phoneNumber 수신자 전화번호
     * @param message 전송할 메시지 내용
     */
    void sendMessage(String phoneNumber, String message);
}
