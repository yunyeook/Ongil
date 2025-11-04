package kr.co.ongil.domain.model.favorite

data class FavoritePlace(
    val favoriteId: Long,
    val patientId: Long,
    val placeName: String,
    val placeAlias: String,
    val category: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val isDefault: Boolean,
    val count: Int,
    val createdAt: String
)

data class FavoritePlaces(
    val totalCount: Int,
    val items: List<FavoritePlace>
)