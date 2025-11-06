package kr.co.ongil.data.datasource.remote.api

import kr.co.ongil.data.model.auth.*
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

/**
 * 인증 관련 API
 *
 * 참고:
 * - login, signup, refresh는 Authorization 헤더 불필요
 * - logout, sendVerificationCode, verifyCode는 AuthInterceptor가 자동으로 헤더 추가
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
        @Body request: LogoutRequest
    ): LogoutResponse

    /**
     * 전화번호 인증번호 발송
     * POST /api/v1/auth/verification/send
     */
    @POST("/api/v1/auth/verification/send")
    suspend fun sendVerificationCode(
        @Body request: SendVerificationRequest
    ): SendVerificationResponse

    /**
     * 인증번호 확인
     * POST /api/v1/auth/verification/verify
     */
    @POST("/api/v1/auth/verification/verify")
    suspend fun verifyCode(
        @Body request: VerifyCodeRequest
    ): VerifyCodeResponse

    /**
     * 토큰 재발급
     * POST /api/v1/auth/refresh
     */
    @POST("/api/v1/auth/refresh")
    suspend fun refreshToken(
        @Body request: RefreshTokenRequest
    ): RefreshTokenResponse
}