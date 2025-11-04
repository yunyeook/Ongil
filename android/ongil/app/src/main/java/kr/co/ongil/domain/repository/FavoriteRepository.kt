package kr.co.ongil.domain.repository

import kr.co.ongil.domain.model.favorite.FavoritePlace
import kr.co.ongil.domain.model.favorite.FavoritePlaces
import kr.co.ongil.domain.model.placedetail.PlaceDetailUpdate

interface FavoriteRepository {
    suspend fun getFavoritePlaces(patientId: Long): Result<FavoritePlaces>

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
    ): Result<Unit>
}