package com.gosty.jejakanak.core.domain.usecases

import androidx.lifecycle.LiveData
import com.gosty.jejakanak.core.domain.models.GeofenceModel
import com.gosty.jejakanak.core.domain.repositories.GeofenceRepository
import com.gosty.jejakanak.utils.Result
import javax.inject.Inject

class GeofenceUseCaseImpl @Inject constructor(
    private val geofenceRepository: GeofenceRepository
) : GeofenceUseCase {
    override fun addGeofence(geofence: GeofenceModel): LiveData<Result<String>> =
        geofenceRepository.addGeofence(geofence)

    override fun removeGeofence(id: String): LiveData<Result<String>> =
        geofenceRepository.removeGeofence(id)

    override fun updateGeofence(geofence: GeofenceModel): LiveData<Result<String>> =
        geofenceRepository.updateGeofence(geofence)

    override fun getAllGeofences(): LiveData<Result<List<GeofenceModel>>> =
        geofenceRepository.getAllGeofences()

    override fun getAllGeofencesService(callback: (List<GeofenceModel>) -> Unit) =
        geofenceRepository.getAllGeofencesService(callback)

    override fun getAllGeofencesOnceService(callback: (List<GeofenceModel>) -> Unit) =
        geofenceRepository.getAllGeofencesOnceService(callback)
}