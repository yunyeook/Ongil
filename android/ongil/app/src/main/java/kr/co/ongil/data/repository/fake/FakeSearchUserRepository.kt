package kr.co.ongil.data.repository.fake

import kotlinx.coroutines.delay
import kr.co.ongil.domain.model.UserSummary
import kr.co.ongil.domain.repository.SearchUserRepository

/**
 * SearchUserRepository의 Fake 구현체
 * 실제 백엔드 연동 전까지 더미 데이터를 제공합니다.
 * 코틀린 방식으로 작성된 깔끔한 구현체입니다.
 */
class FakeSearchUserRepository : SearchUserRepository {

    override suspend fun searchUserByPhone(phoneNumber: String): UserSummary? {
        // 네트워크 지연 시뮬레이션
        delay(800)

        // 더미 데이터 - 특정 번호에 대해서만 결과 반환
        return when {
            phoneNumber.contains("1234") -> UserSummary(
                id = "fake_user_1",
                displayName = "김철수",
                phoneNumber = "010-1234-5678",
                avatarUrl = null
            )
            phoneNumber.contains("5678") -> UserSummary(
                id = "fake_user_2",
                displayName = "이영희",
                phoneNumber = "010-5678-9012",
                avatarUrl = null
            )
            else -> null // 검색 결과 없음
        }
    }

    override suspend fun requestVerificationCode(phoneNumber: String): Boolean {
        // 네트워크 지연 시뮬레이션
        delay(500)

        // 항상 성공으로 처리 (실제로는 SMS 발송 API 호출)
        return true
    }

    override suspend fun verifyCode(
        phoneNumber: String,
        verificationCode: String,
        grants: String
    ): Pair<Boolean, String?> {
        // 네트워크 지연 시뮬레이션
        delay(300)

        // 테스트용 인증번호: "123456"
        return if (verificationCode == "123456") {
            true to "fake_verification_token_${System.currentTimeMillis()}"
        } else {
            false to null
        }
    }

    override suspend fun registerContact(
        targetUserId: String,
        relationshipName: String,
        relationshipType: String,
        memo: String,
        verificationToken: String
    ): Boolean {
        // 네트워크 지연 시뮬레이션
        delay(1000)

        // 유효한 토큰이면 등록 성공
        return verificationToken.startsWith("fake_verification_token_")
    }
}