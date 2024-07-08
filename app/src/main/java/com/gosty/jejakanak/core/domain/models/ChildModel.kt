package com.gosty.jejakanak.core.domain.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ChildModel(
    val id: String? = null,
    val photo: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val email: String? = null,
    val uniqueCode: String? = null,
    val phone: String? = null,
    val grade: String? = null,
    var parentId: List<String>? = null,
    val coordinate: CoordinateModel? = null,
    val geofences: List<GeofenceModel>? = null
) : Parcelable
