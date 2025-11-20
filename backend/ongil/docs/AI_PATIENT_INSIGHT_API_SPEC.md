# 환자 인사이트 AI 분석 API

## 📋 개요

**환자 인사이트(Patient Insight)** 는 치매 환자의 일상 활동 및 건강 데이터를 AI(LLM)로 분석하여 보호자에게 종합적인 리포트를 제공하는 기능입니다.

### 주요 기능
- ✅ **주간/월간 종합 분석**: 환자의 활동 패턴, 건강 상태, 이상행동 등을 기간별로 분석
- ✅ **위험도 평가**: LOW/MEDIUM/HIGH 3단계로 전반적인 위험 수준 평가
- ✅ **긍정 신호 & 경고 신호 구분**: 개선된 점과 주의해야 할 점을 명확히 구분
- ✅ **보호자 맞춤 제안**: 실질적으로 실천 가능한 돌봄 가이드 제공
- ✅ **이력 관리**: 과거 인사이트 조회로 변화 추이 파악 가능

---

## 🏗️ 아키텍처

```
┌─────────────────┐
│  Frontend       │
│  (React/Vue)    │
└────────┬────────┘
         │ HTTP Request
         ▼
┌─────────────────────────────────────────────┐
│  PatientInsightController                   │
│  - POST   /patients/{id}/insights/weekly    │
│  - POST   /patients/{id}/insights/monthly   │
│  - GET    /patients/{id}/insights/weekly    │
│  - GET    /patients/{id}/insights/monthly   │
└────────┬────────────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────────────┐
│  PatientInsightService                      │
│  1. 데이터 검증                              │
│  2. 데이터 집계 (InsightAggregatorService)  │
│  3. 플래그 평가 (InsightFlagEvaluator)      │
│  4. LLM 인사이트 생성 (GmsLLMClient)        │
│  5. DB 저장 (PatientInsightRepository)      │
└────────┬────────────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────────────┐
│  GmsLLMClient (SSAFY GMS API)               │
│  - OpenAI (gpt-4.1-mini) 기본 사용          │
│  - Gemini, Claude, Runway 지원              │
│  - System Prompt + User Prompt 구성         │
│  - JSON 형식 응답 파싱                      │
└────────┬────────────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────────────┐
│  PostgreSQL Database                        │
│  - patient_insights 테이블                  │
│  - JSON 타입 컬럼 (positive_signals 등)     │
└─────────────────────────────────────────────┘
```

---

## 📊 데이터 모델

### PatientInsight Entity

| 컬럼명 | 타입 | 설명 | 예시 |
|--------|------|------|------|
| `id` | Integer | 인사이트 ID (PK) | 1 |
| `patient_id` | Integer | 환자 ID | 100 |
| `period_type` | String (Enum) | 기간 타입 | `WEEKLY`, `MONTHLY` |
| `period_start_date` | Date | 분석 기간 시작일 | `2025-11-10` |
| `period_end_date` | Date | 분석 기간 종료일 | `2025-11-16` |
| `overall_risk_level` | String | 전반적 위험 수준 | `LOW`, `MEDIUM`, `HIGH` |
| `summary` | String(500) | 전체 요약 (2-3문장) | "이번 주 환자분은..." |
| `positive_signals` | JSONB | 긍정적 신호 배열 | `["수면 시간 개선", "규칙적인 산책"]` |
| `warning_signals` | JSONB | 경고 신호 배열 | `["야간 배회 증가", "SOS 호출 증가"]` |
| `possible_interpretations` | JSONB | 가능한 해석 배열 | `["공간 혼란 증가 가능성", ...]` |
| `caregiver_suggestions` | JSONB | 보호자 제안 배열 | `["야간 조명 설치", "정기 산책 권장"]` |
| `data_notes` | JSONB | 데이터 제약사항 배열 | `["수면 데이터 부족", ...]` |
| `input_features` | JSONB | LLM 입력 데이터 (디버깅용) | `{...}` |
| `llm_raw_response` | JSONB | LLM 원본 응답 (디버깅용) | `{...}` |
| `created_at` | Timestamp | 생성 일시 | `2025-11-14 01:23:30` |
| `updated_at` | Timestamp | 수정 일시 | `2025-11-14 01:23:30` |

