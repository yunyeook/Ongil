package kr.co.ongil.data.repository

import kr.co.ongil.presentation.ui.favorite.PlaceData

class FavoriteRepository {

    companion object {
        @Volatile
        private var INSTANCE: FavoriteRepository? = null

        fun getInstance(): FavoriteRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: FavoriteRepository().also { INSTANCE = it }
            }
        }
    }

    // 앱 내부 임시 저장소 (백엔드 나오면 Retrofit/DB로 교체)
    private val favoritePlacesPerPatient: MutableMap<Long, MutableList<PlaceData>> =
        mutableMapOf(
            1L to mutableListOf(
                PlaceData(
                    patientId = 1L,
                    favoriteId = 1L,
                    placeName = "자주가는 한의원",
                    address = "서울특별시 도봉구 방학동 123-45",
                    isDefault = false
                ),
                PlaceData(
                    patientId = 1L,
                    favoriteId = 2L,
                    placeName = "복지센터",
                    address = "서울특별시 도봉구 마들로 536",
                    isDefault = true
                ),
                PlaceData(
                    patientId = 1L,
                    favoriteId = 3L,
                    placeName = "노원구 치매안심센터",
                    address = "서울특별시 노원구 동일로 123-67",
                    isDefault = false
                )
            )
        )

    // 환자별 장소 목록 조회
    suspend fun getFavoritePlaces(
        patientId: Long
    ): List<PlaceData> {
        // TODO: 나중에 Retrofit으로 교체, 지금은 더미
        return favoritePlacesPerPatient[patientId]?.toList() ?: emptyList()
    }

    // 장소 상세 조회
    suspend fun getFavoritePlaceDetail(
        patientId: Long,
        favoriteId: Long
    ): PlaceData? {
        val list = favoritePlacesPerPatient[patientId] ?: return null
        return list.find { it.favoriteId == favoriteId }
    }

    // 장소명/주소 수정
    suspend fun updatePlaceInfo(
        patientId: Long,
        favoriteId: Long,
        newName: String,
        newAddress: String
    ): Boolean {
        val list = favoritePlacesPerPatient[patientId] ?: return false
        val index = list.indexOfFirst { it.favoriteId == favoriteId }
        if (index == -1) return false

        val old = list[index]
        list[index] = old.copy(
            placeName = newName,
            address = newAddress
        )
        return true
    }

    // 장소 삭제
    suspend fun deletePlace(
        patientId: Long,
        favoriteId: Long
    ): Boolean {
        val list = favoritePlacesPerPatient[patientId] ?: return false
        val removed = list.removeIf { it.favoriteId == favoriteId }
        return removed
    }

    // 기본 목적지 설정 (이 환자에서 favoriteId만 isDefault=true)
    suspend fun setDefaultPlace(
        patientId: Long,
        favoriteId: Long
    ): Boolean {
        val list = favoritePlacesPerPatient[patientId] ?: return false
        var found = false

        // 먼저 전체 false
        for (i in list.indices) {
            val p = list[i]
            val shouldBeDefault = (p.favoriteId == favoriteId)
            if (shouldBeDefault) found = true
            list[i] = p.copy(isDefault = shouldBeDefault)
        }

        return found
    }
}