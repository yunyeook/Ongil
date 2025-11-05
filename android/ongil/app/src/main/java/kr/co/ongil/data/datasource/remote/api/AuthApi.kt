package kr.co.ongil.data.datasource.remote.api

import kr.co.ongil.data.model.auth.*
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

/**
 * 인증 관련 API
 */
interface AuthApi {

    /**
     * 로그인
     * POST/api/v1/auth/login
     */
    @POST("api/v1/auth/login")
    suspend fun login(
        @Body request : LoginRequest
    ): LoginResponse



    /**
     * 로그아웃
     * POST /api/v1/auth/logout
     */
    @POST("/api/v1/auth/logout")
    suspend fun logout(
        @Header("Authorization") accessToken: String,
        @Body request: LogoutRequest
    ): LogoutResponse

    /**
     * 전화번호 인증번호 발송
     * POST /api/v1/auth/verification/send
     */
    @POST("/api/v1/auth/verification/send")
    suspend fun sendVerificationCode(
        @Header("Authorization") accessToken: String,
        @Body request: SendVerificationRequest
    ): SendVerificationResponse

    /**
     * 인증번호 확인
     * POST /api/v1/auth/verification/verify
     */
    @POST("/api/v1/auth/verification/verify")
    suspend fun verifyCode(
        @Header("Authorization") accessToken: String,
        @Body request: VerifyCodeRequest
    ): VerifyCodeResponse
}