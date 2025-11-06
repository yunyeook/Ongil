package kr.co.ongil.domain.notification.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationType {
    RELATIONSHIP_REGIST("환자-보호자 관계 등록"),
    SAFEZONE_EXIT("안전범위 이탈"),
    NAVIGATION_START("길안내 시작"),
    NAVIGATION_END("길안내 종료"),
    ABNORMAL_DETECTED("이상행동 감지"),
    CALL_REQUEST("통화 요청"),
    SOS("SOS 요청"),
    SOS_ACK("SOS 재생 완료 여부 응답");

    private final String description;
}