package kr.co.ongil.data.model.dashboard

import kotlinx.serialization.Serializable

@Serializable
data class DashboardResponse(
    val message: String,
    val data: DashboardDto
)
