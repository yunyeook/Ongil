package kr.co.ongil.data.repository

import android.util.Log
import javax.inject.Inject
import kr.co.ongil.data.datasource.remote.api.FavoriteApi
import kr.co.ongil.data.mapper.toDomain
import kr.co.ongil.data.mapper.toDto
import kr.co.ongil.domain.model.FavoritePlace
import kr.co.ongil.domain.model.FavoritePlaces
import kr.co.ongil.domain.model.placedetail.PlaceDetailUpdate
import kr.co.ongil.domain.repository.FavoriteRepository
import kr.co.ongil.presentation.ui.favorite.toPatientData

private fun httpError(tag: String, endpoint: String, code: Int, message: String?): RuntimeException {
    val reason = message?.take(300) ?: "no message"
    Log.e(tag, "HTTP $code at $endpoint - $reason")
    return RuntimeException("HTTP $code at $endpoint: $reason")
}

class FavoriteRepositoryImpl @Inject constructor(
    private val api: FavoriteApi
) : FavoriteRepository {

    override suspend fun getFavoritePlaces(patientId: Long): Result<FavoritePlaces> = runCatching {
        val response = api.getFavoritePlaces(patientId)
        if (!response.isSuccessful) throw httpError("FavoriteRepo", "GET /favorites", response.code(), response.message())
        val body = response.body() ?: throw RuntimeException("Empty body")
        Log.d("FavoriteRepo", "GET 장소목록 - patientId=$patientId, 응답 데이터: ${body.data?.favorites?.map { "favoriteId=${it.favoriteId}, placeName=${it.placeName}, placeAlias=${it.placeAlias}" }}")
        body.toDomain() ?: FavoritePlaces(totalCount = 0, items = emptyList())
    }

    override suspend fun getPlaceDetail(
        patientId: Long,
        favoriteId: Long
    ): Result<FavoritePlace> = runCatching {
        val response = api.getPlaceDetail(patientId, favoriteId)
        if (!response.isSuccessful) throw httpError("FavoriteRepo", "GET /favorites/{id}", response.code(), response.message())
        val body = response.body() ?: throw RuntimeException("Empty body")
        body.toDomain() ?: throw NoSuchElementException("No data")
    }

    override suspend fun updatePlaceDetail(
        patientId: Long,
        favoriteId: Long,
        update: PlaceDetailUpdate
    ): Result<FavoritePlace> = runCatching {
        Log.d("FavoriteRepo", "PATCH 요청 - patientId=$patientId, favoriteId=$favoriteId")
        Log.d("FavoriteRepo", "PATCH body - placeAlias=${update.placeAlias}, address=${update.address}, isDefault=${update.isDefault}")
        val dto = update.toDto()
        Log.d("FavoriteRepo", "PATCH DTO - placeAlias=${dto.placeAlias}, address=${dto.address}, isDefault=${dto.isDefault}")
        val response = api.updateFavoritePlace(patientId, favoriteId, dto)
        Log.d("FavoriteRepo", "PATCH 응답 코드 - ${response.code()}, 메시지 - ${response.message()}")
        if (!response.isSuccessful) throw httpError("FavoriteRepo", "PATCH /api/v1/patients/$patientId/favorites/$favoriteId", response.code(), response.message())
        val resBody = response.body() ?: throw RuntimeException("Empty body")
        Log.d("FavoriteRepo", "PATCH 응답 - favoriteId=${resBody.data?.favoriteId}, placeName=${resBody.data?.placeName}, placeAlias=${resBody.data?.placeAlias}, address=${resBody.data?.address}, isDefault=${resBody.data?.isDefault}")
        resBody.toDomain() ?: throw NoSuchElementException("No data")
    }

    override suspend fun deleteFavoritePlace(
        patientId: Long,
        favoriteId: Long
    ): Result<Unit> = runCatching {
        val response = api.deleteFavoritePlace(patientId, favoriteId)
        if (!response.isSuccessful) throw httpError("FavoriteRepo", "DELETE /favorites/{id}", response.code(), response.message())
        Unit
    }

    // 사용자(환자/보호자) 관계 관련
    override suspend fun getMyRelationships(): Result<List<kr.co.ongil.presentation.ui.favorite.PatientData>> = runCatching {
        val response = api.getMyRelationships()
        if (!response.isSuccessful) throw httpError("FavoriteRepo", "GET /relationships/me", response.code(), response.message())
        val body = response.body() ?: throw RuntimeException("Empty body")
        Log.d("FavoriteRepo", "GET 사용자목록 - 응답 데이터: ${body.data.map { "relationshipId=${it.relationshipId}, name=${it.relationshipName}, counterpartUserId=${it.counterpartUserId}" }}")
        body.data.map { it.toPatientData() }
    }

    override suspend fun getRelationshipDetail(relationshipId: Long): Result<kr.co.ongil.presentation.ui.favorite.PatientData> = runCatching {
        val response = api.getRelationshipDetail(relationshipId)
        if (!response.isSuccessful) throw httpError("FavoriteRepo", "GET /relationships/{id}", response.code(), response.message())
        val body = response.body() ?: throw RuntimeException("Empty body")
        val data = body.data ?: throw NoSuchElementException("No data")
        Log.d("FavoriteRepo", "GET 사용자상세 - relationshipId=${data.relationshipId}, name=${data.relationshipName}")
        data.toPatientData()
    }
}