---

## 🔌 API 명세

### Base URL
```
http://localhost:8080/api/v1
```

---

### 1. 주간 인사이트 생성

**환자의 주간 활동 및 건강 데이터를 분석하여 AI 인사이트를 생성합니다.**

#### Request
```http
POST /patients/{patientId}/insights/weekly
```

#### Path Parameters
| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `patientId` | Integer | ✅ | 환자 ID |

#### Response (성공 시)
```json
{
  "message": "주간 인사이트가 성공적으로 생성되었습니다.",
  "data": {
    "id": 1,
    "patient_id": 100,
    "period_type": "WEEKLY",
    "period_start_date": "2025-11-10",
    "period_end_date": "2025-11-16",
    "overall_risk_level": "LOW",
    "summary": "이번 주 환자분은 안정적인 생활 패턴을 유지하셨습니다. 수면 시간이 양호하며, 규칙적인 외출을 하고 계십니다.",
    "positive_signals": [
      "수면 시간이 평균 7.2시간으로 양호합니다.",
      "규칙적인 산책으로 활동량이 증가했습니다.",
      "안전구역 이탈이 최소화되었습니다."
    ],
    "warning_signals": [],
    "possible_interpretations": [
      "전반적으로 안정적인 일상 패턴을 유지하고 있습니다.",
      "현재 상태가 잘 유지되고 있으니 현 루틴을 지속하는 것이 좋습니다."
    ],
    "caregiver_suggestions": [
      "현재의 좋은 수면 패턴을 계속 유지하세요.",
      "규칙적인 산책을 지속하시면 좋습니다.",
      "가족과의 정기적인 통화를 이어가세요."
    ],
    "data_notes": [],
    "data_availability": {
      "has_activity_data": true,
      "has_safezone_exit_data": true,
      "has_route_data": true,
      "has_wander_data": true,
      "has_emergency_data": true,
      "has_favorite_data": true,
      "has_health_data": true,
      "has_sleep_data": true,
      "has_step_data": true,
      "has_heart_rate_data": false,
      "has_oxygen_data": false
    }
  }
}
```

#### Response (실패 시 - 데이터 부족)
```json
{
  "message": "분석할 데이터가 부족합니다."
}
```

---

### 2. 월간 인사이트 생성

**환자의 월간 활동 및 건강 데이터를 분석하여 AI 인사이트를 생성합니다.**

#### Request
```http
POST /patients/{patientId}/insights/monthly
```

#### Path Parameters
| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `patientId` | Integer | ✅ | 환자 ID |

#### Response
주간 인사이트와 동일하며, `period_type`이 `MONTHLY`이고 기간이 한 달입니다.

---

### 3. 최신 주간 인사이트 조회

**환자의 가장 최근 주간 인사이트를 조회합니다.**

#### Request
```http
GET /patients/{patientId}/insights/weekly/latest
```

#### Path Parameters
| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `patientId` | Integer | ✅ | 환자 ID |

