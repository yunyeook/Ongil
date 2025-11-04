package kr.co.ongil.data.repository.fake

//import kr.co.ongil.data.datasource.remote.RetrofitClient
//import kr.co.ongil.data.datasource.remote.api.NotificationApi
//import kr.co.ongil.data.model.notification.NotificationDto
//import kr.co.ongil.data.model.notification.PageInfo
//import kr.co.ongil.data.util.ErrorHandler
//import kr.co.ongil.domain.repository.NotificationRepository
import kr.co.ongil.data.model.notification.NotificationDto
import kr.co.ongil.data.model.notification.PageInfo
import kr.co.ongil.domain.repository.NotificationRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * 알림 Repository 구현체
 */
class FakeNotificationRepositoryImpl : NotificationRepository {

    override suspend fun getNotifications(
        page: Int,
        size: Int
    ): Result<Pair<List<NotificationDto>, PageInfo>> {
        val now = System.currentTimeMillis()
        val sample = listOf(
            NotificationDto(
                notificationId = 1L,
                type = "SAFEZONE_EXIT",
                title = "안전구역 이탈 감지",
                content = "보호 대상자가 지정된 안전 구역에서 벗어났습니다.",
                createdAt = toUtcIso(now - 10 * 60 * 1000), // 10분 전
                isRead = false
            ),
            NotificationDto(
                notificationId = 2L,
                type = "RELATIONSHIP_REGIST",
                title = "관계 등록 완료",
                content = "보호자-대상자 관계가 등록되었습니다.",
                createdAt = toUtcIso(now - 2 * 60 * 60 * 1000), // 2시간 전
                isRead = true
            ),
            NotificationDto(
                notificationId = 3L,
                type = "NAVIGATION_START",
                title = "길안내 시작",
                content = "목적지까지 길안내가 시작되었습니다.",
                createdAt = toUtcIso(now),
                isRead = false
            )
        )

        val pageInfo = PageInfo(
            totalElements = sample.size, // Int
            totalPages   = 1,
            isLast       = true,
            currPage     = page
        )

        return Result.success(sample to pageInfo)

    }

    private fun toUtcIso(ms: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        return sdf.format(Date(ms))
    }
}


//class NotificationRepositoryImpl(
//    private val notificationApi: NotificationApi = RetrofitClient.notificationApi
//    // TODO: TokenManager 추가하여 accessToken 자동으로 가져오기
//    // TODO: DI(Hilt/Koin)로 주입하도록 변경
//) : NotificationRepository {
//
//    override suspend fun getNotifications(page: Int, size: Int): Result<Pair<List<NotificationDto>, PageInfo>> {
//        return try {
//            // TODO: TokenManager에서 accessToken 가져오기
//            val accessToken = "Bearer YOUR_ACCESS_TOKEN"
//
//            // API 호출
//            val response = notificationApi.getNotifications(
//                accessToken = accessToken,
//                page = page,
//                size = size
//            )
//
//            if (!response.success) {
//                throw Exception(response.message)
//            }
//
//            Result.success(
//                Pair(response.data.notifications, response.data.pageInfo)
//            )
//        } catch (e: Exception) {
//            // HTTP 에러를 ApiException으로 변환
//            val apiException = ErrorHandler.handleException(e)
//            Result.failure(apiException)
//        }
//    }
//}
