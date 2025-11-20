package kr.co.ongil.domain.helper

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import kr.co.ongil.R
import kr.co.ongil.domain.model.FcmMessage
import kr.co.ongil.domain.model.MessageType
import javax.inject.Inject

// FCM 알림 표시를 담당하는 Helper 클래스
class NotificationHelper @Inject constructor() {

    companion object {
        private const val DEFAULT_CHANNEL_ID = "fcm_default_channel"
        private const val URGENT_CHANNEL_ID = "fcm_urgent_channel"
        private const val DEFAULT_CHANNEL_NAME = "일반 알림"
        private const val URGENT_CHANNEL_NAME = "긴급 알림"
    }

    // 알림 채널 생성
    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // 일반 알림 채널
            val defaultChannel = NotificationChannel(
                DEFAULT_CHANNEL_ID,
                DEFAULT_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "일반 알림을 표시합니다"
            }

            // 긴급 알림 채널
            val urgentChannel = NotificationChannel(
                URGENT_CHANNEL_ID,
                URGENT_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "긴급 알림을 표시합니다"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500)
            }

            notificationManager.createNotificationChannel(defaultChannel)
            notificationManager.createNotificationChannel(urgentChannel)
        }
    }

    // 일반 알림 표시
    fun showNotification(context: Context, message: FcmMessage) {
        val intent = createIntent(context, message)
        val pendingIntent = PendingIntent.getActivity(
            context,
            message.notificationId?.toIntOrNull() ?: 0,
            intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, DEFAULT_CHANNEL_ID)
            .setSmallIcon(R.drawable.ongil_icon)
            .setContentTitle(message.title)
            .setContentText(message.content ?: "")
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(message.notificationId?.toIntOrNull() ?: 0, notification)
    }

    // 긴급 알림 표시
    fun showUrgentNotification(context: Context, message: FcmMessage) {
        val intent = createIntent(context, message)
        val pendingIntent = PendingIntent.getActivity(
            context,
            message.notificationId?.toIntOrNull() ?: 0,
            intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, URGENT_CHANNEL_ID)
            .setSmallIcon(R.drawable.ongil_icon)
            .setContentTitle("⚠️ ${message.title}")
            .setContentText(message.content ?: "즉시 확인이 필요합니다")
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(message.notificationId?.toIntOrNull() ?: 0, notification)
    }

    // Intent 생성 (알림 클릭 시 이동)
    private fun createIntent(context: Context, message: FcmMessage): Intent {
        return Intent(context, Class.forName("kr.co.ongil.presentation.MainActivity")).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra("notification_type", message.type.name)
            putExtra("notification_id", message.notificationId)
            putExtra("related_table_id", message.relatedTableId)

            // CALL_MISSED일 때 통화목록으로 이동
            if (message.type == MessageType.CALL_MISSED) {
                putExtra("navigate_to", "call_history")
            }
        }
    }
}
