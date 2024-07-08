package com.gosty.jejakanak.ui.child.map

import androidx.lifecycle.ViewModel
import com.gosty.jejakanak.core.domain.usecases.GeofenceUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ChildMapsViewModel @Inject constructor(
    private val geofenceUseCase: GeofenceUseCase
) : ViewModel() {
    fun getAllGeofences() = geofenceUseCase.getAllGeofences()
}