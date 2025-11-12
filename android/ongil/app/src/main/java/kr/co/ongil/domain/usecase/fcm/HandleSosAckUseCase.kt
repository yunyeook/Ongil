package kr.co.ongil.domain.usecase.fcm

import android.content.Context
import android.util.Log
import kr.co.ongil.domain.helper.NotificationHelper
import kr.co.ongil.domain.model.FcmMessage
import javax.inject.Inject

// SOS ACK 알림 처리 (보호자가 환자의 SOS 재생 완료를 알림받음)
class HandleSosAckUseCase @Inject constructor(
    private val notificationHelper: NotificationHelper
) {
    operator fun invoke(context: Context, message: FcmMessage) {
        Log.d("HandleSosAckUseCase", "✅ SOS ACK 수신 - ${message.title}: ${message.content}")

        // 보호자에게 환자가 SOS를 재생했다는 알림 표시
        notificationHelper.showNotification(context, message)
    }
}
