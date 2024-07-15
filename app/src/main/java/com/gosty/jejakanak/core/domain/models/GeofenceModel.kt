package com.gosty.jejakanak.core.domain.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class GeofenceModel(
    val id: String? = null,
    val label: String? = null,
    val type: String? = null,
    val parentId: String? = null,
    val coordinates: List<CoordinateModel>? = null
) : Parcelable
