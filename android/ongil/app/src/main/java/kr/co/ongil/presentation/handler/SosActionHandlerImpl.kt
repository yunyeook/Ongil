package kr.co.ongil.presentation.handler

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import kr.co.ongil.R
import kr.co.ongil.domain.handler.SosActionHandler
import kr.co.ongil.domain.model.FcmMessage
import javax.inject.Inject

class SosActionHandlerImpl @Inject constructor() : SosActionHandler {

    private var mediaPlayer: MediaPlayer? = null

    override fun startSosAction(context: Context, message: FcmMessage) {
        Log.d("SosActionHandlerImpl", "🚨 SOS 액션 시작: ${message.type}")

        // 기존 사운드가 재생 중이면 중지
        stopSound()

        // SOS 사운드 무한 재생
        playSosSound(context)
    }

    override fun stopSosAction() {
        Log.d("SosActionHandlerImpl", "🛑 SOS 액션 중지")
        stopSound()
    }

    /**
     * SOS 알림 사운드 재생
     */
    private fun playSosSound(context: Context) {
        try {
            // res/raw/sos_alert 파일을 찾아서 재생
            // 파일이 없으면 기본 알림음 재생
            val soundResId = try {
                R.raw::class.java.getField("sos_alert").getInt(null)
            } catch (e: Exception) {
                Log.w("SosActionHandlerImpl", "⚠️ sos_alert.mp3 파일이 없습니다. 기본 알림음을 사용합니다.")
                // 기본 알림음 사용 (RingtoneManager 사용 가능)
                playDefaultNotificationSound(context)
                return
            }

            mediaPlayer = MediaPlayer.create(context, soundResId).apply {
                isLooping = true // 무한 반복

                setOnErrorListener { _, what, extra ->
                    Log.e("SosActionHandlerImpl", "❌ 사운드 재생 오류: what=$what, extra=$extra")
                    release()
                    mediaPlayer = null
                    true
                }

                // 볼륨 설정 (최대)
                setVolume(1.0f, 1.0f)

                // 재생 시작
                start()
                Log.d("SosActionHandlerImpl", "🔊 SOS 사운드 무한 재생 시작")
            }
        } catch (e: Exception) {
            Log.e("SosActionHandlerImpl", "❌ 사운드 재생 실패: ${e.message}", e)
        }
    }

    /**
     * 기본 알림음 재생
     */
    private fun playDefaultNotificationSound(context: Context) {
        try {
            val notification = android.media.RingtoneManager.getDefaultUri(
                android.media.RingtoneManager.TYPE_NOTIFICATION
            )
            val ringtone = android.media.RingtoneManager.getRingtone(context, notification)
            ringtone.play()
            Log.d("SosActionHandlerImpl", "🔔 기본 알림음 재생")
        } catch (e: Exception) {
            Log.e("SosActionHandlerImpl", "❌ 기본 알림음 재생 실패: ${e.message}", e)
        }
    }

    /**
     * 사운드 중지
     */
    private fun stopSound() {
        mediaPlayer?.let {
            try {
                Log.d("SosActionHandlerImpl", "🛑 사운드 중지 시작 - isPlaying: ${it.isPlaying}, isLooping: ${it.isLooping}")

                // 루프 먼저 중지
                it.isLooping = false

                if (it.isPlaying) {
                    it.stop()
                    Log.d("SosActionHandlerImpl", "✅ 사운드 stop() 호출 완료")
                }

                it.reset()
                Log.d("SosActionHandlerImpl", "✅ 사운드 reset() 호출 완료")

                it.release()
                Log.d("SosActionHandlerImpl", "✅ 사운드 release() 호출 완료")

                mediaPlayer = null
                Log.d("SosActionHandlerImpl", "✅ mediaPlayer null 설정 완료")
            } catch (e: Exception) {
                Log.e("SosActionHandlerImpl", "❌ 사운드 중지 실패: ${e.message}", e)
                mediaPlayer = null
            }
        } ?: run {
            Log.w("SosActionHandlerImpl", "⚠️ mediaPlayer가 null이어서 중지할 수 없습니다.")
        }
    }
}