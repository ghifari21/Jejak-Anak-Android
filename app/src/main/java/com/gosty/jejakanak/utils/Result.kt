package com.gosty.jejakanak.utils

sealed class Result<out R> private constructor() {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error<out E>(val errorData: E) : Result<Nothing>()
    data object Loading : Result<Nothing>()
}

fun <T, U> Result<T>.map(transform: (T) -> U): Result<U> {
    return when (this) {
        is Result.Success -> Result.Success(transform(data))
        is Result.Error<*> -> this
        is Result.Loading -> this
    }
}