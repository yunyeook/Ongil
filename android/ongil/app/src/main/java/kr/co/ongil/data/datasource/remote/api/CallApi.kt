package kr.co.ongil.data.datasource.remote.api

import kr.co.ongil.data.model.call.CallDetailResponse
import kr.co.ongil.data.model.call.CallLogResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * 통화 관련 API
 *
 * 참고: Authorization 헤더는 AuthInterceptor가 자동으로 추가합니다.
 */
interface CallApi {

    /**
     * 나의 통화 기록 목록 조회
     * GET /api/v1/calls/log
     */
    @GET("/api/v1/calls/log")
    suspend fun getCallLogs(): Response<CallLogResponse>

    /**
     * 특정 통화 기록 상세 조회
     * GET /api/v1/calls/log/{callLogId}
     */
    @GET("/api/v1/calls/log/{callLogId}")
    suspend fun getCallDetail(
        @Path("callLogId") callLogId: Long
    ): Response<CallDetailResponse>
}
