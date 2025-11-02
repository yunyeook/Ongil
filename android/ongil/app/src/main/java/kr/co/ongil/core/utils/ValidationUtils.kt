package kr.co.ongil.core.utils

/**
 * 검증 유틸리티
 */

/**
 * 비밀번호 검증 결과
 */
sealed class PasswordValidationResult {
    data object Valid : PasswordValidationResult()
    data class Invalid(val message: String) : PasswordValidationResult()
}

/**
 * 전화번호 검증 결과
 */
sealed class PhoneValidationResult {
    data object Valid : PhoneValidationResult()
    data class Invalid(val message: String) : PhoneValidationResult()
}

/**
 * 비밀번호 형식 검증 (영문, 숫자, 특수문자 포함)
 */
fun isValidPasswordFormat(password: String): Boolean {
    val hasLetter = password.any { it.isLetter() }
    val hasDigit = password.any { it.isDigit() }
    val hasSpecialChar = password.any { !it.isLetterOrDigit() }

    return hasLetter && hasDigit && hasSpecialChar
}

/**
 * 비밀번호 변경 시 전체 검증
 */
fun validatePasswordChange(
    currentPassword: String,
    newPassword: String,
    confirmPassword: String
): PasswordValidationResult {
    return when {
        currentPassword.isBlank() -> {
            PasswordValidationResult.Invalid("현재 비밀번호를 입력해주세요.")
        }
        newPassword.isBlank() -> {
            PasswordValidationResult.Invalid("새 비밀번호를 입력해주세요.")
        }
        newPassword.length < 8 -> {
            PasswordValidationResult.Invalid("비밀번호는 8자 이상이어야 합니다.")
        }
        !isValidPasswordFormat(newPassword) -> {
            PasswordValidationResult.Invalid("영문, 숫자, 특수문자를 포함해야 합니다.")
        }
        newPassword != confirmPassword -> {
            PasswordValidationResult.Invalid("비밀번호가 일치하지 않습니다.")
        }
        currentPassword == newPassword -> {
            PasswordValidationResult.Invalid("현재 비밀번호와 새 비밀번호가 같습니다.")
        }
        else -> PasswordValidationResult.Valid
    }
}

/**
 * 전화번호 형식 검증 (010으로 시작하는 11자리 또는 10자리)
 */
fun validatePhoneNumber(phoneNumber: String): PhoneValidationResult {
    // 숫자만 추출
    val digitsOnly = phoneNumber.filter { it.isDigit() }

    return when {
        digitsOnly.isBlank() -> {
            PhoneValidationResult.Invalid("전화번호를 입력해주세요.")
        }
        digitsOnly.length !in 10..11 -> {
            PhoneValidationResult.Invalid("전화번호는 10-11자리여야 합니다.")
        }
        !digitsOnly.startsWith("01") -> {
            PhoneValidationResult.Invalid("올바른 전화번호 형식이 아닙니다.")
        }
        else -> PhoneValidationResult.Valid
    }
}

/**
 * 인증번호 형식 검증 (6자리 숫자)
 */
fun validateVerificationCode(code: String): Boolean {
    return code.length == 6 && code.all { it.isDigit() }
}