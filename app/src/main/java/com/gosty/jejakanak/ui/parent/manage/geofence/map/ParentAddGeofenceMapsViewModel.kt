package com.gosty.jejakanak.ui.parent.manage.geofence.map

import androidx.lifecycle.ViewModel
import com.gosty.jejakanak.core.domain.usecases.GeofenceUseCase
import com.gosty.jejakanak.core.domain.usecases.UserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ParentAddGeofenceMapsViewModel @Inject constructor(
    private val userUseCase: UserUseCase,
    private val geofenceUseCase: GeofenceUseCase
) : ViewModel() {
    fun getAllGeofences() = geofenceUseCase.getAllGeofences()

    fun getAllChildren() = userUseCase.getAllChildren()
}