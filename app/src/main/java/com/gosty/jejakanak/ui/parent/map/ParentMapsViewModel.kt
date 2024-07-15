package com.gosty.jejakanak.ui.parent.map

import androidx.lifecycle.ViewModel
import com.gosty.jejakanak.core.domain.usecases.GeofenceUseCase
import com.gosty.jejakanak.core.domain.usecases.UserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ParentMapsViewModel @Inject constructor(
    private val userUseCase: UserUseCase,
    private val geofenceUseCase: GeofenceUseCase
) : ViewModel() {
    fun getAllGeofences() = geofenceUseCase.getAllGeofences()

    fun getAllChildren() = userUseCase.getAllChildren()
}