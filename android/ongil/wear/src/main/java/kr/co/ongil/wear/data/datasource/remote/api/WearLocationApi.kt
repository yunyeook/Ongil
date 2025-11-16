package kr.co.ongil.wear.data.datasource.remote.api

import kr.co.ongil.wear.data.model.location.NavigationEndRequest
import kr.co.ongil.wear.data.model.location.NavigationEndResponse
import kr.co.ongil.wear.data.model.location.NavigationStartRequest
import kr.co.ongil.wear.data.model.location.NavigationStartResponse
import kr.co.ongil.wear.data.model.location.UpdateLocationRequest
import kr.co.ongil.wear.data.model.location.UpdateLocationResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * Wear OS용 위치 및 네비게이션 API
 */
interface WearLocationApi {

    /**
     * 환자 위치 정보 전송
     * POST /api/v1/patients/{patientId}/location
     */
    @POST("/api/v1/patients/{patientId}/location")
    suspend fun updatePatientLocation(
        @Path("patientId") patientId: Long,
        @Body request: UpdateLocationRequest
    ): Response<UpdateLocationResponse>

    /**
     * 길안내 시작
     * POST /api/v1/navigation/start
     */
    @POST("/api/v1/navigation/start")
    suspend fun startNavigation(
        @Body request: NavigationStartRequest
    ): Response<NavigationStartResponse>

    /**
     * 길안내 종료
     * POST /api/v1/navigation/end
     */
    @POST("/api/v1/navigation/end")
    suspend fun endNavigation(
        @Body request: NavigationEndRequest
    ): Response<NavigationEndResponse>
}
