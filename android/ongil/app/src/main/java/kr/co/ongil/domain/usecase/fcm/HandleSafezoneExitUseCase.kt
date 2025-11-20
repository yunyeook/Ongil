package kr.co.ongil.domain.usecase.fcm

import android.content.Context
import kr.co.ongil.domain.helper.NotificationHelper
import kr.co.ongil.domain.model.FcmMessage
import javax.inject.Inject

// 안전구역 이탈 알림 처리 (긴급)
class HandleSafezoneExitUseCase @Inject constructor(
    private val notificationHelper: NotificationHelper
) {
    operator fun invoke(context: Context, message: FcmMessage) {
        notificationHelper.showUrgentNotification(context, message)
    }
}
