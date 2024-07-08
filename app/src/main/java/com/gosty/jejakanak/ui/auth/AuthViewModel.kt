package com.gosty.jejakanak.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.AuthCredential
import com.gosty.jejakanak.core.domain.usecases.UserUseCase
import com.gosty.jejakanak.utils.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val userUseCase: UserUseCase
) : ViewModel() {
    fun signIn(credential: AuthCredential, isParent: Boolean): LiveData<Result<String>> =
        userUseCase.signIn(credential, isParent)

    fun isUserPhoneNumberExist() = userUseCase.isUserPhoneNumberExist()

    fun inputUserPhoneNumber(phoneNumber: String) = userUseCase.inputUserPhoneNumber(phoneNumber)

    fun setUserRole(role: String) = userUseCase.setUserRole(role)
}