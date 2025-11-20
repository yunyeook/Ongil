package kr.co.ongil.data.mapper

import kr.co.ongil.data.model.health.HeartRateData
import kr.co.ongil.data.model.health.LocalHealthData
import kr.co.ongil.data.model.health.OxygenSaturationData
import kr.co.ongil.data.model.health.SleepData
import kr.co.ongil.data.model.health.StepsData
import kr.co.ongil.domain.model.HealthData
import kr.co.ongil.domain.model.HeartRate
import kr.co.ongil.domain.model.OxygenSaturation
import kr.co.ongil.domain.model.Sleep
import kr.co.ongil.domain.model.Steps
import org.junit.Assert.*
import org.junit.Test

/**
 * HealthDataMapper 테스트
 */
class HealthDataMapperTest {

    @Test
    fun `LocalHealthData를 도메인 모델로 변환 테스트`() {
        // Given
        val localHealthData = LocalHealthData(
            heartRate = HeartRateData(average = 78, max = 120, min = 55),
            oxygenSaturation = OxygenSaturationData(average = 98.3, max = 100.0, min = 95.0),
            sleep = SleepData(average = 7.2, max = 9.0, min = 5.5),
            steps = StepsData(average = 5321, max = 10234, min = 1200)
        )

        // When
        val domainData = localHealthData.toDomain()

        // Then
        assertNotNull(domainData)

        // 심박수 검증
        assertNotNull(domainData.heartRate)
        assertEquals(78L, domainData.heartRate!!.average)
        assertEquals(120L, domainData.heartRate!!.max)
        assertEquals(55L, domainData.heartRate!!.min)

        // 산소포화도 검증
        assertNotNull(domainData.oxygenSaturation)
        assertEquals(98.3, domainData.oxygenSaturation!!.average, 0.01)
        assertEquals(100.0, domainData.oxygenSaturation!!.max, 0.01)
        assertEquals(95.0, domainData.oxygenSaturation!!.min, 0.01)

        // 수면 검증
        assertNotNull(domainData.sleep)
        assertEquals(7.2, domainData.sleep!!.average, 0.01)
        assertEquals(9.0, domainData.sleep!!.max, 0.01)
        assertEquals(5.5, domainData.sleep!!.min, 0.01)

        // 걸음수 검증
        assertNotNull(domainData.steps)
        assertEquals(5321L, domainData.steps!!.average)
        assertEquals(10234L, domainData.steps!!.max)
        assertEquals(1200L, domainData.steps!!.min)
    }

    @Test
    fun `도메인 모델을 UploadRequest로 변환 테스트`() {
        // Given
        val healthData = HealthData(
            heartRate = HeartRate(average = 78, max = 120, min = 55),
            oxygenSaturation = OxygenSaturation(average = 98.3, max = 100.0, min = 95.0),
            sleep = Sleep(average = 7.2, max = 9.0, min = 5.5),
            steps = Steps(average = 5321, max = 10234, min = 1200)
        )

        // When
        val request = healthData.toUploadRequest()

        // Then
        assertEquals(4, request.records.size)

        // 각 타입이 존재하는지 확인
        assertTrue(request.records.any { it.type == "HEART_RATE" })
        assertTrue(request.records.any { it.type == "OXYGEN_SATURATION" })
        assertTrue(request.records.any { it.type == "SLEEP" })
        assertTrue(request.records.any { it.type == "STEP_COUNT" })

        // 심박수 레코드 검증
        val heartRateRecord = request.records.first { it.type == "HEART_RATE" }
        assertEquals("bpm", heartRateRecord.unit)
        assertEquals(78.0, heartRateRecord.average, 0.01)
        assertEquals(120.0, heartRateRecord.max, 0.01)
        assertEquals(55.0, heartRateRecord.min, 0.01)
        assertTrue(heartRateRecord.measuredAt.isNotEmpty())

        // 산소포화도 레코드 검증
        val oxygenRecord = request.records.first { it.type == "OXYGEN_SATURATION" }
        assertEquals("%", oxygenRecord.unit)
        assertEquals(98.3, oxygenRecord.average, 0.01)
        assertEquals(100.0, oxygenRecord.max, 0.01)
        assertEquals(95.0, oxygenRecord.min, 0.01)

        // 수면 레코드 검증
        val sleepRecord = request.records.first { it.type == "SLEEP" }
        assertEquals("hours", sleepRecord.unit)
        assertEquals(7.2, sleepRecord.average, 0.01)
        assertEquals(9.0, sleepRecord.max, 0.01)
        assertEquals(5.5, sleepRecord.min, 0.01)

        // 걸음수 레코드 검증
        val stepsRecord = request.records.first { it.type == "STEP_COUNT" }
        assertEquals("steps", stepsRecord.unit)
        assertEquals(5321.0, stepsRecord.average, 0.01)
        assertEquals(10234.0, stepsRecord.max, 0.01)
        assertEquals(1200.0, stepsRecord.min, 0.01)
    }