#### Response
```json
{
  "message": "환자 인사이트 조회가 완료되었습니다.",
  "data": {
    "id": 5,
    "patient_id": 102,
    "period_type": "WEEKLY",
    "period_start_date": "2025-11-10",
    "period_end_date": "2025-11-16",
    "overall_risk_level": "HIGH",
    "summary": "최근 한 주 동안 환자분은 외출 빈도와 보행량이 줄고, 야간 배회가 증가하여 불안 징후가 나타났습니다.",
    "positive_signals": [],
    "warning_signals": [
      "야간 시간대 안전구역 이탈이 8회 발생했습니다.",
      "수면 시간이 평균 3.8시간으로 크게 감소했습니다.",
      "걸음 수가 2000보로 급감했습니다."
    ],
    "possible_interpretations": [
      "공간 혼란과 불안이 증가하고 있을 가능성이 있습니다.",
      "수면 부족으로 인한 신체 상태 저하가 우려됩니다."
    ],
    "caregiver_suggestions": [
      "야간 조명을 설치하여 안전성을 높이세요.",
      "의료진과 상담하여 수면 문제를 개선하세요.",
      "낮 시간대 활동량을 늘릴 수 있도록 도와주세요."
    ],
    "data_notes": [
      "심박수 및 산소포화도 데이터가 없어 신체 건강 상태 평가가 제한적입니다."
    ],
    "data_availability": {
      "has_activity_data": true,
      "has_safezone_exit_data": true,
      "has_route_data": true,
      "has_wander_data": true,
      "has_emergency_data": true,
      "has_favorite_data": true,
      "has_health_data": true,
      "has_sleep_data": true,
      "has_step_data": true,
      "has_heart_rate_data": false,
      "has_oxygen_data": false
    }
  }
}
```

#### Response (실패 시 - 인사이트 없음)
```json
{
  "message": "환자 인사이트를 찾을 수 없습니다."
}
```

---

### 4. 최신 월간 인사이트 조회

**환자의 가장 최근 월간 인사이트를 조회합니다.**

#### Request
```http
GET /patients/{patientId}/insights/monthly/latest
```

#### Response
주간 인사이트 조회와 동일하며, `period_type`이 `MONTHLY`입니다.

---

### 5. 주간 인사이트 이력 조회

**환자의 주간 인사이트 이력을 최근순으로 조회합니다.**

#### Request
```http
GET /patients/{patientId}/insights/weekly?limit=10
```

#### Query Parameters
| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
|----------|------|------|--------|------|
| `limit` | Integer | ❌ | 10 | 조회할 개수 (최대값 제한 없음) |

#### Response
```json
{
  "message": "환자 인사이트 목록 조회가 완료되었습니다.",
  "data": [
    {
      "id": 5,
      "patient_id": 100,
      "period_type": "WEEKLY",
      "period_start_date": "2025-11-10",
      "period_end_date": "2025-11-16",
      "overall_risk_level": "LOW",
      "summary": "...",
      "positive_signals": [...],
      "warning_signals": [...],
      "possible_interpretations": [...],
      "caregiver_suggestions": [...],
      "data_notes": [...]
    },
    {
      "id": 4,
      "patient_id": 100,
      "period_type": "WEEKLY",
      "period_start_date": "2025-11-03",
      "period_end_date": "2025-11-09",
      "overall_risk_level": "MEDIUM",
      "summary": "...",
      ...
    }
  ]
}
```

---

### 6. 월간 인사이트 이력 조회

**환자의 월간 인사이트 이력을 최근순으로 조회합니다.**

#### Request
```http
GET /patients/{patientId}/insights/monthly?limit=10
```

#### Response
주간 인사이트 이력 조회와 동일하며, `period_type`이 `MONTHLY`입니다.

---

## 📖 응답 필드 설명

### `overall_risk_level` (전반적 위험 수준)
환자의 전반적인 상태를 **심각도 점수 기반 매트릭스**로 평가합니다.

#### 평가 기준
```
           주의(4+) 개수
           0    1    2    3+
심각  0    L    L    M    M
(7+)  1    L    M    H    H
개수  2+   M    H    H    H
```

| 값 | 의미 | 평가 기준 | UI 표시 권장 색상 |
|----|------|----------|-------------------|
| `LOW` | 안정적인 상태 | 심각(7+) 0개 + 주의(4+) 0-1개 | 🟢 녹색 |
| `MEDIUM` | 주의 필요 | 심각(7+) 0개 + 주의(4+) 2개 이상<br>또는 심각(7+) 1개 + 주의(4+) 2개 | 🟡 노란색 |
| `HIGH` | 긴급 대응 필요 | 심각(7+) 2개 이상<br>또는 심각(7+) 1개 + 주의(4+) 3개 이상 | 🔴 빨간색 |

