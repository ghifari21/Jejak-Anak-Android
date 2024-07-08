package com.gosty.jejakanak.core.data.source

import androidx.lifecycle.LiveData
import com.google.firebase.auth.AuthCredential
import com.gosty.jejakanak.core.data.models.ChildEntity
import com.gosty.jejakanak.core.data.models.CoordinateEntity
import com.gosty.jejakanak.core.data.models.GeofenceEntity
import com.gosty.jejakanak.core.data.models.ParentEntity
import com.gosty.jejakanak.utils.Result

interface FirebaseDataSource {
    fun signIn(credential: AuthCredential, isParent: Boolean): LiveData<Result<String>>

    fun isUserPhoneNumberExist(isParent: Boolean): LiveData<Result<Boolean>>

    fun inputUserPhoneNumber(phoneNumber: String, isParent: Boolean): LiveData<Result<String>>

    fun updateChildCoordinate(coordinate: CoordinateEntity): LiveData<Result<String>>

    fun addChild(uniqueCode: String): LiveData<Result<String>>

    fun getAllChildren(): LiveData<Result<List<ChildEntity>>>

    fun getAllChildrenService(callback: (List<ChildEntity>) -> Unit)

    fun getAllChildrenOnceService(callback: (List<ChildEntity>) -> Unit)

    fun removeChild(childId: String): LiveData<Result<String>>

    fun addGeofence(geofence: GeofenceEntity): LiveData<Result<String>>

    fun removeGeofence(id: String): LiveData<Result<String>>

    fun updateGeofence(geofence: GeofenceEntity): LiveData<Result<String>>

    fun getAllGeofences(isParent: Boolean): LiveData<Result<List<GeofenceEntity>>>

    fun getAllGeofencesService(isParent: Boolean, callback: (List<GeofenceEntity>) -> Unit)

    fun getAllGeofencesOnceService(isParent: Boolean, callback: (List<GeofenceEntity>) -> Unit)

    fun updateParentProfile(user: ParentEntity): LiveData<Result<String>>

    fun getParentProfile(): LiveData<Result<ParentEntity>>

    fun updateChildProfile(user: ChildEntity): LiveData<Result<String>>

    fun getChildProfile(): LiveData<Result<ChildEntity>>

    fun getParentsProfile(): LiveData<Result<List<ParentEntity>>>
}