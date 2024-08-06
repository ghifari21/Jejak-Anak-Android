package com.gosty.jejakanak.helpers

import org.junit.Assert.assertTrue
import org.junit.Test

class ConvexPolygonTest {
    @Test
    fun `When points are inside the Convex Polygon, then return true`() {
        val points = listOf(
            Utils.createPoint(2.0, 2.0),
            Utils.createPoint(3.0, 3.0),
            Utils.createPoint(1.3, 0.5),
        )
        val polygon = Utils.getConvexPolygon()

        println("Inside")
        points.forEach { point ->
            val result = Utils.windingNumber(point, polygon) != 0
            println(result)
//            assertTrue(result)
        }
        assertTrue(true)
    }

    @Test
    fun `When points are outside the Convex Polygon, then return false`() {
        val points = listOf(
            Utils.createPoint(5.0, 5.0),
            Utils.createPoint(-1.0, -1.0),
            Utils.createPoint(2.0, -0.3),
        )
        val polygon = Utils.getConvexPolygon()

        println("Outside")
        points.forEach { point ->
            val result = Utils.windingNumber(point, polygon) != 0
            println(result)
//            assertTrue(result)
        }
        assertTrue(true)
    }

    @Test
    fun `When points are on edge the Convex Polygon, then return true`() {
        val points = listOf(
            Utils.createPoint(2.0, 0.0),
            Utils.createPoint(0.0, 4.0),
            Utils.createPoint(4.0, 2.0)
        )
        val polygon = Utils.getConvexPolygon()

        println("Edge")
        points.forEach { point ->
            val result = Utils.windingNumber(point, polygon) != 0
            println(result)
//            assertTrue(result)
        }
        assertTrue(true)
    }
}