**프론트엔드 사용 예시:**
```javascript
const riskLevelColor = {
  LOW: '#4CAF50',      // 녹색
  MEDIUM: '#FFC107',   // 노란색
  HIGH: '#F44336'      // 빨간색
};

const color = riskLevelColor[data.overall_risk_level];
```

---

### `summary` (전체 요약)
**2-3문장**으로 이번 기간의 환자 상태를 종합적으로 설명합니다.

**특징:**
- 보호자가 가장 먼저 읽는 핵심 메시지
- 전문 용어 대신 일상적인 언어 사용
- 이전 기간 대비 변화를 포함

**프론트엔드 표시 권장:**
- 카드 상단에 큰 글씨로 강조
- 모바일: 2줄 이상이면 "더보기" 버튼

---

### `positive_signals` (긍정적 신호)
**개선되었거나 양호한 점들**을 배열로 제공합니다.

**예시:**
```json
[
  "수면 시간이 평균 7.2시간으로 양호합니다.",
  "규칙적인 산책으로 활동량이 증가했습니다.",
  "안전구역 이탈이 지난주 대비 50% 감소했습니다."
]
```

**프론트엔드 표시 권장:**
- ✅ 체크 아이콘과 함께 표시
- 🟢 녹색 배경 또는 테두리
- 리스트 형태로 표시

---

### `warning_signals` (경고 신호)
**주의해야 할 점들**을 배열로 제공합니다.

**예시:**
```json
[
  "야간 시간대 안전구역 이탈이 8회 발생했습니다.",
  "수면 시간이 평균 3.8시간으로 크게 감소했습니다.",
  "SOS 요청이 3회 있었으며, 2회는 응답하지 않았습니다."
]
```

**프론트엔드 표시 권장:**
- ⚠️ 경고 아이콘과 함께 표시
- 🟡 노란색 또는 🔴 빨간색 배경
- 중요도 순으로 정렬

---

### `possible_interpretations` (가능한 해석)
**데이터에서 관찰된 패턴의 의미**를 설명합니다.

**예시:**
```json
[
  "공간 혼란과 불안이 증가하고 있을 가능성이 있습니다.",
  "수면 부족으로 인한 신체 상태 저하가 우려됩니다.",
  "야간 배회는 치매 진행과 관련될 수 있습니다."
]
```

**특징:**
- 의학적 진단이 아닌 **관찰된 패턴**만 설명
- "~가능성", "~우려", "~관련될 수 있음" 등의 표현 사용

**프론트엔드 표시 권장:**
- 💡 전구 아이콘과 함께 표시
- 회색 배경 또는 테두리
- "AI 분석 결과" 섹션에 표시

---

### `caregiver_suggestions` (보호자 제안)
**보호자가 실질적으로 실천할 수 있는 조언**을 제공합니다.

**예시:**
```json
[
  "야간 조명을 설치하여 안전성을 높이세요.",
  "의료진과 상담하여 수면 문제를 개선하세요.",
  "낮 시간대 활동량을 늘릴 수 있도록 도와주세요.",
  "정기적인 통화로 환자분의 상태를 확인하세요."
]
```

**특징:**
- 구체적이고 실천 가능한 행동 중심
- "~하세요", "~해보세요" 등의 권장 표현

**프론트엔드 표시 권장:**
- ✅ 체크박스 형태로 표시 (실천 여부 체크)
- 🔵 파란색 배경
- "보호자 가이드" 섹션에 표시

---

### `data_notes` (데이터 제약사항)
**분석에 사용된 데이터가 부족하거나 제약이 있는 경우** 명시합니다.

