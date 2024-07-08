package com.gosty.jejakanak.core.data.models

import android.os.Parcelable
import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties
import kotlinx.parcelize.Parcelize

@IgnoreExtraProperties
@Parcelize
data class ChildEntity(
    val id: String? = null,
    val photo: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val email: String? = null,
    val uniqueCode: String? = null,
    val phone: String? = null,
    val grade: String? = null,
    var parentId: List<String>? = null,
    val coordinate: CoordinateEntity? = null,
    val geofences: List<GeofenceEntity>? = null
) : Parcelable {
    @Exclude
    fun toMap(): Map<String, Any?> =
        mapOf(
            "id" to id,
            "photo" to photo,
            "firstName" to firstName,
            "lastName" to lastName,
            "email" to email,
            "uniqueCode" to uniqueCode,
            "phone" to phone,
            "grade" to grade,
            "parentId" to parentId,
            "coordinate" to coordinate,
            "geofences" to geofences
        )
}
