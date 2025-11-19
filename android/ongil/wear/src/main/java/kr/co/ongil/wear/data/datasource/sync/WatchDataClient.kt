package kr.co.ongil.wear.data.datasource.sync

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 워치에서 폰으로 메시지 전송 클라이언트
 *
 * Watch → Phone 단방향 메시지 전송
 * - MessageClient 사용 (DataClient는 Phone → Watch만 사용)
 */
@Singleton
class WatchDataClient @Inject constructor(
    private val context: Context
) {
    companion object {
        private const val TAG = "WatchDataClient"

        // 메시지 경로 (Phone의 WearMessageListenerService와 동일해야 함)
        private const val SELECTED_PATIENT_PATH = "/selected_patient"
    }

    private val messageClient: MessageClient = Wearable.getMessageClient(context)

    /**
     * 선택한 환자 ID를 Phone 앱으로 전송
     *
     * @param patientId 선택한 환자 ID
     */
    suspend fun sendSelectedPatientId(patientId: Long): Boolean {
        return try {
            Log.d(TAG, "Phone 앱으로 선택한 환자 ID 전송 시작: $patientId")

            // 연결된 노드 (Phone) 목록 가져오기
            val nodeClient = Wearable.getNodeClient(context)
            val nodes = nodeClient.connectedNodes.await()

            if (nodes.isEmpty()) {
                Log.w(TAG, "연결된 Phone이 없습니다")
                return false
            }

            // 각 노드(Phone)에 메시지 전송
            var successCount = 0
            nodes.forEach { node ->
                try {
                    messageClient.sendMessage(
                        node.id,
                        SELECTED_PATIENT_PATH,
                        patientId.toString().toByteArray()
                    ).await()

                    successCount++
                    Log.d(TAG, "Phone 앱으로 환자 ID 전송 성공: nodeId=${node.id}, patientId=$patientId")
                } catch (e: Exception) {
                    Log.e(TAG, "Phone 앱으로 환자 ID 전송 실패: nodeId=${node.id}", e)
                }
            }

            successCount > 0

        } catch (e: Exception) {
            Log.e(TAG, "Phone 앱으로 환자 ID 전송 실패", e)
            false
        }
    }
}
