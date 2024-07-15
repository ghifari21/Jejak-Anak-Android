package com.gosty.jejakanak.ui.parent.manage.geofence

import androidx.lifecycle.ViewModel
import com.gosty.jejakanak.core.domain.models.GeofenceModel
import com.gosty.jejakanak.core.domain.usecases.GeofenceUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ParentGeofencesViewModel @Inject constructor(
    private val geofenceUseCase: GeofenceUseCase
) : ViewModel() {
    fun getAllGeofences() = geofenceUseCase.getAllGeofences()

    fun addGeofence(geofence: GeofenceModel) = geofenceUseCase.addGeofence(geofence)

    fun removeGeofence(id: String) = geofenceUseCase.removeGeofence(id)

    fun updateGeofence(geofence: GeofenceModel) = geofenceUseCase.updateGeofence(geofence)
}