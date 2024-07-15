package com.gosty.jejakanak.core.domain.usecases

import androidx.lifecycle.LiveData
import com.google.firebase.auth.AuthCredential
import com.gosty.jejakanak.core.domain.models.ChildModel
import com.gosty.jejakanak.core.domain.models.CoordinateModel
import com.gosty.jejakanak.core.domain.models.ParentModel
import com.gosty.jejakanak.utils.Result

interface UserUseCase {
    fun signIn(credential: AuthCredential, isParent: Boolean): LiveData<Result<String>>

    suspend fun signOut()

    fun isUserPhoneNumberExist(): LiveData<Result<Boolean>>

    fun inputUserPhoneNumber(phoneNumber: String): LiveData<Result<String>>

    fun updateChildCoordinate(coordinate: CoordinateModel): LiveData<Result<String>>

    fun addChild(uniqueCode: String): LiveData<Result<String>>

    fun removeChild(id: String): LiveData<Result<String>>

    fun getAllChildren(): LiveData<Result<List<ChildModel>>>

    fun getAllChildrenService(callback: (List<ChildModel>) -> Unit)

    fun getAllChildrenOnceService(callback: (List<ChildModel>) -> Unit)

    fun updateParentProfile(user: ParentModel): LiveData<Result<String>>

    fun getParentProfile(): LiveData<Result<ParentModel>>

    fun updateChildProfile(user: ChildModel): LiveData<Result<String>>

    fun getChildProfile(): LiveData<Result<ChildModel>>

    fun getParentsProfile(): LiveData<Result<List<ParentModel>>>

    fun getUserRole(): String

    fun setUserRole(role: String)
}