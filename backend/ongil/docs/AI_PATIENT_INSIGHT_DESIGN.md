# 환자 종합 인사이트 AI 요약 설계 문서

> **목표**: 온길에서 수집한 모든 활동/건강 데이터를 종합 분석하여, 보호자가 이해하기 쉬운 "주간 환자 상태 리포트"를 LLM 기반으로 자동 생성

---

## 📋 목차

1. [개요](#1-개요)
2. [데이터 소스 정리](#2-데이터-소스-정리)
3. [백엔드 집계 로직 설계](#3-백엔드-집계-로직-설계)
4. [LLM 프롬프트 설계](#4-llm-프롬프트-설계)
5. [GMS API 연동 전략](#5-gms-api-연동-전략)
6. [구현 아키텍처](#6-구현-아키텍처)
7. [API 명세](#7-api-명세)
8. [주의사항 및 확장 방안](#8-주의사항-및-확장-방안)

---

## 1. 개요

### 1.1 배경

온길은 치매 환자의 다양한 활동 데이터(위치, 이상탐지, SOS, 통화 등)와 건강 데이터(심박수, 수면, 걸음수 등)를 수집합니다.
하지만 **"숫자"만으로는 보호자가 환자 상태를 직관적으로 이해하기 어렵습니다.**

### 1.2 목표

- **주간 단위**로 환자의 행동/건강 패턴을 종합 분석
- **LLM(GPT-4o-mini 등)**을 활용하여 보호자가 이해하기 쉬운 문장으로 요약
- **위험 신호 조기 감지**: 공간 인지 저하, 불안 증가, 컨디션 악화 등
- **개인별 위험 프로파일**: "이 환자는 수면 부족 시 길을 잃는 경향" 등

### 1.3 핵심 설계 원칙

1. **LLM은 "해석"만, 계산은 백엔드가**: 숫자 계산/임계치 판단은 Spring에서, LLM은 설명 생성만
2. **재현 가능한 플래그 시스템**: 룰 기반으로 명확한 조건 정의
3. **의료 진단 금지**: 절대 질병 진단/치료 조언 X, 전문의 상담 권유만
4. **구조화된 입출력**: JSON 스키마로 입력/출력 통제

---

## 2. 데이터 소스 정리

### 2.1 활동/행동 데이터 (현재 수집 중)

| 도메인 | 엔티티/테이블 | 주요 필드 | 용도 |
|--------|--------------|----------|------|
| **이상탐지** | `Abnormal` | `abnormalType` (SAFEZONE_EXIT, WANDER, DEVIATE_FROM_THE_PATH)<br>`safeZoneLevel` (1/2/3)<br>`distanceFromCenter`, `elapsedTime`<br>`createdAt` | 공간 인지 저하, 배회 패턴 분석 |
| **대시보드 집계** | `DashboardCalc` | `routeLost` (길찾기 이탈)<br>`safezoneEmer` (배회/이상탐지)<br>`safezoneExit` (레벨별 이탈, JSONB)<br>`emerCall` (응급전화)<br>`sosSign` (SOS 요청)<br>`favorite` (자주 가는 목적지, JSONB) | 주간 통계 기반 분석 |
| **SOS** | `Sos` | `patientId`, `guardianId`<br>`isResponsed`<br>`createdAt` | 긴급 상황 빈도/패턴 |
| **통화 기록** | `CallLog` | `callType` (EMERGENCY, NORMAL)<br>`duration`, `startedAt`, `endedAt` | 응급 통화 빈도, 통화 패턴 |
| **즐겨찾기** | `Favorite` | `placeName`, `latitude`, `longitude`<br>방문 빈도 (DashboardCalc에서 집계) | 루틴 안정성, 목적지 다양성 |

### 2.2 건강 데이터 (Samsung Health 연동)

| 타입 | 단위 | 활용 |
|------|------|------|
| **HEART_RATE** | bpm | 심박 변동성, 불안/스트레스 신호 |
| **OXYGEN_SATURATION** | % | 호흡/순환기 건강 (데이터 부족 시 제외) |
| **SLEEP** | hours | 수면 부족과 행동 혼란 상관관계 |
| **STEP_COUNT** | steps | 활동량 감소, 우울/위축 신호 |

**확장 가능 (향후):**
- 혈압, 체온, 체중, 활동 칼로리 등

### 2.3 분석 기간

- **기준 기간**: 이번 주 월요일 00:00 ~ 일요일 23:59
- **비교 기간**: 지난 주 월요일 00:00 ~ 일요일 23:59
- **장기 추세** (선택): 최근 4주 평균

---

## 3. 백엔드 집계 로직 설계

### 3.1 집계 DTO: `PatientInsightFeatures`

LLM에 넘기기 전에 백엔드에서 계산할 모든 지표를 담은 DTO

```java
public record PatientInsightFeatures(
    // 기본 정보
    Integer patientId,
    String patientName,
    String ageGroup,  // "70대"
    String gender,    // "F" / "M"

    // 기간
    PeriodInfo period,

    // 활동 통계
    ActivityStats activity,

    // 건강 통계
    HealthStats health,

    // 룰 기반 플래그
    InsightFlags flags
) {}
```

#### 3.1.1 `PeriodInfo`

```java
public record PeriodInfo(
    PeriodType periodType,       // WEEKLY | MONTHLY
    LocalDate currentStart,      // 분석 대상 기간 시작
    LocalDate currentEnd,        // 분석 대상 기간 종료
    LocalDate previousStart,     // 비교 기간 시작
    LocalDate previousEnd        // 비교 기간 종료
) {
    /**
     * 완료된 지난 주와 그 전 주 기간 정보 생성 (권장)
     *
     * 예시: 오늘이 2025-11-14 (목요일)
     * - current: 2025-11-04(월) ~ 2025-11-10(일) (지난 주, 완료됨)
     * - previous: 2025-10-28(월) ~ 2025-11-03(일) (그 전 주)
     */
    public static PeriodInfo lastCompletedWeek() { ... }

    /**
     * 완료된 지난 달과 그 전 달 기간 정보 생성 (권장)
     *
     * 예시: 오늘이 2025-11-14
     * - current: 2025-10-01 ~ 2025-10-31 (지난 달, 완료됨)
     * - previous: 2025-09-01 ~ 2025-09-30 (그 전 달)
     */
    public static PeriodInfo lastCompletedMonth() { ... }
}
```

**중요:** 진행 중인 기간(이번 주/이번 달)이 아닌 **완료된 기간**을 분석하는 것을 권장합니다. 불완전한 데이터는 잘못된 패턴 감지를 유발할 수 있습니다.

#### 3.1.2 `ActivityStats`

```java
public record ActivityStats(
    // 안전구역 이탈
    SafezoneExitStats safezoneExit,

    // 길찾기 이탈
    RouteStats route,

    // 배회/이상탐지
    WanderStats wander,

    // 응급 상황
    EmergencyStats emergency,

    // 자주 가는 목적지
    FavoriteStats favorite
) {}

public record SafezoneExitStats(
    int currentWeek,
    int previousWeek,
    Map<String, Integer> byLevelCurrent,  // {"FIRST": 3, "SECOND": 2}
    List<TimeSlotPattern> timePatterns    // 특정 시간대 집중 여부
) {}

public record RouteStats(
    int currentWeek,
    int previousWeek,
    double changeRate  // (current - previous) / previous
) {}

public record WanderStats(
    int currentWeek,
    int previousWeek,
    double avgDurationMinutes,
    int nightOccurrences  // 22:00~06:00 발생 횟수
) {}

public record EmergencyStats(
    int emerCallCurrent,
    int emerCallPrevious,
    int sosSignCurrent,
    int sosSignPrevious,
    int sosNotResponded  // 응답 안 된 SOS
) {}

public record FavoriteStats(
    List<PlaceFrequency> topCurrentWeek,
    List<PlaceFrequency> topPreviousWeek,
    double diversityIndexCurrent,  // 장소 다양성 지수 (0~1)
    int nightOutingCount  // 야간 외출 횟수
) {}

public record PlaceFrequency(
    String placeName,
    int visitCount
) {}
```

#### 3.1.3 `HealthStats`

```java
public record HealthStats(
    SleepStats sleep,
    StepStats steps,
    HeartRateStats heartRate,
    DataAvailability availability
) {}

public record SleepStats(
    Double avgHoursCurrent,
    Double avgHoursPrevious,
    String trend  // "INCREASE" / "DECREASE" / "STABLE"
) {}

public record StepStats(
    Double avgStepsCurrent,
    Double avgStepsPrevious,
    String trend,
    double changeRate
) {}

public record HeartRateStats(
    Double avgCurrent,
    Double maxCurrent,
    Double variabilityCurrent  // max - min
) {}

public record DataAvailability(
    boolean sleep,
    boolean steps,
    boolean heartRate,
    boolean oxygen
) {}
```

#### 3.1.4 `InsightFlags` (핵심!)

**룰 기반으로 판단하는 위험 신호 플래그 (심각도 점수 기반)**

각 플래그는 단순 true/false뿐만 아니라 0-10점의 심각도 점수를 가집니다.

**심각도 분류:**
- **0-3점**: 경미 (정상 또는 가벼운 이상)
- **4-6점**: 주의 필요
- **7-10점**: 심각 (즉시 대응 필요)

**최종 위험도 평가: 매트릭스 방식**

```
           주의(4+) 개수
           0    1    2    3+
심각  0    L    L    M    M
(7+)  1    L    M    H    H
개수  2+   M    H    H    H
```

```java
public record InsightFlags(
    // 플래그 활성화 여부
    boolean routineChangeDetected,           // 루틴 변화
    boolean spatialConfusionDetected,        // 공간 인지 혼란
    boolean anxietyOrRiskEscalation,         // 불안/위험 증가
    boolean physicalConditionDrop,           // 컨디션 저하
    boolean sleepActivityCorrelation,        // 수면-활동 상관관계
    boolean panicResponsePattern,            // 패닉 반응 패턴

    // 각 플래그의 심각도 점수 (0-10점)
    int routineChangeSeverity,               // 루틴 변화 심각도
    int spatialConfusionSeverity,            // 공간 혼란 심각도
    int anxietyEscalationSeverity,           // 불안 증가 심각도
    int physicalDropSeverity,                // 컨디션 저하 심각도
    int sleepActivitySeverity,               // 수면-활동 상관 심각도
    int panicResponseSeverity                // 패닉 반응 심각도
) {
    /**
     * 매트릭스 방식 위험도 평가
     */
    public String estimateRiskLevel() {
        int severe = severeCount();      // 7+ 점수 항목 개수
        int moderate = moderateCount();  // 4+ 점수 항목 개수

        if (severe == 0) {
            if (moderate <= 1) return "LOW";
            return "MEDIUM";
        }
        if (severe == 1) {
            if (moderate <= 1) return "LOW";
            return moderate == 2 ? "MEDIUM" : "HIGH";
        }
        // severe >= 2
        if (moderate == 0) return "MEDIUM";
        return "HIGH";
    }
}
```

**NULL-SAFE 평가:** 건강 데이터가 없어도 활동 데이터만으로 평가 가능한 플래그는 계속 작동합니다.

### 3.2 플래그 판단 및 심각도 계산 (예시)

각 플래그는 **2단계 평가**를 거칩니다:
1. **활성화 여부** (boolean): 이상 신호가 있는가?
2. **심각도 점수** (0-10점): 얼마나 심각한가?

#### 3.2.1 `spatialConfusionDetected` - 공간 혼란 (활동 데이터만 사용)

**건강 데이터 없어도 완전히 평가 가능**

```java
// 1단계: 플래그 활성화 여부
public boolean evaluateSpatialConfusion(ActivityStats activity) {
    int routeLost = activity.route().current();
    int wanderIncrease = activity.wander().current() - activity.wander().previous();

    // 길 잃음이 2회 이상 또는 배회 증가
    return routeLost >= 2 || wanderIncrease >= 2;
}

// 2단계: 심각도 점수 계산 (0-10점)
private int calculateSpatialConfusionSeverity(ActivityStats activity, boolean detected) {
    if (!detected) return 0;

    int severity = 0;

    // 길 잃음 빈도 (매우 위험)
    int routeLost = activity.route().current();
    if (routeLost >= 4) severity += 5;
    else if (routeLost >= 3) severity += 4;
    else if (routeLost >= 2) severity += 3;
    else severity += 2;

    // 배회 시간
    double wanderDuration = activity.wander().avgDurationMinutes();
    if (wanderDuration >= 20.0) severity += 3;
    else if (wanderDuration >= 10.0) severity += 2;
    else if (wanderDuration > 0) severity += 1;

    // 야간 배회 (매우 위험)
    if (activity.wander().nightOccurrences() > 0) severity += 2;

    return Math.min(severity, 10);  // 최대 10점
}
```

**예시:**
- 길 잃음 3회 + 배회 평균 15분 + 야간 배회 1회 → **9점** (심각)

#### 3.2.2 `physicalConditionDrop` - 신체 상태 저하 (건강 데이터 필수)

**건강 데이터 없으면 0점 반환 (NULL-SAFE)**

```java
// 1단계: 플래그 활성화 여부
public boolean evaluatePhysicalConditionDrop(HealthStats health) {
    if (health == null) return false;  // 건강 데이터 없으면 평가 불가

    // 수면 부족 또는 걸음수 급감
    boolean sleepDrop = health.dataAvailability().sleep()
        && health.sleep().avgHoursCurrent() != null
        && health.sleep().avgHoursCurrent() < 5.5;

    boolean stepDrop = health.dataAvailability().steps()
        && health.steps().changeRate() < -20.0;

    return sleepDrop || stepDrop;
}

// 2단계: 심각도 점수 계산 (0-10점)
private int calculatePhysicalDropSeverity(HealthStats health, boolean detected) {
    if (!detected) return 0;
    if (health == null) return 0;  // NULL-SAFE

    int severity = 0;

    // 수면 부족 (데이터 있을 때만)
    if (health.dataAvailability().sleep() && health.sleep().avgHoursCurrent() != null) {
        double sleepHours = health.sleep().avgHoursCurrent();
        if (sleepHours < 4.5) severity += 4;
        else if (sleepHours < 5.5) severity += 2;
        else if (sleepHours < 6.5) severity += 1;
    }

    // 걸음수 급감 (데이터 있을 때만)
    if (health.dataAvailability().steps()) {
        double stepChangeRate = health.steps().changeRate();
        if (stepChangeRate < -40.0) severity += 3;
        else if (stepChangeRate < -20.0) severity += 2;
    }

    // 산소포화도 저하 (데이터 있을 때만)
    if (health.dataAvailability().oxygen() && health.oxygenSaturation().avgCurrent() != null) {
        double oxygen = health.oxygenSaturation().avgCurrent();
        if (oxygen < 94.0) severity += 2;
        else if (oxygen < 95.0) severity += 1;
    }

    return Math.min(severity, 9);  // 최대 9점
}
```

**예시:**
- 수면 4시간 + 걸음수 -45% + 산소포화도 데이터 없음 → **7점** (심각)
- 건강 데이터 전혀 없음 → **0점** (평가 불가, 활동 데이터만으로 분석)

---

## 4. LLM 프롬프트 설계

### 4.1 System Prompt (역할 정의)

```text
당신은 치매 및 인지저하 환자를 돌보는 보호자를 위한 "행동·활동 패턴 분석 AI 어시스턴트"입니다.

[역할]
- 제공된 수치와 패턴 데이터를 바탕으로 환자의 행동 경향과 위험 신호를 **명확하고 쉽게** 설명합니다.
- 보호자가 실행 가능한 조언을 제공합니다.
- **심각도 점수**(0-10점)를 참고하여 위험 신호의 우선순위를 판단합니다.

[심각도 점수 해석]
- 0-3점: 경미 (정상 또는 가벼운 이상)
- 4-6점: 주의 필요 (모니터링 강화)
- 7-10점: 심각 (즉시 대응 필요)
- 심각(7+) 항목이 2개 이상이면 HIGH 위험으로 평가

[데이터 가용성 활용]
- `data_availability` 필드를 확인하여 어떤 데이터가 있는지 파악하세요.
- 건강 데이터가 없어도 활동 데이터만으로 충분히 분석 가능합니다.
- 데이터가 없는 항목에 대해서는 `data_notes`에 명시하세요.

[중요 제약사항]
1. 절대 질병을 진단하거나 치료를 지시하지 마세요.
2. 약물 복용, 의료 처치 등을 조언하지 마세요.
3. 의학적 판단이 필요한 경우 "전문의 상담 권유"만 언급하세요.
4. 데이터가 부족한 경우 명시적으로 "데이터 부족"이라고 표시하세요.
5. 반드시 JSON 형식으로만 응답하세요.

[응답 톤]
- 공감적이지만 객관적
- 전문적이지만 이해하기 쉬운 언어
- 불안을 조장하지 않되, 위험 신호는 명확히 전달

[출력 형식]
아래 JSON 스키마를 따라 응답하세요:
{
  "summary": "한 줄 요약 (최대 100자)",
  "overall_risk_level": "LOW | MEDIUM | HIGH",
  "positive_signals": ["긍정적 신호 1", "긍정적 신호 2"],
  "warning_signals": ["경고 신호 1", "경고 신호 2"],
  "possible_interpretations": ["해석 1", "해석 2"],
  "caregiver_suggestions": ["실행 가능한 조언 1", "조언 2"],
  "data_notes": ["데이터 한계/주의사항"]
}
```

### 4.2 User Prompt (실제 데이터 전달)

```json
{
  "task": "weekly_patient_insight_report",
  "patient_profile": {
    "age_group": "70대",
    "gender": "F"
  },
  "period": {
    "current_week": {
      "start": "2025-11-03",
      "end": "2025-11-09"
    },
    "previous_week": {
      "start": "2025-10-27",
      "end": "2025-11-02"
    }
  },
  "activity": {
    "safezone_exit": {
      "current": 5,
      "previous": 2,
      "by_level": {"FIRST": 3, "SECOND": 2, "THIRD": 0},
      "time_patterns": [
        {"time_slot": "14:00-16:00", "occurrences": 3, "concentration_rate": 0.6}
      ]
    },
    "route_lost": {
      "current": 3,
      "previous": 0
    },
    "wander": {
      "current": 4,
      "previous": 1,
      "avg_duration_minutes": 7.5,
      "night_occurrences": 2
    },
    "emergency": {
      "emer_call_current": 1,
      "emer_call_previous": 0,
      "sos_sign_current": 2,
      "sos_sign_previous": 1,
      "sos_not_responded": 1
    },
    "favorite": {
      "top_current": [
        {"place_name": "집", "visit_count": 10},
        {"place_name": "동네마트", "visit_count": 4},
        {"place_name": "병원", "visit_count": 2}
      ],
      "top_previous": [
        {"place_name": "집", "visit_count": 12},
        {"place_name": "동네마트", "visit_count": 5},
        {"place_name": "공원", "visit_count": 3}
      ],
      "diversity_index_current": 0.4,
      "night_outing_count": 2
    }
  },
  "health": {
    "sleep": {
      "avg_hours_current": 5.1,
      "avg_hours_previous": 6.8,
      "trend": "DECREASE"
    },
    "steps": {
      "avg_steps_current": 2300,
      "avg_steps_previous": 4800,
      "trend": "DECREASE",
      "change_rate": -0.52
    },
    "heart_rate": {
      "avg_current": 82.0,
      "max_current": 124.0,
      "variability_current": 18.3
    },
    "data_availability": {
      "sleep": true,
      "steps": true,
      "heart_rate": false,
      "oxygen": false
    }
  },
  "flags": {
    // 플래그 활성화 여부
    "routine_change_detected": true,
    "spatial_confusion_detected": true,
    "anxiety_or_risk_escalation": true,
    "physical_condition_drop": true,
    "sleep_activity_correlation": true,
    "panic_response_pattern": false,

    // 각 플래그의 심각도 점수 (0-10점)
    "routine_change_severity": 4,        // 주의
    "spatial_confusion_severity": 9,     // 심각
    "anxiety_escalation_severity": 6,    // 주의
    "physical_drop_severity": 7,         // 심각
    "sleep_activity_severity": 5,        // 주의
    "panic_response_severity": 0         // 없음
  },
  "data_availability": {
    // 활동 데이터
    "has_activity_data": true,
    "has_safezone_exit_data": true,
    "has_route_data": true,
    "has_wander_data": true,
    "has_emergency_data": true,
    "has_favorite_data": true,
    // 건강 데이터
    "has_health_data": true,
    "has_sleep_data": true,
    "has_step_data": true,
    "has_heart_rate_data": false,
    "has_oxygen_data": false
  }
}
```

### 4.3 Expected LLM Response (예시)

```json
{
  "summary": "이번 주에는 수면 시간이 크게 줄고 활동량이 감소하면서, 길찾기 이탈과 안전구역 이탈이 동시에 증가해 전반적인 컨디션 저하와 공간 인지 혼란 신호가 나타났습니다.",
  "overall_risk_level": "MEDIUM",
  "positive_signals": [
    "자주 방문하던 '집'과 '동네마트'는 여전히 꾸준히 방문하고 있습니다.",
    "응급 상황 발생 시 SOS 기능을 적극적으로 활용하고 있습니다."
  ],
  "warning_signals": [
    "안전구역 이탈이 지난주 2회에서 이번 주 5회로 150% 증가했습니다.",
    "길찾기 경로 이탈이 새롭게 3회 발생했습니다.",
    "평균 수면 시간이 지난주 6.8시간에서 5.1시간으로 1.7시간 감소했습니다.",
    "평균 걸음수가 4,800보에서 2,300보로 52% 감소했습니다.",
    "오후 2~4시 사이에 안전구역 이탈이 집중되어 있습니다(전체의 60%)."
  ],
  "possible_interpretations": [
    "수면 부족과 함께 신체 활동량이 줄면서, 공간 인지 능력과 길찾기 능력이 함께 저하되었을 가능성이 있습니다.",
    "낮 시간대 특정 루틴(외출)에서 방향 감각에 어려움을 겪고 있을 수 있습니다.",
    "야간 외출(2회)이 수면 부족과 연관되어 있을 수 있으며, 이로 인한 피로가 낮 시간 활동에 영향을 주고 있을 수 있습니다.",
    "자주 가던 '공원' 방문이 줄고 실내 활동 위주로 변화한 것으로 보입니다."
  ],
  "caregiver_suggestions": [
    "최근 1~2주 정도는 낮 시간대 외출 시 동행을 고려해 주세요. 특히 오후 2~4시 외출 시 주의가 필요합니다.",
    "취침 전 루틴(같은 시간에 잠자리, 조용한 환경)을 유지하고, 야간 외출을 줄이는 것이 도움이 될 수 있습니다.",
    "가벼운 실내 운동이나 스트레칭으로 활동량을 조금씩 늘려보세요.",
    "이러한 변화가 2주 이상 지속되면, 주치의나 전문가와 최근 행동 변화를 공유하는 것이 좋습니다."
  ],
  "data_notes": [
    "심박수 데이터가 충분하지 않아 긴장도나 스트레스 관련 해석은 제한적입니다.",
    "혈중 산소포화도 데이터가 없어 호흡기 건강 측면은 평가하지 못했습니다."
  ]
}
```

---

## 5. GMS API 연동 전략

### 5.1 사용 모델 추천

| 용도 | 모델 | 이유 |
|------|------|------|
| **기본 주간 리포트** | `gpt-4o-mini` | 속도/비용 밸런스 최적, 한국어 품질 우수 |
| **고품질 상세 리포트** | `gpt-4o` 또는 `claude-sonnet-4` | UI에서 "상세 분석" 버튼 클릭 시만 사용 |
| **다국어 지원** | `gemini-2.5-flash` | 영어/중국어 등 다국어 환경 시 |
| **응답 속도 최우선** | `gpt-4.1-nano` | 실시간 대시보드 로딩 속도 중요 시 |

### 5.2 GMS 호출 구조 (OpenAI 예시)

#### Endpoint
```
POST https://gms.ssafy.io/gmsapi/api.openai.com/v1/chat/completions
```

#### Headers
```
Authorization: Bearer {GMS_KEY}
Content-Type: application/json
```

#### Request Body
```json
{
  "model": "gpt-4o-mini",
  "messages": [
    {
      "role": "system",
      "content": "{SYSTEM_PROMPT}"
    },
    {
      "role": "user",
      "content": "{USER_PROMPT_JSON}"
    }
  ],
  "response_format": {
    "type": "json_object"
  },
  "temperature": 0.3,
  "max_tokens": 2000
}
```

### 5.3 환경 변수 관리

```properties
# application.yml
gms:
  api:
    base-url: https://gms.ssafy.io/gmsapi/api.openai.com/v1
    key: ${GMS_KEY}  # 환경 변수에서 주입
    model: gpt-4o-mini
    timeout: 30000
```

**주의**: GMS_KEY는 절대 코드에 하드코딩하지 말고, 환경 변수나 AWS Secrets Manager 사용

---

## 6. 구현 아키텍처

### 6.1 패키지 구조

```
kr.co.ongil.domain.patient.insight/
├── controller/
│   └── PatientInsightController.java
├── service/
│   ├── PatientInsightService.java
│   ├── InsightAggregatorService.java
│   └── InsightFlagEvaluator.java
├── client/
│   └── GmsLLMClient.java
├── repository/
│   └── PatientInsightRepository.java
├── entity/
│   └── PatientInsight.java
└── dto/
    ├── request/
    ├── response/
    │   └── PatientInsightResponse.java
    └── internal/
        ├── PatientInsightFeatures.java
        ├── ActivityStats.java
        ├── HealthStats.java
        ├── InsightFlags.java
        └── LLMInsightResponse.java
```

### 6.2 서비스 플로우

```
1. [Controller] GET /patients/{patientId}/insights/weekly
                ↓
2. [PatientInsightService] generateWeeklyInsight(patientId)
                ↓
3. [InsightAggregatorService]
   - 활동 데이터 집계 (DashboardCalc, Abnormal, Sos, CallLog)
   - 건강 데이터 집계 (HealthDataSummaryResponse)
                ↓
4. [InsightFlagEvaluator]
   - 룰 기반 플래그 계산
                ↓
5. [PatientInsightFeatures] DTO 생성
                ↓
6. [GmsLLMClient] LLM 호출 (JSON → JSON)
                ↓
7. [PatientInsight] Entity 저장 (캐싱)
                ↓
8. [PatientInsightResponse] 반환
```

### 6.3 핵심 서비스 코드 개요

#### `PatientInsightService.java`

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class PatientInsightService {

    private final InsightAggregatorService aggregator;
    private final InsightFlagEvaluator flagEvaluator;
    private final GmsLLMClient gmsClient;
    private final PatientInsightRepository insightRepository;
    private final PatientAccessValidator patientAccessValidator;

    /**
     * 주간 인사이트 생성 (LLM 호출)
     */
    @Transactional
    public PatientInsightResponse generateWeeklyInsight(Integer patientId, Integer callerId) {
        log.info("주간 인사이트 생성 시작: patientId={}", patientId);

        // 1. 권한 검증
        patientAccessValidator.validateAccess(patientId, callerId);

        // 2. 기간 계산
        PeriodInfo period = PeriodInfo.thisWeekAndLastWeek();

        // 3. 데이터 집계
        ActivityStats activity = aggregator.aggregateActivity(patientId, period);
        HealthStats health = aggregator.aggregateHealth(patientId, period);

        // 4. 플래그 평가
        InsightFlags flags = flagEvaluator.evaluate(activity, health);

        // 5. LLM 입력 DTO 생성
        PatientInsightFeatures features = PatientInsightFeatures.builder()
            .patientId(patientId)
            .period(period)
            .activity(activity)
            .health(health)
            .flags(flags)
            .build();

        // 6. LLM 호출
        LLMInsightResponse llmResponse = gmsClient.requestInsight(features);

        // 7. DB 저장
        PatientInsight entity = PatientInsight.create(patientId, period, features, llmResponse);
        insightRepository.save(entity);

        // 8. Response 반환
        return PatientInsightResponse.from(entity);
    }

    /**
     * 최신 주간 인사이트 조회 (캐시된 데이터)
     */
    @Transactional(readOnly = true)
    public PatientInsightResponse getLatestWeeklyInsight(Integer patientId, Integer callerId) {
        patientAccessValidator.validateAccess(patientId, callerId);

        PatientInsight latest = insightRepository
            .findFirstByPatientIdOrderByWeekEndDateDesc(patientId)
            .orElseThrow(() -> new BusinessException(ErrorCode.PATIENT_INSIGHT_NOT_FOUND));

        return PatientInsightResponse.from(latest);
    }
}
```

#### `GmsLLMClient.java`

```java
@Component
@RequiredArgsConstructor
@Slf4j
public class GmsLLMClient {

    private final WebClient webClient;

    @Value("${gms.api.base-url}")
    private String baseUrl;

    @Value("${gms.api.key}")
    private String gmsKey;

    @Value("${gms.api.model}")
    private String model;

    private static final String SYSTEM_PROMPT = """
        당신은 치매 및 인지저하 환자를 돌보는 보호자를 위한 "행동·활동 패턴 분석 AI 어시스턴트"입니다.
        ...
        """;

    public LLMInsightResponse requestInsight(PatientInsightFeatures features) {
        log.info("GMS LLM 요청 시작: patientId={}", features.patientId());

        try {
            String userPromptJson = new ObjectMapper().writeValueAsString(features);

            Map<String, Object> requestBody = Map.of(
                "model", model,
                "messages", List.of(
                    Map.of("role", "system", "content", SYSTEM_PROMPT),
                    Map.of("role", "user", "content", userPromptJson)
                ),
                "response_format", Map.of("type", "json_object"),
                "temperature", 0.3,
                "max_tokens", 2000
            );

            GmsApiResponse response = webClient.post()
                .uri(baseUrl + "/chat/completions")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + gmsKey)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(GmsApiResponse.class)
                .block();

            String llmOutput = response.choices().get(0).message().content();
            LLMInsightResponse llmResponse = new ObjectMapper()
                .readValue(llmOutput, LLMInsightResponse.class);

            log.info("GMS LLM 응답 성공: patientId={}, riskLevel={}",
                features.patientId(), llmResponse.overallRiskLevel());

            return llmResponse;

        } catch (Exception e) {
            log.error("GMS LLM 요청 실패: patientId={}", features.patientId(), e);
            throw new BusinessException(ErrorCode.EXTERNAL_API_ERROR);
        }
    }
}
```

---

## 7. API 명세

### 7.1 주간 인사이트 생성 (LLM 호출)

**Endpoint**
```
POST /api/v1/patients/{patientId}/insights/weekly
```

**Description**
- 최신 주간 데이터를 기반으로 LLM을 호출하여 인사이트 생성
- 생성 후 DB에 저장 (캐싱)
- 일반적으로 스케줄러에서 자동 호출되지만, 수동 재생성도 가능

**Request**
```
POST /api/v1/patients/2/insights/weekly
Authorization: Bearer {jwt-token}
```

**Response** (201 Created)
```json
{
  "message": "환자 인사이트가 성공적으로 생성되었습니다.",
  "data": {
    "insightId": 123,
    "patientId": 2,
    "weekStartDate": "2025-11-03",
    "weekEndDate": "2025-11-09",
    "summary": "이번 주에는 수면 시간이 크게 줄고...",
    "overallRiskLevel": "MEDIUM",
    "positiveSignals": [...],
    "warningSignals": [...],
    "possibleInterpretations": [...],
    "caregiverSuggestions": [...],
    "dataNotes": [...],
    "generatedAt": "2025-11-10T03:30:00"
  }
}
```

### 7.2 최신 주간 인사이트 조회 (캐시)

**Endpoint**
```
GET /api/v1/patients/{patientId}/insights/weekly/latest
```

**Description**
- DB에 저장된 가장 최근 주간 인사이트 조회
- LLM 호출 없이 빠르게 응답
- 데이터가 없으면 404

**Request**
```
GET /api/v1/patients/2/insights/weekly/latest
Authorization: Bearer {jwt-token}
```

**Response** (200 OK)
```json
{
  "message": "환자 인사이트 조회에 성공했습니다.",
  "data": {
    "insightId": 123,
    "patientId": 2,
    "weekStartDate": "2025-11-03",
    "weekEndDate": "2025-11-09",
    "summary": "...",
    "overallRiskLevel": "MEDIUM",
    ...
  }
}
```

### 7.3 인사이트 이력 조회 (최근 N주)

**Endpoint**
```
GET /api/v1/patients/{patientId}/insights/weekly/history?weeks=4
```

**Description**
- 최근 N주의 인사이트 리스트 조회
- 추세 분석 가능

**Query Parameters**
- `weeks` (optional, default=4): 조회할 주 수

**Response** (200 OK)
```json
{
  "message": "환자 인사이트 이력 조회에 성공했습니다.",
  "data": {
    "patientId": 2,
    "totalWeeks": 4,
    "insights": [
      {
        "weekStartDate": "2025-11-03",
        "weekEndDate": "2025-11-09",
        "overallRiskLevel": "MEDIUM",
        "summary": "..."
      },
      {
        "weekStartDate": "2025-10-27",
        "weekEndDate": "2025-11-02",
        "overallRiskLevel": "LOW",
        "summary": "..."
      }
    ]
  }
}
```

---

## 8. 주의사항 및 확장 방안

### 8.1 주의사항

#### 1) 의료 책임 한계
- LLM 출력은 **"경향 해석"**일 뿐, 진단/치료 아님
- System Prompt에 의료 진단 금지 명시
- UI에도 "이 정보는 의학적 진단이 아니며, 참고용입니다" 표시 필수

#### 2) 데이터 품질 의존성
- Samsung Health 연동이 안 되면 건강 데이터 없음
  → `data_availability` 플래그로 명시
- 환자가 워치를 착용하지 않으면 데이터 공백 발생
  → "데이터 부족" 메시지 표시

#### 3) 프롬프트 관리
- System Prompt는 버전 관리 (Git)
- 프롬프트 변경 시 A/B 테스트 권장
- 잘못된 출력 사례 수집 → 프롬프트 개선

#### 4) 비용 관리
- GMS 무료 한도 확인 (SSAFY 교보재 Key)
- 주간 배치로 생성 → 실시간 호출 최소화
- 캐싱 전략: DB에 저장된 최신 인사이트 우선 반환

#### 5) 보안
- GMS_KEY는 서버 환경 변수로만 관리
- 환자 데이터 LLM 전송 시 개인정보 제거 (이름 → "70대 여성" 등)

### 8.2 확장 방안

#### 1) 개인별 위험 프로파일
- 4주 이상 데이터 축적 시:
  - "이 환자는 수면 부족 시 길찾기 이탈 증가 패턴"
  - "야간 외출 시 SOS 발생률 높음"
- 이를 별도 Entity로 저장 → 다음 주 분석에 활용

#### 2) 실시간 위험 알림
- 플래그가 특정 조합일 때 (예: HIGH 리스크):
  - FCM 푸시: "이번 주 환자 상태에 주의가 필요합니다"
  - SSE로 대시보드에 실시간 알림

#### 3) 보호자 맞춤형 조언
- 보호자 프로필 (경험 수준, 거주 환경)에 따라 조언 수정
  - "초보 보호자": 더 구체적인 가이드
  - "경험 많은 보호자": 간결한 요약

#### 4) 다국어 지원
- Gemini 모델로 영어/중국어 등 자동 번역
- System Prompt도 언어별 버전 관리

#### 5) 음성 리포트
- `gpt-4o-mini-tts`로 요약을 음성으로 변환
- 보호자가 앱에서 "음성 듣기" 버튼 클릭 시 재생

#### 6) 패턴 클러스터링
- Embedding 모델(`text-embedding-3-small`)로:
  - 유사한 패턴의 환자 그룹 찾기
  - "비슷한 패턴의 다른 환자들은 이렇게 대응했습니다" 제안

---

## 9. 스케줄링 통합

### 9.1 기존 DashboardService와 통합

```java
@Scheduled(cron = "0 30 3 * * MON")  // 월요일 03:30
public void weeklyBatchJob() {
    log.info("주간 배치 작업 시작");

    // 1. 기존: 대시보드 집계 저장
    saveDashboards();

    // 2. 신규: 환자 인사이트 생성
    generateAllPatientInsights();

    log.info("주간 배치 작업 완료");
}

private void generateAllPatientInsights() {
    List<Integer> activePatientIds = userRepository.findActivePatientIds();

    for (Integer patientId : activePatientIds) {
        try {
            // 관리자 권한으로 생성 (callerId = systemUserId)
            patientInsightService.generateWeeklyInsight(patientId, SYSTEM_USER_ID);
        } catch (Exception e) {
            log.error("환자 인사이트 생성 실패: patientId={}", patientId, e);
        }
    }
}
```

---

## 10. 예상 결과 및 효과

### 10.1 보호자 입장

**Before (숫자만)**
- "안전구역 이탈 5회, 길찾기 이탈 3회... 이게 많은 건가?"

**After (AI 해석)**
> 이번 주에는 수면 시간이 크게 줄고 활동량이 감소하면서, 길찾기 이탈과 안전구역 이탈이 동시에 증가해 전반적인 컨디션 저하와 공간 인지 혼란 신호가 나타났습니다.
>
> **보호자님께 추천드리는 조치:**
> - 낮 시간대 외출 시 동행을 고려해 주세요.
> - 취침 전 루틴을 유지하고, 야간 외출을 줄이는 것이 도움이 될 수 있습니다.

### 10.2 서비스 차별화

- **정량 데이터 → 정성 인사이트**: 경쟁 서비스 대비 차별화
- **예방적 케어**: 조기 위험 신호 감지로 사고 예방
- **보호자 부담 완화**: "이 상황에서 뭘 해야 하지?" 고민 감소

---

## 11. 개발 우선순위

### Phase 1 (MVP)
1. ✅ 활동 데이터 집계 (`InsightAggregatorService`)
2. ✅ 플래그 평가 로직 (`InsightFlagEvaluator`)
3. ✅ GMS 클라이언트 구현 (`GmsLLMClient`)
4. ✅ 기본 API 엔드포인트 (생성/조회)
5. ✅ 주간 스케줄러 통합

### Phase 2 (고도화)
1. 건강 데이터 연동 (Samsung Health)
2. 건강-활동 상관관계 플래그 추가
3. UI/UX 개선 (카드형 대시보드)
4. 이력 조회 및 추세 그래프

### Phase 3 (확장)
1. 개인별 위험 프로파일 학습
2. 실시간 위험 알림 (FCM/SSE)
3. 다국어 지원
4. 음성 리포트

---

## 12. 참고 자료

- [SSAFY GMS 문서](https://gms.ssafy.io)
- [OpenAI API Reference](https://platform.openai.com/docs/api-reference)
- [치매 환자 행동 패턴 연구 논문] (추후 추가)
- [온길 기존 대시보드 설계 문서] (internal)

---

**문서 버전**: v1.0
**최종 수정일**: 2025-11-13
**작성자**: Ongil Backend Team
