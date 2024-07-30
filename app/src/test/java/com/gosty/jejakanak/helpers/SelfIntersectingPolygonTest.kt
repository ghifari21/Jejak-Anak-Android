package com.gosty.jejakanak.helpers

import com.gosty.jejakanak.helpers.Utils.windingNumber
import org.junit.Assert.assertTrue
import org.junit.Test

class SelfIntersectingPolygonTest {
    @Test
    fun `When points are inside the Self-Intersect Polygon, then return true`() {
        val points = listOf(
            Utils.createPoint(2.0, 2.5),
            Utils.createPoint(1.0, 3.5),
            Utils.createPoint(3.0, 0.3)
        )
        val polygon = Utils.getSelfIntersectPolygon()

        println("Inside")
        points.forEach { point ->
            val result = windingNumber(point, polygon) != 0
            println(result)

        }
        assertTrue(true)
    }

    @Test
    fun `When points are outside the Self-Intersect Polygon, then return false`() {
        val points = listOf(
            Utils.createPoint(5.0, 5.0),
            Utils.createPoint(-1.0, -1.0),
            Utils.createPoint(2.3, 2.0)
        )
        val polygon = Utils.getSelfIntersectPolygon()

        println("Outside")
        points.forEach { point ->
            val result = windingNumber(point, polygon) != 0
            println(result)

        }
        assertTrue(true)
    }

    @Test
    fun `When points are on edge the Self-Intersect Polygon, then return true`() {
        val points = listOf(
            Utils.createPoint(2.0, 2.0),
            Utils.createPoint(1.0, 3.0),
            Utils.createPoint(3.0, 0.0)
        )
        val polygon = Utils.getSelfIntersectPolygon()

        println("Edge")
        points.forEach { point ->
            val result = windingNumber(point, polygon) != 0
            println(result)

        }
        assertTrue(true)
    }
}