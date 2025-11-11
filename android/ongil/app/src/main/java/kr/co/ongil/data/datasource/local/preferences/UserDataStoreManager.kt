package kr.co.ongil.data.datasource.local.preferences

import kotlinx.coroutines.flow.Flow

/**
 * 사용자 정보 관리 인터페이스
 */
interface UserDataStoreManager {
    /**
     * AccessToken 저장
     */
    suspend fun saveAccessToken(token: String)

    /**
     * RefreshToken 저장
     */
    suspend fun saveRefreshToken(token: String)

    /**
     * AccessToken 불러오기
     */
    fun getAccessToken(): Flow<String?>

    /**
     * RefreshToken 불러오기
     */
    fun getRefreshToken(): Flow<String?>

    /**
     * 토큰 삭제 (로그아웃 시 사용)
     */
    suspend fun clearTokens()

    /**
     * AccessToken과 RefreshToken을 동시에 저장
     */
    suspend fun saveTokens(accessToken: String, refreshToken: String)

    /**
     * FCM 토큰 저장
     */
    suspend fun saveFcmToken(token: String)

    /**
     * FCM 토큰 불러오기
     */
    fun getFcmToken(): Flow<String?>

    fun getLoginUserId(): Flow<String?>
    suspend fun saveLoginUserId(loginUserId: String)
    suspend fun clearLoginUserId()

    fun getUserType(): Flow<String?>
    suspend fun saveUserType(userType: String)

    fun getSelectedPatientId(): Flow<String?>
    suspend fun saveSelectedPatientId(selectedPatientId: String)

    /**
     * 이상 상황 감지 여부 저장
     */
    suspend fun saveAbnormalDetection(isDetected: Boolean, stage: String, detectedTime: String)

    /**
     * 이상 상황 감지 여부 불러오기
     */
    fun getIsAbnormalDetected(): Flow<Boolean?>

    /**
     * 이상 상황 단계 불러오기
     */
    fun getAbnormalStage(): Flow<String?>

    /**
     * 이상 상황 감지 시간 불러오기
     */
    fun getAbnormalDetectedTime(): Flow<String?>

    /**
     * 이상 상황 정보 초기화
     */
    suspend fun clearAbnormalDetection()
}