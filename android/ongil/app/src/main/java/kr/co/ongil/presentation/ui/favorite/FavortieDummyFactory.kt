package kr.co.ongil.presentation.ui.favorite

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kr.co.ongil.data.repository.FavoriteRepository


class FavoriteDummyFactory(
    private val initialPatientId: Long
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        if (modelClass.isAssignableFrom(FavoriteViewModel::class.java)) {

            // 싱글톤 Repository 주입
            val repo = FavoriteRepository.getInstance()

            // FavoriteViewModel의 생성자에 우리가 직접 값을 넣어서 만든다
            @Suppress("UNCHECKED_CAST")
            return FavoriteViewModel(
                favoriteRepository = repo,
                initialPatientId = initialPatientId
            ) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}