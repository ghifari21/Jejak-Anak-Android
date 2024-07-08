package com.gosty.jejakanak.core.domain.usecases

import androidx.lifecycle.LiveData
import com.gosty.jejakanak.core.domain.models.GeofenceModel
import com.gosty.jejakanak.utils.Result

interface GeofenceUseCase {
    fun addGeofence(geofence: GeofenceModel): LiveData<Result<String>>

    fun removeGeofence(id: String): LiveData<Result<String>>

    fun updateGeofence(geofence: GeofenceModel): LiveData<Result<String>>

    fun getAllGeofences(): LiveData<Result<List<GeofenceModel>>>

    fun getAllGeofencesService(callback: (List<GeofenceModel>) -> Unit)

    fun getAllGeofencesOnceService(callback: (List<GeofenceModel>) -> Unit)
}