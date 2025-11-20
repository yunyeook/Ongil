package kr.co.ongil.data.repository.fake

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kr.co.ongil.data.model.user.UserDto
import kr.co.ongil.domain.repository.UserRepository
import java.io.File

/**
 * Preview 및 테스트용 Fake UserRepository
 */
class FakeUserRepository : UserRepository {

    override fun getMyInfo(): Flow<Result<UserDto>> = flow {
        emit(Result.success(
            UserDto(
                id = 1,
                name = "홍길동",
                birth = "19980919",
                phoneNumber = "01012341234",
                userType = "PATIENT",
                profileImage = null
                )
            )
        )
    }

    override suspend fun updateMyInfo(
        name: String?,
        birth: String?,
        phoneNumber: String?,
        verificationToken: String?,
        profileImage: File?
    ): Result<UserDto> {
        return Result.success(
            UserDto(
                id = 1,
                name = name ?: "홍길동",
                birth = birth ?: "19980919",
                phoneNumber = phoneNumber ?: "01012341234",
                userType = "PATIENT",
                profileImage = null
            )
        )
    }

    override suspend fun sendVerificationCode(phoneNumber: String): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun verifyCode(phoneNumber: String, code: String): Result<String> {
        return Result.success("fake_verification_token")
    }

    override suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit> {
        return Result.success(Unit)
    }
}
