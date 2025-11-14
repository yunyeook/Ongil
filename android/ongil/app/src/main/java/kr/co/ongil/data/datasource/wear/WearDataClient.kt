package kr.co.ongil.data.datasource.wear

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 앱(폰)에서 워치로 데이터 전송 클라이언트
 *
 * 스프링의 RestTemplate/WebClient와 비슷
 * - 워치로 데이터 전송
 * - Wearable Data Layer API 사용
 */
@Singleton
class WearDataClient @Inject constructor(
    private val context: Context
) {
    companion object {
        private const val TAG = "WearDataClient"

        // 워치의 PhoneDataSyncManager와 동일한 경로 사용
        private const val LOGIN_DATA_PATH = "/login_data"

        // Key 이름도 워치와 동일해야 함
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_TYPE = "user_type"
        private const val KEY_SELECTED_PATIENT_ID = "selected_patient_id"
    }

    private val dataClient: DataClient = Wearable.getDataClient(context)

    /**
     * 로그인 정보를 워치로 전송
     *
     * @param accessToken JWT 액세스 토큰
     * @param refreshToken JWT 리프레시 토큰
     * @param userId 사용자 ID
     * @param userType 사용자 타입 (PATIENT or GUARDIAN)
     * @param selectedPatientId 선택된 환자 ID (nullable)
     */
    suspend fun syncLoginData(
        accessToken: String,
        refreshToken: String,
        userId: String,
        userType: String,
        selectedPatientId: String? = null
    ): Boolean {
        return try {
            Log.d(TAG, "워치로 로그인 정보 전송 시작: userId=$userId")

            // PutDataMapRequest 생성 (데이터 전송 요청)
            val putDataReq = PutDataMapRequest.create(LOGIN_DATA_PATH).apply {
                dataMap.apply {
                    putString(KEY_ACCESS_TOKEN, accessToken)
                    putString(KEY_REFRESH_TOKEN, refreshToken)
                    putString(KEY_USER_ID, userId)
                    putString(KEY_USER_TYPE, userType)

                    // selectedPatientId는 null일 수 있음
                    if (selectedPatientId != null) {
                        putString(KEY_SELECTED_PATIENT_ID, selectedPatientId)
                    }

                    // 타임스탬프 추가 (데이터 변경 감지용)
                    putLong("timestamp", System.currentTimeMillis())
                }
            }

            // 데이터 전송
            val putDataTask = dataClient.putDataItem(putDataReq.asPutDataRequest())
            putDataTask.await()

            Log.d(TAG, "워치로 로그인 정보 전송 성공")
            true

        } catch (e: Exception) {
            Log.e(TAG, "워치로 로그인 정보 전송 실패", e)
            false
        }
    }

    /**
     * 워치의 로그인 정보 삭제 (로그아웃)
     */
    suspend fun clearLoginData(): Boolean {
        return try {
            Log.d(TAG, "워치 로그인 정보 삭제 시작")

            // 빈 데이터로 덮어쓰기
            val putDataReq = PutDataMapRequest.create(LOGIN_DATA_PATH).apply {
                dataMap.apply {
                    putString(KEY_ACCESS_TOKEN, "")
                    putString(KEY_REFRESH_TOKEN, "")
                    putString(KEY_USER_ID, "")
                    putString(KEY_USER_TYPE, "")
                    putString(KEY_SELECTED_PATIENT_ID, "")
                    putLong("timestamp", System.currentTimeMillis())
                }
            }

            val putDataTask = dataClient.putDataItem(putDataReq.asPutDataRequest())
            putDataTask.await()

            Log.d(TAG, "워치 로그인 정보 삭제 성공")
            true

        } catch (e: Exception) {
            Log.e(TAG, "워치 로그인 정보 삭제 실패", e)
            false
        }
    }
}