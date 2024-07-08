package com.gosty.jejakanak.core.domain.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CoordinateModel(
    val id: String?,
    val dateTime: Long?,
    val latitude: Double?,
    val longitude: Double?,
    val updatedAt: Long?,
) : Parcelable
