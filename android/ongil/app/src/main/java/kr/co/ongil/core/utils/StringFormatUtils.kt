package kr.co.ongil.core.utils

/**
 * 문자열 포맷팅 유틸리티
 */

/**
 * 전화번호 포맷팅 (01012341234 -> 010-1234-1234)
 */
fun formatPhoneNumber(phoneNumber: String): String {
    return when (phoneNumber.length) {
        11 -> "${phoneNumber.substring(0, 3)}-${phoneNumber.substring(3, 7)}-${phoneNumber.substring(7)}"
        10 -> "${phoneNumber.substring(0, 3)}-${phoneNumber.substring(3, 6)}-${phoneNumber.substring(6)}"
        else -> phoneNumber
    }
}
