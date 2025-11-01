package kr.co.ongil.data.repository

import kotlinx.coroutines.delay
import kr.co.ongil.data.datasource.remote.api.UserApi
import kr.co.ongil.data.model.user.UserDto
import kr.co.ongil.domain.repository.UserRepository

/**
 * 사용자 Repository 구현체
 */
class UserRepositoryImpl(
    // private val userApi: UserApi // TODO: 실제 API 연동시 주석 해제
    // TODO: TokenManager 추가하여 accessToken 자동으로 가져오기
) : UserRepository {

    override suspend fun getMyInfo(): Result<UserDto> {
        return try {
            // 네트워크 지연 시뮬레이션
            delay(500)

            // 하드코딩된 Mock 데이터
            val mockUser = UserDto(
                id = 1,
                name = "홍길동",
                birth = "19980919",
                phoneNumber = "01012341234",
                userType = "PATIENT",
                profileImage = null // 또는 "https://example.com/profile.jpg"
            )

            Result.success(mockUser)

            /* TODO: 실제 API 연동 (위의 하드코딩 부분을 아래로 교체)
            val accessToken = tokenManager.getAccessToken()
            val response = userApi.getMyInfo("Bearer $accessToken")
            Result.success(response.data.user)
            */
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}