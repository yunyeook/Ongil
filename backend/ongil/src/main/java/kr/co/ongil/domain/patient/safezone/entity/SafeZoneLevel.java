package kr.co.ongil.domain.patient.safezone.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 안전범위 단계
 */
@Getter
@RequiredArgsConstructor
public enum SafeZoneLevel {
    FIRST(1, "1단계"),
    SECOND(2, "2단계"),
    THIRD(3, "3단계");

    private final int level;
    private final String description;
}