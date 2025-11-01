package kr.co.ongil.data.repository

import kotlinx.coroutines.delay
import kr.co.ongil.data.datasource.remote.api.AuthApi
import kr.co.ongil.data.model.auth.LogoutRequest
import kr.co.ongil.domain.repository.AuthRepository

/**
 * 인증 Repository 구현체
 */
class AuthRepositoryImpl(
    // private val authApi: AuthApi // TODO: 실제 API 연동시 주석 해제
    // TODO: TokenManager 추가
) : AuthRepository {

    override suspend fun logout(): Result<String> {
        return try {
            // 네트워크 지연 시뮬레이션
            delay(500)

            // 하드코딩된 Mock 응답
            // TODO: 실제로는 로컬에 저장된 토큰 삭제 처리 필요
            Result.success("로그아웃이 성공적으로 완료되었습니다!")

            /* TODO: 실제 API 연동 (위의 하드코딩 부분을 아래로 교체)
            val accessToken = tokenManager.getAccessToken()
            val refreshToken = tokenManager.getRefreshToken()

            val response = authApi.logout(
                accessToken = "Bearer $accessToken",
                request = LogoutRequest(refreshToken = refreshToken)
            )

            // 로컬 토큰 삭제
            tokenManager.clearTokens()

            Result.success(response.message)
            */
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
