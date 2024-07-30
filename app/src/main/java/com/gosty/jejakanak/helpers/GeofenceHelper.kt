package com.gosty.jejakanak.helpers

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolygonOptions
import com.gosty.jejakanak.R
import com.gosty.jejakanak.core.domain.models.CoordinateModel
import de.hdodenhof.circleimageview.CircleImageView
import kotlin.math.cos

object GeofenceHelper {
    fun windingNumber(point: CoordinateModel, polygon: List<CoordinateModel>): Int {
        var windingNumber = 0

        for (i in polygon.indices) {
            val currentPoint = polygon[i]
            val nextPoint = polygon[(i + 1) % polygon.size]

            if (isPointOnEdge(point, currentPoint, nextPoint)) return 1

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

    private fun isPointOnEdge(
        point: CoordinateModel,
        start: CoordinateModel,
        end: CoordinateModel
    ): Boolean {
        // Cek apakah titik berada di antara dua ujung dan pada garis yang menghubungkan dua ujung
        val crossProduct =
            (point.latitude!! - start.latitude!!) * (end.longitude!! - start.longitude!!) -
                    (point.longitude!! - start.longitude) * (end.latitude!! - start.latitude)
        if (crossProduct != 0.0) return false

        val dotProduct =
            (point.latitude - start.latitude) * (end.latitude - start.latitude) +
                    (point.longitude - start.longitude) * (end.longitude - start.longitude)
        if (dotProduct < 0) return false

        val squaredLengthBA =
            (end.latitude - start.latitude) * (end.latitude - start.latitude) +
                    (end.longitude - start.longitude) * (end.longitude - start.longitude)
        if (dotProduct > squaredLengthBA) return false

        return true
    }

    fun calculateCentroid(points: List<CoordinateModel>): LatLng {
        var latitude = 0.0
        var longitude = 0.0

        for (point in points) {
            latitude += point.latitude!!
            longitude += point.longitude!!
        }

        val numPoints = points.size
        return LatLng(latitude / numPoints, longitude / numPoints)
    }

    fun createPolygonZone(coordinates: List<LatLng>, zoneType: String): PolygonOptions {
        val polygonOptions = if (zoneType == "danger") {
            PolygonOptions()
                .strokeColor(Color.RED)
                .fillColor(Color.argb(128, 255, 0, 0))
        } else {
            PolygonOptions()
                .strokeColor(Color.GREEN)
                .fillColor(Color.argb(128, 0, 255, 0))
        }
        polygonOptions.addAll(coordinates)

        return polygonOptions
    }

    fun addBuffer(coordinate: CoordinateModel, bufferDistance: Double): CoordinateModel {
        val distance = 111320.0

        return CoordinateModel(
            id = coordinate.id,
            latitude = coordinate.latitude!! + (bufferDistance / distance),
            longitude = coordinate.longitude!! + (bufferDistance / (distance * cos(coordinate.latitude))),
            dateTime = coordinate.dateTime,
            updatedAt = coordinate.updatedAt
        )
    }

    fun createCustomMarker(context: Context, imageUrl: String, callback: (Bitmap) -> Unit) {
        val markerView = LayoutInflater.from(context).inflate(R.layout.custom_child_marker, null)
        val markerImageView = markerView.findViewById<CircleImageView>(R.id.iv_child_marker)

        Glide.with(context)
            .asBitmap()
            .load(imageUrl)
            .placeholder(R.drawable.ic_image_black)
            .error(R.drawable.ic_broken_image_black) // You can use the same placeholder for error case
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    markerImageView.setImageBitmap(resource)
                    markerView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
                    markerView.layout(0, 0, markerView.measuredWidth, markerView.measuredHeight)
                    markerView.buildDrawingCache()
                    val bitmap = Bitmap.createBitmap(
                        markerView.measuredWidth,
                        markerView.measuredHeight,
                        Bitmap.Config.ARGB_8888
                    )
                    val canvas = Canvas(bitmap)
                    markerView.draw(canvas)
                    callback(bitmap)
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    // Handle placeholder if needed
                    placeholder?.let {
                        markerImageView.setImageDrawable(it)
                        markerView.measure(
                            View.MeasureSpec.UNSPECIFIED,
                            View.MeasureSpec.UNSPECIFIED
                        )
                        markerView.layout(0, 0, markerView.measuredWidth, markerView.measuredHeight)
                        markerView.buildDrawingCache()
                        val bitmap = Bitmap.createBitmap(
                            markerView.measuredWidth,
                            markerView.measuredHeight,
                            Bitmap.Config.ARGB_8888
                        )
                        val canvas = Canvas(bitmap)
                        markerView.draw(canvas)
                        callback(bitmap)
                    }
                }

                override fun onLoadFailed(errorDrawable: Drawable?) {
                    super.onLoadFailed(errorDrawable)
                    // Handle error placeholder if needed
                    errorDrawable?.let {
                        markerImageView.setImageDrawable(it)
                        markerView.measure(
                            View.MeasureSpec.UNSPECIFIED,
                            View.MeasureSpec.UNSPECIFIED
                        )
                        markerView.layout(0, 0, markerView.measuredWidth, markerView.measuredHeight)
                        markerView.buildDrawingCache()
                        val bitmap = Bitmap.createBitmap(
                            markerView.measuredWidth,
                            markerView.measuredHeight,
                            Bitmap.Config.ARGB_8888
                        )
                        val canvas = Canvas(bitmap)
                        markerView.draw(canvas)
                        callback(bitmap)
                    }
                }
            })
    }
}