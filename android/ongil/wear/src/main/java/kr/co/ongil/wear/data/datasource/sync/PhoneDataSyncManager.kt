package kr.co.ongil.wear.data.datasource.sync

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kr.co.ongil.wear.data.model.WearLoginData
import kr.co.ongil.wear.data.model.WearNavigationData
import kr.co.ongil.wear.domain.model.PatientInfo
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 환자 정보 (워치 수신용)
 */
@Serializable
data class WearPatientInfo(
    val patientId: Long,
    val name: String,
    val relationship: String? = null,
    val phoneNumber: String? = null
)

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
        const val NAVIGATION_ROUTE_PATH = "/navigation_route"
        const val HELP_REQUEST_PATH = "/help_request_to_watch"
        const val PATIENT_LIST_PATH = "/patient_list"

        // Login Data Map Keys (폰과 동일해야 함)
        const val KEY_ACCESS_TOKEN = "access_token"
        const val KEY_REFRESH_TOKEN = "refresh_token"
        const val KEY_USER_ID = "user_id"
        const val KEY_USER_TYPE = "user_type"
        const val KEY_SELECTED_PATIENT_ID = "selected_patient_id"

        // Navigation Route Data Map Keys (폰과 동일해야 함)
        const val KEY_NAVIGATION_ID = "navigation_id"
        const val KEY_START_NAME = "start_name"
        const val KEY_END_NAME = "end_name"
        const val KEY_TOTAL_DISTANCE = "total_distance"
        const val KEY_TOTAL_TIME = "total_time"
        const val KEY_ROUTE_PATH = "route_path"

        // Help Request Data Map Keys (폰과 동일해야 함)
        const val KEY_HELP_MESSAGE = "help_message"
        const val KEY_TIMESTAMP = "timestamp"

        // Patient List Data Map Keys (폰과 동일해야 함)
        const val KEY_PATIENT_LIST = "patient_list"
    }

    private val dataClient: DataClient = Wearable.getDataClient(context)

    // 데이터 변경 리스너 (외부에서 설정)
    private var onLoginDataReceived: ((WearLoginData) -> Unit)? = null
    private var onNavigationRouteReceived: ((WearNavigationData) -> Unit)? = null
    private var onHelpRequestReceived: ((String) -> Unit)? = null
    private var onPatientListReceived: ((List<PatientInfo>) -> Unit)? = null

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
     * 네비게이션 경로 데이터 수신 콜백 설정
     *
     * @param listener 경로 데이터 받았을 때 실행할 함수
     */
    fun setOnNavigationRouteReceivedListener(listener: (WearNavigationData) -> Unit) {
        onNavigationRouteReceived = listener
    }

    /**
     * 도움 요청 수신 콜백 설정 (Phone → Watch)
     *
     * @param listener 도움 요청 메시지 받았을 때 실행할 함수
     */
    fun setOnHelpRequestReceivedListener(listener: (String) -> Unit) {
        onHelpRequestReceived = listener
    }

    /**
     * 환자 목록 수신 콜백 설정 (Phone → Watch)
     *
     * @param listener 환자 목록 받았을 때 실행할 함수
     */
    fun setOnPatientListReceivedListener(listener: (List<PatientInfo>) -> Unit) {
        onPatientListReceived = listener
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

                // LOGIN_DATA_PATH 경로의 데이터 처리
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
                // NAVIGATION_ROUTE_PATH 경로의 데이터 처리
                else if (dataItem.uri.path == NAVIGATION_ROUTE_PATH) {
                    try {
                        // DataItem → DataMap 변환
                        val dataMap = DataMapItem.fromDataItem(dataItem).dataMap

                        // DataMap에서 값 추출
                        val navigationId = dataMap.getString(KEY_NAVIGATION_ID) ?: ""
                        val startName = dataMap.getString(KEY_START_NAME) ?: ""
                        val endName = dataMap.getString(KEY_END_NAME) ?: ""
                        val totalDistance = dataMap.getInt(KEY_TOTAL_DISTANCE, 0)
                        val totalTime = dataMap.getInt(KEY_TOTAL_TIME, 0)
                        val routePath = dataMap.getString(KEY_ROUTE_PATH) ?: ""

                        // WearNavigationData 객체 생성
                        val navigationData = WearNavigationData(
                            navigationId = navigationId,
                            startLocationName = startName,
                            endLocationName = endName,
                            totalDistanceMeters = totalDistance,
                            totalTimeMinutes = totalTime,
                            routePath = routePath
                        )

                        Log.d(TAG, "네비게이션 경로 수신: $navigationId, $startName → $endName")

                        // 콜백 실행
                        onNavigationRouteReceived?.invoke(navigationData)

                    } catch (e: Exception) {
                        Log.e(TAG, "네비게이션 데이터 파싱 에러", e)
                    }
                }
                // HELP_REQUEST_PATH 경로의 데이터 처리 (Phone → Watch)
                else if (dataItem.uri.path == HELP_REQUEST_PATH) {
                    try {
                        // DataItem → DataMap 변환
                        val dataMap = DataMapItem.fromDataItem(dataItem).dataMap

                        // DataMap에서 값 추출
                        val helpMessage = dataMap.getString(KEY_HELP_MESSAGE) ?: "도와주세요!"
                        val timestamp = dataMap.getLong(KEY_TIMESTAMP, System.currentTimeMillis())

                        Log.d(TAG, "도움 요청 수신 (Phone → Watch): message=$helpMessage, timestamp=$timestamp")

                        // 콜백 실행 (HelpRequestViewModel에서 TTS 재생)
                        onHelpRequestReceived?.invoke(helpMessage)

                    } catch (e: Exception) {
                        Log.e(TAG, "도움 요청 데이터 파싱 에러", e)
                    }
                }
                // PATIENT_LIST_PATH 경로의 데이터 처리 (Phone → Watch)
                else if (dataItem.uri.path == PATIENT_LIST_PATH) {
                    try {
                        // DataItem → DataMap 변환
                        val dataMap = DataMapItem.fromDataItem(dataItem).dataMap

                        // DataMap에서 JSON 문자열 추출
                        val patientListJson = dataMap.getString(KEY_PATIENT_LIST) ?: "[]"

                        // JSON 문자열을 List<WearPatientInfo>로 역직렬화
                        val wearPatients = Json.decodeFromString<List<WearPatientInfo>>(patientListJson)

                        // WearPatientInfo를 PatientInfo로 변환
                        val patients = wearPatients.map { wearPatient ->
                            PatientInfo(
                                patientId = wearPatient.patientId,
                                name = wearPatient.name,
                                relationship = wearPatient.relationship,
                                phoneNumber = wearPatient.phoneNumber
                            )
                        }

                        Log.d(TAG, "환자 목록 수신 (Phone → Watch): ${patients.size}명")

                        // 콜백 실행
                        onPatientListReceived?.invoke(patients)

                    } catch (e: Exception) {
                        Log.e(TAG, "환자 목록 데이터 파싱 에러", e)
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
