package kr.co.ongil.domain.repository

import kotlinx.coroutines.flow.Flow
import kr.co.ongil.data.model.health.HealthData

interface HealthConnectRepository {
    fun getHealthData(): Flow<Result<HealthData>>
    suspend fun checkPermissions(): Boolean
    suspend fun getPermissionsToRequest(): Set<String>
}