**예시:**
```json
[
  "수면 데이터가 3일치만 수집되어 정확도가 낮을 수 있습니다.",
  "GPS 위치 데이터가 불안정하여 이동 패턴 분석이 제한적입니다.",
  "건강 데이터가 기록되지 않아 신체 상태 분석을 수행하지 못했습니다."
]
```

**특징:**
- 분석 결과의 **신뢰도**를 보호자에게 투명하게 공개
- 데이터 수집 개선 유도

**프론트엔드 표시 권장:**
- ℹ️ 정보 아이콘과 함께 표시
- 회색 또는 연한 노란색 배경
- "참고 사항" 또는 "데이터 품질" 섹션에 표시

---

### `data_availability` (데이터 가용성 정보)
**어떤 데이터가 수집되었는지 구조화된 정보**를 제공합니다.

**예시:**
```json
{
  "has_activity_data": true,           // 활동 데이터 전체
  "has_safezone_exit_data": true,      // 안전구역 이탈
  "has_route_data": true,              // 길찾기 이탈
  "has_wander_data": true,             // 배회
  "has_emergency_data": true,          // 긴급 상황 (SOS/응급전화)
  "has_favorite_data": true,           // 자주 가는 장소
  "has_health_data": true,             // 건강 데이터 전체
  "has_sleep_data": true,              // 수면
  "has_step_data": true,               // 걸음수
  "has_heart_rate_data": false,        // 심박수
  "has_oxygen_data": false             // 산소포화도
}
```

**활용:**
- **프론트엔드**: 데이터 연동 상태 UI 표시 (예: "심박수 데이터 미연동")
- **AI 분석**: 데이터 부족 시 보수적 평가 수행
- **보호자 안내**: 어떤 데이터 수집이 필요한지 명확히 표시

**프론트엔드 표시 권장:**
- 데이터 연동 현황 섹션에 표시
- ✅ 연동됨 (녹색) / ❌ 미연동 (회색)
- 미연동 데이터에 대해 "연동 방법" 버튼 제공

---

## 🎨 프론트엔드 UI 권장 구조

### 1. 인사이트 카드 (요약 뷰)
```
┌─────────────────────────────────────────┐
│ 📊 주간 인사이트                        │
│ 2025-11-10 ~ 2025-11-16                │
│                                         │
│ 🔴 HIGH - 긴급 대응 필요                │
│                                         │
│ 최근 한 주 동안 환자분은 외출 빈도와... │
│                                         │
│ [자세히 보기]                           │
└─────────────────────────────────────────┘
```

### 2. 인사이트 상세 페이지
```
┌─────────────────────────────────────────┐
│ 전체 요약                                │
│ ─────────────────────────────────────   │
│ 최근 한 주 동안 환자분은 외출 빈도와    │
│ 보행량이 줄고, 야간 배회가 증가하여...  │
└─────────────────────────────────────────┘

┌─────────────────────────────────────────┐
│ ✅ 긍정적 신호                          │
│ ─────────────────────────────────────   │
│ (비어있음)                              │
└─────────────────────────────────────────┘

┌─────────────────────────────────────────┐
│ ⚠️  경고 신호                           │
│ ─────────────────────────────────────   │
│ • 야간 시간대 안전구역 이탈이 8회       │
│ • 수면 시간이 평균 3.8시간으로 감소     │
│ • 걸음 수가 2000보로 급감               │
└─────────────────────────────────────────┘

┌─────────────────────────────────────────┐
│ 💡 AI 분석 결과                         │
│ ─────────────────────────────────────   │
│ • 공간 혼란과 불안이 증가 가능성        │
│ • 수면 부족으로 인한 신체 상태 저하     │
└─────────────────────────────────────────┘

┌─────────────────────────────────────────┐
│ 📋 보호자 가이드                        │
│ ─────────────────────────────────────   │
│ ☐ 야간 조명을 설치하여 안전성 높이기   │
│ ☐ 의료진과 상담하여 수면 문제 개선     │
│ ☐ 낮 시간대 활동량 늘리기               │
└─────────────────────────────────────────┘

┌─────────────────────────────────────────┐
│ ℹ️  참고 사항                           │
│ ─────────────────────────────────────   │
│ (비어있음)                              │
└─────────────────────────────────────────┘
```

