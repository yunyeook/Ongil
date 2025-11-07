package kr.co.ongil.data.datasource.remote.api

import kr.co.ongil.data.model.notification.NotificationResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Query

/**
 * 알림 관련 API
 *
 * 참고: Authorization 헤더는 AuthInterceptor가 자동으로 추가합니다.
 */
interface NotificationApi {

    /**
     * 알림 목록 조회
     * GET /api/v1/notifications?page={page}&size={size}&read={read}
     */
    @GET("/api/v1/notifications")
    suspend fun getNotifications(
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 10,
        @Query("read") read: Boolean? = null  // null이면 전체 조회
    ): Response<NotificationResponse>

    /**
     * 전체 읽음 처리
     * PATCH /api/v1/notifications/read
     */
    @PATCH("/api/v1/notifications/read")
    suspend fun markAllAsRead(): Response<Unit>
}