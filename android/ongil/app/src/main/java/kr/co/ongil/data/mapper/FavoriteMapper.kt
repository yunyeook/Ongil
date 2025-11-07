package kr.co.ongil.data.mapper

import kr.co.ongil.data.model.favorite.PlaceDetailDto
import kr.co.ongil.data.model.favorite.FavoritePlaceDto
import kr.co.ongil.data.model.favorite.FavoritePlacesDto
import kr.co.ongil.domain.model.FavoritePlace
import kr.co.ongil.domain.model.FavoritePlaces

fun FavoritePlaceDto.toDomain(): FavoritePlace =
    FavoritePlace(
        favoriteId = favoriteId,
        patientId = patientId,
        placeName = placeName,
        placeAlias = placeAlias ?: "",
        category = category ?: "",
        address = address,
        latitude = latitude,
        longitude = longitude,
        isDefault = isDefault,
        count = count,
        createdAt = createdAt
    )

fun FavoritePlacesDto.toDomain(): FavoritePlaces? {
    val d = data ?: return null
    return FavoritePlaces(
        totalCount = d.totalCount,
        items = d.favorites.map { it.toDomain() }
    )
}

fun PlaceDetailDto.toDomain(): FavoritePlace? =
    data?.toDomain()