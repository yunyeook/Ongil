//package kr.co.ongil.data.repository.fake
//
//import kr.co.ongil.domain.model.FavoritePlace
//import kr.co.ongil.domain.model.FavoritePlaces
//import kr.co.ongil.domain.repository.FavoriteRepository
//import kr.co.ongil.presentation.ui.favorite.PlaceData
//import kr.co.ongil.presentation.ui.favorite.PatientData
//import kr.co.ongil.data.model.favorite.PlaceDetailUpdateDto
//
//class FakeFavoriteRepository : FavoriteRepository {
//
//    companion object {
//        @Volatile
//        private var INSTANCE: FakeFavoriteRepository? = null
//
//        fun getInstance(): FakeFavoriteRepository {
//            return INSTANCE ?: synchronized(this) {
//                INSTANCE ?: FakeFavoriteRepository().also { INSTANCE = it }
//            }
//        }
//    }
//
//    // 앱 내부 임시 저장소 (백엔드 나오면 Retrofit/DB로 교체)
//
//    // Patient 더미 데이터
//    private val favoritePatients: MutableList<PatientData> =
//        mutableListOf(
//            PatientData(
//                id = 1L,
//                name = "김철수 할아버지",
//                phoneNumber = "010-1234-5678",
//                gender = "남성",
//                registeredDate = "2024-01-15"
//            ),
//            PatientData(
//                id = 2L,
//                name = "이영희 할머니",
//                phoneNumber = "010-9876-5432",
//                gender = "여성",
//                registeredDate = "2024-02-20"
//            ),
//            PatientData(
//                id = 3L,
//                name = "박민수 어르신",
//                phoneNumber = "010-5555-2222",
//                gender = "남성",
//                registeredDate = "2024-03-10"
//            )
//        )
//
//
//    suspend fun updatePatientDetail(
//        updatedId: Long,
//        newName: String,
//        newPhoneNumber: String,
//        newGender: String
//    ): Boolean {
//        val index = favoritePatients.indexOfFirst { it.id == updatedId }
//        if (index == -1) return false
//
//        val old = favoritePatients[index]
//        favoritePatients[index] = old.copy(
//            name = newName,
//            phoneNumber = newPhoneNumber,
//            gender = newGender
//            // registeredDate는 유지 (환자 등록 시점이므로 바뀌지 않는 값)
//        )
//
//        return true
//    }
//
//    suspend fun deletePatient(
//        patientId: Long
//    ): Boolean {
//        return favoritePatients.removeIf { it.id == patientId }
//    }
//
//
//
//
//    // Place 더미 데이터
//    private val favoritePlacesPerPatient: MutableMap<Long, MutableList<PlaceData>> =
//        mutableMapOf(
//            1L to mutableListOf(
//                PlaceData(
//                    patientId = 1L,
//                    favoriteId = 1L,
//                    placeName = "자주가는 한의원",
//                    address = "서울특별시 도봉구 방학동 123-45",
//                    isDefault = false
//                ),
//                PlaceData(
//                    patientId = 1L,
//                    favoriteId = 2L,
//                    placeName = "복지센터",
//                    address = "서울특별시 도봉구 마들로 536",
//                    isDefault = true
//                ),
//                PlaceData(
//                    patientId = 1L,
//                    favoriteId = 3L,
//                    placeName = "노원구 치매안심센터",
//                    address = "서울특별시 노원구 동일로 123-67",
//                    isDefault = false
//                )
//            )
//        )
//
//    // 환자별 장소 목록 조회
//    override suspend fun getFavoritePlaces(
//        patientId: Long
//    ): Result<FavoritePlaces> = runCatching {
//        val places = favoritePlacesPerPatient[patientId]?.map { placeData ->
//            FavoritePlace(
//                favoriteId = placeData.favoriteId,
//                patientId = placeData.patientId,
//                placeName = placeData.placeName,
//                placeAlias = "",
//                category = "",
//                address = placeData.address,
//                latitude = 0.0,
//                longitude = 0.0,
//                isDefault = placeData.isDefault,
//                count = 0,
//                createdAt = ""
//            )
//        } ?: emptyList()
//        FavoritePlaces(totalCount = places.size, items = places)
//    }
//
//    suspend fun getFavoritePlacesOld(
//        patientId: Long
//    ): List<PlaceData> {
//        // TODO: 나중에 Retrofit으로 교체, 지금은 더미
//        return favoritePlacesPerPatient[patientId]?.toList() ?: emptyList()
//    }
//
//    // 장소 상세 조회
//    override suspend fun getFavoritePlaceDetail(
//        patientId: Long,
//        favoriteId: Long
//    ): Result<FavoritePlace> = runCatching {
//        val list = favoritePlacesPerPatient[patientId] ?: throw NoSuchElementException("No places for patient")
//        val placeData = list.find { it.favoriteId == favoriteId } ?: throw NoSuchElementException("Place not found")
//        FavoritePlace(
//            favoriteId = placeData.favoriteId,
//            patientId = placeData.patientId,
//            placeName = placeData.placeName,
//            placeAlias = "",
//            category = "",
//            address = placeData.address,
//            latitude = 0.0,
//            longitude = 0.0,
//            isDefault = placeData.isDefault,
//            count = 0,
//            createdAt = ""
//        )
//    }
//
//    suspend fun getFavoritePlaceDetailOld(
//        patientId: Long,
//        favoriteId: Long
//    ): PlaceData? {
//        val list = favoritePlacesPerPatient[patientId] ?: return null
//        return list.find { it.favoriteId == favoriteId }
//    }
//
//    // 장소명/주소 수정
//    override suspend fun updateFavoritePlace(
//        patientId: Long,
//        favoriteId: Long,
//        body: PlaceDetailUpdateDto
//    ): Result<FavoritePlace> = runCatching {
//        val list = favoritePlacesPerPatient[patientId] ?: throw NoSuchElementException("No places for patient")
//        val index = list.indexOfFirst { it.favoriteId == favoriteId }
//        if (index == -1) throw NoSuchElementException("Place not found")
//
//        val old = list[index]
//        val updated = old.copy(
//            placeName = body.placeName,
//            address = body.address
//        )
//        list[index] = updated
//
//        FavoritePlace(
//            favoriteId = updated.favoriteId,
//            patientId = updated.patientId,
//            placeName = updated.placeName,
//            placeAlias = "",
//            category = "",
//            address = updated.address,
//            latitude = 0.0,
//            longitude = 0.0,
//            isDefault = updated.isDefault,
//            count = 0,
//            createdAt = ""
//        )
//    }
//
//    suspend fun updatePlaceInfo(
//        patientId: Long,
//        favoriteId: Long,
//        newName: String,
//        newAddress: String
//    ): Boolean {
//        val list = favoritePlacesPerPatient[patientId] ?: return false
//        val index = list.indexOfFirst { it.favoriteId == favoriteId }
//        if (index == -1) return false
//
//        val old = list[index]
//        list[index] = old.copy(
//            placeName = newName,
//            address = newAddress
//        )
//        return true
//    }
//
//    // 장소 삭제
//    override suspend fun deleteFavoritePlace(
//        patientId: Long,
//        favoriteId: Long
//    ): Result<Unit> = runCatching {
//        val list = favoritePlacesPerPatient[patientId] ?: throw NoSuchElementException("No places for patient")
//        val removed = list.removeIf { it.favoriteId == favoriteId }
//        if (!removed) throw NoSuchElementException("Place not found")
//    }
//
//    suspend fun deletePlace(
//        patientId: Long,
//        favoriteId: Long
//    ): Boolean {
//        val list = favoritePlacesPerPatient[patientId] ?: return false
//        val removed = list.removeIf { it.favoriteId == favoriteId }
//        return removed
//    }
//
//    // 기본 목적지 설정 (이 환자에서 favoriteId만 isDefault=true)
//    suspend fun setDefaultPlace(
//        patientId: Long,
//        favoriteId: Long
//    ): Boolean {
//        val list = favoritePlacesPerPatient[patientId] ?: return false
//        var found = false
//
//        // 먼저 전체 false
//        for (i in list.indices) {
//            val p = list[i]
//            val shouldBeDefault = (p.favoriteId == favoriteId)
//            if (shouldBeDefault) found = true
//            list[i] = p.copy(isDefault = shouldBeDefault)
//        }
//
//        return found
//    }
//
//    // ===== Patient 관련 메서드들 =====
//
//    // 즐겨찾기 환자 목록 조회
//    suspend fun getFavoritePatients(): List<PatientData> {
//        // TODO: 나중에 Retrofit으로 교체, 지금은 더미
//        return favoritePatients.toList()
//    }
//
//    // 환자 상세 조회
//    suspend fun getFavoritePatientDetail(patientId: Long): PatientData? {
//        return favoritePatients.find { it.id == patientId }
//    }
//
//    // 환자 추가
//    suspend fun addFavoritePatient(patientData: PatientData): Boolean {
//        return try {
//            favoritePatients.add(patientData)
//            true
//        } catch (e: Exception) {
//            false
//        }
//    }
//
//    // 환자 제거
//    suspend fun removeFavoritePatient(patientId: Long): Boolean {
//        return favoritePatients.removeIf { it.id == patientId }
//    }
//}