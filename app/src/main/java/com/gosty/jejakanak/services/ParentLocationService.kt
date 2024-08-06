package com.gosty.jejakanak.services

import android.app.Notification
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
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE
import com.google.firebase.auth.FirebaseAuth
import com.gosty.jejakanak.R
import com.gosty.jejakanak.core.domain.models.ChildModel
import com.gosty.jejakanak.core.domain.models.GeofenceModel
import com.gosty.jejakanak.core.domain.usecases.GeofenceUseCase
import com.gosty.jejakanak.core.domain.usecases.UserUseCase
import com.gosty.jejakanak.helpers.GeofenceHelper
import com.gosty.jejakanak.ui.parent.main.ParentActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.min

@AndroidEntryPoint
class ParentLocationService : Service() {

    @Inject
    lateinit var userUseCase: UserUseCase

    @Inject
    lateinit var geofenceUseCase: GeofenceUseCase

    @Inject
    lateinit var firebaseAuth: FirebaseAuth

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    private val geofences = mutableMapOf<String, GeofenceModel>()
    private val geofenceStatus = mutableMapOf<String, MutableMap<GeofenceModel, Boolean>>()
    private val children = mutableMapOf<String, ChildModel>()
    private val childExitTimeouts = mutableMapOf<Pair<String, GeofenceModel>, Job?>()

    override fun onCreate() {
        super.onCreate()

        getGeofencesOnce()
        getGeofences()
        getChildrenOnce()
        getChildrenLocation()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_FOREGROUND_SERVICE_STOP) {
            stopService()
        }

        if (firebaseAuth.currentUser == null && userUseCase.getUserRole() != "Parent") {
            stopService()
        }

