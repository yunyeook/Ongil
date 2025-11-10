package kr.co.ongil.data.datasource.remote.api

import kr.co.ongil.data.model.call.*
import kr.co.ongil.data.model.call.ApiResponse
import kr.co.ongil.data.model.call.CallCreateRequest
import kr.co.ongil.data.model.call.CallDetailResponse
import kr.co.ongil.data.model.call.CallLogResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.PUT
import retrofit2.http.Query

/**
 * 통화 관련 API
 *
 * 참고: Authorization 헤더는 AuthInterceptor가 자동으로 추가합니다.
 */
interface CallApi {

    /**
     * 나의 통화 기록 목록 조회
     * GET /api/v1/calls/log?page=0&size=20
     */
    @GET("/api/v1/calls/logs")
    suspend fun getCallLogs(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20,
        @Query("sort") sort: String = "createdAt,desc"
    ): Response<CallLogResponse>

    /**
     * 특정 통화 기록 상세 조회
     * GET /api/v1/calls/log/{callLogId}
     */
    @GET("/api/v1/calls/log/{callLogId}")
    suspend fun getCallDetail(
        @Path("callLogId") callLogId: Long
    ): Response<CallDetailResponse>

    // 1) 통화 생성
    @POST("/api/v1/calls")
    suspend fun createVoipCall(
        @Body body: CallCreateRequest
    ): Response<ApiResponse<VoipCallDto>>


    // 2) 통화 상태 변경
    @PUT("/api/v1/calls/{callId}/status")
    suspend fun updateVoipCallStatus(
        @Path("callId") callId: Long,
        @Body body: CallStatusUpdateRequest
    ): Response<ApiResponse<VoipCallDto>>

    // 3) callId 로 조회
    @GET("/api/v1/calls/{callId}")
    suspend fun getVoipCall(
        @Path("callId") callId: Long
    ): Response<ApiResponse<VoipCallDto>>

    // 4) 환자 위치 1회 전송
    @POST("/api/v1/calls/{callId}/start-location")
    suspend fun sendVoipCallStartLocation(
        @Path("callId") callId: Long,
        @Body body: CallStartLocationRequest
    ): Response<ApiResponse<Unit>>

    //인증
    @GET("/api/v1/calls/rtc/turn-credentials")
    suspend fun getTurnCredentials(): Response<ApiResponse<TurnCredentialsDto>>

}
