package com.gosty.jejakanak.core.data.models

import android.os.Parcelable
import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties
import kotlinx.parcelize.Parcelize

@IgnoreExtraProperties
@Parcelize
data class CoordinateEntity(
    var id: String? = null,
    val dateTime: Long? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val updatedAt: Long? = null
) : Parcelable {
    @Exclude
    fun toMap(): Map<String, Any?> =
        mapOf(
            "id" to id,
            "dateTime" to dateTime,
            "latitude" to latitude,
            "longitude" to longitude,
            "updatedAt" to updatedAt
        )
}
