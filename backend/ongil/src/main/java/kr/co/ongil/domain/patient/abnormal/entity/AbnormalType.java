package kr.co.ongil.domain.patient.abnormal.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AbnormalType {
    SAFEZONE_EXIT("안전범위 이탈"),
    WANDER("배회 감지"),
    DEVIATE_FROM_THE_PATH("길찾기 중 경로 이탈");
    private final String description;
}