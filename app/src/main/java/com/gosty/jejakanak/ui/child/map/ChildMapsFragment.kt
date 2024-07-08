package com.gosty.jejakanak.ui.child.map

import android.Manifest
import android.annotation.TargetApi
import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
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
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polygon
import com.gosty.jejakanak.R
import com.gosty.jejakanak.databinding.FragmentChildMapsBinding
import com.gosty.jejakanak.helpers.GeofenceHelper
import com.gosty.jejakanak.services.ChildLocationService
import com.gosty.jejakanak.utils.Result
import com.gosty.jejakanak.utils.isServiceRunning
import com.gosty.jejakanak.utils.showContentState
import com.gosty.jejakanak.utils.showErrorState
import com.gosty.jejakanak.utils.showLoadingState
import com.kennyc.view.MultiStateView
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class ChildMapsFragment : Fragment(), MultiStateView.StateListener {

    private var _binding: FragmentChildMapsBinding? = null
    private val binding get() = _binding
    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var multiStateView: MultiStateView
    private val viewModel: ChildMapsViewModel by viewModels()
    private val polygons = mutableListOf<Polygon>()

    private val callback = OnMapReadyCallback { googleMap ->

        googleMap.uiSettings.isZoomControlsEnabled = true

        mMap = googleMap

        getMyLocation()
        createLocationRequest()
    }

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
                        requireActivity(),
                        "Anda harus mengaktifkan GPS untuk menggunakan aplikasi ini!",
                        Toast.LENGTH_SHORT
                    ).show()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentChildMapsBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment =
            childFragmentManager.findFragmentById(R.id.child_map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)

        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireActivity())

        initView()

        multiStateView = binding?.childMapsContainer!!
        multiStateView.listener = this@ChildMapsFragment

        multiStateView.getView(MultiStateView.ViewState.ERROR)
            ?.findViewById<Button>(R.id.btn_refresh)
            ?.setOnClickListener {
                initView()
            }
    }

    private fun initView() {
        viewModel.getAllGeofences().observe(viewLifecycleOwner) {
            when (it) {
                is Result.Success -> {
                    binding?.childMapsContainer?.showContentState()
                    polygons.forEach { polygon ->
                        polygon.remove()
                    }
                    it.data.forEach { geofence ->
                        val coordinates = geofence.coordinates?.map { coordinate ->
                            LatLng(coordinate.latitude!!, coordinate.longitude!!)
                        }
                        polygons.add(
                            mMap.addPolygon(
                                GeofenceHelper.addPolygonZone(
                                    coordinates!!,
                                    geofence.type!!
                                )
                            )
                        )
                    }
                }

                is Result.Error<*> -> {
                    binding?.childMapsContainer?.showErrorState()
                    Toast.makeText(requireActivity(), it.errorData.toString(), Toast.LENGTH_SHORT)
                        .show()
                }

                is Result.Loading -> {
                    binding?.childMapsContainer?.showLoadingState()
                }
            }
        }
    }

    private fun checkPermission(permission: String): Boolean =
        ContextCompat.checkSelfPermission(
            requireActivity(),
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
        val client: SettingsClient = LocationServices.getSettingsClient(requireActivity())
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
                        Toast.makeText(requireActivity(), sendEx.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }

    private fun getMyLocation() {
        if (checkForegroundAndBackgroundLocationPermission()) {
            mMap.isMyLocationEnabled = true
            fusedLocationProviderClient.lastLocation
                .addOnSuccessListener {
                    if (it != null) {
                        mMap.moveCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                LatLng(
                                    it.latitude,
                                    it.longitude
                                ),
                                17f
                            )
                        )
                    }
                }

            // start foreground service if not running yet
            if (!isServiceRunning(ChildLocationService::class.java, requireActivity())) {
                val intent = Intent(requireActivity(), ChildLocationService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    activity?.startForegroundService(intent)
                } else {
                    activity?.startService(intent)
                }
            }
        } else {
            requestLocationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onStateChanged(viewState: MultiStateView.ViewState) {
    }

    companion object {
        private val TAG = ChildMapsFragment::class.java.simpleName
    }
}