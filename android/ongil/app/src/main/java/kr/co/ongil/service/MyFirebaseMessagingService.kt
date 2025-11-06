package kr.co.ongil.service
//
//import android.app.NotificationChannel
//import android.app.NotificationManager
//import android.app.PendingIntent
//import android.content.Context
//import android.content.Intent
//import android.os.Build
//import android.util.Log
//import androidx.core.app.NotificationCompat
//import com.google.firebase.messaging.FirebaseMessagingService
//import com.google.firebase.messaging.RemoteMessage
//import java.io.IOException
//import kr.co.ongil.R
//import kr.co.ongil.presentation.MainActivity
//import okhttp3.Call
//import okhttp3.Callback
//import okhttp3.MediaType.Companion.toMediaType
//import okhttp3.OkHttpClient
//import okhttp3.Request
//import okhttp3.RequestBody.Companion.toRequestBody
//import okhttp3.Response
//import org.json.JSONObject
//
//class MyFirebaseMessagingService : FirebaseMessagingService() {
//
//    override fun onNewToken(token: String) {
//        super.onNewToken(token)
//        Log.d("FCM", "새로운 FCM 토큰: $token")
//        sendFcmTokenToServer(token)
//    }
//
//    override fun onMessageReceived(remoteMessage: RemoteMessage) {
//        Log.d("FCM", "📩 수신된 메시지: ${remoteMessage.data}")
//
//        remoteMessage.notification?.let {
//            sendNotification(it.title ?: "알림", it.body ?: "내용 없음")
//        }
//
//        if (remoteMessage.data.isNotEmpty()) {
//            val title = remoteMessage.data["title"] ?: "알림"
//            val body = remoteMessage.data["body"] ?: "내용 없음"
//            sendNotification(title, body)
//        }
//    }
//
//    private fun sendNotification(title: String, messageBody: String) {
//        val intent = Intent(this, MainActivity::class.java).apply {
//            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
//        }
//
//        val pendingIntent = PendingIntent.getActivity(
//            this, 0, intent,
//            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
//        )
//
//        val channelId = "default_channel"
//        val builder = NotificationCompat.Builder(this, channelId)
//            .setSmallIcon(R.drawable.ic_launcher_foreground)
//            .setContentTitle(title)
//            .setContentText(messageBody)
//            .setAutoCancel(true)
//            .setContentIntent(pendingIntent)
//
//        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val channel = NotificationChannel(
//                channelId, "기본 알림 채널", NotificationManager.IMPORTANCE_DEFAULT
//            )
//            manager.createNotificationChannel(channel)
//        }
//        manager.notify(0, builder.build())
//    }
//
//    private fun sendFcmTokenToServer(token: String) {
//        val userId = 1
//        val client = OkHttpClient()
//
//        val json = JSONObject().apply {
//            put("token", token)
//        }
//
//        val body = json.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
//        val request = Request.Builder()
//            .url("http://10.0.2.2:8080/api/v1/fcm/register")
//            .post(body)
//            .build()
//
//        client.newCall(request).enqueue(object : Callback {
//            override fun onFailure(call: Call, e: IOException) {
//                Log.e("FCM", "토큰 전송 실패: ${e.message}")
//            }
//
//            override fun onResponse(call: Call, response: Response) {
//                if (response.isSuccessful) Log.d("FCM", "서버에 FCM 토큰 전송 성공 ✅")
//                else Log.e("FCM", "서버 응답 오류 ❌ ${response.code}")
//            }
//        })
//    }
//}
