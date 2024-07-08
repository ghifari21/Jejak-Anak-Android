package com.gosty.jejakanak.core.domain.repositories

import androidx.lifecycle.LiveData
import com.gosty.jejakanak.core.domain.models.GeofenceModel
import com.gosty.jejakanak.utils.Result

interface GeofenceRepository {
    fun addGeofence(geofence: GeofenceModel): LiveData<Result<String>>

    fun removeGeofence(id: String): LiveData<Result<String>>

    fun updateGeofence(geofence: GeofenceModel): LiveData<Result<String>>

    fun getAllGeofences(): LiveData<Result<List<GeofenceModel>>>

    fun getAllGeofencesService(callback: (List<GeofenceModel>) -> Unit)

    fun getAllGeofencesOnceService(callback: (List<GeofenceModel>) -> Unit)
}