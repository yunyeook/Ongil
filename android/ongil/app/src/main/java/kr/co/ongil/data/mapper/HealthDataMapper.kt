package kr.co.ongil.data.mapper

import kr.co.ongil.data.model.health.HealthDataRecordRequest
import kr.co.ongil.data.model.health.HealthDataUploadRequest
import kr.co.ongil.data.model.health.LocalHealthData
import kr.co.ongil.data.model.health.HeartRateData
import kr.co.ongil.data.model.health.OxygenSaturationData
import kr.co.ongil.data.model.health.SleepData
import kr.co.ongil.data.model.health.StepsData
import kr.co.ongil.domain.model.HealthData
import kr.co.ongil.domain.model.HeartRate
import kr.co.ongil.domain.model.OxygenSaturation
import kr.co.ongil.domain.model.Sleep
import kr.co.ongil.domain.model.Steps
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * LocalHealthData (Health Connect 데이터) → HealthData (도메인 모델) 변환
 */
fun LocalHealthData.toDomain(): HealthData {
    return HealthData(
        heartRate = this.heartRate?.let {
            HeartRate(
                average = it.average,
                max = it.max,
                min = it.min
            )
        },
        oxygenSaturation = this.oxygenSaturation?.let {
            OxygenSaturation(
                average = it.average,
                max = it.max,
                min = it.min
            )
        },
        sleep = this.sleep?.let {
            Sleep(
                average = it.average,
                max = it.max,
                min = it.min
            )
        },
        steps = this.steps?.let {
            Steps(
                average = it.average,
                max = it.max,
                min = it.min
            )
        }
    )
}

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

/**
 * LocalHealthData → HealthDataUploadRequest 직접 변환 (편의 함수)
 */
fun LocalHealthData.toUploadRequest(): HealthDataUploadRequest {
    return this.toDomain().toUploadRequest()
}