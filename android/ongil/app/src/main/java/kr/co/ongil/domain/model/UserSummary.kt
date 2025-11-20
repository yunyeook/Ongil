package kr.co.ongil.domain.model

data class UserSummary(
    val id: String,
    val displayName: String,
    val phoneNumber: String,
    val avatarUrl: String? = null
)