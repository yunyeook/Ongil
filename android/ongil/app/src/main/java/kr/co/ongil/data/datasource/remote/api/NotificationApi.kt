package kr.co.ongil.data.datasource.remote.api
import kr.co.ongil.data.model.notification.NotificationResponse
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.Query
interface NotificationApi {

    /**
     * 알림 목록 조회
     * GET /api/v1/notifications?page={page}&size={size}
     * 반환 타입: kr.co.ongil.data.model.notification.NotificationResponse
     */
    @GET("/api/v1/notifications")
    suspend fun getNotifications(
        @Header("Authorization") accessToken: String,
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 20
    ): NotificationResponse

    /**
     * (명세서) 전체 읽음 처리
     * PATCH /api/v1/notifications/read
     * 지금은 ViewModel에서 로컬로 처리 중이라 필수는 아니지만,
     * 나중에 실제 API로 바꿀 계획이면 미리 선언해두면 좋아요.
     */
    @PATCH("/api/v1/notifications/read")
    suspend fun markAllAsRead(
        @Header("Authorization") accessToken: String
    ): retrofit2.Response<Unit> // 서버가 message만 주면 Unit/204로 받아도 OK
}