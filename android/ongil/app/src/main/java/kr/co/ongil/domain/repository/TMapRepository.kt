package kr.co.ongil.domain.repository

import kr.co.ongil.domain.model.SearchPlace

/**
 * TMap POI 검색 Repository
 */
interface TMapRepository {
    suspend fun searchPlaces(query: String, latitude: Double? = null, longitude: Double? = null): Result<List<SearchPlace>>
}