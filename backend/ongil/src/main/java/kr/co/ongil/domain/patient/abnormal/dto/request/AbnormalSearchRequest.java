package kr.co.ongil.domain.patient.abnormal.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.co.ongil.domain.patient.abnormal.entity.AbnormalType;
import kr.co.ongil.domain.patient.safezone.entity.SafeZoneLevel;
import kr.co.ongil.global.exception.BusinessException;
import kr.co.ongil.global.exception.ErrorCode;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Schema(name = "AbnormalSearchRequest", description = "이상탐지 검색 조건")
public record AbnormalSearchRequest(
    @Schema(description = "이상탐지 유형", example = "SAFEZONE_EXIT")
    String type,

    @Schema(description = "안전범위 단계", example = "SECOND")
    String level,

    @Schema(description = "조회 시작날짜 (YYYYMMDD)", example = "20251001")
    String from,

    @Schema(description = "조회 종료날짜 (YYYYMMDD)", example = "20251022")
    String to,

    @Schema(description = "페이지 번호", example = "1")
    Integer page,

    @Schema(description = "페이지 크기", example = "10")
    Integer size
) {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * 기본값을 적용한 생성자
     */
    public AbnormalSearchRequest {
        page = (page != null) ? page : 1;
        size = (size != null) ? size : 10;
    }

    /**
     * AbnormalType Enum으로 변환
     */
    public AbnormalType getAbnormalType() {
        if (type == null || type.isEmpty()) {
            return null;
        }
        try {
            return AbnormalType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    /**
     * SafeZoneLevel Enum으로 변환
     */
    public SafeZoneLevel getSafeZoneLevel() {
        if (level == null || level.isEmpty()) {
            return null;
        }
        try {
            return SafeZoneLevel.valueOf(level.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    /**
     * 시작 날짜 파싱 (기본값: 7일 전)
     */
    public LocalDateTime getFromDateTime() {
        if (from == null || from.isEmpty()) {
            return LocalDate.now().minusDays(7).atStartOfDay();
        }
        try {
            LocalDate date = LocalDate.parse(from, DATE_FORMATTER);
            return date.atStartOfDay();
        } catch (DateTimeParseException e) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    /**
     * 종료 날짜 파싱 (기본값: 오늘)
     */
    public LocalDateTime getToDateTime() {
        if (to == null || to.isEmpty()) {
            return LocalDate.now().atTime(LocalTime.MAX);
        }
        try {
            LocalDate date = LocalDate.parse(to, DATE_FORMATTER);
            return date.atTime(LocalTime.MAX);
        } catch (DateTimeParseException e) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static AbnormalSearchRequest of(
        String type,
        String level,
        String from,
        String to,
        Integer page,
        Integer size
    ) {
        return new AbnormalSearchRequest(type, level, from, to, page, size);
    }
}