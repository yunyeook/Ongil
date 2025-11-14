package kr.co.ongil.wear.domain.usecase

import kotlinx.coroutines.flow.Flow
import kr.co.ongil.wear.data.model.WearLoginData
import kr.co.ongil.wear.domain.repository.WearAuthRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 로그인 데이터 동기화 UseCase
 *
 * 스프링부트의 @Service 메서드와 비슷
 * - 하나의 비즈니스 기능 = 하나의 UseCase
 * - "폰에서 로그인 정보 받아서 워치에 저장" 로직
 */
@Singleton
class SyncLoginDataUseCase @Inject constructor(
    private val authRepository: WearAuthRepository
) {

    /**
     * 로그인 정보 동기화 실행
     *
     * @param loginData 폰에서 받은 로그인 정보
     */
    suspend operator fun invoke(loginData: WearLoginData) {
        // 1. 로그인 정보 저장
        authRepository.saveLoginData(loginData)

        // 2. 추가 로직 (나중에 필요하면)
        // - 로그 남기기
        // - 동기화 성공 알림
        // - 분석 데이터 전송 등
    }

    /**
     * 로그인 상태 확인
     *
     * @return Flow<Boolean> true면 로그인됨
     */
    fun isLoggedIn(): Flow<Boolean> {
        return authRepository.isLoggedIn()
    }

    /**
     * 사용자 정보 가져오기
     *
     * @return Flow<Pair<String?, String?>> (userId, userType)
     */
    fun getUserInfo(): Flow<Pair<String?, String?>> {
        // Flow 변환 (combine)
        return kotlinx.coroutines.flow.combine(
            authRepository.getUserId(),
            authRepository.getUserType()
        ) { userId, userType ->
            Pair(userId, userType)
        }
    }
}
