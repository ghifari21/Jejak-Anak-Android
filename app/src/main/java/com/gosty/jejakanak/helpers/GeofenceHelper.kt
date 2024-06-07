package com.gosty.jejakanak.helpers

import com.gosty.jejakanak.core.data.models.CoordinateModel

object GeofenceHelper {
    fun windingNumber(point: CoordinateModel, polygon: List<CoordinateModel>): Int {
        var windingNumber = 0

        for (i in polygon.indices) {
            val currentPoint = polygon[i]
            val nextPoint = polygon[(i + 1) % polygon.size]

            if (currentPoint.latitude!! <= point.latitude!!) {
                if (nextPoint.latitude!! > point.latitude && isLeft(
                        currentPoint,
                        nextPoint,
                        point
                    ) > 0
                ) {
                    windingNumber++
                }
            } else if (nextPoint.latitude!! <= point.latitude && isLeft(
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
        return (p1.longitude!! - p0.longitude!!) * (p2.latitude!! - p0.latitude!!) - (p2.longitude!! - p0.longitude) * (p1.latitude!! - p0.latitude)
    }
}