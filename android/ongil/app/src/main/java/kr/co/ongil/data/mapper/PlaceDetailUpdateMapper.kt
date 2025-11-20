package kr.co.ongil.data.mapper

import kr.co.ongil.domain.model.placedetail.PlaceDetailUpdate
import kr.co.ongil.data.model.favorite.PlaceDetailUpdateDto

fun PlaceDetailUpdate.toDto(): PlaceDetailUpdateDto =
    PlaceDetailUpdateDto(
        placeAlias = placeAlias, // placeName 대신 placeAlias 사용
        address    = address,
        category   = category,
        latitude   = latitude,
        longitude  = longitude,
        isDefault  = isDefault
    )