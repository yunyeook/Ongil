package kr.co.ongil.data.mapper

import kr.co.ongil.data.model.map.SearchPlaceDto
import kr.co.ongil.domain.model.SearchPlace

/**
 * SearchPlaceDto를 도메인 모델로 변환
 */
fun SearchPlaceDto.toDomain(): SearchPlace =
    SearchPlace(
        name = name,
        address = address,
        latitude = latitude,
        longitude = longitude,
        distance = distance,
        category = category?.lowerCategory ?: category?.middleCategory ?: category?.upperCategory
    )
