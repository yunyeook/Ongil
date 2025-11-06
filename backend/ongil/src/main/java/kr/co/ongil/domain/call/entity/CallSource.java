package kr.co.ongil.domain.call.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 통화 발신 출처
 * 앱 내에서 발신했는지, 시스템 다이얼러를 통해 발신했는지 구분합니다.
 */
@Getter
@RequiredArgsConstructor
public enum CallSource {

    /**
     * 앱 내 통화 (VoIP)
     */
    APP("앱 내 통화"),

    /**
     * 시스템 다이얼러를 통한 통화
     */
    SYSTEM_DIALER("시스템 다이얼러");

    private final String description;
}
