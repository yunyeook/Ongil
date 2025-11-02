package kr.co.ongil.data.datasource.remote.api

import kr.co.ongil.data.model.user.UserResponse
import retrofit2.http.GET
import retrofit2.http.Header

/**
 * 사용자 관련 API
 */
interface UserApi {

    /**
     * 로그인한 회원의 기본 정보 조회
     * GET /api/v1/users/me
     */
    @GET("/api/v1/users/me")
    suspend fun getMyInfo(
        @Header("Authorization") accessToken: String
    ): UserResponse
}