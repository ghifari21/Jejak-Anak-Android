package com.gosty.jejakanak.core.domain.usecases

import androidx.lifecycle.LiveData
import com.google.firebase.auth.AuthCredential
import com.gosty.jejakanak.core.domain.models.ChildModel
import com.gosty.jejakanak.core.domain.models.CoordinateModel
import com.gosty.jejakanak.core.domain.models.ParentModel
import com.gosty.jejakanak.core.domain.repositories.UserRepository
import com.gosty.jejakanak.utils.Result
import javax.inject.Inject

class UserUseCaseImpl @Inject constructor(
    private val userRepository: UserRepository
) : UserUseCase {
    override fun signIn(credential: AuthCredential, isParent: Boolean): LiveData<Result<String>> =
        userRepository.signIn(credential, isParent)

    override suspend fun signOut() = userRepository.signOut()

    override fun isUserPhoneNumberExist(): LiveData<Result<Boolean>> =
        userRepository.isUserPhoneNumberExist()

    override fun inputUserPhoneNumber(phoneNumber: String): LiveData<Result<String>> =
        userRepository.inputUserPhoneNumber(phoneNumber)

    override fun updateChildCoordinate(coordinate: CoordinateModel): LiveData<Result<String>> =
        userRepository.updateChildCoordinate(coordinate)

    override fun addChild(uniqueCode: String): LiveData<Result<String>> =
        userRepository.addChild(uniqueCode)

    override fun removeChild(id: String): LiveData<Result<String>> =
        userRepository.removeChild(id)

    override fun getAllChildren(): LiveData<Result<List<ChildModel>>> =
        userRepository.getAllChildren()

    override fun getAllChildrenService(callback: (List<ChildModel>) -> Unit) =
        userRepository.getAllChildrenService(callback)

    override fun getAllChildrenOnceService(callback: (List<ChildModel>) -> Unit) =
        userRepository.getAllChildrenOnceService(callback)

    override fun updateParentProfile(user: ParentModel): LiveData<Result<String>> =
        userRepository.updateParentProfile(user)

    override fun getParentProfile(): LiveData<Result<ParentModel>> =
        userRepository.getParentProfile()

    override fun updateChildProfile(user: ChildModel): LiveData<Result<String>> =
        userRepository.updateChildProfile(user)

    override fun getChildProfile(): LiveData<Result<ChildModel>> =
        userRepository.getChildProfile()

    override fun getParentsProfile(): LiveData<Result<List<ParentModel>>> =
        userRepository.getParentsProfile()

    override fun getUserRole(): String = userRepository.getUserRole()

    override fun setUserRole(role: String) = userRepository.setUserRole(role)
}