package kr.co.ongil.data.mapper

import kr.co.ongil.domain.model.placedetail.PlaceDetailUpdate
import kr.co.ongil.data.model.favorite.PlaceDetailUpdateDto

fun PlaceDetailUpdate.toDto(): PlaceDetailUpdateDto =
    PlaceDetailUpdateDto(
        placeName  = placeName,
        address    = address,
        placeAlias = placeAlias,
        category   = category,
        latitude   = latitude,
        longitude  = longitude,
        isDefault  = isDefault
    )