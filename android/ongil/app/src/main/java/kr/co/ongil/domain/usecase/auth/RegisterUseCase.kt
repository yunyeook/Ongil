package kr.co.ongil.domain.usecase.auth

import kr.co.ongil.domain.repository.AuthRepository
import javax.inject.Inject

class RegisterUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        name: String,
        birth: String?,              // yyyyMMdd or null
        phoneNumber: String,
        verificationToken: String,   // LOCAL 필수
        password: String,            // 8–16자
        userType: String,            // PATIENT | GUARDIAN
        profileImagePath: String?    // 파일 경로 or null
    ): Result<String> {
        validate(
            name = name,
            birth = birth,
            phoneNumber = phoneNumber,
            verificationToken = verificationToken,
            password = password,
            userType = userType,
            profileImagePath = profileImagePath
        )
        return authRepository.registerUser(
            name = name,
            birth = birth,
            phoneNumber = phoneNumber,
            verificationToken = verificationToken,
            password = password,
            userType = userType,
            profileImagePath = profileImagePath
        )
    }

    private fun validate(
        name: String,
        birth: String?,
        phoneNumber: String,
        verificationToken: String,
        password: String,
        userType: String,
        profileImagePath: String?
    ) {
        require(name.isNotBlank()) { "이름은 필수입니다." }
        if (!birth.isNullOrBlank()) {
            require(birth.length == 8 && birth.all { it.isDigit() }) { "생년월일은 yyyyMMdd 형식이어야 합니다." }
        }
        require(phoneNumber.isNotBlank() && phoneNumber.all { it.isDigit() }) { "전화번호는 숫자만 입력하세요." }
        require(verificationToken.isNotBlank()) { "전화번호 인증 토큰이 필요합니다." }
        require(password.length in 8..16) { "비밀번호는 8–16자여야 합니다." }
        require(userType in setOf("PATIENT", "GUARDIAN")) { "userType은 PATIENT 또는 GUARDIAN 이어야 합니다." }
        if (!profileImagePath.isNullOrBlank()) {
            val lower = profileImagePath.lowercase()
            require(lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png")) {
                "이미지는 jpg/jpeg/png만 허용됩니다."
            }
        }
    }
}