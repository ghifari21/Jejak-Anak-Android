package com.gosty.jejakanak.helpers

import com.gosty.jejakanak.core.domain.models.CoordinateModel
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GeofenceTest {

    @Test
    fun `When points are inside the Convex Polygon, then return true`() {
        val points = listOf(
            Utils.createPoint(2.0, 2.0),
            Utils.createPoint(3.0, 3.0),
            Utils.createPoint(1.3, 0.5),
        )
        val polygon = Utils.getConvexPolygon()

        points.forEach { point ->
            val result = windingNumber(point, polygon) != 0
            assertTrue(result)
        }
    }

    @Test
    fun `When points are outside the Convex Polygon, then return false`() {
        val points = listOf(
            Utils.createPoint(5.0, 5.0),
            Utils.createPoint(-1.0, -1.0),
            Utils.createPoint(2.0, -0.3),
        )
        val polygon = Utils.getConvexPolygon()

        points.forEach { point ->
            val result = windingNumber(point, polygon) != 0
            assertFalse(result)
        }
    }

    @Test
    fun `When points are on edge the Convex Polygon, then return true`() {
        val points = listOf(
            Utils.createPoint(2.0, 0.0),
            Utils.createPoint(0.0, 4.0),
            Utils.createPoint(4.0, 2.0),
        )
        val polygon = Utils.getConvexPolygon()

        points.forEach { point ->
            val result = windingNumber(point, polygon) != 0
            println(result)
            assertTrue(result)
        }
    }

    @Test
    fun `When points are inside the Concave Polygon, then return true`() {
        val points = listOf(
            Utils.createPoint(1.0, 2.0),
            Utils.createPoint(2.0, 1.0),
            Utils.createPoint(3.8, 3.5)
        )
        val polygon = Utils.getConcavePolygon()

        points.forEach { point ->
            val result = windingNumber(point, polygon) != 0
            assertTrue(result)
        }
    }

    @Test
    fun `When points are outside the Concave Polygon, then return false`() {
        val points = listOf(
            Utils.createPoint(5.0, 2.0),
            Utils.createPoint(3.0, 3.3),
            Utils.createPoint(-1.0, 3.0)
        )
        val polygon = Utils.getConcavePolygon()

        points.forEach { point ->
            val result = windingNumber(point, polygon) != 0
            assertFalse(result)
        }
    }

    @Test
    fun `When points are on edge the Concave Polygon, then return true`() {
        val points = listOf(
            Utils.createPoint(2.0, 2.0),
            Utils.createPoint(0.0, 4.0),
            Utils.createPoint(4.0, 0.0)
        )
        val polygon = Utils.getConcavePolygon()

        points.forEach { point ->
            val result = windingNumber(point, polygon) != 0
            assertTrue(result)
        }
    }

    @Test
    fun `When point are inside the Complex Polygon, then return true`() {
        val points = listOf(
            Utils.createPoint(3.0, 2.0),
            Utils.createPoint(4.0, 3.0),
            Utils.createPoint(0.5, 1.0)
        )
        val polygon = Utils.getComplexPolygon()

        points.forEach { point ->
            val result = windingNumber(point, polygon) != 0
            assertTrue(result)
        }
    }

    @Test
    fun `When points are outside the Complex Polygon, then return false`() {
        val points = listOf(
            Utils.createPoint(5.5, 3.5),
            Utils.createPoint(6.5, 2.5),
            Utils.createPoint(-1.0, 1.0)
        )
        val polygon = Utils.getComplexPolygon()

        points.forEach { point ->
            val result = windingNumber(point, polygon) != 0
            assertFalse(result)
        }
    }

    @Test
    fun `When points are on edge the Complex Polygon, then return true`() {
        val points = listOf(
            Utils.createPoint(0.0, 2.0),
            Utils.createPoint(4.0, 4.0),
            Utils.createPoint(5.0, 3.0)
        )
        val polygon = Utils.getComplexPolygon()

        points.forEach { point ->
            val result = windingNumber(point, polygon) != 0
            assertTrue(result)
        }
    }

    @Test
    fun `When points are inside the Self-Intersect Polygon, then return true`() {
        val points = listOf(
            Utils.createPoint(2.0, 2.5),
            Utils.createPoint(1.0, 3.5),
            Utils.createPoint(3.0, 0.3)
        )
        val polygon = Utils.getSelfIntersectPolygon()

        points.forEach { point ->
            val result = windingNumber(point, polygon) != 0
            assertTrue(result)
        }
    }

    @Test
    fun `When points are outside the Self-Intersect Polygon, then return false`() {
        val points = listOf(
            Utils.createPoint(5.0, 5.0),
            Utils.createPoint(-1.0, -1.0),
            Utils.createPoint(2.3, 2.0)
        )
        val polygon = Utils.getSelfIntersectPolygon()

        points.forEach { point ->
            val result = windingNumber(point, polygon) != 0
            assertFalse(result)
        }
    }

    @Test
    fun `When points are on edge the Self-Intersect Polygon, then return true`() {
        val points = listOf(
            Utils.createPoint(2.0, 2.0),
            Utils.createPoint(1.0, 3.0),
            Utils.createPoint(3.0, 0.0)
        )
        val polygon = Utils.getSelfIntersectPolygon()

        points.forEach { point ->
            val result = windingNumber(point, polygon) != 0
            assertTrue(result)
        }
    }

    private fun windingNumber(point: CoordinateModel, polygon: List<CoordinateModel>): Int {
        var windingNumber = 0

        for (i in polygon.indices) {
            val currentPoint = polygon[i]
            val nextPoint = polygon[(i + 1) % polygon.size]

            if (isPointOnEdge(point, currentPoint, nextPoint)) {
                return 1  // Titik berada di tepi, anggap di dalam
            }

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
        // Cek apakah titik berada di antara dua ujung dan pada garis yang menghubungkan dua ujung
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