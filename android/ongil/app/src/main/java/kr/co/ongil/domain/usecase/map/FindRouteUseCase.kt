package kr.co.ongil.domain.usecase.map

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kr.co.ongil.data.mapper.toRoute
import kr.co.ongil.domain.model.Route
import kr.co.ongil.domain.repository.MapRepository
import javax.inject.Inject

/**
 * 길안내 시작 결과
 */
data class NavigationStartResult(
    val route: Route,
    val navigationId: String
)

/**
 * 경로 탐색 UseCase
 * Repository를 통해 경로 데이터를 가져오고, Dto를 Domain 모델로 변환한다.
*/
class FindRouteUseCase @Inject constructor(
    private val mapRepository: MapRepository
) {
    // invoke 함수를 사용하여 UseCase를 함수처럼 호출할 수 있게 함
    suspend operator fun invoke(
        patientId: Long,
        startLatitude: Double,
        startLongitude: Double,
        startName: String,endLatitude: Double,
        endLongitude: Double,
        endName: String,
        initiatedBy: String
    ): Result<NavigationStartResult> {
// Repository에 길안내 시작을 요청
        return mapRepository.startNavigation(
            patientId = patientId,
            startLatitude = startLatitude,
            startLongitude = startLongitude,
            startName = startName,
            endLatitude = endLatitude,
            endLongitude = endLongitude,
            endName = endName,
            initiatedBy = initiatedBy
        ).mapCatching { response ->
            // ✅ 성공 시, 응답의 RouteDto를 toRoute() 매퍼를 사용해 Route로 변환하고 navigationId와 함께 반환
            NavigationStartResult(
                route = response.data.route.toRoute(),
                navigationId = response.data.navigationId
            )
        }
    }
}


