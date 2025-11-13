package kr.co.ongil.global.sms;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Slf4j
@Primary
@Service
@Profile({"local", "dev"})
public class MockSmsService implements SmsService {

    @Override
    public void sendVerificationCode(String phoneNumber, String verificationCode) {
        log.info("========================================");
        log.info("     [개발용] SMS 전송 (Mock)");
        log.info("========================================");
        log.info("수신번호: {}", phoneNumber);
        log.info("인증번호: {}", verificationCode);
        log.info("유효시간: 3분");
        log.info("========================================");
    }

    @Override
    public void sendMessage(String phoneNumber, String message) {
        return;
    }
}
