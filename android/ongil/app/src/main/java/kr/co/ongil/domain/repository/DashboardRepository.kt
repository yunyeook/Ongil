package kr.co.ongil.domain.repository

import kotlinx.coroutines.flow.Flow
import kr.co.ongil.data.model.dashboard.DashboardDto

interface DashboardRepository {
    fun getDashboard(patientId: Int): Flow<Result<DashboardDto>>
}
