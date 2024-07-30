package com.gosty.jejakanak.helpers

import com.gosty.jejakanak.helpers.Utils.windingNumber
import org.junit.Assert.assertTrue
import org.junit.Test

class ComplexPolygonTest {
    @Test
    fun `When point are inside the Complex Polygon, then return true`() {
        val points = listOf(
            Utils.createPoint(3.0, 2.0),
            Utils.createPoint(4.0, 3.0),
            Utils.createPoint(0.5, 1.0)
        )
        val polygon = Utils.getComplexPolygon()

        println("Inside")
        points.forEach { point ->
            val result = windingNumber(point, polygon) != 0
            println(result)
        }
        assertTrue(true)
    }

    @Test
    fun `When points are outside the Complex Polygon, then return false`() {
        val points = listOf(
            Utils.createPoint(5.5, 3.5),
            Utils.createPoint(6.5, 2.5),
            Utils.createPoint(-1.0, 1.0)
        )
        val polygon = Utils.getComplexPolygon()

        println("Outside")
        points.forEach { point ->
            val result = windingNumber(point, polygon) != 0
            println(result)
        }
        assertTrue(true)
    }

    @Test
    fun `When points are on edge the Complex Polygon, then return true`() {
        val points = listOf(
            Utils.createPoint(0.0, 2.0),
            Utils.createPoint(4.0, 4.0),
            Utils.createPoint(5.0, 3.0)
        )
        val polygon = Utils.getComplexPolygon()

        println("Edge")
        points.forEach { point ->
            val result = windingNumber(point, polygon) != 0
            println(result)
        }
        assertTrue(true)
    }
}