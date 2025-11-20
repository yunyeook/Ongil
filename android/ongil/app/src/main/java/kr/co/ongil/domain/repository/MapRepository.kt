package kr.co.ongil.domain.repository

import kr.co.ongil.data.model.map.NavigationEndResponse
import kr.co.ongil.data.model.map.NavigationStartResponse
import kr.co.ongil.domain.model.PlaceDetail
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

    suspend fun getPlaceDetail(poiId: String): Result<PlaceDetail>

    suspend fun createCallLog(
        receiverPhoneNumber: String,
        callType: String,
        source: String,
        patientState: String,
        latitude: Double,
        longitude: Double,
        startedAt: String
    ): Result<Unit>

    suspend fun startNavigation(
        patientId: Long,
        startLatitude: Double,
        startLongitude: Double,
        startName: String,
        endLatitude: Double,
        endLongitude: Double,
        endName: String,
        initiatedBy: String
    ): Result<NavigationStartResponse>

    suspend fun endNavigation(
        patientId: Long,
        navigationId: Int,
        isSuccessful: Boolean
    ): Result<NavigationEndResponse>
}