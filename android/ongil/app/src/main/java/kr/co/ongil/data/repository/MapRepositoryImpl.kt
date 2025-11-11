package kr.co.ongil.data.repository

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kr.co.ongil.data.datasource.remote.api.CallApi
import kr.co.ongil.data.datasource.remote.api.MapApi
import kr.co.ongil.data.mapper.toDomain
import kr.co.ongil.data.model.call.CallLocationDto
import kr.co.ongil.data.model.call.CallLogRequest
import kr.co.ongil.data.model.map.NavigationEndRequest
import kr.co.ongil.data.model.map.NavigationEndResponse
import kr.co.ongil.data.model.map.NavigationLocationDto
import kr.co.ongil.data.model.map.NavigationStartRequest
import kr.co.ongil.data.model.map.NavigationStartResponse
import kr.co.ongil.domain.model.SearchPlace
import kr.co.ongil.domain.repository.MapRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MapRepositoryImpl @Inject constructor(
    private val mapApi: MapApi,
    private val callApi: CallApi
) : MapRepository {

    override suspend fun searchPlaces(
        query: String,
        latitude: Double?,
        longitude: Double?,
        radius: Int?,
        page: Int,
        size: Int
    ): Result<List<SearchPlace>> = withContext(Dispatchers.IO) {
        try {
            if (query.isBlank()) {
                return@withContext Result.success(emptyList())
            }

            Log.d("MapRepository", "장소 검색: $query")

            val response = mapApi.searchPlaces(
                keyword = query,
                latitude = latitude,
                longitude = longitude,
                radius = radius,
                page = page,
                size = size
            )

            val places = response.data.places.map { it.toDomain() }
            Log.d("MapRepository", "검색 성공: ${places.size}개 결과")

            Result.success(places)
        } catch (e: Exception) {
            Log.e("MapRepository", "검색 오류: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun getPlaceDetail(poiId: String): Result<kr.co.ongil.domain.model.PlaceDetail> = withContext(Dispatchers.IO) {
        try {
            Log.d("MapRepository", "장소 상세 조회: $poiId")

            val response = mapApi.getPlaceDetail(poiId)
            val placeDetail = response.data.toDomain()

            Log.d("MapRepository", "장소 상세 조회 성공: ${placeDetail.name}")
            Result.success(placeDetail)
        } catch (e: Exception) {
            Log.e("MapRepository", "장소 상세 조회 오류: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun createCallLog(
        receiverPhoneNumber: String,
        callType: String,
        source: String,
        patientState: String,
        latitude: Double,
        longitude: Double,
        startedAt: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            Log.d("MapRepository", "통화 로그 생성: $receiverPhoneNumber")

            val request = CallLogRequest(
                receiverPhoneNumber = receiverPhoneNumber,
                callType = callType,
                source = source,
                patientState = patientState,
                patientLocation = CallLocationDto(
                    latitude = latitude,
                    longitude = longitude
                ),
                startedAt = startedAt
            )

            val response = callApi.createCallLog(request)

            if (response.isSuccessful) {
                Log.d("MapRepository", "통화 로그 생성 성공")
                Result.success(Unit)
            } else {
                Log.e("MapRepository", "통화 로그 생성 실패: ${response.code()}")
                Result.failure(Exception("통화 로그 생성 실패: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("MapRepository", "통화 로그 생성 오류: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun startNavigation(
        patientId: Long,
        startLatitude: Double,
        startLongitude: Double,
        startName: String,
        endLatitude: Double,
        endLongitude: Double,
        endName: String,
        initiatedBy: String
    ): Result<NavigationStartResponse> = withContext(Dispatchers.IO) {
        try {
            Log.d("MapRepository", "길안내 시작: $startName -> $endName")

            val request = NavigationStartRequest(
                patientId = patientId,
                startLocation = NavigationLocationDto(
                    latitude = startLatitude,
                    longitude = startLongitude,
                    name = startName
                ),
                endLocation = NavigationLocationDto(
                    latitude = endLatitude,
                    longitude = endLongitude,
                    name = endName
                ),
                initiatedBy = initiatedBy
            )

            val response = mapApi.startNavigation(request)
            Log.d("MapRepository", "길안내 시작 성공: navigationId=${response.data.navigationId}")

            Result.success(response)
        } catch (e: Exception) {
            Log.e("MapRepository", "길안내 시작 오류: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun endNavigation(
        patientId: Long,
        navigationId: Int,
        isSuccessful: Boolean
    ): Result<NavigationEndResponse> = withContext(Dispatchers.IO) {
        try {
            Log.d("MapRepository", "길안내 종료: navigationId=$navigationId")

            val request = NavigationEndRequest(
                patientId = patientId,
                navigationId = navigationId,
                isSuccessful = isSuccessful
            )

            val response = mapApi.endNavigation(request)
            Log.d("MapRepository", "길안내 종료 성공: durationSeconds=${response.data.durationSeconds}")

            Result.success(response)
        } catch (e: Exception) {
            Log.e("MapRepository", "길안내 종료 오류: ${e.message}", e)
            Result.failure(e)
        }
    }
}
