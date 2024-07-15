package com.gosty.jejakanak.ui.parent.manage.children

import android.Manifest
import android.annotation.TargetApi
import android.app.Activity
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
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import com.google.android.gms.location.SettingsClient
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.gosty.jejakanak.R
import com.gosty.jejakanak.core.domain.models.ChildModel
import com.gosty.jejakanak.core.ui.RvChildListAdapter
import com.gosty.jejakanak.databinding.FragmentParentChildrenListBinding
import com.gosty.jejakanak.ui.parent.map.ParentMapsFragment
import com.gosty.jejakanak.utils.Result
import com.gosty.jejakanak.utils.showContentState
import com.gosty.jejakanak.utils.showErrorState
import com.gosty.jejakanak.utils.showLoadingState
import com.kennyc.view.MultiStateView
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class ParentChildrenListFragment : Fragment(), MultiStateView.StateListener {
    private var _binding: FragmentParentChildrenListBinding? = null
    private val binding get() = _binding
    private val viewModel: ParentChildrenListViewModel by viewModels()
    private val adapter = RvChildListAdapter()
    private lateinit var multiStateView: MultiStateView
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest

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
                Activity.RESULT_OK ->
                    Log.i(TAG, "onActivityResult: All location settings are satisfied.")

                Activity.RESULT_CANCELED ->
                    Toast.makeText(
                        requireActivity(),
                        "Anda harus mengaktifkan GPS untuk menggunakan aplikasi ini!",
                        Toast.LENGTH_SHORT
                    ).show()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentParentChildrenListBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireActivity())

        getMyLocation()
        initView()
        initRecyclerView()
        createLocationRequest()

        multiStateView = binding?.msvChildrenList!!
        multiStateView.listener = this@ParentChildrenListFragment
        multiStateView.getView(MultiStateView.ViewState.ERROR)
            ?.findViewById<Button>(R.id.btn_refresh)
            ?.setOnClickListener {
                initView()
            }

        multiStateView.getView(MultiStateView.ViewState.EMPTY)
            ?.findViewById<Button>(R.id.btn_add_child_empty_state)
            ?.setOnClickListener {
                addChildDialog()
            }

        binding?.refreshChildrenList?.setOnRefreshListener {
            initView()
            binding?.refreshChildrenList?.isRefreshing = false
        }

        binding?.btnAddChild?.setOnClickListener {
            addChildDialog()
        }
    }

    private fun initView() {
        viewModel.getAllChildren().observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Loading -> {
                    binding?.msvChildrenList?.showLoadingState()
                }

                is Result.Success -> {
                    if (result.data.isEmpty()) {
                        binding?.msvChildrenList?.viewState = MultiStateView.ViewState.EMPTY
                    } else {
                        binding?.msvChildrenList?.viewState = MultiStateView.ViewState.CONTENT
                        adapter.submitList(result.data)
                    }
                }

                is Result.Error<*> -> {
                    binding?.msvChildrenList?.viewState = MultiStateView.ViewState.ERROR
                    showToast(result.errorData.toString())
                }
            }
        }
    }

    private fun getMyLocation() {
        if (checkForegroundAndBackgroundLocationPermission()) {
            fusedLocationProviderClient.lastLocation
                .addOnSuccessListener {
                    if (it != null) {
                        adapter.updateParentLocation(
                            LatLng(
                                it.latitude,
                                it.longitude
                            )
                        )
                    }
                }
        } else {
            requestLocationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun initRecyclerView() {
        val layoutManager = LinearLayoutManager(requireActivity())
        binding?.apply {
            rvParentChildren.layoutManager = layoutManager
            rvParentChildren.adapter = adapter
            rvParentChildren.setHasFixedSize(true)
        }

        adapter.setOnItemClickCallback(
            object : RvChildListAdapter.OnItemClickCallback {
                override fun onSeeChildClicked(childModel: ChildModel) {
                    seeChildLocation(childModel)
                }

                override fun onDeleteChildClicked(childModel: ChildModel) {
                    removeChildDialog(childModel)
                }
            }
        )
    }

    private fun addChildDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.child_dialog_layout, null)
        val codeInput = dialogView.findViewById<TextInputEditText>(R.id.tiet_add_child)

        MaterialAlertDialogBuilder(requireActivity())
            .setTitle(activity?.getString(R.string.add_child))
            .setView(dialogView)
            .setPositiveButton(activity?.getString(R.string.save)) { _, _ ->
                val uniqueCode = codeInput.text.toString()
                if (uniqueCode.isNotEmpty()) {
                    addChild(uniqueCode)
                } else {
                    showToast(activity?.getString(R.string.fill_all_fields).toString())
                }
            }
            .setNegativeButton(activity?.getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun addChild(uniqueCode: String) {
        viewModel.addChild(uniqueCode).observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Loading -> {
                    binding?.msvChildrenList?.showLoadingState()
                }

                is Result.Success -> {
                    binding?.msvChildrenList?.showContentState()
                    showToast(result.data)
                    adapter.notifyDataSetChanged()
                }

                is Result.Error<*> -> {
                    binding?.msvChildrenList?.showContentState()
                    showToast(result.errorData.toString())
                }
            }
        }
    }

    private fun removeChildDialog(child: ChildModel) {
        MaterialAlertDialogBuilder(requireActivity())
            .setTitle(activity?.getString(R.string.confirmation))
            .setMessage(activity?.getString(R.string.are_you_sure))
            .setPositiveButton(activity?.getString(R.string.yes)) { _, _ ->
                removeChild(child.id!!)
            }
            .setNegativeButton(activity?.getString(R.string.no)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun removeChild(id: String) {
        viewModel.removeChild(id).observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Loading -> {
                    binding?.msvChildrenList?.showLoadingState()
                }

                is Result.Success -> {
                    binding?.msvChildrenList?.showContentState()
                    showToast(result.data)
                    adapter.notifyDataSetChanged()
                    initView()
                }

                is Result.Error<*> -> {
                    binding?.msvChildrenList?.showErrorState()
                    showToast(result.errorData.toString())
                }
            }
        }
    }

    private fun seeChildLocation(child: ChildModel) {
        val bundle = Bundle().apply {
            putDouble(ParentMapsFragment.EXTRA_LATITUDE, child.coordinate?.latitude!!)
            putDouble(ParentMapsFragment.EXTRA_LONGITUDE, child.coordinate.longitude!!)
        }

        findNavController().navigate(
            R.id.action_parent_navigation_manage_to_parent_navigation_map, bundle
        )
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

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onStateChanged(viewState: MultiStateView.ViewState) {}

    companion object {
        private val TAG = ParentChildrenListFragment::class.java.simpleName
    }
}