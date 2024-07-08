package com.gosty.jejakanak.core.data.repositories

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.google.firebase.auth.AuthCredential
import com.gosty.jejakanak.core.data.source.FirebaseDataSource
import com.gosty.jejakanak.core.data.source.LocalDataSource
import com.gosty.jejakanak.core.domain.models.ChildModel
import com.gosty.jejakanak.core.domain.models.CoordinateModel
import com.gosty.jejakanak.core.domain.models.ParentModel
import com.gosty.jejakanak.core.domain.repositories.UserRepository
import com.gosty.jejakanak.utils.Result
import com.gosty.jejakanak.utils.map
import com.gosty.jejakanak.utils.toEntity
import com.gosty.jejakanak.utils.toModel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val firebaseDataSource: FirebaseDataSource,
    private val localDataSource: LocalDataSource
) : UserRepository {
    override fun signIn(credential: AuthCredential, isParent: Boolean): LiveData<Result<String>> =
        firebaseDataSource.signIn(credential, isParent)

    override fun isUserPhoneNumberExist(): LiveData<Result<Boolean>> =
        firebaseDataSource.isUserPhoneNumberExist(localDataSource.getUserRole() == "Parent")

    override fun inputUserPhoneNumber(phoneNumber: String): LiveData<Result<String>> =
        firebaseDataSource.inputUserPhoneNumber(
            phoneNumber,
            localDataSource.getUserRole() == "Parent"
        )

    override fun updateChildCoordinate(coordinate: CoordinateModel): LiveData<Result<String>> =
        firebaseDataSource.updateChildCoordinate(coordinate.toEntity())

    override fun addChild(uniqueCode: String): LiveData<Result<String>> =
        firebaseDataSource.addChild(uniqueCode)

    override fun removeChild(id: String): LiveData<Result<String>> =
        firebaseDataSource.removeChild(id)

    override fun getAllChildren(): LiveData<Result<List<ChildModel>>> =
        firebaseDataSource.getAllChildren().map {
            it.map { list ->
                list.map { entity ->
                    entity.toModel()
                }
            }
        }

    override fun getAllChildrenService(callback: (List<ChildModel>) -> Unit) =
        firebaseDataSource.getAllChildrenService { list ->
            callback(list.map { it.toModel() })
        }

    override fun getAllChildrenOnceService(callback: (List<ChildModel>) -> Unit) =
        firebaseDataSource.getAllChildrenOnceService { list ->
            callback(list.map { it.toModel() })
        }

    override fun updateParentProfile(user: ParentModel): LiveData<Result<String>> =
        firebaseDataSource.updateParentProfile(user.toEntity())

    override fun getParentProfile(): LiveData<Result<ParentModel>> =
        firebaseDataSource.getParentProfile().map {
            it.map { entity ->
                entity.toModel()
            }
        }

    override fun updateChildProfile(user: ChildModel): LiveData<Result<String>> =
        firebaseDataSource.updateChildProfile(user.toEntity())

    override fun getChildProfile(): LiveData<Result<ChildModel>> =
        firebaseDataSource.getChildProfile().map {
            it.map { entity ->
                entity.toModel()
            }
        }

    override fun getParentsProfile(): LiveData<Result<List<ParentModel>>> =
        firebaseDataSource.getParentsProfile().map {
            it.map { list ->
                list.map { entity ->
                    entity.toModel()
                }
            }
        }

    override fun getUserRole(): String = localDataSource.getUserRole()

    override fun setUserRole(role: String) = localDataSource.setUserRole(role)
}