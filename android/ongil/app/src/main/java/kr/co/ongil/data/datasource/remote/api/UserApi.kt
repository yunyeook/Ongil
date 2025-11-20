package kr.co.ongil.data.datasource.remote.api

import kr.co.ongil.data.model.auth.ChangePasswordRequest
import kr.co.ongil.data.model.auth.ChangePasswordResponse
import kr.co.ongil.data.model.user.CreateRelationshipRequest
import kr.co.ongil.data.model.user.CreateRelationshipResponse
import kr.co.ongil.data.model.user.UpdateUserResponse
import kr.co.ongil.data.model.user.UserResponse
import kr.co.ongil.data.model.user.UserSearchRequest
import kr.co.ongil.data.model.user.UserSearchResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

/**
 * 사용자 관련 API
 *
 * 참고: Authorization 헤더는 AuthInterceptor가 자동으로 추가합니다.
 */
interface UserApi {

    /**
     * 로그인한 회원의 기본 정보 조회
     * GET /api/v1/users/me
     */
    @GET("/api/v1/users/me")
    suspend fun getMyInfo(): UserResponse

    /**
     * 로그인한 회원의 기본 정보 수정
     * PATCH /api/v1/users/me
     */
    @Multipart
    @PATCH("/api/v1/users/me")
    suspend fun updateMyInfo(
        @Part("name") name: RequestBody? = null,
        @Part("birth") birth: RequestBody? = null,
        @Part("phoneNumber") phoneNumber: RequestBody? = null,
        @Part("verificationToken") verificationToken: RequestBody? = null,
        @Part profileImage: MultipartBody.Part? = null
    ): UpdateUserResponse

    /**
     * 비밀번호 변경
     * PATCH /api/v1/password/reset
     */
    @PATCH("/api/v1/password/reset")
    suspend fun changePassword(
        @Body request: ChangePasswordRequest
    ): ChangePasswordResponse

    /**
     * 사용자 검색 (전화번호로 검색)
     * POST /api/v1/users/searches
     */
    @POST("/api/v1/users/searches")
    suspend fun searchUser(
        @Body request: UserSearchRequest
    ): Response<UserSearchResponse>

    /**
     * 관계 생성
     * POST /api/v1/relationships/me
     */
    @POST("/api/v1/relationships/me")
    suspend fun createRelationship(
        @Body request: CreateRelationshipRequest
    ): Response<CreateRelationshipResponse>
}