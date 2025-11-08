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
    private var playCount = 0
    private val maxPlayCount = 100

    override fun startSosAction(context: Context, message: FcmMessage) {
        Log.d("SosActionHandlerImpl", "🚨 SOS 액션 시작: ${message.type}")

        // 기존 사운드가 재생 중이면 중지
        stopSound()

        // 재생 카운트 초기화
        playCount = 0

        // SOS 사운드 재생
        playSosSound(context)

        // TODO: 추가 SOS 액션 (진동, 화면 표시 등)
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
                setOnCompletionListener {
                    playCount++
                    Log.d("SosActionHandlerImpl", "🔊 SOS 사운드 재생 완료 ($playCount/$maxPlayCount)")

                    if (playCount < maxPlayCount) {
                        // 다시 재생
                        seekTo(0)
                        start()
                    } else {
                        // 100번 재생 완료
                        Log.d("SosActionHandlerImpl", "✅ SOS 사운드 100번 재생 완료")
                        release()
                        mediaPlayer = null
                    }
                }

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
                Log.d("SosActionHandlerImpl", "🔊 SOS 사운드 재생 시작")
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
            if (it.isPlaying) {
                it.stop()
                Log.d("SosActionHandlerImpl", "🛑 기존 사운드 중지")
            }
            it.release()
            mediaPlayer = null
        }
    }
}