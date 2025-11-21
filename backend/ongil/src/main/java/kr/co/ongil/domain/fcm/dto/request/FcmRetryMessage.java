package kr.co.ongil.domain.fcm.dto.request;

import java.io.Serializable;
import java.util.Map;

/**
 * [재시도 전용 DTO (Data Transfer Object)]
 * 전송에 실패해서 '한 번 더 보내야 할 정보'들을 담는 바구니역할.
 * 
 * record: 자바 14부터 도입된 데이터 전용 클래스입니다.
 * 불변(개수, 내용 수정 불가)이며, 자동으로 Getter 등이 생성되어 코드가 간결합니다.
 * 
 * implements Serializable: 네트워크를 타고 전송되기 위해 직렬화가 가능해야 함을 의미.
 */
public record FcmRetryMessage(
        String token, // 수신자의 FCM 토큰
        Map<String, String> data, // 알림 내용 (제목, 본문 등)
        int retryCount, // 현재 몇 번째 재시도인지
        String messageType // 메시지 종류 (일반 알림인지, 통화 알림인지 구분)
) implements Serializable {

    // 처음 큐에 넣을 때 사용하기 편하도록 만든 생성자 (재시도 횟수 0으로 시작)
    public FcmRetryMessage(String token, Map<String, String> data, String messageType) {
        this(token, data, 0, messageType);
    }

    /**
     * 불변 객체이므로 값을 수정하는 대신,
     * 재시도 횟수가 1 증가한 '새로운 바구니'를 만들어서 반환.
     */
    public FcmRetryMessage incrementRetry() {
        return new FcmRetryMessage(token, data, retryCount + 1, messageType);
    }
}
