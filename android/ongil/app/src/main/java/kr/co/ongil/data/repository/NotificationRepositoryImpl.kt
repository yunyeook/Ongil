package kr.co.ongil.data.repository

import android.util.Log
import kr.co.ongil.data.datasource.remote.api.NotificationApi
import kr.co.ongil.data.model.notification.NotificationDto
import kr.co.ongil.data.model.notification.PageInfo
import kr.co.ongil.data.util.ErrorHandler
import kr.co.ongil.domain.repository.NotificationRepository
import javax.inject.Inject

/**
 * 알림 Repository 구현체
 */
class NotificationRepositoryImpl @Inject constructor(
    private val notificationApi: NotificationApi
) : NotificationRepository {

    override suspend fun getNotifications(
        page: Int,
        size: Int,
        read: Boolean?
    ): Result<Pair<List<NotificationDto>, PageInfo>> {
        return try {
            Log.d("NotificationRepository", "알림 목록 조회 시작 - page: $page, size: $size, read: $read")

            // API 호출 (AuthInterceptor가 자동으로 Authorization 헤더 추가)
            val response = notificationApi.getNotifications(
                page = page,
                size = size,
                read = read
            )

            Log.d("NotificationRepository", "응답 코드: ${response.code()}")
            Log.d("NotificationRepository", "응답 성공 여부: ${response.isSuccessful}")

            if (!response.isSuccessful) {
                val errorBody = response.errorBody()?.string()
                Log.e("NotificationRepository", "에러 응답: $errorBody")
            }

            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                val notifications = body.data.notifications
                val pageInfo = body.data.pageInfo

                Log.d("NotificationRepository", "알림 ${notifications.size}개 조회 성공")
                Result.success(Pair(notifications, pageInfo))
            } else {
                // 에러 응답 처리
                val exception = ErrorHandler.handleException(
                    retrofit2.HttpException(response)
                )
                Log.e("NotificationRepository", "알림 조회 실패: ${exception.message}")
                Result.failure(exception)
            }
        } catch (e: Exception) {
            // HTTP 에러를 ApiException으로 변환
            Log.e("NotificationRepository", "알림 조회 예외 발생", e)
            val apiException = ErrorHandler.handleException(e)
            Result.failure(apiException)
        }
    }

    override suspend fun markAllAsRead(): Result<Unit> {
        return try {
            Log.d("NotificationRepository", "전체 읽음 처리 시작")

            val response = notificationApi.markAllAsRead()

            Log.d("NotificationRepository", "응답 코드: ${response.code()}")

            if (response.isSuccessful) {
                Log.d("NotificationRepository", "전체 읽음 처리 성공")
                Result.success(Unit)
            } else {
                val exception = ErrorHandler.handleException(
                    retrofit2.HttpException(response)
                )
                Log.e("NotificationRepository", "전체 읽음 처리 실패: ${exception.message}")
                Result.failure(exception)
            }
        } catch (e: Exception) {
            Log.e("NotificationRepository", "전체 읽음 처리 예외 발생", e)
            val apiException = ErrorHandler.handleException(e)
            Result.failure(apiException)
        }
    }
}
