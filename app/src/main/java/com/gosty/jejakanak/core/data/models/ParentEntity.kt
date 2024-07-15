package com.gosty.jejakanak.core.data.models

import android.os.Parcelable
import com.google.firebase.database.IgnoreExtraProperties
import kotlinx.parcelize.Parcelize

@IgnoreExtraProperties
@Parcelize
data class ParentEntity(
    val id: String? = null,
    val photo: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val childrenId: List<String>? = null,
    val geofences: List<GeofenceEntity>? = null
) : Parcelable {
    fun toMap(): Map<String, Any?> =
        mapOf(
            "id" to id,
            "photo" to photo,
            "firstName" to firstName,
            "lastName" to lastName,
            "email" to email,
            "phone" to phone,
            "childrenId" to childrenId,
            "geofences" to geofences
        )
}
