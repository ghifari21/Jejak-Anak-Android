package com.gosty.jejakanak.ui.splash

import androidx.lifecycle.ViewModel
import com.gosty.jejakanak.core.domain.usecases.UserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val userUseCase: UserUseCase
) : ViewModel() {
    fun getUserRole(): String = userUseCase.getUserRole()
}