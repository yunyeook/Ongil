package kr.co.ongil.data.repository

import kotlinx.coroutines.delay
import kr.co.ongil.domain.model.UserSummary
import kr.co.ongil.domain.repository.SearchUserRepository

/**
 * 실제 서버/DB 대신 하드코딩된 데이터로 동작하는 Fake 구현체.
 * ViewModel은 SearchUserRepository만 보고 있기 때문에
 * 여기 구현체를 주입하면 UI를 그대로 테스트할 수 있음.
 */
class FakeSearchUserRepository : SearchUserRepository {

    // 더미 사용자 데이터
    private val dummyUsers = listOf(
        UserSummary(
            id = "user-001",
            displayName = "김서현",
            phoneNumber = "01012345678",
            avatarUrl = null
        ),
        UserSummary(
            id = "user-002",
            displayName = "이도윤",
            phoneNumber = "01087654321",
            avatarUrl = null
        )
    )

    // 인증에 사용될 더미 코드 & 토큰
    private val validVerificationCode = "123456"
    private val issuedToken = "dummy-verification-token"

    override suspend fun searchUserByPhone(phoneNumber: String): UserSummary? {
        // 네트워크 지연처럼 보이도록 약간의 delay
        delay(800)

        // 실제 구현에서는 하이픈 없이 비교할 가능성 높음
        val normalized = phoneNumber.filter { it.isDigit() }

        return dummyUsers.find { it.phoneNumber == normalized }
    }

    override suspend fun requestVerificationCode(phoneNumber: String): Boolean {
        // 실제라면 /phone-verifications 호출
        delay(400)

        // 더미에서는 존재하는 유저에 대해서만 "전송 성공" 처리
        val normalized = phoneNumber.filter { it.isDigit() }
        return dummyUsers.any { it.phoneNumber == normalized }
    }

    override suspend fun verifyCode(
        phoneNumber: String,
        verificationCode: String,
        grants: String
    ): Pair<Boolean, String?> {
        // 실제라면 /phone-verifications/verify 호출
        delay(400)

        return if (verificationCode == validVerificationCode) {
            // 성공: verified=true, verificationToken 반환
            true to issuedToken
        } else {
            // 실패
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
        // 실제라면 /relationships/me 호출
        delay(500)

        // 조건 예시
        val isKnownTarget = dummyUsers.any { it.id == targetUserId }
        val tokenOk = (verificationToken == issuedToken)
        val relationOk = relationshipType in listOf("자녀", "부모", "배우자", "기타")

        return isKnownTarget && tokenOk && relationOk
    }
}