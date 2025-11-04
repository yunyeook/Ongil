package kr.co.ongil.presentation.ui.favorite

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kr.co.ongil.data.repository.fake.FakeFavoriteRepository


class FavoriteDummyFactory(
    private val initialPatientId: Long
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        if (modelClass.isAssignableFrom(FavoriteViewModel::class.java)) {

            val repo = FakeFavoriteRepository.getInstance()

            @Suppress("UNCHECKED_CAST")
            return FavoriteViewModel(
                fakeFavoriteRepository = repo,
                initialPatientId = initialPatientId
            ) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}