package com.gosty.jejakanak.ui.parent.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gosty.jejakanak.core.domain.usecases.UserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ParentProfileViewModel @Inject constructor(
    private val userUseCase: UserUseCase
) : ViewModel() {
    fun getUser() = userUseCase.getParentProfile()

    fun signOut() {
        viewModelScope.launch {
            userUseCase.signOut()
        }
    }
}