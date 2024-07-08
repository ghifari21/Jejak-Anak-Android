package com.gosty.jejakanak.core.data.models

import android.os.Parcelable
import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties
import kotlinx.parcelize.Parcelize

@IgnoreExtraProperties
@Parcelize
data class GeofenceEntity(
    var id: String? = null,
    val label: String? = null,
    val type: String? = null,
    var parentId: String? = null,
    val coordinates: List<CoordinateEntity>? = null
) : Parcelable {
    @Exclude
    fun toMap(): Map<String, Any?> =
        mapOf(
            "id" to id,
            "label" to label,
            "type" to type,
            "parentId" to parentId,
            "coordinates" to coordinates
        )
}
