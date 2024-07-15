package com.gosty.jejakanak.services

import android.app.Notification
import android.app.Notification.FOREGROUND_SERVICE_IMMEDIATE
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.firebase.auth.FirebaseAuth
import com.gosty.jejakanak.R
import com.gosty.jejakanak.core.domain.models.CoordinateModel
import com.gosty.jejakanak.core.domain.models.GeofenceModel
import com.gosty.jejakanak.core.domain.usecases.GeofenceUseCase
import com.gosty.jejakanak.core.domain.usecases.UserUseCase
import com.gosty.jejakanak.helpers.GeofenceHelper
import com.gosty.jejakanak.ui.child.main.ChildActivity
import com.gosty.jejakanak.utils.getRandomString
import dagger.hilt.android.AndroidEntryPoint
import java.util.Date
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class ChildLocationService : Service() {

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    @Inject
    lateinit var userUseCase: UserUseCase

    @Inject
    lateinit var geofenceUseCase: GeofenceUseCase

    @Inject
    lateinit var firebaseAuth: FirebaseAuth

    private val geofences = mutableMapOf<String, GeofenceModel>()
    private val geofenceStatus = mutableMapOf<GeofenceModel, Boolean>()

    override fun onCreate() {
        super.onCreate()

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        getGeofencesOnce()
        getGeofences()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_FOREGROUND_SERVICE_STOP) {
            stopService()
        }

        if (firebaseAuth.currentUser == null && userUseCase.getUserRole() != "Child") {
            stopService()
        }

        createLocationRequest()
        createLocationCallback()

        val notification = buildTrackingNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                TRACKING_NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
            )
        } else {
            startForeground(TRACKING_NOTIFICATION_ID, notification)
        }

        startLocationUpdates()

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun createLocationRequest() {
        val priority = Priority.PRIORITY_HIGH_ACCURACY
        val interval = TimeUnit.SECONDS.toMillis(1)
        val maxWaitTime = TimeUnit.SECONDS.toMillis(1)

        locationRequest = LocationRequest.Builder(
            priority,
            interval
        ).apply {
            setMaxUpdateDelayMillis(maxWaitTime)
        }.build()
    }

    private fun createLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation ?: return
                val lastLocation = CoordinateModel(
                    id = getRandomString(),
                    dateTime = Date().time,
                    latitude = locationResult.lastLocation?.latitude,
                    longitude = locationResult.lastLocation?.longitude,
                    updatedAt = Date().time
                )
                Log.d(TAG, "onLocationResult: $lastLocation")

                if (firebaseAuth.currentUser == null && userUseCase.getUserRole() != "Child") {
                    stopService()
                } else {
                    userUseCase.updateChildCoordinate(lastLocation)
                }

                geofences.values.forEach { geofence ->
                    val result = GeofenceHelper.windingNumber(lastLocation, geofence.coordinates!!)

                    // Cek apakah status pengguna berubah untuk geofence ini
                    val isInsideGeofence = result != 0
                    val wasInsideGeofence = geofenceStatus[geofence] ?: false

                    if (isInsideGeofence != wasInsideGeofence) {
                        val type = if (geofence.type == "danger") "bahaya" else "aman"
                        if (isInsideGeofence) {
                            // Pengguna masuk ke geofence
                            buildGeofenceNotification(
                                geofence.label!!,
                                "memasuki",
                                type,
                                geofence.id!!
                            )
                        } else {
                            // Pengguna keluar dari geofence
                            buildGeofenceNotification(
                                geofence.label!!,
                                "keluar dari",
                                type,
                                geofence.id!!
                            )
                        }
                        // Update status pengguna di geofence ini
                        geofenceStatus[geofence] = isInsideGeofence
                    }
                }
            }
        }
    }

    private fun startLocationUpdates() {
        try {
            fusedLocationProviderClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (exception: SecurityException) {
            Log.e(TAG, "Error : " + exception.message)
        }
    }

    private fun stopLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }

    private fun buildTrackingNotification(): Notification {
        val notificationIntent = Intent(this, ChildActivity::class.java)
        val pendingFlags: Int =
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, pendingFlags)

        val stopNotificationIntent = Intent(this, ChildLocationService::class.java).apply {
            action = ACTION_FOREGROUND_SERVICE_STOP
        }
        val stopPendingIntent =
            PendingIntent.getService(this, 1, stopNotificationIntent, pendingFlags)

        val mNotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationBuilder = NotificationCompat.Builder(this, TRACKING_CHANNEL_ID)
            .setContentTitle(getString(R.string.shared_location_title))
            .setContentText(getString(R.string.shared_location_content))
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                getString(R.string.stop),
                stopPendingIntent
            )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            notificationBuilder.setForegroundServiceBehavior(FOREGROUND_SERVICE_IMMEDIATE)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                TRACKING_CHANNEL_ID,
                TRACKING_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            channel.description = TRACKING_CHANNEL_NAME
            notificationBuilder.setChannelId(TRACKING_CHANNEL_ID)
            mNotificationManager.createNotificationChannel(channel)
        }

        return notificationBuilder.build()
    }

    private fun buildGeofenceNotification(
        label: String,
        type: String,
        zoneType: String,
        geofenceId: String
    ) {
        val notificationIntent = Intent(this, ChildActivity::class.java)
        val pendingFlags: Int =
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        val pendingIntent = PendingIntent.getActivity(this, 3, notificationIntent, pendingFlags)

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val vibrationPattern = if (zoneType == "bahaya" || type == "keluar dari") {
            longArrayOf(
                0,   // Initial delay
                1000, 500,  // First vibration and pause
                1000, 500,  // Second vibration and pause
                1000, 500,  // Third vibration and pause
                1000, 500,  // Fourth vibration and pause
                1000        // Fifth vibration (no pause needed at the end)
            )
        } else {
            longArrayOf(
                0,   // Initial delay
                1000, 500,  // First vibration and pause
                1000
            )
        }

        val mNotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationBuilder = NotificationCompat.Builder(this, GEOFENCE_CHANNEL_ID)
            .setContentTitle(label)
            .setContentText(getString(R.string.child_geofence_content, type, zoneType))
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setVibrate(vibrationPattern)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            notificationBuilder.setForegroundServiceBehavior(FOREGROUND_SERVICE_IMMEDIATE)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                GEOFENCE_CHANNEL_ID,
                GEOFENCE_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                this.vibrationPattern = vibrationPattern
                enableVibration(true)
            }
            channel.description = GEOFENCE_CHANNEL_NAME
            notificationBuilder.setChannelId(GEOFENCE_CHANNEL_ID)
            mNotificationManager.createNotificationChannel(channel)
        }

