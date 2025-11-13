# 안드로이드 건강 데이터 연동 가이드

이 문서는 Samsung Health SDK에서 수집한 건강 데이터를 Ongil 백엔드 API로 전송하는 방법을 설명합니다.

---

## 📋 목차

1. [개요](#개요)
2. [백엔드 API 명세](#백엔드-api-명세)
3. [안드로이드 구현](#안드로이드-구현)
4. [데이터 매핑](#데이터-매핑)
5. [테스트](#테스트)

---

## 개요

### 지원하는 건강 데이터 타입

현재 구현된 기본 4대장:
- **HEART_RATE** (심박수) - bpm
- **OXYGEN_SATURATION** (혈중 산소포화도) - %
- **SLEEP** (수면 시간) - hours
- **STEP_COUNT** (걸음 수) - steps

확장 가능한 추가 타입:
- **BLOOD_PRESSURE_SYSTOLIC** (수축기 혈압) - mmHg
- **BLOOD_PRESSURE_DIASTOLIC** (이완기 혈압) - mmHg
- **BODY_TEMPERATURE** (체온) - °C
- **BLOOD_GLUCOSE** (혈당) - mg/dL
- **BODY_WEIGHT** (체중) - kg
- **ACTIVE_ENERGY** (활동 칼로리) - kcal
- **RESTING_HEART_RATE** (휴식시 심박수) - bpm
- **RESPIRATORY_RATE** (호흡수) - breaths/min
- **STRESS_LEVEL** (스트레스 지수) - score

---

## 백엔드 API 명세

### 1. 생체 데이터 업로드

**Endpoint:** `POST /api/v1/patients/{patientId}/health-data`

**Request Body:**
```json
{
  "records": [
    {
      "type": "HEART_RATE",
      "average": 78.0,
      "max": 120.0,
      "min": 55.0,
      "unit": "bpm",
      "measuredAt": "2025-10-18T14:22:00"
    },
    {
      "type": "STEP_COUNT",
      "average": 5321.0,
      "max": 10234.0,
      "min": 1200.0,
      "unit": "steps",
      "measuredAt": "2025-10-18T14:22:00"
    },
    {
      "type": "SLEEP",
      "average": 7.2,
      "max": 9.0,
      "min": 5.5,
      "unit": "hours",
      "measuredAt": "2025-10-18T14:22:00"
    },
    {
      "type": "OXYGEN_SATURATION",
      "average": 98.3,
      "max": 100.0,
      "min": 95.0,
      "unit": "%",
      "measuredAt": "2025-10-18T14:22:00"
    }
  ]
}
```

**Response:**
```json
{
  "message": "생체 데이터가 성공적으로 업로드되었습니다.",
  "data": {
    "uploadedCount": 4
  }
}
```

**Status Codes:**
- `201 Created` - 정상 저장
- `400 Bad Request` - 필드 누락/형식 오류
- `401 Unauthorized` - 토큰 누락 또는 만료
- `403 Forbidden` - 권한 없음
- `409 Conflict` - 중복된 데이터
- `500 Internal Server Error` - 서버 예외

---

### 2. 생체 데이터 조회

**Endpoint:** `GET /api/v1/patients/{patientId}/health-data`

**Query Parameters:**
- `type` (optional) - 데이터 종류 (HEART_RATE, STEP_COUNT 등)
- `from` (optional) - 시작 날짜 (yyyyMMdd)
- `to` (optional) - 종료 날짜 (yyyyMMdd)
- `sort` (optional) - 정렬 기준 (measuredAt,desc 기본)

**Example:**
```
GET /api/v1/patients/2/health-data?type=HEART_RATE&from=20251017&to=20251018
```

**Response:**
```json
{
  "message": "생체 데이터 조회가 완료되었습니다.",
  "data": {
    "patientId": 2,
    "type": "HEART_RATE",
    "records": [
      {
        "recordId": 1001,
        "type": "HEART_RATE",
        "average": 78.0,
        "max": 120.0,
        "min": 55.0,
        "unit": "bpm",
        "measuredAt": "2025-10-18T14:22:00"
      }
    ]
  }
}
```

---

### 3. 생체 데이터 요약 통계 조회

**Endpoint:** `GET /api/v1/patients/{patientId}/health-data/summary`

**Query Parameters:**
- `type` (optional) - 데이터 종류
- `from` (optional) - 시작 날짜 (yyyyMMdd)
- `to` (optional) - 종료 날짜 (yyyyMMdd)

**Example:**
```
GET /api/v1/patients/2/health-data/summary?type=HEART_RATE&from=20251010&to=20251017
```

**Response:**
```json
{
  "message": "생체 데이터 요약 통계 조회가 완료되었습니다.",
  "data": {
    "patientId": 2,
    "type": "HEART_RATE",
    "unit": "bpm",
    "summary": [
      {
        "date": "2025-10-10",
        "average": 84.3,
        "max": 112.0,
        "min": 62.0,
        "count": 38
      },
      {
        "date": "2025-10-11",
        "average": 79.5,
        "max": 101.0,
        "min": 59.0,
        "count": 35
      }
    ]
  }
}
```

---

## 안드로이드 구현

### 1. 데이터 모델 정의

#### Request DTO
```kotlin
package kr.co.ongil.data.model.health

import com.google.gson.annotations.SerializedName

/**
 * 건강 데이터 업로드 요청
 */
data class HealthDataUploadRequest(
    @SerializedName("records")
    val records: List<HealthDataRecordRequest>
)

/**
 * 단일 건강 데이터 레코드
 */
data class HealthDataRecordRequest(
    @SerializedName("type")
    val type: String,           // "HEART_RATE", "OXYGEN_SATURATION", "SLEEP", "STEP_COUNT"

    @SerializedName("average")
    val average: Double,

    @SerializedName("max")
    val max: Double,

    @SerializedName("min")
    val min: Double,

    @SerializedName("unit")
    val unit: String,           // "bpm", "%", "hours", "steps"

    @SerializedName("measuredAt")
    val measuredAt: String      // ISO-8601 형식: "2025-10-18T14:22:00"
)
```

#### Response DTO
```kotlin
/**
 * 건강 데이터 업로드 응답
 */
data class HealthDataUploadResponse(
    @SerializedName("uploadedCount")
    val uploadedCount: Int
)

/**
 * API 공통 응답 래퍼
 */
data class ApiResponse<T>(
    @SerializedName("message")
    val message: String,

    @SerializedName("data")
    val data: T
)
```

---

### 2. Retrofit API 인터페이스

```kotlin
package kr.co.ongil.data.api

import kr.co.ongil.data.model.health.HealthDataUploadRequest
import kr.co.ongil.data.model.health.HealthDataUploadResponse
import kr.co.ongil.data.model.common.ApiResponse
import retrofit2.http.*

interface HealthDataApi {

    /**
     * 생체 데이터 업로드
     */
    @POST("api/v1/patients/{patientId}/health-data")
    suspend fun uploadHealthData(
        @Path("patientId") patientId: Int,
        @Body request: HealthDataUploadRequest
    ): ApiResponse<HealthDataUploadResponse>
}
```

---

### 3. 데이터 변환 (HealthData → Request 매핑)

```kotlin
package kr.co.ongil.domain.mapper

import kr.co.ongil.data.model.health.HealthDataRecordRequest
import kr.co.ongil.data.model.health.HealthDataUploadRequest
import kr.co.ongil.domain.model.HealthData
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * HealthData (도메인 모델) → HealthDataUploadRequest 변환
 */
fun HealthData.toUploadRequest(): HealthDataUploadRequest {
    val now = LocalDateTime.now()
        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"))

    val records = mutableListOf<HealthDataRecordRequest>()

    // 심박수
    heartRate?.let {
        records += HealthDataRecordRequest(
            type = "HEART_RATE",
            average = it.average.toDouble(),
            max = it.max.toDouble(),
            min = it.min.toDouble(),
            unit = "bpm",
            measuredAt = now
        )
    }

    // 혈중 산소포화도
    oxygenSaturation?.let {
        records += HealthDataRecordRequest(
            type = "OXYGEN_SATURATION",
            average = it.average,
            max = it.max,
            min = it.min,
            unit = "%",
            measuredAt = now
        )
    }

    // 수면 시간
    sleep?.let {
        records += HealthDataRecordRequest(
            type = "SLEEP",
            average = it.average,
            max = it.max,
            min = it.min,
            unit = "hours",
            measuredAt = now
        )
    }

    // 걸음 수
    steps?.let {
        records += HealthDataRecordRequest(
            type = "STEP_COUNT",
            average = it.average.toDouble(),
            max = it.max.toDouble(),
            min = it.min.toDouble(),
            unit = "steps",
            measuredAt = now
        )
    }

    return HealthDataUploadRequest(records = records)
}
```

---

### 4. Repository 구현

```kotlin
package kr.co.ongil.data.repository

import kr.co.ongil.data.api.HealthDataApi
import kr.co.ongil.domain.mapper.toUploadRequest
import kr.co.ongil.domain.model.HealthData
import javax.inject.Inject

interface HealthDataRemoteRepository {
    suspend fun uploadHealthData(patientId: Int, healthData: HealthData): Result<Int>
}

class HealthDataRemoteRepositoryImpl @Inject constructor(
    private val api: HealthDataApi
) : HealthDataRemoteRepository {

    override suspend fun uploadHealthData(
        patientId: Int,
        healthData: HealthData
    ): Result<Int> = runCatching {
        val request = healthData.toUploadRequest()
        val response = api.uploadHealthData(patientId, request)
        response.data.uploadedCount
    }
}
```

---

### 5. UseCase 구현

```kotlin
package kr.co.ongil.domain.usecase.health

import kr.co.ongil.data.repository.HealthDataRemoteRepository
import kr.co.ongil.domain.model.HealthData
import javax.inject.Inject

class UploadHealthDataUseCase @Inject constructor(
    private val repository: HealthDataRemoteRepository
) {
    suspend operator fun invoke(
        patientId: Int,
        healthData: HealthData
    ): Result<Int> {
        return repository.uploadHealthData(patientId, healthData)
    }
}
```

---

### 6. ViewModel 통합

```kotlin
@HiltViewModel
class PatientInfoViewModel @Inject constructor(
    private val getPatientInfoUseCase: GetPatientInfoUseCase,
    private val getHealthDataUseCase: GetHealthDataUseCase,
    private val uploadHealthDataUseCase: UploadHealthDataUseCase, // ⬅ 추가
    private val healthConnectRepository: HealthConnectRepository,
    private val userDataStoreManager: UserDataStoreManager
) : ViewModel() {

    private var currentPatientId: Int? = null

    // 건강 데이터 로드 + 서버 동기화
    private fun loadHealthData() {
        viewModelScope.launch {
            try {
                getHealthDataUseCase().collectLatest { result ->
                    result.onSuccess { healthData ->
                        _uiState.value = _uiState.value.copy(healthData = healthData)

                        // 🔁 서버에 동기화
                        val pid = currentPatientId
                        if (pid != null && healthData != null) {
                            launch {
                                uploadHealthDataUseCase(pid, healthData)
                                    .onSuccess { count ->
                                        android.util.Log.d(TAG, "HealthData 서버 업로드 성공: $count 개")
                                    }
                                    .onFailure { e ->
                                        android.util.Log.e(TAG, "HealthData 서버 업로드 실패", e)
                                    }
                            }
                        }
                    }.onFailure {
                        _uiState.value = _uiState.value.copy(healthData = null)
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e(TAG, "loadHealthData() - 예외", e)
                _uiState.value = _uiState.value.copy(healthData = null)
            }
        }
    }

    // 수동 동기화 함수 (버튼으로 호출 가능)
    fun syncHealthDataToServer() {
        viewModelScope.launch {
            val pid = currentPatientId ?: return@launch
            val healthData = _uiState.value.healthData ?: return@launch

            uploadHealthDataUseCase(pid, healthData)
                .onSuccess { count ->
                    android.util.Log.d(TAG, "수동 동기화 성공: $count 개")
                    // UI 업데이트 (Toast 등)
                }
                .onFailure { e ->
                    android.util.Log.e(TAG, "수동 동기화 실패", e)
                    // 에러 처리
                }
        }
    }
}
```

---

## 데이터 매핑

### Samsung Health → Backend API 매핑표

| Samsung Health | Backend Type | Unit | Average | Max | Min |
|----------------|--------------|------|---------|-----|-----|
| HeartRateData | HEART_RATE | bpm | average (Long) | max (Long) | min (Long) |
| OxygenSaturationData | OXYGEN_SATURATION | % | average (Double) | max (Double) | min (Double) |
| SleepData | SLEEP | hours | average (Double) | max (Double) | min (Double) |
| StepsData | STEP_COUNT | steps | average (Long) | max (Long) | min (Long) |

**확장 가능 (추후):**
- BloodPressureData → BLOOD_PRESSURE_SYSTOLIC, BLOOD_PRESSURE_DIASTOLIC
- BodyTemperatureData → BODY_TEMPERATURE
- WeightData → BODY_WEIGHT
- ActiveEnergyData → ACTIVE_ENERGY

---

## 테스트

### 1. 로컬 테스트

```kotlin
@Test
fun `HealthData를 UploadRequest로 변환 테스트`() {
    // Given
    val healthData = HealthData(
        heartRate = HeartRateData(average = 78, max = 120, min = 55),
        steps = StepsData(average = 5321, max = 10234, min = 1200),
        sleep = SleepData(average = 7.2, max = 9.0, min = 5.5),
        oxygenSaturation = OxygenSaturationData(average = 98.3, max = 100.0, min = 95.0)
    )

    // When
    val request = healthData.toUploadRequest()

    // Then
    assertEquals(4, request.records.size)
    assertTrue(request.records.any { it.type == "HEART_RATE" })
    assertTrue(request.records.any { it.type == "STEP_COUNT" })
}
```

### 2. API 통합 테스트

Postman 또는 HTTP Client 사용:

```http
POST http://localhost:8080/api/v1/patients/1/health-data
Authorization: Bearer {your-jwt-token}
Content-Type: application/json

{
  "records": [
    {
      "type": "HEART_RATE",
      "average": 78.0,
      "max": 120.0,
      "min": 55.0,
      "unit": "bpm",
      "measuredAt": "2025-10-18T14:22:00"
    }
  ]
}
```

---

## 주의사항

1. **권한 관리**
   - 환자 본인 또는 등록된 보호자만 데이터 업로드 가능
   - JWT 토큰 필수

2. **중복 데이터 방지**
   - `(patientId, type, measuredAt)` 유니크 제약
   - 동일 시각에 같은 타입 데이터는 중복 저장 불가

3. **데이터 유효성**
   - `measuredAt`는 현재 시각보다 미래일 수 없음
   - 시작 날짜는 종료 날짜보다 이전이어야 함

4. **에러 처리**
   - 네트워크 에러 시 재시도 로직 구현 권장
   - 업로드 실패 시 로컬 큐에 저장 후 나중에 재시도

5. **성능 최적화**
   - 대량 데이터 업로드 시 배치 처리 고려
   - Health Connect 데이터 가져오기는 백그라운드 스레드에서 실행

---

## 문의

백엔드 관련 문의: [Ongil Backend Team]
안드로이드 관련 문의: [Ongil Android Team]
