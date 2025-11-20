package kr.co.ongil.wear.data.datasource.local

import kotlinx.coroutines.flow.Flow
import kr.co.ongil.wear.data.model.WearLoginData

/**
 * 워치 로컬 데이터 저장소 인터페이스
 *
 * 스프링부트의 Repository 인터페이스와 동일
 * - 계약만 정의
 * - 구현은 WearDataStoreManagerImpl에서
 */
interface WearDataStoreManager {

    /**
     * 로그인 정보 전체 저장
     */
    suspend fun saveLoginData(loginData: WearLoginData)

    /**
     * 액세스 토큰만 저장 (Token Refresh 시 사용)
     */
    suspend fun saveAccessToken(accessToken: String)

    /**
     * 리프레시 토큰만 저장 (Token Refresh 시 사용)
     */
    suspend fun saveRefreshToken(refreshToken: String)

    /**
     * 액세스 토큰 가져오기
     */
    fun getAccessToken(): Flow<String?>

    /**
     * 리프레시 토큰 가져오기
     */
    fun getRefreshToken(): Flow<String?>

    /**
     * 사용자 ID 가져오기
     */
    fun getUserId(): Flow<String?>

    /**
     * 사용자 타입 가져오기 (PATIENT or GUARDIAN)
     *   - 처음 앱 실행 시 토큰 없음 → null
     *   - 로그아웃 시 토큰 삭제 → null
     */
    fun getUserType(): Flow<String?>

    /**
     * 선택된 환자 ID 가져오기
     */
    fun getSelectedPatientId(): Flow<String?>

    /**
     * 선택된 환자 ID 저장
     */
    suspend fun saveSelectedPatientId(patientId: String)

    /**
     * 로그인 정보 전체 삭제 (로그아웃)
     */
    suspend fun clearLoginData()

    /**
     * 로그인 여부 확인  - accessToken이 있으면 true, 없으면 false
     */
    fun isLoggedIn(): Flow<Boolean>
}
