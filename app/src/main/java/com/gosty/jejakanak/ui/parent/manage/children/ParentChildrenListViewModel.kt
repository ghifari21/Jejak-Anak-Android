package com.gosty.jejakanak.ui.parent.manage.children

import androidx.lifecycle.ViewModel
import com.gosty.jejakanak.core.domain.usecases.UserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ParentChildrenListViewModel @Inject constructor(
    private val userUseCase: UserUseCase
) : ViewModel() {
    fun getAllChildren() = userUseCase.getAllChildren()

    fun addChild(uniqueCode: String) = userUseCase.addChild(uniqueCode)

    fun removeChild(childId: String) = userUseCase.removeChild(childId)
}