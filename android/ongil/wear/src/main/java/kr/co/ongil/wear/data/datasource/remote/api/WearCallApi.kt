package kr.co.ongil.wear.data.datasource.remote.api

import kr.co.ongil.wear.data.model.call.ApiResponse
import kr.co.ongil.wear.data.model.call.CallCreateRequest
import kr.co.ongil.wear.data.model.call.CallStatusUpdateRequest
import kr.co.ongil.wear.data.model.call.TurnCredentialsDto
import kr.co.ongil.wear.data.model.call.VoipCallDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

/**
 * Wear OS용 통화 API
 */
interface WearCallApi {

    /**
     * 통화 생성
     * POST /api/v1/calls
     */
    @POST("/api/v1/calls")
    suspend fun createVoipCall(
        @Body body: CallCreateRequest
    ): Response<ApiResponse<VoipCallDto>>

    /**
     * 통화 상태 변경
     * PUT /api/v1/calls/{callId}/status
     */
    @PUT("/api/v1/calls/{callId}/status")
    suspend fun updateVoipCallStatus(
        @Path("callId") callId: Long,
        @Body body: CallStatusUpdateRequest
    ): Response<ApiResponse<VoipCallDto>>

    /**
     * callId로 통화 정보 조회
     * GET /api/v1/calls/{callId}
     */
    @GET("/api/v1/calls/{callId}")
    suspend fun getVoipCall(
        @Path("callId") callId: Long
    ): Response<ApiResponse<VoipCallDto>>

    /**
     * TURN 서버 인증 정보 조회
     * GET /api/v1/calls/rtc/turn-credentials
     */
    @GET("/api/v1/calls/rtc/turn-credentials")
    suspend fun getTurnCredentials(): Response<ApiResponse<TurnCredentialsDto>>
}