---

## 🔍 분석 로직 상세

### 1. 데이터 수집 범위

#### 주간 인사이트 (WEEKLY)
- **현재 기간**: 최근 7일 (월요일 ~ 일요일)
- **비교 기간**: 그 이전 7일
- **데이터 소스**:
  - `dashboard_calc`: 이상행동 빈도, 안전구역 이탈, SOS 호출
  - `abnormal_logs`: 안전구역 이탈 상세 (시간대, 레벨)
  - `sos_logs`: 긴급 호출 상세 (응답 여부)
  - `health_data`: 수면, 걸음 수, 심박수, 산소포화도

#### 월간 인사이트 (MONTHLY)
- **현재 기간**: 최근 30일
- **비교 기간**: 그 이전 30일
- **데이터 소스**: 주간과 동일

---

### 2. 플래그 평가 로직 (심각도 점수 기반)

각 플래그는 **2단계 평가**를 거칩니다:
1. **활성화 여부** (boolean): 이상 신호가 있는가?
2. **심각도 점수** (0-10점): 얼마나 심각한가?

#### 심각도 분류
- **0-3점**: 경미 (정상 또는 가벼운 이상)
- **4-6점**: 주의 필요 (모니터링 강화)
- **7-10점**: 심각 (즉시 대응 필요)

#### 위험도 평가: 매트릭스 방식

```
           주의(4+) 개수
           0    1    2    3+
심각  0    L    L    M    M
(7+)  1    L    M    H    H
개수  2+   M    H    H    H
```

- **심각(7+)** 항목이 2개 이상이면 → **HIGH**
- **심각(7+)** 1개 + **주의(4+)** 3개 이상 → **HIGH**
- 그 외 조합에 따라 **MEDIUM** 또는 **LOW**

#### 6가지 플래그

| 플래그 | 활성화 조건 | 심각도 평가 | NULL-SAFE |
|--------|------------|------------|-----------|
| **일상 패턴 변화** | 안전구역 이탈/배회 증가 | 증가 폭에 따라 0-6점 | ✅ (활동 데이터만) |
| **공간 혼란** | 길 잃음 2회+ 또는 배회 증가 | 야간 배회 시 최대 10점 | ✅ (활동 데이터만) |
| **불안/위험 증가** | SOS/응급전화 증가 | 미응답 SOS 시 최대 9점 | ✅ (활동 데이터만) |
| **신체 상태 저하** | 수면 부족 또는 걸음수 감소 | 데이터 없으면 0점 | ❌ (건강 데이터 필요) |
| **수면-활동 상관** | 수면 감소 + 활동 패턴 변화 | 수면 부족 정도에 따라 0-5점 | ❌ (수면 데이터 필요) |
| **패닉 반응** | 미응답 SOS 또는 긴급 상황 빈발 | 미응답 3회+ 시 최대 10점 | ✅ (활동 데이터만) |

**예시:**
```json
{
  "flags": {
    "spatial_confusion_detected": true,
    "spatial_confusion_severity": 9,      // 심각 (7+)
    "physical_condition_drop": true,
    "physical_drop_severity": 7,          // 심각 (7+)
    "anxiety_or_risk_escalation": true,
    "anxiety_escalation_severity": 6,     // 주의 (4-6)
    ...
  }
}
```
→ 심각 2개 + 주의 1개 = **HIGH 위험도**

---

### 3. AI 프롬프트 전략

