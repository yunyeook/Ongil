package kr.co.ongil.data.datasource.wear

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 환자 정보 (워치 전송용)
 */
@Serializable
data class WearPatientInfo(
    val patientId: Long,
    val name: String,
    val relationship: String? = null,
    val phoneNumber: String? = null
)

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

        private const val LOCATION_DATA_PATH = "/location_data"
        private const val KEY_LATITUDE = "latitude"
        private const val KEY_LONGITUDE = "longitude"

        // 네비게이션 경로 데이터
        private const val NAVIGATION_ROUTE_PATH = "/navigation_route"
        private const val KEY_NAVIGATION_ID = "navigation_id"
        private const val KEY_START_NAME = "start_name"
        private const val KEY_END_NAME = "end_name"
        private const val KEY_TOTAL_DISTANCE = "total_distance"
        private const val KEY_TOTAL_TIME = "total_time"
        private const val KEY_ROUTE_PATH = "route_path" // JSON 문자열

        // 환자 목록 데이터 (보호자용)
        private const val PATIENT_LIST_PATH = "/patient_list"
        private const val KEY_PATIENT_LIST = "patient_list" // JSON 문자열
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

    /**
     * 현재 위치를 워치로 전송
     *
     * @param latitude 위도
     * @param longitude 경도
     */
    suspend fun syncLocation(
        latitude: Double,
        longitude: Double
    ): Boolean {
        return try {
            // PutDataMapRequest 생성
            val putDataReq = PutDataMapRequest.create(LOCATION_DATA_PATH).apply {
                dataMap.apply {
                    putDouble(KEY_LATITUDE, latitude)
                    putDouble(KEY_LONGITUDE, longitude)
                    putLong("timestamp", System.currentTimeMillis())
                }
            }

            // 데이터 전송
            val putDataTask = dataClient.putDataItem(putDataReq.asPutDataRequest())
            putDataTask.await()

            Log.d(TAG, "워치로 위치 전송 성공: lat=$latitude, lon=$longitude")
            true

        } catch (e: Exception) {
            Log.e(TAG, "워치로 위치 전송 실패", e)
            false
        }
    }

    /**
     * 네비게이션 경로 데이터를 워치로 전송
     *
     * @param navigationId 네비게이션 ID
     * @param startLocationName 출발지 이름
     * @param endLocationName 목적지 이름
     * @param totalDistanceMeters 총 거리 (미터)
     * @param totalTimeMinutes 예상 소요 시간 (분)
     * @param routePath 경로 좌표 리스트 (JSON 문자열)
     */
    suspend fun syncNavigationRoute(
        navigationId: String,
        startLocationName: String,
        endLocationName: String,
        totalDistanceMeters: Int,
        totalTimeMinutes: Int,
        routePath: String // JSON 형식: [{"lat":37.5,"lon":127.0},...]
    ): Boolean {
        return try {
            Log.d(TAG, "워치로 경로 데이터 전송 시작: $endLocationName")

            val putDataReq = PutDataMapRequest.create(NAVIGATION_ROUTE_PATH).apply {
                dataMap.apply {
                    putString(KEY_NAVIGATION_ID, navigationId)
                    putString(KEY_START_NAME, startLocationName)
                    putString(KEY_END_NAME, endLocationName)
                    putInt(KEY_TOTAL_DISTANCE, totalDistanceMeters)
                    putInt(KEY_TOTAL_TIME, totalTimeMinutes)
                    putString(KEY_ROUTE_PATH, routePath)
                    putLong("timestamp", System.currentTimeMillis())
                }
            }

            val putDataTask = dataClient.putDataItem(putDataReq.asPutDataRequest())
            putDataTask.await()

            Log.d(TAG, "워치로 경로 데이터 전송 성공: $endLocationName")
            true

        } catch (e: Exception) {
            Log.e(TAG, "워치로 경로 데이터 전송 실패", e)
            false
        }
    }

    /**
     * 네비게이션 경로 초기화 (워치)
     */
    suspend fun clearNavigationRoute(): Boolean {
        return try {
            Log.d(TAG, "워치 경로 데이터 삭제 시작")

            val putDataReq = PutDataMapRequest.create(NAVIGATION_ROUTE_PATH).apply {
                dataMap.apply {
                    putString(KEY_NAVIGATION_ID, "")
                    putString(KEY_START_NAME, "")
                    putString(KEY_END_NAME, "")
                    putInt(KEY_TOTAL_DISTANCE, 0)
                    putInt(KEY_TOTAL_TIME, 0)
                    putString(KEY_ROUTE_PATH, "[]")
                    putLong("timestamp", System.currentTimeMillis())
                }
            }

            val putDataTask = dataClient.putDataItem(putDataReq.asPutDataRequest())
            putDataTask.await()

            Log.d(TAG, "워치 경로 데이터 삭제 성공")
            true

        } catch (e: Exception) {
            Log.e(TAG, "워치 경로 데이터 삭제 실패", e)
            false
        }
    }

    /**
     * 환자 목록을 워치로 전송 (보호자용)
     *
     * @param patients 환자 목록
     */
    suspend fun syncPatientList(patients: List<WearPatientInfo>): Boolean {
        return try {
            Log.d(TAG, "워치로 환자 목록 전송 시작: ${patients.size}명")

            // 환자 목록을 JSON 문자열로 직렬화
            val patientListJson = Json.encodeToString(patients)

            val putDataReq = PutDataMapRequest.create(PATIENT_LIST_PATH).apply {
                dataMap.apply {
                    putString(KEY_PATIENT_LIST, patientListJson)
                    putLong("timestamp", System.currentTimeMillis())
                }
            }

            val putDataTask = dataClient.putDataItem(putDataReq.asPutDataRequest())
            putDataTask.await()

            Log.d(TAG, "워치로 환자 목록 전송 성공: ${patients.size}명")
            true

        } catch (e: Exception) {
            Log.e(TAG, "워치로 환자 목록 전송 실패", e)
            false
        }
    }

    /**
     * 환자 목록 초기화 (워치)
     */
    suspend fun clearPatientList(): Boolean {
        return try {
            Log.d(TAG, "워치 환자 목록 삭제 시작")

            val putDataReq = PutDataMapRequest.create(PATIENT_LIST_PATH).apply {
                dataMap.apply {
                    putString(KEY_PATIENT_LIST, "[]")
                    putLong("timestamp", System.currentTimeMillis())
                }
            }

            val putDataTask = dataClient.putDataItem(putDataReq.asPutDataRequest())
            putDataTask.await()

            Log.d(TAG, "워치 환자 목록 삭제 성공")
            true

        } catch (e: Exception) {
            Log.e(TAG, "워치 환자 목록 삭제 실패", e)
            false
        }
    }
}