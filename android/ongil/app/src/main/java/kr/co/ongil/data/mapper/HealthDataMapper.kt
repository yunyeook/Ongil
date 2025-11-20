package kr.co.ongil.data.mapper

import kr.co.ongil.data.model.health.HealthDataRecordRequest
import kr.co.ongil.data.model.health.HealthDataUploadRequest
import kr.co.ongil.data.model.health.LocalHealthData
import kr.co.ongil.domain.model.HealthData
import kr.co.ongil.domain.model.HeartRate
import kr.co.ongil.domain.model.OxygenSaturation
import kr.co.ongil.domain.model.Sleep
import kr.co.ongil.domain.model.Steps

/**
 * LocalHealthData → HealthData (도메인 모델) 변환
 */
fun LocalHealthData.toDomain(): HealthData {
    return HealthData(
        heartRateRecords = this.heartRateRecords.map { record ->
            HeartRate(
                beatsPerMinute = record.beatsPerMinute,
                measuredAt = record.measuredAt
            )
        },
        oxygenSaturationRecords = this.oxygenSaturationRecords.map { record ->
            OxygenSaturation(
                percentage = record.percentage,
                measuredAt = record.measuredAt
            )
        },
        sleepRecords = this.sleepRecords.map { record ->
            Sleep(
                durationHours = record.durationHours,
                measuredAt = record.measuredAt
            )
        },
        stepsRecords = this.stepsRecords.map { record ->
            Steps(
                count = record.count,
                measuredAt = record.measuredAt
            )
        }
    )
}

/**
 * HealthData (도메인 모델) → HealthDataUploadRequest 변환
 */
fun HealthData.toUploadRequest(): HealthDataUploadRequest {
    val records = mutableListOf<HealthDataRecordRequest>()

    // 심박수 레코드들 변환
    this.heartRateRecords.forEach { record ->
        records += HealthDataRecordRequest(
            type = "HEART_RATE",
            average = record.beatsPerMinute.toDouble(),
            max = record.beatsPerMinute.toDouble(),
            min = record.beatsPerMinute.toDouble(),
            unit = "bpm",
            measuredAt = record.measuredAt
        )
    }

    // 혈중 산소포화도 레코드들 변환
    this.oxygenSaturationRecords.forEach { record ->
        records += HealthDataRecordRequest(
            type = "OXYGEN_SATURATION",
            average = record.percentage,
            max = record.percentage,
            min = record.percentage,
            unit = "%",
            measuredAt = record.measuredAt
        )
    }

    // 수면 레코드들 변환
    this.sleepRecords.forEach { record ->
        records += HealthDataRecordRequest(
            type = "SLEEP",
            average = record.durationHours,
            max = record.durationHours,
            min = record.durationHours,
            unit = "hours",
            measuredAt = record.measuredAt
        )
    }

    // 걸음수 레코드들 변환
    this.stepsRecords.forEach { record ->
        records += HealthDataRecordRequest(
            type = "STEP_COUNT",
            average = record.count.toDouble(),
            max = record.count.toDouble(),
            min = record.count.toDouble(),
            unit = "steps",
            measuredAt = record.measuredAt
        )
    }

    return HealthDataUploadRequest(records = records)
}