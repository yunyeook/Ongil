package kr.co.ongil.data.mapper

import kr.co.ongil.data.model.map.PlaceDetailDto
import kr.co.ongil.domain.model.PlaceDetail

/**
 * PlaceDetailDto를 도메인 모델로 변환
 */
fun PlaceDetailDto.toDomain(): PlaceDetail =
    PlaceDetail(
        id = id,
        name = name,
        roadAddress = address.roadAddress,
        jibunAddress = address.jibunAddress,
        latitude = coordinate.latitude,
        longitude = coordinate.longitude,
        category = category.lowerCategory ?: category.middleCategory ?: category.upperCategory,
        phoneNumber = phoneNumber,
        description = description,
        zipCode = zipCode,
        parking = parking,
        businessHours = businessInfo?.businessHours,
        closedDays = businessInfo?.closedDays,
        is24Hours = businessInfo?.is24Hours,
        isYearRound = businessInfo?.isYearRound
    )
