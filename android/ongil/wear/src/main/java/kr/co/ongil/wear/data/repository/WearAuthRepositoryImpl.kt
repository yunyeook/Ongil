package kr.co.ongil.wear.data.repository

import kotlinx.coroutines.flow.Flow
import kr.co.ongil.wear.data.datasource.local.WearDataStoreManager
import kr.co.ongil.wear.data.model.WearLoginData
import kr.co.ongil.wear.domain.repository.WearAuthRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * WearAuthRepository 구현체
 *
 * 스프링부트의 @Service 구현체와 동일
 * - WearDataStoreManager를 사용해서 실제 데이터 저장/읽기
 */
@Singleton
class WearAuthRepositoryImpl @Inject constructor(
    private val dataStoreManager: WearDataStoreManager
) : WearAuthRepository {

    /**
     * 로그인 정보 저장
     */
    override suspend fun saveLoginData(loginData: WearLoginData) {
        dataStoreManager.saveLoginData(loginData)
    }

    /**
     * 로그인 상태 확인
     */
    override fun isLoggedIn(): Flow<Boolean> {
        return dataStoreManager.isLoggedIn()
    }

    /**
     * 사용자 ID 가져오기
     */
    override fun getUserId(): Flow<String?> {
        return dataStoreManager.getUserId()
    }

    /**
     * 사용자 타입 가져오기
     */
    override fun getUserType(): Flow<String?> {
        return dataStoreManager.getUserType()
    }

    /**
     * 액세스 토큰 가져오기
     */
    override fun getAccessToken(): Flow<String?> {
        return dataStoreManager.getAccessToken()
    }

    /**
     * 로그아웃
     */
    override suspend fun logout() {
        dataStoreManager.clearLoginData()
    }
}
