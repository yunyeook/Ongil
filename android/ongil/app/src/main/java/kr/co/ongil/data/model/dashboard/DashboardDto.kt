package kr.co.ongil.data.model.dashboard

import kotlinx.serialization.Serializable

@Serializable
data class DashboardDto(
    val favoriteName: String?,
    val safezoneExit: Int,
    val routeLost: Int
)
