package kr.co.ongil.domain.repository

import kr.co.ongil.domain.model.SearchPlace

/**
 * 지도 관련 Repository
 */
interface MapRepository {
    suspend fun searchPlaces(
        query: String,
        latitude: Double? = null,
        longitude: Double? = null,
        radius: Int? = null,
        page: Int = 1,
        size: Int = 10
    ): Result<List<SearchPlace>>
}