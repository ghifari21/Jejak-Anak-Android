package com.gosty.jejakanak.core.data.models

import android.os.Parcelable
import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties
import kotlinx.parcelize.Parcelize

@IgnoreExtraProperties
@Parcelize
data class ChildModel(
    val id: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val email: String? = null,
    val uniqueCode: String? = null,
    val phone: String? = null,
    val grade: String? = null,
    var parentId: List<String>? = null,
    val coordinate: CoordinateModel? = null
) : Parcelable {
    @Exclude
    fun toMap(): Map<String, Any?> =
        mapOf(
            "id" to id,
            "firstName" to firstName,
            "lastName" to lastName,
            "email" to email,
            "uniqueCode" to uniqueCode,
            "phone" to phone,
            "grade" to grade,
            "parentId" to parentId,
            "coordinate" to coordinate
        )
}