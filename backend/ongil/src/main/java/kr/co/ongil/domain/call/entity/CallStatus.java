package kr.co.ongil.domain.call.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 통화 상태
 * VoIP 통화의 상태 전이를 나타냅니다.
 */
@Getter
@RequiredArgsConstructor
public enum CallStatus {

    /**
     * 통화 요청 생성
     */
    CREATED("통화 요청 생성"),

    /**
     * 벨 울리는 중
     */
    RINGING("벨 울리는 중"),

    /**
     * 통화 연결
     */
    CONNECTED("통화 연결"),

    /**
     * 정상 종료
     */
    ENDED("정상 종료"),

    /**
     * 발신자 취소
     */
    CANCELED("발신자 취소"),

    /**
     * 수신자 거절
     */
    REJECTED("수신자 거절"),

    /**
     * 연결 실패
     */
    FAILED("연결 실패"),

    /**
     * 부재중
     */
    MISSED("부재중"),

    /**
     * 통화 끊김 (네트워크 등 이슈)
     */
    DROPPED("통화 끊김");

    private final String description;
}
