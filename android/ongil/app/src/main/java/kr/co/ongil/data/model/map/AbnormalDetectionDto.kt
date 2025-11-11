package kr.co.ongil.data.model.map

import kotlinx.serialization.Serializable

/**
 * 이상 탐지 이벤트 등록 Request
 * POST /api/v1/patients/{patientId}/abnormals
 */
@Serializable
data class ReportAbnormalRequest(
    val abnormalType: String, // "WANDER" (배회)
    val latitude: Double, // 발생 위도
    val longitude: Double, // 발생 경도
    val safeZoneLevel: String, // "FIRST", "SECOND", "THIRD"
    val centerLatitude: Double, // 안전범위 중심 위도
    val centerLongitude: Double, // 안전범위 중심 경도
    val distanceFromCenter: Double, // 중심으로부터의 거리 (미터)
    val boundaryRadius: Double, // 해당 단계의 경계 반경 (미터)
    val elapsedTime: Int, // 배회 경과 시간 (초)
    val thresholdTime: Int // 배회 기준 시간 (초)
)

/**
 * 이상 탐지 이벤트 등록 Response
 */
@Serializable
data class ReportAbnormalResponse(
    val message: String,
    val data: AbnormalData? = null
)

@Serializable
data class AbnormalData(
    val abnormalId: Long,
    val abnormalType: String,
    val safeZoneLevel: String,
    val createdAt: String, // ISO 8601 형식
    val latitude: Double,
    val longitude: Double,
    val centerLatitude: Double,
    val centerLongitude: Double,
    val distanceFromCenter: Double,
    val boundaryRadius: Double,
    // WANDER 타입 전용 필드
    val elapsedTime: Int? = null,
    val thresholdTime: Int? = null,
    // 기타 타입 전용 필드
    val description: String? = null
)