        val notification = buildMonitoringNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                MONITORING_NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
            )
        } else {
            startForeground(MONITORING_NOTIFICATION_ID, notification)
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder? = null

    private fun buildMonitoringNotification(): Notification {
        val notificationIntent = Intent(this, ParentActivity::class.java)
        val pendingFlags: Int =
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, pendingFlags)

        val stopNotificationIntent = Intent(this, ParentLocationService::class.java).apply {
            action = ACTION_FOREGROUND_SERVICE_STOP
        }
        val stopPendingIntent =
            PendingIntent.getService(this, 1, stopNotificationIntent, pendingFlags)

        val mNotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationBuilder = NotificationCompat.Builder(this, MONITORING_CHANNEL_ID)
            .setContentTitle(getString(R.string.child_monitored))
            .setContentText(getString(R.string.child_monitored_content))
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                getString(R.string.stop),
                stopPendingIntent
            )
            .setOngoing(true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            notificationBuilder.setForegroundServiceBehavior(FOREGROUND_SERVICE_IMMEDIATE)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                MONITORING_CHANNEL_ID,
                MONITORING_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            channel.description = MONITORING_CHANNEL_NAME
            notificationBuilder.setChannelId(MONITORING_CHANNEL_ID)
            mNotificationManager.createNotificationChannel(channel)
        }

        return notificationBuilder.build()
    }

    private fun buildGeofenceNotification(
        childName: String,
        label: String,
        action: String,
        zoneType: String,
        child: ChildModel,
        geofenceId: String
    ) {
        val notificationIntent = Intent(this, ParentActivity::class.java).apply {
            this.action = ACTION_OPEN_MAP_FRAGMENT
            putExtra(EXTRA_CHILD_SERVICE, child)
        }
        val pendingFlags: Int =
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        val pendingIntent = PendingIntent.getActivity(this, 442, notificationIntent, pendingFlags)


        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val vibrationPattern = if (zoneType == "bahaya" || action == "keluar dari") {
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
            .setContentTitle(childName)
            .setContentText(
                getString(
                    R.string.parent_geofence_content,
                    childName,
                    action,
                    zoneType,
                    label
                )
            )
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setVibrate(vibrationPattern)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            notificationBuilder.setForegroundServiceBehavior(Notification.FOREGROUND_SERVICE_IMMEDIATE)
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
        val id = geofenceId + child.phone
        val notificationId = (id.substring(0, min(id.length, 15)).toLong() % Int.MAX_VALUE).toInt()
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

    private fun getChildrenOnce() {
        userUseCase.getAllChildrenOnceService { list ->
            if (list.isNotEmpty()) {
                list.forEach { child ->
                    children[child.id!!] = child
                    geofenceStatus[child.id] = mutableMapOf()
                }
            }
        }
    }

    private fun getChildrenLocation() {
        userUseCase.getAllChildrenService { list ->
            if (list.isNotEmpty()) {
                if (children.values.size != list.size) {
                    getChildrenOnce()
                }
                list.forEach { child ->
                    // Check geofence status for each child
                    geofences.values.forEach { geofence ->
                        checkGeofenceStatus(geofence, child)
                    }
                }
            }
        }
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
                } else {
                    for (i in list.indices) {
                        if (geofences[list[i].id!!] != list[i]) {
                            getGeofencesOnce()
                            break
                        }
                    }
                }
                // TODO REMOVE THIS IF NOT NEEDED
                children.values.forEach { child ->
                    list.forEach { geofence ->
                        checkGeofenceStatus(geofence, child)
                    }
                }
            }
        }
    }

    private fun checkGeofenceStatus(geofence: GeofenceModel, child: ChildModel) {
        // Add buffer to geofence
        val bufferedGeofence = geofence.coordinates!!.map { coordinate ->
            GeofenceHelper.addBuffer(coordinate, 10.0) // 10 meters buffer
        }

        val result = GeofenceHelper.windingNumber(child.coordinate!!, bufferedGeofence)
        val wasInsideGeofence = geofenceStatus[child.id!!]?.get(geofence) ?: false

        val isInsideGeofence = result != 0
        val timeoutDuration = 2 * 60 * 1000L // 2 minutes

        val timeoutKey = Pair(child.id, geofence)

        if (isInsideGeofence != wasInsideGeofence) {
            val type = if (geofence.type == "danger") "bahaya" else "aman"
            if (isInsideGeofence) {
                if (childExitTimeouts[timeoutKey] == null) {
                    buildGeofenceNotification(
                        child.firstName!!,
                        geofence.label!!,
                        "memasuki",
                        type,
                        child,
                        geofence.id!!
                    )
                }

                // Cancel any existing exit timeout job
                childExitTimeouts[timeoutKey]?.cancel()
                childExitTimeouts[timeoutKey] = null
            } else {
                // Start a coroutine to wait for 2 minutes before sending the exit notification
                childExitTimeouts[timeoutKey]?.cancel()
                childExitTimeouts[timeoutKey] = serviceScope.launch {
                    delay(timeoutDuration) // 2 minutes delay

                    buildGeofenceNotification(
                        child.firstName!!,
                        geofence.label!!,
                        "keluar dari",
                        type,
                        child,
                        geofence.id!!
                    )

                    childExitTimeouts[timeoutKey] = null
                }
            }

            geofenceStatus[child.id]?.set(geofence, isInsideGeofence)
        } else if (isInsideGeofence && childExitTimeouts[timeoutKey] != null) {
            // If the child re-enters the geofence within 2 minutes, cancel the exit timeout
            childExitTimeouts[timeoutKey]?.cancel()
            childExitTimeouts[timeoutKey] = null
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

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
    }

    companion object {
        private const val MONITORING_NOTIFICATION_ID = 64
        private const val MONITORING_CHANNEL_ID = "monitoring_channel"
        private const val MONITORING_CHANNEL_NAME = "Monitoring Child Location Channel"
        private const val GEOFENCE_CHANNEL_ID = "geofence channel"
        private const val GEOFENCE_CHANNEL_NAME = "parent geofence channel"
        internal val TAG = ParentLocationService::class.java.simpleName
        const val EXTRA_CHILD_SERVICE = "extra_child_service"
        private const val ACTION_FOREGROUND_SERVICE_STOP =
            "com.gosty.jejakanak.services.FOREGROUND_SERVICE_STOP"
        const val ACTION_OPEN_MAP_FRAGMENT = "com.gosty.jejakanak.services.OPEN_MAP_FRAGMENT"
    }
}