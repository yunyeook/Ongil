package kr.co.ongil.domain.call.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 통화 유형
 * 통화의 성격을 나타냅니다.
 * 통화 방식(VoIP/기본전화)은 CallSource로 구분합니다.
 */
@Getter
@RequiredArgsConstructor
public enum CallType {

    /**
     * 일반 통화
     */
    NORMAL("일반 통화"),

    /**
     * 긴급 통화 (SOS)
     */
    EMERGENCY("긴급 통화");

    private final String description;
}
