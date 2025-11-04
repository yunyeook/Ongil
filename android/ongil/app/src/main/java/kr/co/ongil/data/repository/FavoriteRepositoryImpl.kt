package kr.co.ongil.data.repository

import javax.inject.Inject
import kr.co.ongil.data.datasource.remote.api.FavoriteApi
import kr.co.ongil.data.mapper.toDomain
import kr.co.ongil.data.mapper.toDto
import kr.co.ongil.domain.model.favorite.FavoritePlace
import kr.co.ongil.domain.model.favorite.FavoritePlaces
import kr.co.ongil.domain.model.placedetail.PlaceDetailUpdate
import kr.co.ongil.domain.repository.FavoriteRepository

class FavoriteRepositoryImpl @Inject constructor(
    private val api: FavoriteApi
) : FavoriteRepository {

    override suspend fun getFavoritePlaces(patientId: Long): Result<FavoritePlaces> = runCatching {
        val response = api.getFavoritePlaces(patientId)
        if (!response.isSuccessful) throw RuntimeException("HTTP ${response.code()}")
        val body = response.body() ?: throw RuntimeException("Empty body")
        body.toDomain() ?: FavoritePlaces(totalCount = 0, items = emptyList())
    }

    override suspend fun getPlaceDetail(
        patientId: Long,
        favoriteId: Long
    ): Result<FavoritePlace> = runCatching {
        val response = api.getPlaceDetail(patientId, favoriteId)
        if (!response.isSuccessful) throw RuntimeException("HTTP ${response.code()}")
        val body = response.body() ?: throw RuntimeException("Empty body")
        body.toDomain() ?: throw NoSuchElementException("No data")
    }

    override suspend fun updatePlaceDetail(
        patientId: Long,
        favoriteId: Long,
        update: PlaceDetailUpdate
    ): Result<FavoritePlace> = runCatching {
        val response = api.updateFavoritePlace(patientId, favoriteId, update.toDto())
        if (!response.isSuccessful) throw RuntimeException("HTTP ${response.code()}")
        val resBody = response.body() ?: throw RuntimeException("Empty body")
        resBody.toDomain() ?: throw NoSuchElementException("No data")
    }

    override suspend fun deleteFavoritePlace(
        patientId: Long,
        favoriteId: Long
    ): Result<Unit> = runCatching {
        val response = api.deleteFavoritePlace(patientId, favoriteId)
        if (!response.isSuccessful) throw RuntimeException("HTTP ${response.code()}")
        Unit
    }
}