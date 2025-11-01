package kr.co.ongil.data.datasource.remote.api

import kr.co.ongil.data.model.auth.LogoutRequest
import kr.co.ongil.data.model.auth.LogoutResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

/**
 * 인증 관련 API
 */
interface AuthApi {

    /**
     * 로그아웃
     * POST /api/v1/auth/logout
     */
    @POST("/api/v1/auth/logout")
    suspend fun logout(
        @Header("Authorization") accessToken: String,
        @Body request: LogoutRequest
    ): LogoutResponse
}