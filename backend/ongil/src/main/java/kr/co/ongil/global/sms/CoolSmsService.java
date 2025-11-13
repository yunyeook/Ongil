package kr.co.ongil.global.sms;

import kr.co.ongil.global.exception.BusinessException;
import kr.co.ongil.global.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import net.nurigo.sdk.NurigoApp;
import net.nurigo.sdk.message.model.Message;
import net.nurigo.sdk.message.request.SingleMessageSendingRequest;
import net.nurigo.sdk.message.service.DefaultMessageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * CoolSMS 서비스 구현체
 *
 * CoolSMS API를 사용하여 실제 SMS를 발송합니다.
 */
@Slf4j
@Service
public class CoolSmsService implements SmsService {

    private final DefaultMessageService messageService;
    private final String fromNumber;

    public CoolSmsService(
            @Value("${coolsms.api.key}") String apiKey,
            @Value("${coolsms.api.secret}") String apiSecret,
            @Value("${coolsms.from}") String fromNumber,
            @Value("${coolsms.domain}") String domain
    ) {
        this.messageService = NurigoApp.INSTANCE.initialize(apiKey, apiSecret, domain);
        this.fromNumber = fromNumber;
        log.info("CoolSMS 서비스 초기화 완료 (발신번호: {})", fromNumber);
    }

    /**
     * 인증번호 SMS 전송
     */
    @Override
    public void sendVerificationCode(String phoneNumber, String verificationCode) {
        String messageText = String.format(
                "[온길] 인증번호는 [%s]입니다.\n인증번호를 입력해주세요.\n유효시간: 3분",
                verificationCode
        );

        sendMessage(phoneNumber, messageText);
        log.info("인증번호 SMS 발송 완료: phoneNumber={}, code={}", phoneNumber, verificationCode);
    }

    /**
     * 일반 메시지 SMS 전송
     */
    @Override
    public void sendMessage(String phoneNumber, String messageText) {
        try {
            Message message = new Message();
            message.setFrom(fromNumber);
            message.setTo(phoneNumber);
            message.setText(messageText);

            messageService.sendOne(new SingleMessageSendingRequest(message));

            log.info("SMS 발송 성공: to={}, from={}", phoneNumber, fromNumber);

        } catch (Exception e) {
            log.error("SMS 발송 실패: phoneNumber={}, error={}", phoneNumber, e.getMessage(), e);
            throw new BusinessException(ErrorCode.SMS_SEND_FAILED);
        }
    }
}
