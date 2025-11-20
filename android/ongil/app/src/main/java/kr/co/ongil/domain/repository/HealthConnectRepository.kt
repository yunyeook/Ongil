package kr.co.ongil.domain.repository

import kotlinx.coroutines.flow.Flow
import kr.co.ongil.data.model.health.LocalHealthData

interface HealthConnectRepository {
    fun getHealthData(): Flow<Result<LocalHealthData>>
    suspend fun checkPermissions(): Boolean
    suspend fun getPermissionsToRequest(): Set<String>
}
