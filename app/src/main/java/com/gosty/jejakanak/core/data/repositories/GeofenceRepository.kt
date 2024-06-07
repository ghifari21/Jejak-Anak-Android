package com.gosty.jejakanak.core.data.repositories

import androidx.lifecycle.LiveData
import com.gosty.jejakanak.core.data.models.GeofenceModel
import com.gosty.jejakanak.utils.Result

interface GeofenceRepository {
    fun addGeofence(geofence: GeofenceModel): LiveData<Result<String>>

    fun removeGeofence(id: String): LiveData<Result<String>>

    fun getAllGeofences(): LiveData<Result<List<GeofenceModel>>>
}