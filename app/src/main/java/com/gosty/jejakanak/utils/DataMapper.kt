package com.gosty.jejakanak.utils

import com.gosty.jejakanak.core.data.models.ChildEntity
import com.gosty.jejakanak.core.data.models.CoordinateEntity
import com.gosty.jejakanak.core.data.models.GeofenceEntity
import com.gosty.jejakanak.core.data.models.ParentEntity
import com.gosty.jejakanak.core.domain.models.ChildModel
import com.gosty.jejakanak.core.domain.models.CoordinateModel
import com.gosty.jejakanak.core.domain.models.GeofenceModel
import com.gosty.jejakanak.core.domain.models.ParentModel

fun CoordinateEntity.toModel(): CoordinateModel =
    CoordinateModel(
        id = this.id,
        dateTime = this.dateTime,
        latitude = this.latitude,
        longitude = this.longitude,
        updatedAt = this.updatedAt,
    )

fun CoordinateModel.toEntity(): CoordinateEntity =
    CoordinateEntity(
        id = this.id,
        dateTime = this.dateTime,
        latitude = this.latitude,
        longitude = this.longitude,
        updatedAt = this.updatedAt,
    )

fun GeofenceEntity.toModel(): GeofenceModel =
    GeofenceModel(
        id = this.id,
        label = this.label,
        type = this.type,
        parentId = this.parentId,
        coordinates = this.coordinates?.map { it.toModel() },
    )

fun GeofenceModel.toEntity(): GeofenceEntity =
    GeofenceEntity(
        id = this.id,
        label = this.label,
        type = this.type,
        parentId = this.parentId,
        coordinates = this.coordinates?.map { it.toEntity() },
    )

fun ChildEntity.toModel(): ChildModel =
    ChildModel(
        id = this.id,
        photo = this.photo,
        firstName = this.firstName,
        lastName = this.lastName,
        email = this.email,
        phone = this.phone,
        grade = this.grade,
        parentId = this.parentId,
        uniqueCode = this.uniqueCode,
        coordinate = this.coordinate?.toModel(),
        geofences = this.geofences?.map { it.toModel() },
    )

fun ChildModel.toEntity(): ChildEntity =
    ChildEntity(
        id = this.id,
        photo = this.photo,
        firstName = this.firstName,
        lastName = this.lastName,
        email = this.email,
        phone = this.phone,
        grade = this.grade,
        parentId = this.parentId,
        uniqueCode = this.uniqueCode,
        coordinate = this.coordinate?.toEntity(),
        geofences = this.geofences?.map { it.toEntity() },
    )

fun ParentEntity.toModel(): ParentModel =
    ParentModel(
        id = this.id,
        photo = this.photo,
        firstName = this.firstName,
        lastName = this.lastName,
        email = this.email,
        phone = this.phone,
        childrenId = this.childrenId,
        geofences = this.geofences?.map { it.toModel() },
    )

fun ParentModel.toEntity(): ParentEntity =
    ParentEntity(
        id = this.id,
        photo = this.photo,
        firstName = this.firstName,
        lastName = this.lastName,
        email = this.email,
        phone = this.phone,
        childrenId = this.childrenId,
        geofences = this.geofences?.map { it.toEntity() },
    )

fun <T, U> Result<T>.map(transform: (T) -> U): Result<U> {
    return when (this) {
        is Result.Success -> Result.Success(transform(data))
        is Result.Error<*> -> this
        is Result.Loading -> this
    }
}