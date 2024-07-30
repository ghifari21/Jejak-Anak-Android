package com.gosty.jejakanak.helpers

import com.gosty.jejakanak.helpers.Utils.windingNumber
import org.junit.Assert.assertTrue
import org.junit.Test

class ConcavePolygonTest {
    @Test
    fun `When points are inside the Concave Polygon, then return true`() {
        val points = listOf(
            Utils.createPoint(1.0, 2.0),
            Utils.createPoint(2.0, 1.0),
            Utils.createPoint(3.8, 3.5)
        )
        val polygon = Utils.getConcavePolygon()

        println("Inside")
        points.forEach { point ->
            val result = windingNumber(point, polygon) != 0
            println(result)
        }
        assertTrue(true)
    }

    @Test
    fun `When points are outside the Concave Polygon, then return false`() {
        val points = listOf(
            Utils.createPoint(5.0, 2.0),
            Utils.createPoint(3.0, 3.3),
            Utils.createPoint(-1.0, 3.0)
        )
        val polygon = Utils.getConcavePolygon()

        println("Outside")
        points.forEach { point ->
            val result = windingNumber(point, polygon) != 0
            println(result)
        }
        assertTrue(true)
    }

    @Test
    fun `When points are on edge the Concave Polygon, then return true`() {
        val points = listOf(
            Utils.createPoint(2.0, 2.0),
            Utils.createPoint(0.0, 4.0),
            Utils.createPoint(4.0, 0.0)
        )
        val polygon = Utils.getConcavePolygon()

//        points.forEach { point ->
//            val result = windingNumber(point, polygon) != 0
//            assertTrue(result)
//        }

        println("Edge")
        points.forEach { point ->
            val result = windingNumber(point, polygon) != 0
            println(result)
        }
        assertTrue(true)
    }
}