#### System Prompt
```
당신은 치매 환자의 일상 활동과 건강 데이터를 분석하는 의료 AI 어시스턴트입니다.

**역할:**
- 보호자가 이해하기 쉽도록 환자의 상태를 요약하고 해석합니다.
- **심각도 점수**(0-10점)를 참고하여 위험 신호의 우선순위를 판단합니다.
- 긍정적 신호와 경고 신호를 명확히 구분합니다.
- 전문 용어 대신 일상적인 언어를 사용합니다.

**심각도 점수 해석:**
- 0-3점: 경미 (정상 또는 가벼운 이상)
- 4-6점: 주의 필요 (모니터링 강화)
- 7-10점: 심각 (즉시 대응 필요)
- **심각(7+) 항목이 2개 이상이면 HIGH 위험으로 평가**

**데이터 가용성 활용:**
- `data_availability` 필드를 확인하여 어떤 데이터가 있는지 파악하세요.
- 건강 데이터가 없어도 활동 데이터만으로 충분히 분석 가능합니다.
- 데이터가 없는 항목에 대해서는 `dataNotes`에 명시하세요.

**출력 형식 (JSON):**
{
  "summary": "전체 요약 (2-3문장)",
  "overallRiskLevel": "LOW | MEDIUM | HIGH",
  "positiveSignals": ["긍정적 신호 1", ...],
  "warningSignals": ["경고 신호 1", ...],
  "possibleInterpretations": ["해석 1", ...],
  "caregiverSuggestions": ["제안 1", ...],
  "dataNotes": ["데이터 제약사항 1", ...]
}

**주의사항:**
- 데이터가 부족한 경우, dataNotes에 명시하고 가능한 범위에서 분석합니다.
- 의학적 진단을 내리지 않으며, 관찰된 패턴만 설명합니다.
- 보호자에게 실질적인 도움이 되는 구체적인 제안을 합니다.
```

#### User Prompt
```
다음은 환자의 주간 활동 및 건강 데이터입니다.

**분석 기간:**
- 현재 기간: 2025-11-10 ~ 2025-11-16
- 비교 기간: 2025-11-03 ~ 2025-11-09

**데이터:**
{
  "patientProfile": {...},
  "activity": {...},
  "health": {...},
  "flags": {...}
}

**플래그 및 심각도:**
- 공간 혼란: true (심각도 9/10) ⚠️ 심각
- 신체 상태 저하: true (심각도 7/10) ⚠️ 심각
- 불안/위험 증가: true (심각도 6/10) ⚠️ 주의
- 일상 패턴 변화: true (심각도 4/10)
→ **예상 위험도: HIGH** (심각 2개 + 주의 1개)

**데이터 가용성:**
- 활동 데이터: ✅ 전체 수집됨
- 건강 데이터: ⚠️ 부분 수집 (수면/걸음수만, 심박/산소포화도 없음)

**요청사항:**
1. 이전 기간 대비 현재 기간의 변화를 분석해주세요.
2. **심각도가 높은 플래그를 우선**으로 경고 신호에 포함하세요.
3. 보호자가 주의해야 할 점과 실천 가능한 제안을 해주세요.
4. 데이터가 부족한 부분이 있다면 dataNotes에 명시해주세요.
```

---

## ⚙️ 설정 가이드

### application.yml
```yaml
gms:
  api:
    key: ${GMS_API_KEY}        # 환경변수로 설정
    provider: openai           # openai | gemini | claude
    model: gpt-4.1-mini        # 비용 효율적인 모델
    timeout: 90                # 타임아웃 (초)
```

### 환경변수 설정
```bash
# Windows (PowerShell)
$env:GMS_API_KEY="your_gms_api_key_here"

# Linux/Mac
export GMS_API_KEY="your_gms_api_key_here"
```

---

## 🧪 테스트 가이드

### 1. 테스트 데이터 생성
```bash
# PostgreSQL에 테스트 데이터 삽입
psql -h localhost -U postgres -d ongil_db -f src/main/resources/db/patient-insight-test-data.sql
```

