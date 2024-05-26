package com.gosty.jejakanak.data.repositories

import androidx.lifecycle.LiveData
import com.google.firebase.auth.AuthCredential
import com.gosty.jejakanak.data.models.ChildModel
import com.gosty.jejakanak.data.models.CoordinateModel
import com.gosty.jejakanak.data.models.ParentModel
import com.gosty.jejakanak.data.source.FirebaseDataSource
import com.gosty.jejakanak.utils.Result
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val firebaseDataSource: FirebaseDataSource
) : UserRepository {
    override fun signIn(credential: AuthCredential, isParent: Boolean): LiveData<Result<String>> =
        firebaseDataSource.signIn(credential, isParent)

    override fun updateChildCoordinate(coordinate: CoordinateModel): LiveData<Result<String>> =
        firebaseDataSource.updateChildCoordinate(coordinate)

    override fun addChild(email: String): LiveData<Result<String>> =
        firebaseDataSource.addChild(email)

    override fun updateParentProfile(user: ParentModel): LiveData<Result<String>> =
        firebaseDataSource.updateParentProfile(user)

    override fun getParentProfile(): LiveData<Result<ParentModel>> =
        firebaseDataSource.getParentProfile()

    override fun updateChildProfile(user: ChildModel): LiveData<Result<String>> =
        firebaseDataSource.updateChildProfile(user)

    override fun getChildProfile(): LiveData<Result<ChildModel>> =
        firebaseDataSource.getChildProfile()
}