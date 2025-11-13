package kr.co.ongil.domain.usecase.fcm

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kr.co.ongil.domain.handler.SosActionHandler
import kr.co.ongil.domain.model.FcmMessage
import kr.co.ongil.domain.usecase.sosalert.AckSosAlertUseCase
import javax.inject.Inject

// SOS 알림 처리 (음성 재생 등)
class HandleSosUseCase @Inject constructor(
    private val sosActionHandler: SosActionHandler,
    private val ackSosAlertUseCase: AckSosAlertUseCase
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    operator fun invoke(context: Context, message: FcmMessage) {
        // SOS 사운드 재생 시작
        sosActionHandler.startSosAction(context, message)

        // 서버에 ACK 전송 (비동기)
        message.relatedTableId?.toLongOrNull()?.let { sosId ->
            scope.launch {
                ackSosAlertUseCase(sosId)
                    .onSuccess {
                        Log.d("HandleSosUseCase", "✅ SOS ACK 전송 성공 - sosId: $sosId")
                    }
                    .onFailure { error ->
                        Log.e("HandleSosUseCase", "❌ SOS ACK 전송 실패: ${error.message}")
                    }
            }
        } ?: run {
            Log.w("HandleSosUseCase", "⚠️ sosId가 없어서 ACK를 전송하지 않습니다.")
        }
    }
}