**생성되는 환자:**
- **환자 ID 100**: 정상 (LOW 위험도) - 안정적인 패턴
- **환자 ID 101**: 주의 (MEDIUM 위험도) - 일부 경고 신호
- **환자 ID 102**: 위험 (HIGH 위험도) - 다수의 경고 신호

### 2. API 호출 테스트
```bash
# 주간 인사이트 생성
curl -X POST http://localhost:8080/api/v1/patients/100/insights/weekly

# 월간 인사이트 생성
curl -X POST http://localhost:8080/api/v1/patients/100/insights/monthly

# 최신 인사이트 조회
curl http://localhost:8080/api/v1/patients/100/insights/weekly/latest

# 인사이트 이력 조회
curl http://localhost:8080/api/v1/patients/100/insights/weekly?limit=5
```

---

## 🚨 트러블슈팅

### 1. `null` 값이 많이 나오는 경우

#### 원인
- AI가 JSON 형식을 제대로 반환하지 않음
- 필드명 매칭 오류 (camelCase vs snake_case)
- AI 응답이 시스템 프롬프트 형식과 다름

#### 해결 방법
1. **DB에서 `llm_raw_response` 확인**:
   ```sql
   SELECT llm_raw_response FROM patient_insights WHERE id = 5;
   ```

2. **AI 응답 형식 확인**:
   - `overallRiskLevel` 대신 `overall_risk_level`로 응답했는지 확인
   - JSON 형식이 아닌 일반 텍스트로 응답했는지 확인

3. **LLMInsightResponse DTO 수정**:
   - `@JsonProperty` 어노테이션으로 필드명 매핑 확인

---

### 2. `PATIENT_NOT_FOUND` 에러

#### 원인
- 환자 ID가 존재하지 않음
- 환자가 삭제됨

#### 해결 방법
```sql
SELECT * FROM users WHERE id = 100 AND deleted_at IS NULL;
```

---

### 3. `INSUFFICIENT_DATA` 에러

#### 원인
- 분석 기간 동안 데이터가 충분하지 않음
- `dashboard_calc` 테이블에 데이터 없음

#### 해결 방법
```sql
-- dashboard_calc 데이터 확인
SELECT * FROM dashboard_calc WHERE patient_id = 100;

-- health_data 데이터 확인
SELECT * FROM health_data WHERE patient_id = 100;
```

---

## 📚 추가 리소스

### 관련 문서
- [GMS API 문서](https://gms.ssafy.io/docs)
- [OpenAI Chat Completions API](https://platform.openai.com/docs/guides/chat)
- [Swagger UI](http://localhost:8080/swagger-ui.html)

### 코드 구조
```
kr.co.ongil.domain.patient.insight/
├── controller/
│   └── PatientInsightController.java       # REST API 엔드포인트
├── service/
│   ├── PatientInsightService.java          # 메인 비즈니스 로직
│   ├── InsightAggregatorService.java       # 데이터 집계
│   ├── InsightFlagEvaluator.java           # 플래그 평가
│   └── GmsLLMClient.java                   # LLM API 호출
├── repository/
│   └── PatientInsightRepository.java       # DB 접근
├── entity/
│   ├── PatientInsight.java                 # 엔티티
│   └── PeriodType.java                     # Enum (WEEKLY, MONTHLY)
├── dto/
│   ├── response/
│   │   └── PatientInsightResponse.java     # API 응답 DTO
│   └── internal/
│       ├── LLMInsightResponse.java         # LLM 응답 DTO
│       ├── PatientInsightFeatures.java     # LLM 입력 DTO
│       ├── ActivityStats.java              # 활동 통계
│       ├── HealthStats.java                # 건강 통계
│       └── InsightFlags.java               # 플래그
└── README.md                               # 이 문서
```

---

## 📞 문의

백엔드 개발팀 - Ongil 프로젝트
