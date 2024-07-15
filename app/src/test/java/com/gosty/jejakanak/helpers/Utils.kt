package com.gosty.jejakanak.helpers

import com.gosty.jejakanak.core.domain.models.CoordinateModel
import com.gosty.jejakanak.utils.getRandomString
import java.util.Date

object Utils {
    fun getConvexPolygon() = listOf(
        createPoint(0.0, 0.0),
        createPoint(4.0, 0.0),
        createPoint(4.0, 4.0),
        createPoint(0.0, 4.0)
    )

    fun getConcavePolygon() = listOf(
        createPoint(0.0, 0.0),
        createPoint(4.0, 0.0),
        createPoint(4.0, 4.0),
        createPoint(2.0, 2.0),
        createPoint(0.0, 4.0)
    )

    fun getComplexPolygon() = listOf(
        createPoint(0.0, 0.0),
        createPoint(5.0, 0.0),
        createPoint(6.0, 2.0),
        createPoint(4.0, 4.0),
        createPoint(1.0, 4.0),
        createPoint(0.0, 2.0)
    )

    fun getSelfIntersectPolygon() = listOf(
        createPoint(0.0, 0.0),
        createPoint(4.0, 4.0),
        createPoint(0.0, 4.0),
        createPoint(4.0, 0.0)
    )

    fun createPoint(latitude: Double, longitude: Double) = CoordinateModel(
        id = getRandomString(),
        latitude = latitude,
        longitude = longitude,
        dateTime = Date().time,
        updatedAt = Date().time
    )
}