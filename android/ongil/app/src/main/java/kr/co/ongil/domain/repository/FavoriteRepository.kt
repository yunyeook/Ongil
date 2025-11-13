package kr.co.ongil.domain.repository

import kr.co.ongil.domain.model.FavoritePlace
import kr.co.ongil.domain.model.FavoritePlaces
import kr.co.ongil.domain.model.placedetail.PlaceDetailUpdate
import kr.co.ongil.presentation.ui.favorite.PatientData

interface FavoriteRepository {
    // 장소 관련
    suspend fun getFavoritePlaces(patientId: Long): Result<FavoritePlaces>

    suspend fun addFavoritePlace(
        patientId: Long,
        placeName: String,
        placeAlias: String?,
        category: String?,
        address: String,
        latitude: Double,
        longitude: Double,
        isDefault: Boolean = false
    ): Result<FavoritePlace>

    suspend fun getPlaceDetail(
        patientId: Long,
        favoriteId: Long
    ): Result<FavoritePlace>

    suspend fun updatePlaceDetail(
        patientId: Long,
        favoriteId: Long,
        update: PlaceDetailUpdate
    ): Result<FavoritePlace>

    suspend fun deleteFavoritePlace(
        patientId: Long,
        favoriteId: Long
    ): Result<String>

    // 사용자(환자/보호자) 관계 관련
    suspend fun getMyRelationships(): Result<List<PatientData>>

    suspend fun getRelationshipDetail(relationshipId: Long): Result<PatientData>
}