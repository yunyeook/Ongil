package kr.co.ongil.data.model.favorite

// api명세서 기준으로 작성함
data class FavoritePlaceDto(
    val favoriteId: Long,
    val patientId: Long,
    val placeName: String,
    val placeAlias: String?,   // 사용자 수정 별칭
    val address: String,
    val category: String,
    val latitude: Double,
    val longitude: Double,
    val count: Int,
    val isDefault: Boolean,
    val createdAt: String
)

data class FavoritePlacesResponseDto(
    val message: String,
    val data: List<FavoritePlaceDto>
)