package kr.co.ongil.wear.domain.repository

import kotlinx.coroutines.flow.Flow
import kr.co.ongil.wear.data.model.WearLoginData

/**
 * 워치 인증 Repository 인터페이스
 *
 * 스프링부트의 Service 인터페이스와 동일
 * - 도메인 레이어 (비즈니스 로직)
 * - 구현은 data 레이어에서
 */
interface WearAuthRepository {

    /**
     * 로그인 정보 저장
     *
     * @param loginData 폰에서 받은 로그인 정보
     */
    suspend fun saveLoginData(loginData: WearLoginData)

    /**
     * 로그인 상태 확인
     *
     * @return Flow<Boolean> true면 로그인됨, false면 비로그인
     */
    fun isLoggedIn(): Flow<Boolean>

    /**
     * 사용자 ID 가져오기
     *
     * @return Flow<String?> 사용자 ID (없으면 null)
     */
    fun getUserId(): Flow<String?>

    /**
     * 사용자 타입 가져오기
     *
     * @return Flow<String?> "PATIENT" 또는 "GUARDIAN"
     */
    fun getUserType(): Flow<String?>

    /**
     * 액세스 토큰 가져오기
     *
     * @return Flow<String?> JWT 액세스 토큰
     */
    fun getAccessToken(): Flow<String?>

    /**
     * 로그아웃 (로그인 정보 삭제)
     */
    suspend fun logout()
}