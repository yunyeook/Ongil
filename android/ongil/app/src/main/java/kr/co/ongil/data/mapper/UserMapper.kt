package kr.co.ongil.data.mapper

import kr.co.ongil.core.utils.formatPhoneNumber
import kr.co.ongil.data.model.user.UserDto

/**
 * UserDto를 UI 모델로 변환하는 Mapper
 */

/**
 * UI에서 사용할 사용자 정보 모델
 */
data class UserUiModel(
    val id: Int,
    val name: String,
    val birth: String,              // 원본 (YYYYMMDD)
    val formattedBirth: String,     // 포맷팅된 생년월일 (YYYY년 MM월 DD일)
    val phoneNumber: String,        // 원본 (숫자만)
    val formattedPhoneNumber: String, // 포맷팅된 전화번호 (010-1234-1234)
    val userType: String,           // 원본 (PATIENT, CAREGIVER)
    val userTypeLabel: String,      // 한글 레이블 (환자, 보호자)
    val profileImageUrl: String?
)

/**
 * UserDto → UserUiModel 변환
 */
fun UserDto.toUiModel(): UserUiModel {
    return UserUiModel(
        id = id,
        name = name,
        birth = birth,
        formattedBirth = formatBirthDate(birth),
        phoneNumber = phoneNumber,
        formattedPhoneNumber = formatPhoneNumber(phoneNumber),
        userType = userType,
        userTypeLabel = getUserTypeLabel(userType),
        profileImageUrl = profileImage
    )
}

/**
 * 생년월일 포맷팅
 * 예: "19980919" → "1998년 09월 19일"
 */
private fun formatBirthDate(birth: String): String {
    return try {
        if (birth.length == 8) {
            val year = birth.substring(0, 4)
            val month = birth.substring(4, 6)
            val day = birth.substring(6, 8)
            "${year}년 ${month}월 ${day}일"
        } else {
            birth // 포맷 불가 시 원본 반환
        }
    } catch (e: Exception) {
        birth
    }
}

/**
 * 사용자 유형 한글 레이블 변환
 */
private fun getUserTypeLabel(userType: String): String {
    return when (userType.uppercase()) {
        "PATIENT" -> "환자"
        "CAREGIVER" -> "보호자"
        "ADMIN" -> "관리자"
        else -> userType
    }
}