    @Test
    fun `LocalHealthData를 직접 UploadRequest로 변환 테스트`() {
        // Given
        val localHealthData = LocalHealthData(
            heartRate = HeartRateData(average = 78, max = 120, min = 55),
            oxygenSaturation = OxygenSaturationData(average = 98.3, max = 100.0, min = 95.0),
            sleep = SleepData(average = 7.2, max = 9.0, min = 5.5),
            steps = StepsData(average = 5321, max = 10234, min = 1200)
        )

        // When
        val request = localHealthData.toUploadRequest()

        // Then
        assertEquals(4, request.records.size)
        assertTrue(request.records.any { it.type == "HEART_RATE" })
        assertTrue(request.records.any { it.type == "OXYGEN_SATURATION" })
        assertTrue(request.records.any { it.type == "SLEEP" })
        assertTrue(request.records.any { it.type == "STEP_COUNT" })
    }

    @Test
    fun `일부 데이터만 있을 때 변환 테스트`() {
        // Given - 심박수와 걸음수만 있음
        val healthData = HealthData(
            heartRate = HeartRate(average = 78, max = 120, min = 55),
            oxygenSaturation = null,
            sleep = null,
            steps = Steps(average = 5321, max = 10234, min = 1200)
        )

        // When
        val request = healthData.toUploadRequest()

        // Then
        assertEquals(2, request.records.size)
        assertTrue(request.records.any { it.type == "HEART_RATE" })
        assertTrue(request.records.any { it.type == "STEP_COUNT" })
        assertFalse(request.records.any { it.type == "OXYGEN_SATURATION" })
        assertFalse(request.records.any { it.type == "SLEEP" })
    }

    @Test
    fun `데이터가 없을 때 변환 테스트`() {
        // Given
        val healthData = HealthData(
            heartRate = null,
            oxygenSaturation = null,
            sleep = null,
            steps = null
        )

        // When
        val request = healthData.toUploadRequest()

        // Then
        assertEquals(0, request.records.size)
    }

    @Test
    fun `measuredAt 형식 검증 테스트`() {
        // Given
        val healthData = HealthData(
            heartRate = HeartRate(average = 78, max = 120, min = 55),
            oxygenSaturation = null,
            sleep = null,
            steps = null
        )

        // When
        val request = healthData.toUploadRequest()

        // Then
        val record = request.records.first()
        // ISO-8601 형식: yyyy-MM-ddTHH:mm:ss
        val regex = Regex("""\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}""")
        assertTrue(regex.matches(record.measuredAt))
    }

    @Test
    fun `Long to Double 변환 검증 테스트`() {
        // Given
        val healthData = HealthData(
            heartRate = HeartRate(average = 78, max = 120, min = 55),
            oxygenSaturation = null,
            sleep = null,
            steps = Steps(average = 5321, max = 10234, min = 1200)
        )

        // When
        val request = healthData.toUploadRequest()

        // Then
        val heartRateRecord = request.records.first { it.type == "HEART_RATE" }
        assertTrue(heartRateRecord.average is Double)
        assertTrue(heartRateRecord.max is Double)
        assertTrue(heartRateRecord.min is Double)

        val stepsRecord = request.records.first { it.type == "STEP_COUNT" }
        assertTrue(stepsRecord.average is Double)
        assertTrue(stepsRecord.max is Double)
        assertTrue(stepsRecord.min is Double)
    }
}
