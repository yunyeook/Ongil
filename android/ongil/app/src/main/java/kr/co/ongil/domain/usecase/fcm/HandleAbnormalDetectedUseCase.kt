package kr.co.ongil.domain.usecase.fcm

import android.content.Context
import kr.co.ongil.domain.helper.NotificationHelper
import kr.co.ongil.domain.model.FcmMessage
import javax.inject.Inject

// 이상행동 감지 알림 처리 (긴급)
class HandleAbnormalDetectedUseCase @Inject constructor(
    private val notificationHelper: NotificationHelper
) {
    operator fun invoke(context: Context, message: FcmMessage) {
        notificationHelper.showUrgentNotification(context, message)
    }
}
