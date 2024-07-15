package com.gosty.jejakanak.core.data.repositories

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.gosty.jejakanak.core.data.source.FirebaseDataSource
import com.gosty.jejakanak.core.data.source.LocalDataSource
import com.gosty.jejakanak.core.domain.models.GeofenceModel
import com.gosty.jejakanak.core.domain.repositories.GeofenceRepository
import com.gosty.jejakanak.utils.Result
import com.gosty.jejakanak.utils.map
import com.gosty.jejakanak.utils.toEntity
import com.gosty.jejakanak.utils.toModel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeofenceRepositoryImpl @Inject constructor(
    private val firebaseDataSource: FirebaseDataSource,
    private val localDataSource: LocalDataSource
) : GeofenceRepository {
    override fun addGeofence(geofence: GeofenceModel): LiveData<Result<String>> =
        firebaseDataSource.addGeofence(geofence.toEntity())

    override fun removeGeofence(id: String): LiveData<Result<String>> =
        firebaseDataSource.removeGeofence(id)

    override fun updateGeofence(geofence: GeofenceModel): LiveData<Result<String>> =
        firebaseDataSource.updateGeofence(geofence.toEntity())

    override fun getAllGeofences(): LiveData<Result<List<GeofenceModel>>> =
        firebaseDataSource.getAllGeofences(localDataSource.getUserRole() == "Parent").map {
            it.map { list ->
                list.map { entity ->
                    entity.toModel()
                }
            }
        }

    override fun getAllGeofencesService(callback: (List<GeofenceModel>) -> Unit) =
        firebaseDataSource.getAllGeofencesService(localDataSource.getUserRole() == "Parent") { list ->
            callback(list.map { it.toModel() })
        }

    override fun getAllGeofencesOnceService(callback: (List<GeofenceModel>) -> Unit) =
        firebaseDataSource.getAllGeofencesOnceService(localDataSource.getUserRole() == "Parent") { list ->
            callback(list.map { it.toModel() })
        }
}