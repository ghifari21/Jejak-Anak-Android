package com.gosty.jejakanak.ui.parent.manage.geofence.map

import android.Manifest
import android.annotation.TargetApi
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import com.google.android.gms.location.SettingsClient
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.gosty.jejakanak.R
import com.gosty.jejakanak.core.domain.models.ChildModel
import com.gosty.jejakanak.core.domain.models.CoordinateModel
import com.gosty.jejakanak.databinding.ActivityParentAddGeofenceMapsBinding
import com.gosty.jejakanak.helpers.GeofenceHelper
import com.gosty.jejakanak.ui.parent.manage.geofence.ParentGeofencesFragment
import com.gosty.jejakanak.utils.Result
import com.gosty.jejakanak.utils.getRandomString
import com.gosty.jejakanak.utils.showContentState
import com.gosty.jejakanak.utils.showErrorState
import com.gosty.jejakanak.utils.showLoadingState
import com.kennyc.view.MultiStateView
import dagger.hilt.android.AndroidEntryPoint
import java.util.Date
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class ParentAddGeofenceMapsActivity : AppCompatActivity(), OnMapReadyCallback,
    MultiStateView.StateListener {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityParentAddGeofenceMapsBinding
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var multiStateView: MultiStateView
    private val viewModel: ParentAddGeofenceMapsViewModel by viewModels()
    private val childMarkers = HashMap<String, Marker>()
    private val geofenceMarkers = mutableListOf<Marker>()
    private val coordinates = ArrayList<CoordinateModel>()
    private val latLngList = mutableListOf<LatLng>()
    private var label = ""
    private var zoneType = ""
    private var id: String? = ""

    private val requestBackgroundLocationPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                getMyLocation()
            }
        }

    private val runningQOrLater = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

    @TargetApi(Build.VERSION_CODES.Q)
    private val requestLocationPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                if (runningQOrLater) {
                    requestBackgroundLocationPermissionLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                } else {
                    getMyLocation()
                }
            }
        }

    private val resolutionLauncher =
        registerForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult()
        ) { result ->
            when (result.resultCode) {
                RESULT_OK ->
                    Log.i(TAG, "onActivityResult: All location settings are satisfied.")

                RESULT_CANCELED ->
                    Toast.makeText(
                        this@ParentAddGeofenceMapsActivity,
                        "Anda harus mengaktifkan GPS untuk menggunakan aplikasi ini!",
                        Toast.LENGTH_SHORT
                    ).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityParentAddGeofenceMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.parent_add_geofence_maps) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        label = intent.getStringExtra(ParentGeofencesFragment.EXTRA_LABEL)!!
        zoneType = intent.getStringExtra(ParentGeofencesFragment.EXTRA_ZONE_TYPE)!!
        id = intent.getStringExtra(ParentGeofencesFragment.EXTRA_ID)

        initView()
        createInstructionDialog()

        multiStateView = binding.parentAddGeofenceMapsContainer
        multiStateView.listener = this@ParentAddGeofenceMapsActivity

        multiStateView.getView(MultiStateView.ViewState.ERROR)
            ?.findViewById<Button>(R.id.btn_refresh)
            ?.setOnClickListener {
                initView()
            }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = true

        getMyLocation()
        createLocationRequest()
        createPointForGeofence()
    }

    private fun initView() {
        // setup geofence
        viewModel.getAllGeofences().observe(this@ParentAddGeofenceMapsActivity) { result ->
            when (result) {
                is Result.Loading -> {
                    binding.parentAddGeofenceMapsContainer.showLoadingState()
                }

                is Result.Success -> {
                    binding.parentAddGeofenceMapsContainer.showContentState()
                    result.data.forEach { geofence ->
                        if (geofence.id != id) {
                            val coordinates = geofence.coordinates?.map { coordinate ->
                                LatLng(coordinate.latitude!!, coordinate.longitude!!)
                            }
                            mMap.addPolygon(
                                GeofenceHelper.addPolygonZone(
                                    coordinates!!,
                                    geofence.type!!
                                )
                            )
                        } else {
                            mMap.moveCamera(
                                CameraUpdateFactory.newLatLngZoom(
                                    LatLng(
                                        geofence.coordinates?.get(
                                            0
                                        )?.latitude!!, geofence.coordinates[0].longitude!!
                                    ), 17f
                                )
                            )
                        }
                    }
                }

                is Result.Error<*> -> {
                    binding.parentAddGeofenceMapsContainer.showErrorState()
                    Toast.makeText(
                        this@ParentAddGeofenceMapsActivity,
                        result.errorData.toString(),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        // setup child marker
        viewModel.getAllChildren().observe(this@ParentAddGeofenceMapsActivity) { result ->
            when (result) {
                is Result.Loading -> {
                    binding.parentAddGeofenceMapsContainer.showLoadingState()
                }

                is Result.Success -> {
                    binding.parentAddGeofenceMapsContainer.showContentState()
                    result.data.forEach { child ->
                        updateChildMarker(child)
                    }
                }

                is Result.Error<*> -> {
                    binding.parentAddGeofenceMapsContainer.showErrorState()
                    Toast.makeText(
                        this@ParentAddGeofenceMapsActivity,
                        result.errorData.toString(),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun createInstructionDialog() {
        MaterialAlertDialogBuilder(this@ParentAddGeofenceMapsActivity)
            .setTitle(getString(R.string.instruction_title))
            .setMessage(getString(R.string.instruction_message))
            .setPositiveButton(getString(R.string.understand)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun createPointForGeofence() {
        mMap.setOnMapClickListener { latLng ->
            val coordinateModel = CoordinateModel(
                id = getRandomString(),
                dateTime = Date().time,
                latitude = latLng.latitude,
                longitude = latLng.longitude,
                updatedAt = Date().time
            )
            val marker = mMap.addMarker(
                MarkerOptions().position(latLng)
            )

            geofenceMarkers.add(marker!!)
            coordinates.add(coordinateModel)
            latLngList.add(latLng)
        }

        mMap.setOnMarkerClickListener { marker ->
            val position = geofenceMarkers.indexOf(marker)
            if (position != -1) {
                geofenceMarkers.remove(marker)
                coordinates.removeAt(position)
                latLngList.removeAt(position)
                marker.remove()
            }
            true
        }

        binding.btnSaveGeofence.setOnClickListener {
            if (coordinates.size < 3) {
                Toast.makeText(
                    this@ParentAddGeofenceMapsActivity,
                    "Geofence harus memiliki minimal 3 titik",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                geofenceMarkers.forEach { marker ->
                    marker.remove()
                }
                val polygon = GeofenceHelper.addPolygonZone(latLngList, "danger")
                mMap.addPolygon(polygon)
                val intent = Intent()
                if (id != null) {
                    intent.apply {
                        putParcelableArrayListExtra(
                            ParentGeofencesFragment.MAPS_RESULT_DATA,
                            coordinates
                        )
                        putExtra(ParentGeofencesFragment.MAPS_RESULT_LABEL, label)
                        putExtra(ParentGeofencesFragment.MAPS_RESULT_ZONE_TYPE, zoneType)
                        putExtra(ParentGeofencesFragment.MAPS_RESULT_ID, id)
                    }
                } else {
                    intent.apply {
                        putParcelableArrayListExtra(
                            ParentGeofencesFragment.MAPS_RESULT_DATA,
                            coordinates
                        )
                        putExtra(ParentGeofencesFragment.MAPS_RESULT_LABEL, label)
                        putExtra(ParentGeofencesFragment.MAPS_RESULT_ZONE_TYPE, zoneType)
                    }
                }
                setResult(ParentGeofencesFragment.MAPS_RESULT, intent)
                finish()
            }
        }
    }

    private fun updateChildMarker(child: ChildModel) {
        val position = LatLng(child.coordinate?.latitude!!, child.coordinate.longitude!!)
        val marker = childMarkers[child.id]
        if (marker == null) {
            // Create new marker
            GeofenceHelper.createCustomMarker(
                this@ParentAddGeofenceMapsActivity,
                child.photo!!
            ) { bitmap ->
                val newMarker = mMap.addMarker(
                    MarkerOptions()
                        .position(position)
                        .title("${child.firstName} ${child.lastName}")
                        .icon(BitmapDescriptorFactory.fromBitmap(bitmap))
                )
                if (newMarker != null) {
                    childMarkers[child.id!!] = newMarker
                }
            }
        } else {
            // Update existing marker
            marker.position = position
        }
    }

    private fun checkPermission(permission: String): Boolean =
        ContextCompat.checkSelfPermission(
            this@ParentAddGeofenceMapsActivity,
            permission
        ) == PackageManager.PERMISSION_GRANTED

    @TargetApi(Build.VERSION_CODES.Q)
    private fun checkForegroundAndBackgroundLocationPermission(): Boolean {
        val foregroundLocationApproved = checkPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        val backgroundPermissionApproved =
            if (runningQOrLater) {
                checkPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            } else {
                true
            }
        return foregroundLocationApproved && backgroundPermissionApproved
    }

    private fun getMyLocation() {
        if (checkForegroundAndBackgroundLocationPermission()) {
            mMap.isMyLocationEnabled = true
            fusedLocationProviderClient.lastLocation
                .addOnSuccessListener { location ->
                    if (location != null && id == null) {
                        mMap.moveCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                LatLng(
                                    location.latitude,
                                    location.longitude
                                ), 17f
                            )
                        )
                    }
                }
        } else {
            requestLocationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun createLocationRequest() {
        val priority = Priority.PRIORITY_HIGH_ACCURACY
        val interval = TimeUnit.SECONDS.toMillis(5)
        val maxWaitTime = TimeUnit.SECONDS.toMillis(5)

        locationRequest = LocationRequest.Builder(
            priority,
            interval
        ).apply {
            setMaxUpdateDelayMillis(maxWaitTime)
        }.build()

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
        val client: SettingsClient =
            LocationServices.getSettingsClient(this@ParentAddGeofenceMapsActivity)
        client.checkLocationSettings(builder.build())
            .addOnSuccessListener {
                getMyLocation()
            }
            .addOnFailureListener { exception ->
                if (exception is ResolvableApiException) {
                    try {
                        resolutionLauncher.launch(
                            IntentSenderRequest.Builder(exception.resolution).build()
                        )
                    } catch (sendEx: IntentSender.SendIntentException) {
                        Toast.makeText(
                            this@ParentAddGeofenceMapsActivity,
                            sendEx.message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
    }

    override fun onStateChanged(viewState: MultiStateView.ViewState) {}

    companion object {
        private val TAG = ParentAddGeofenceMapsActivity::class.java.simpleName
    }
}