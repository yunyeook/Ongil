package kr.co.ongil.data.repository

import kotlinx.coroutines.delay
import kr.co.ongil.data.datasource.remote.api.AuthApi
import kr.co.ongil.data.model.auth.LoginRequest
import kr.co.ongil.data.model.auth.LoginResponse
import kr.co.ongil.data.model.auth.LogoutRequest
import kr.co.ongil.data.model.user.UserDto
import kr.co.ongil.data.util.ErrorHandler
import kr.co.ongil.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * 인증 Repository 구현체
 */
class AuthRepositoryImpl @Inject constructor(
    // TODO: AuthApi 주입 활성화
    // TODO: TokenManager 추가
) : AuthRepository {

    // Mock 데이터를 위해 임시로 null 설정
    private val authApi: AuthApi? = null

    override suspend fun login(phoneNumber: String, password: String): Result<LoginResponse> {
        return try {
            // API 주입 확인
            if (authApi == null) {
                // Mock 데이터 반환 (개발/테스트용)
                delay(500)

                // 비밀번호가 "1234"이면 성공
                if (password == "1234") {
                    val mockResponse = LoginResponse(
                        message = "로그인 성공",
                        data = kr.co.ongil.data.model.auth.LoginData(
                            accessToken = "mock_access_token_12345",
                            refreshToken = "mock_refresh_token_12345",
                            user = UserDto(
                                id = 1,
                                name = "홍길동",
                                birth = "19980919",
                                phoneNumber = phoneNumber,
                                userType = "PATIENT",
                                profileImage = null
                            )
                        )
                    )
                    return Result.success(mockResponse)
                } else {
                    throw Exception("전화번호 또는 비밀번호가 올바르지 않습니다.")
                }
            }

            // 실제 API 호출
            val response = authApi.login(
                request = LoginRequest(
                    phoneNumber = phoneNumber,
                    password = password
                )
            )

            // TODO: 토큰 저장
            // tokenManager.saveTokens(response.data.accessToken, response.data.refreshToken)

            Result.success(response)
        } catch (e: Exception) {
            // HTTP 에러를 ApiException으로 변환
            val apiException = ErrorHandler.handleException(e)
            Result.failure(apiException)
        }
    }

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
