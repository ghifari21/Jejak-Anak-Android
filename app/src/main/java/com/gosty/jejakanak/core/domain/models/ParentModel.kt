package com.gosty.jejakanak.core.domain.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ParentModel(
    val id: String? = null,
    val photo: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val childrenId: List<String>? = null,
    val geofences: List<GeofenceModel>? = null
) : Parcelable
