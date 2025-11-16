package kr.co.ongil.wear.data.datasource.sync

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.tasks.await
import kr.co.ongil.wear.data.model.WearLoginData
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 폰에서 워치로 데이터 수신 관리자
 *
 * 스프링의 WebSocket/SSE 리스너와 비슷
 * - 폰이 보낸 데이터를 받음
 * - DataClient.OnDataChangedListener 구현
 */
@Singleton
class PhoneDataSyncManager @Inject constructor(
    private val context: Context
) : DataClient.OnDataChangedListener {

    companion object {
        private const val TAG = "PhoneDataSyncManager"

        // Data Layer에서 사용할 경로 (Key)
        // 폰과 워치가 동일한 경로 사용해야 함
        const val LOGIN_DATA_PATH = "/login_data"

        // Data Map Keys (폰과 동일해야 함)
        const val KEY_ACCESS_TOKEN = "access_token"
        const val KEY_REFRESH_TOKEN = "refresh_token"
        const val KEY_USER_ID = "user_id"
        const val KEY_USER_TYPE = "user_type"
        const val KEY_SELECTED_PATIENT_ID = "selected_patient_id"
    }

    private val dataClient: DataClient = Wearable.getDataClient(context)

    // 데이터 변경 리스너 (외부에서 설정)
    private var onLoginDataReceived: ((WearLoginData) -> Unit)? = null

    /**
     * 데이터 수신 리스너 시작
     *
     * Activity/ViewModel에서 호출
     */
    fun startListening() {
        dataClient.addListener(this)
        Log.d(TAG, "데이터 리스닝 시작")
    }

    /**
     * 데이터 수신 리스너 중지
     */
    fun stopListening() {
        dataClient.removeListener(this)
        Log.d(TAG, "데이터 리스닝 중지")
    }

    /**
     * 로그인 데이터 수신 콜백 설정
     *
     * @param listener 데이터 받았을 때 실행할 함수
     */
    fun setOnLoginDataReceivedListener(listener: (WearLoginData) -> Unit) {
        onLoginDataReceived = listener
    }

    /**
     * DataClient.OnDataChangedListener 구현
     *
     * 폰에서 데이터가 변경되면 자동으로 호출됨
     * 스프링의 @EventListener와 비슷
     */
    override fun onDataChanged(dataEvents: DataEventBuffer) {
        Log.d(TAG, "데이터 변경 감지")

        dataEvents.forEach { event ->
            // 데이터 추가/변경 이벤트만 처리
            if (event.type == DataEvent.TYPE_CHANGED) {
                val dataItem = event.dataItem

                // LOGIN_DATA_PATH 경로의 데이터만 처리
                if (dataItem.uri.path == LOGIN_DATA_PATH) {
                    try {
                        // DataItem → DataMap 변환
                        val dataMap = DataMapItem.fromDataItem(dataItem).dataMap

                        // DataMap에서 값 추출
                        val accessToken = dataMap.getString(KEY_ACCESS_TOKEN) ?: ""
                        val refreshToken = dataMap.getString(KEY_REFRESH_TOKEN) ?: ""
                        val userId = dataMap.getString(KEY_USER_ID) ?: ""
                        val userType = dataMap.getString(KEY_USER_TYPE) ?: ""
                        val selectedPatientId = dataMap.getString(KEY_SELECTED_PATIENT_ID)

                        // WearLoginData 객체 생성
                        val loginData = WearLoginData(
                            accessToken = accessToken,
                            refreshToken = refreshToken,
                            userId = userId,
                            userType = userType,
                            selectedPatientId = selectedPatientId
                        )

                        Log.d(TAG, "로그인 데이터 수신: userId=$userId, userType=$userType")

                        // 콜백 실행
                        onLoginDataReceived?.invoke(loginData)

                    } catch (e: Exception) {
                        Log.e(TAG, "데이터 파싱 에러", e)
                    }
                }
            }
        }
    }

    /**
     * 현재 저장된 데이터 수동으로 가져오기
     *
     * 앱 시작 시 한 번 호출 (폰에서 이미 보낸 데이터 확인)
     */
    /**
     * 현재 저장된 데이터 수동으로 가져오기
     *
     * 앱 시작 시 한 번 호출 (폰에서 이미 보낸 데이터 확인)
     */
    suspend fun fetchCurrentLoginData(): WearLoginData? {
        return try {
            // getDataItems()를 인자 없이 호출 (모든 데이터 가져오기)
            val dataItems = dataClient.getDataItems().await()

            // LOGIN_DATA_PATH와 일치하는 항목 찾기
            var result: WearLoginData? = null

            for (i in 0 until dataItems.count) {
                val dataItem = dataItems.get(i)

                // path 확인
                if (dataItem.uri.path == LOGIN_DATA_PATH) {
                    val dataMap = DataMapItem.fromDataItem(dataItem).dataMap

                    result = WearLoginData(
                        accessToken = dataMap.getString(KEY_ACCESS_TOKEN) ?: "",
                        refreshToken = dataMap.getString(KEY_REFRESH_TOKEN) ?: "",
                        userId = dataMap.getString(KEY_USER_ID) ?: "",
                        userType = dataMap.getString(KEY_USER_TYPE) ?: "",
                        selectedPatientId = dataMap.getString(KEY_SELECTED_PATIENT_ID)
                    )
                    break
                }
            }

            // DataItemBuffer 닫기 (메모리 누수 방지)
            dataItems.release()

            result

        } catch (e: Exception) {
            Log.e(TAG, "데이터 가져오기 에러", e)
            null
        }
    }
}
