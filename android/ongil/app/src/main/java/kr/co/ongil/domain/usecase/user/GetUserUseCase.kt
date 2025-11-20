package kr.co.ongil.domain.usecase.user

import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import kr.co.ongil.data.model.user.UserDto
import kr.co.ongil.domain.repository.UserRepository
import javax.inject.Inject

/**
 * 사용자 정보 조회 UseCase
 *
 * 로그인한 사용자의 정보를 조회합니다.
 */
class GetUserUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    companion object {
        private const val TAG = "GetUserUseCase"
    }

    /**
     * 사용자 정보 조회 실행
     * @return 사용자 정보
     */
    operator fun invoke(): Flow<Result<UserDto>> {
        Log.d(TAG, "invoke() - 사용자 정보 조회 시작")

        return userRepository.getMyInfo()
            .onEach { result ->
                result.onSuccess {
                    Log.d(TAG, "invoke() - 사용자 정보 조회 성공: $it")
                }.onFailure {
                    Log.e(TAG, "invoke() - 사용자 정보 조회 실패", it)
                }
            }

//        val result = userRepository.getMyInfo()
//        result.onSuccess {
//            Log.d(TAG, "invoke() - 사용자 정보 조회 성공: $it")
//        }.onFailure {
//            Log.e(TAG, "invoke() - 사용자 정보 조회 실패", it)
//        }
//        return result
    }
}
