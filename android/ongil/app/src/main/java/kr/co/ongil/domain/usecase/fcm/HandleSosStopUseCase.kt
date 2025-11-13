package kr.co.ongil.domain.usecase.fcm

import android.content.Context
import android.util.Log
import kr.co.ongil.domain.handler.SosActionHandler
import kr.co.ongil.domain.model.FcmMessage
import javax.inject.Inject

// SOS 알림 중지 처리
class HandleSosStopUseCase @Inject constructor(
    private val sosActionHandler: SosActionHandler
) {
    operator fun invoke(context: Context, message: FcmMessage) {
        Log.d("HandleSosStopUseCase", "🛑 SOS 중지 요청 수신")
        sosActionHandler.stopSosAction()
    }
}
