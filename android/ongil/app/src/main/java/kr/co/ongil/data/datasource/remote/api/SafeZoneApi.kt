package kr.co.ongil.data.datasource.remote.api

import kr.co.ongil.data.model.safezone.SafeZoneUpdateRequest
import kr.co.ongil.data.model.safezone.SafeZoneUpdateResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path

/**
 * 안전범위 설정 관련 API
 *
 * 참고: Authorization 헤더는 AuthInterceptor가 자동으로 추가합니다.
 */
interface SafeZoneApi {

    /**
     * 환자의 안전범위 조회
     * GET /api/v1/patients/{patientId}/safezone
     *
     * @param patientId 안전범위를 조회할 환자 ID
     * @return 환자의 안전범위 정보
     */
    @GET("/api/v1/patients/{patientId}/safezone")
    suspend fun getSafeZone(
        @Path("patientId") patientId: Long
    ): SafeZoneUpdateResponse

    /**
     * 환자의 안전범위 부분 수정
     * PATCH /api/v1/patients/{patientId}/safezone
     *
     * @param patientId 안전범위를 수정할 환자 ID
     * @param request 수정할 안전범위 정보
     * @return 수정된 안전범위 정보
     */
    @PATCH("/api/v1/patients/{patientId}/safezone")
    suspend fun updateSafeZone(
        @Path("patientId") patientId: Long,
        @Body request: SafeZoneUpdateRequest
    ): SafeZoneUpdateResponse
}
