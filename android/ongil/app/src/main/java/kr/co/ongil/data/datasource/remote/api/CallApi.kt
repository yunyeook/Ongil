package kr.co.ongil.data.datasource.remote.api

import kr.co.ongil.data.model.call.CallLogResponse
import retrofit2.http.GET
import retrofit2.http.Header

/**
 * 통화 관련 API
 */
interface CallApi {

    /**
     * 나의 통화 기록 목록 조회
     * GET /api/v1/calls/log
     */
    @GET("/api/v1/calls/log")
    suspend fun getCallLogs(
        @Header("Authorization") accessToken: String
    ): CallLogResponse
}
