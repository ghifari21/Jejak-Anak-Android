package com.gosty.jejakanak.ui.child.profile

import androidx.lifecycle.ViewModel
import com.gosty.jejakanak.core.domain.usecases.UserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ChildProfileViewModel @Inject constructor(
    private val userUseCase: UserUseCase
) : ViewModel() {
    fun getChildProfile() = userUseCase.getChildProfile()

    fun getParentsProfile() = userUseCase.getParentsProfile()

    fun deleteUserRole() = userUseCase.setUserRole("")
}