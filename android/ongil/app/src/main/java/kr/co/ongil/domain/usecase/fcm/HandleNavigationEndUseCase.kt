package kr.co.ongil.domain.usecase.fcm

import android.content.Context
import kr.co.ongil.domain.helper.NotificationHelper
import kr.co.ongil.domain.model.FcmMessage
import javax.inject.Inject

// 길안내 종료 알림 처리
class HandleNavigationEndUseCase @Inject constructor(
    private val notificationHelper: NotificationHelper
) {
    operator fun invoke(context: Context, message: FcmMessage) {
        notificationHelper.showNotification(context, message)
    }
}
