package kr.co.ongil.domain.notification.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationType {

    // 관계 등록 관련 알림
    RELATIONSHIP_REGIST("환자-보호자 관계 등록"),

    // 안전범위 관련 알림
    SAFEZONE_EXIT("안전범위 이탈"),
    WANDER("배회 감지"),
    DEVIATE_FROM_THE_PATH("길찾기 중 경로 이탈"),

    // 길찾기 관련 알림
    NAVIGATION_START("길안내 시작"),
    NAVIGATION_END("길안내 종료"),

    // 이상행동 감지 관련 알림
    ABNORMAL_DETECTED("이상행동 감지"),

    // Call 관련 알림
//    CALL_REQUEST("통화 요청"),
    CALL_INCOMING("전화가 왔어요"),
    CALL_MISSED("부재중 통화"),
    CALL_REJECTED("통화 거절"), // 거절은 발신자에게만 전송

    // SOS (도움 요청) 관련 알림
    SOS_REQUEST("SOS 요청"),
    SOS_ACK("SOS 재생 완료 여부 응답"),
    SOS_STOP("SOS 음성 재생 종료");

    private final String description;
}