package com.gosty.jejakanak.core.data.source

import androidx.lifecycle.LiveData
import com.google.firebase.auth.AuthCredential
import com.gosty.jejakanak.core.data.models.ChildModel
import com.gosty.jejakanak.core.data.models.CoordinateModel
import com.gosty.jejakanak.core.data.models.GeofenceModel
import com.gosty.jejakanak.core.data.models.ParentModel
import com.gosty.jejakanak.utils.Result

interface FirebaseDataSource {
    fun signIn(credential: AuthCredential, isParent: Boolean): LiveData<Result<String>>

    fun updateChildCoordinate(coordinate: CoordinateModel): LiveData<Result<String>>

    fun addChild(email: String): LiveData<Result<String>>

    fun getAllChildren(): LiveData<Result<List<ChildModel>>>

    fun addGeofence(geofence: GeofenceModel): LiveData<Result<String>>

    fun removeGeofence(id: String): LiveData<Result<String>>

    fun getAllGeofences(): LiveData<Result<List<GeofenceModel>>>

    fun updateParentProfile(user: ParentModel): LiveData<Result<String>>

    fun getParentProfile(): LiveData<Result<ParentModel>>

    fun updateChildProfile(user: ChildModel): LiveData<Result<String>>

    fun getChildProfile(): LiveData<Result<ChildModel>>
}