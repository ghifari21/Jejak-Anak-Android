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

    fun windingNumber(point: CoordinateModel, polygon: List<CoordinateModel>): Int {
        var windingNumber = 0

        for (i in polygon.indices) {
            val currentPoint = polygon[i]
            val nextPoint = polygon[(i + 1) % polygon.size]

            if (isPointOnEdge(point, currentPoint, nextPoint)) return 1

            if (currentPoint.latitude!! <= point.latitude!!) {
                if (nextPoint.latitude!! > point.latitude!! && isLeft(
                        currentPoint,
                        nextPoint,
                        point
                    ) > 0
                ) {
                    windingNumber++
                }
            } else if (nextPoint.latitude!! <= point.latitude!! && isLeft(
                    currentPoint,
                    nextPoint,
                    point
                ) < 0
            ) {
                windingNumber--
            }
        }

        return windingNumber
    }

    private fun isLeft(p0: CoordinateModel, p1: CoordinateModel, p2: CoordinateModel): Double {
        return (p1.longitude!! - p0.longitude!!) * (p2.latitude!! - p0.latitude!!) - (p2.longitude!! - p0.longitude!!) * (p1.latitude!! - p0.latitude!!)
    }

    private fun isPointOnEdge(
        point: CoordinateModel,
        start: CoordinateModel,
        end: CoordinateModel
    ): Boolean {
        val crossProduct =
            (point.latitude!! - start.latitude!!) * (end.longitude!! - start.longitude!!) -
                    (point.longitude!! - start.longitude!!) * (end.latitude!! - start.latitude!!)
        if (crossProduct != 0.0) return false

        val dotProduct =
            (point.latitude!! - start.latitude!!) * (end.latitude!! - start.latitude!!) +
                    (point.longitude!! - start.longitude!!) * (end.longitude!! - start.longitude!!)
        if (dotProduct < 0) return false

        val squaredLengthBA =
            (end.latitude!! - start.latitude!!) * (end.latitude!! - start.latitude!!) +
                    (end.longitude!! - start.longitude!!) * (end.longitude!! - start.longitude!!)
        if (dotProduct > squaredLengthBA) return false

        return true
    }
}