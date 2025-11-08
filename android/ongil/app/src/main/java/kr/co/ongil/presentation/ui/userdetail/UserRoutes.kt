package kr.co.ongil.presentation.ui.userdetail

import android.net.Uri

object UserRoutes {
    // 사용자 상세 Graph의 Route (필요하다면 추가)
    const val GRAPH_ROUTE = "user_graph"

    // 사용자 상세 화면 Route (relationshipId만 사용)
    const val DETAIL_ROUTE = "user_detail/{relationshipId}"

    fun createRoute(relationshipId: Long): String {
        return "user_detail/$relationshipId"
    }
}