package kr.co.ongil.presentation.ui.map

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kr.co.ongil.common.location.LocationStreamBus
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    val locationBus: LocationStreamBus
) : ViewModel()
