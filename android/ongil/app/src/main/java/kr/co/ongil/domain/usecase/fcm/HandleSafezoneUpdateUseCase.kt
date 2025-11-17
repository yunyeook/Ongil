package kr.co.ongil.domain.usecase.fcm

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kr.co.ongil.domain.helper.NotificationHelper
import kr.co.ongil.domain.model.FcmMessage
import kr.co.ongil.domain.usecase.safezone.GetAndSaveSafeZoneUseCase
import javax.inject.Inject

class HandleSafezoneUpdateUseCase @Inject constructor(
    private val notificationHelper: NotificationHelper,
    private val getAndSaveSafeZoneUseCase: GetAndSaveSafeZoneUseCase
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    operator fun invoke(context: Context, message: FcmMessage) {

        notificationHelper.showNotification(context, message)

        scope.launch {
            val patientId = message.receiverId.toLongOrNull()

            if (patientId != null) {
                val result = getAndSaveSafeZoneUseCase(patientId)
            } else {
                Log.e("HandleSafezoneUpdate", "❌ receiverId를 Long으로 변환 실패: ${message.receiverId}")
            }
        }
    }
}