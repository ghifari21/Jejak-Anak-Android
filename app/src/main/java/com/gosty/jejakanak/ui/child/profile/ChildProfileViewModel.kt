package com.gosty.jejakanak.ui.child.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gosty.jejakanak.core.domain.usecases.UserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChildProfileViewModel @Inject constructor(
    private val userUseCase: UserUseCase
) : ViewModel() {
    fun getChildProfile() = userUseCase.getChildProfile()

    fun getParentsProfile() = userUseCase.getParentsProfile()

    fun signOut() {
        viewModelScope.launch {
            userUseCase.signOut()
        }
    }
}