package kr.co.ongil.wear.data.datasource.remote.api

import kr.co.ongil.wear.data.model.auth.RefreshTokenRequest
import kr.co.ongil.wear.data.model.auth.RefreshTokenResponse
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Wear OS용 인증 API
 *
 * 워치에서는 직접 로그인하지 않고, Phone 앱에서 동기화된 토큰을 사용
 * 따라서 Refresh만 필요
 */
interface WearAuthApi {

    /**
     * Access Token 갱신
     * POST /api/v1/auth/refresh
     */
    @POST("/api/v1/auth/refresh")
    suspend fun refreshToken(
        @Body request: RefreshTokenRequest
    ): RefreshTokenResponse
}