//        val notificationId = (System.currentTimeMillis() % Int.MAX_VALUE).toInt()
        val notificationId = (geofenceId.toLong() % Int.MAX_VALUE).toInt()
        mNotificationManager.notify(notificationId, notificationBuilder.build())

        triggerVibration(vibrationPattern)
    }

    private fun triggerVibration(pattern: LongArray) {
        val vibrator = this.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
        } else {
            vibrator.vibrate(pattern, -1)
        }
    }

    private fun stopService() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            stopForeground(true)
        }
        stopSelf()
    }

    private fun getGeofencesOnce() {
        geofenceUseCase.getAllGeofencesOnceService { list ->
            if (list.isNotEmpty()) {
                list.forEach { geofence ->
                    geofences[geofence.id!!] = geofence
                }
            }
        }
    }

    private fun getGeofences() {
        geofenceUseCase.getAllGeofencesService { list ->
            if (list.isNotEmpty()) {
                if (geofences.values.size != list.size) {
                    getGeofencesOnce()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopLocationUpdates()
    }

    companion object {
        private const val TRACKING_NOTIFICATION_ID = 1
        private const val TRACKING_CHANNEL_ID = "channel_01"
        private const val TRACKING_CHANNEL_NAME = "child location channel"
        private const val GEOFENCE_CHANNEL_ID = "channel_02"
        private const val GEOFENCE_CHANNEL_NAME = "child geofence channel"
        internal val TAG = ChildLocationService::class.java.simpleName
        private const val ACTION_FOREGROUND_SERVICE_STOP =
            "com.gosty.jejakanak.services.FOREGROUND_SERVICE_STOP"
    }
}