package com.gosty.jejakanak.data.repositories

import androidx.lifecycle.LiveData
import com.google.firebase.auth.AuthCredential
import com.gosty.jejakanak.data.models.ChildModel
import com.gosty.jejakanak.data.models.CoordinateModel
import com.gosty.jejakanak.data.models.ParentModel
import com.gosty.jejakanak.utils.Result

interface UserRepository {
    fun signIn(credential: AuthCredential, isParent: Boolean): LiveData<Result<String>>

    fun updateChildCoordinate(coordinate: CoordinateModel): LiveData<Result<String>>

    fun addChild(email: String): LiveData<Result<String>>

    fun updateParentProfile(user: ParentModel): LiveData<Result<String>>

    fun getParentProfile(): LiveData<Result<ParentModel>>

    fun updateChildProfile(user: ChildModel): LiveData<Result<String>>

    fun getChildProfile(): LiveData<Result<ChildModel>>
}