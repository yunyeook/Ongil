package kr.co.ongil.domain.patient.insight.dto.internal;

import com.fasterxml.jackson.annotation.JsonProperty;
import kr.co.ongil.domain.patient.insight.entity.PeriodType;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

/**
 * 분석 기간 정보 (주간/월간)
 */
public record PeriodInfo(
    @JsonProperty("period_type")
    PeriodType periodType,

    @JsonProperty("current_start")
    LocalDate currentStart,

    @JsonProperty("current_end")
    LocalDate currentEnd,

    @JsonProperty("previous_start")
    LocalDate previousStart,

    @JsonProperty("previous_end")
    LocalDate previousEnd
) {

    /**
     * 이번 주와 지난 주 기간 정보 생성
     * 기준: 월요일 00:00 ~ 일요일 23:59
     * @deprecated 이번 주가 아직 진행 중일 수 있으므로 lastCompletedWeek() 사용 권장
     */
    @Deprecated
    public static PeriodInfo thisWeekAndLastWeek() {
        LocalDate today = LocalDate.now();

        // 이번 주 월요일
        LocalDate thisMonday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        // 이번 주 일요일
        LocalDate thisSunday = thisMonday.plusDays(6);

        // 지난 주 월요일
        LocalDate lastMonday = thisMonday.minusWeeks(1);
        // 지난 주 일요일
        LocalDate lastSunday = lastMonday.plusDays(6);

        return new PeriodInfo(PeriodType.WEEKLY, thisMonday, thisSunday, lastMonday, lastSunday);
    }

    /**
     * 이번 달과 지난 달 기간 정보 생성
     * 기준: 1일 00:00 ~ 말일 23:59
     * @deprecated 이번 달이 아직 진행 중일 수 있으므로 lastCompletedMonth() 사용 권장
     */
    @Deprecated
    public static PeriodInfo thisMonthAndLastMonth() {
        LocalDate today = LocalDate.now();

        // 이번 달 1일
        LocalDate thisMonthFirst = today.with(TemporalAdjusters.firstDayOfMonth());
        // 이번 달 말일
        LocalDate thisMonthLast = today.with(TemporalAdjusters.lastDayOfMonth());

        // 지난 달 1일
        LocalDate lastMonthFirst = thisMonthFirst.minusMonths(1);
        // 지난 달 말일
        LocalDate lastMonthLast = lastMonthFirst.with(TemporalAdjusters.lastDayOfMonth());

        return new PeriodInfo(PeriodType.MONTHLY, thisMonthFirst, thisMonthLast, lastMonthFirst, lastMonthLast);
    }

    /**
     * 완료된 지난 주와 그 전 주 기간 정보 생성 (권장)
     * 기준: 월요일 00:00 ~ 일요일 23:59
     *
     * 예시: 오늘이 2025-11-14 (목요일)
     * - current: 2025-11-04(월) ~ 2025-11-10(일) (지난 주, 완료됨)
     * - previous: 2025-10-28(월) ~ 2025-11-03(일) (그 전 주)
     */
    public static PeriodInfo lastCompletedWeek() {
        LocalDate today = LocalDate.now();

        // 이번 주 월요일
        LocalDate thisMonday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        // 지난 주 월요일 (완료된 주)
        LocalDate lastMonday = thisMonday.minusWeeks(1);
        LocalDate lastSunday = lastMonday.plusDays(6);

        // 그 전 주 월요일
        LocalDate beforeLastMonday = lastMonday.minusWeeks(1);
        LocalDate beforeLastSunday = beforeLastMonday.plusDays(6);

        return new PeriodInfo(PeriodType.WEEKLY, lastMonday, lastSunday, beforeLastMonday, beforeLastSunday);
    }

    /**
     * 완료된 지난 달과 그 전 달 기간 정보 생성 (권장)
     * 기준: 1일 00:00 ~ 말일 23:59
     *
     * 예시: 오늘이 2025-11-14
     * - current: 2025-10-01 ~ 2025-10-31 (지난 달, 완료됨)
     * - previous: 2025-09-01 ~ 2025-09-30 (그 전 달)
     */
    public static PeriodInfo lastCompletedMonth() {
        LocalDate today = LocalDate.now();

        // 이번 달 1일
        LocalDate thisMonthFirst = today.with(TemporalAdjusters.firstDayOfMonth());

        // 지난 달 1일 (완료된 달)
        LocalDate lastMonthFirst = thisMonthFirst.minusMonths(1);
        LocalDate lastMonthLast = lastMonthFirst.with(TemporalAdjusters.lastDayOfMonth());

        // 그 전 달 1일
        LocalDate beforeLastMonthFirst = lastMonthFirst.minusMonths(1);
        LocalDate beforeLastMonthLast = beforeLastMonthFirst.with(TemporalAdjusters.lastDayOfMonth());

        return new PeriodInfo(PeriodType.MONTHLY, lastMonthFirst, lastMonthLast, beforeLastMonthFirst, beforeLastMonthLast);
    }

    /**
     * 특정 날짜가 속한 주와 그 이전 주 기간 정보 생성
     */
    public static PeriodInfo forWeek(LocalDate date) {
        // 해당 날짜가 속한 주의 월요일
        LocalDate monday = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        // 해당 주 일요일
        LocalDate sunday = monday.plusDays(6);

        // 지난 주 월요일
        LocalDate lastMonday = monday.minusWeeks(1);
        // 지난 주 일요일
        LocalDate lastSunday = lastMonday.plusDays(6);

        return new PeriodInfo(PeriodType.WEEKLY, monday, sunday, lastMonday, lastSunday);
    }

    /**
     * 특정 날짜가 속한 월과 그 이전 월 기간 정보 생성
     */
    public static PeriodInfo forMonth(LocalDate date) {
        // 해당 월 1일
        LocalDate monthFirst = date.with(TemporalAdjusters.firstDayOfMonth());
        // 해당 월 말일
        LocalDate monthLast = date.with(TemporalAdjusters.lastDayOfMonth());

        // 지난 달 1일
        LocalDate lastMonthFirst = monthFirst.minusMonths(1);
        // 지난 달 말일
        LocalDate lastMonthLast = lastMonthFirst.with(TemporalAdjusters.lastDayOfMonth());

        return new PeriodInfo(PeriodType.MONTHLY, monthFirst, monthLast, lastMonthFirst, lastMonthLast);
    }
}
