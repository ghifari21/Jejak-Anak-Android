package com.gosty.jejakanak.data.repositories

import androidx.lifecycle.LiveData
import com.gosty.jejakanak.data.models.GeofenceModel
import com.gosty.jejakanak.data.source.FirebaseDataSource
import com.gosty.jejakanak.utils.Result
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeofenceRepositoryImpl @Inject constructor(
    private val firebaseDataSource: FirebaseDataSource
) : GeofenceRepository {
    override fun addGeofence(geofence: GeofenceModel): LiveData<Result<String>> =
        firebaseDataSource.addGeofence(geofence)

    override fun removeGeofence(id: String): LiveData<Result<String>> =
        firebaseDataSource.removeGeofence(id)

    override fun getAllGeofences(): LiveData<Result<List<GeofenceModel>>> =
        firebaseDataSource.getAllGeofences()
}