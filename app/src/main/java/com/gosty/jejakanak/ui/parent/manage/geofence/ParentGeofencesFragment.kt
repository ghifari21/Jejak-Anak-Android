package com.gosty.jejakanak.ui.parent.manage.geofence

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.gosty.jejakanak.R
import com.gosty.jejakanak.core.domain.models.CoordinateModel
import com.gosty.jejakanak.core.domain.models.GeofenceModel
import com.gosty.jejakanak.core.ui.RvGeofenceListAdapter
import com.gosty.jejakanak.databinding.FragmentParentGeonfencesBinding
import com.gosty.jejakanak.ui.parent.manage.geofence.map.ParentAddGeofenceMapsActivity
import com.gosty.jejakanak.utils.Result
import com.gosty.jejakanak.utils.showContentState
import com.gosty.jejakanak.utils.showEmptyState
import com.gosty.jejakanak.utils.showErrorState
import com.gosty.jejakanak.utils.showLoadingState
import com.kennyc.view.MultiStateView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ParentGeofencesFragment : Fragment(), MultiStateView.StateListener {
    private var _binding: FragmentParentGeonfencesBinding? = null
    private val binding get() = _binding
    private val viewModel: ParentGeofencesViewModel by viewModels()
    private val adapter = RvGeofenceListAdapter()
    private lateinit var multiStateView: MultiStateView

    private val launcherIntentMaps = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == MAPS_RESULT) {
            val data =
                it.data?.getParcelableArrayListExtra<CoordinateModel>(MAPS_RESULT_DATA)
            val label = it.data?.getStringExtra(MAPS_RESULT_LABEL)
            val zoneType = it.data?.getStringExtra(MAPS_RESULT_ZONE_TYPE)
            val id = it.data?.getStringExtra(MAPS_RESULT_ID)
            if (!data.isNullOrEmpty()) {
                if (id.isNullOrEmpty()) {
                    val geofence = GeofenceModel(
                        label = label,
                        type = zoneType,
                        coordinates = data
                    )
                    addGeofence(geofence)
                } else {
                    val geofence = GeofenceModel(
                        id = id,
                        label = label,
                        type = zoneType,
                        coordinates = data
                    )
                    updateGeofence(geofence)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentParentGeonfencesBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        initRecyclerView()

        multiStateView = binding?.msvParentGeofences!!
        multiStateView.listener = this@ParentGeofencesFragment
        multiStateView.getView(MultiStateView.ViewState.ERROR)
            ?.findViewById<Button>(R.id.btn_refresh)
            ?.setOnClickListener {
                initView()
            }

        multiStateView.getView(MultiStateView.ViewState.EMPTY)
            ?.findViewById<Button>(R.id.btn_add_geofence_empty_state)
            ?.setOnClickListener {
                addGeofenceDialog()
            }

        binding?.refreshParentGeofences?.setOnRefreshListener {
            initView()
            binding?.refreshParentGeofences?.isRefreshing = false
        }

        binding?.btnAddGeofence?.setOnClickListener {
            addGeofenceDialog()
        }
    }

    private fun initView() {
        viewModel.getAllGeofences().observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Loading -> {
                    binding?.msvParentGeofences?.showLoadingState()
                }

                is Result.Success -> {
                    if (result.data.isNotEmpty()) {
                        adapter.submitList(result.data)
                        binding?.msvParentGeofences?.showContentState()
                    } else {
                        binding?.msvParentGeofences?.showEmptyState()
                    }
                }

                is Result.Error<*> -> {
                    showToast(result.errorData.toString())
                    multiStateView.showErrorState()
                }

            }
        }
    }

    private fun initRecyclerView() {
        val layoutManager = LinearLayoutManager(requireActivity())
        binding?.apply {
            rvParentGeofences.layoutManager = layoutManager
            rvParentGeofences.adapter = adapter
            rvParentGeofences.setHasFixedSize(true)
        }

        adapter.setOnItemClickCallback(
            object : RvGeofenceListAdapter.OnItemClickCallback {
                override fun onEditGeofenceClicked(geofenceModel: GeofenceModel) {
                    editGeofenceDialog(geofenceModel)
                }

                override fun onDeleteGeofenceClicked(geofenceModel: GeofenceModel) {
                    deleteGeofenceDialog(geofenceModel)
                }
            }
        )
    }

    private fun addGeofenceDialog() {
        val dialogView =
            LayoutInflater.from(requireContext()).inflate(R.layout.geofence_dialog_layout, null)
        val labelInput = dialogView.findViewById<TextInputEditText>(R.id.tiet_geofence_label)
        val zoneTypeInput = dialogView.findViewById<AutoCompleteTextView>(R.id.actv_geofence_type)

        // Initialize the zoneType as a list with a single empty string
        val zoneType = mutableListOf("")

        // Set the item click listener for the AutoCompleteTextView
        zoneTypeInput.onItemClickListener =
            AdapterView.OnItemClickListener { parent, _, position, _ ->
                val zoneTypeItem = parent.getItemAtPosition(position).toString()
                zoneType[0] =
                    if (zoneTypeItem == activity?.getString(R.string.danger_zone)) "danger" else "safe"
            }

        MaterialAlertDialogBuilder(requireActivity())
            .setTitle(activity?.getString(R.string.add_geofence))
            .setView(dialogView)
            .setPositiveButton(activity?.getString(R.string.next)) { _, _ ->
                val label = labelInput.text.toString()

                if (label.isNotEmpty() && zoneType[0].isNotEmpty()) {
                    val intent =
                        Intent(requireActivity(), ParentAddGeofenceMapsActivity::class.java).apply {
                            putExtra(EXTRA_LABEL, label)
                            putExtra(EXTRA_ZONE_TYPE, zoneType[0])
                        }
                    launcherIntentMaps.launch(intent)
                } else {
                    showToast(activity?.getString(R.string.fill_all_fields).toString())
                }
            }
            .setNegativeButton(activity?.getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }


    private fun addGeofence(geofence: GeofenceModel) {
        viewModel.addGeofence(geofence).observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Loading -> {
                    binding?.msvParentGeofences?.showLoadingState()
                }

                is Result.Success -> {
                    showToast(result.data)
                    binding?.msvParentGeofences?.showContentState()
                    adapter.notifyDataSetChanged()
                }

                is Result.Error<*> -> {
                    showToast(result.errorData.toString())
                    binding?.msvParentGeofences?.showErrorState()
                }
            }
        }
    }

    private fun editGeofenceDialog(geofenceModel: GeofenceModel) {
        val dialogView =
            LayoutInflater.from(requireContext()).inflate(R.layout.geofence_dialog_layout, null)
        val labelInput = dialogView.findViewById<TextInputEditText>(R.id.tiet_geofence_label)
        val zoneTypeInput = dialogView.findViewById<AutoCompleteTextView>(R.id.actv_geofence_type)
        labelInput.setText(geofenceModel.label)
        val previousZoneType = if (geofenceModel.type == "danger") {
            activity?.getString(R.string.danger_zone)
        } else {
            activity?.getString(R.string.safe_zone)
        }
        zoneTypeInput.setText(previousZoneType, false)

        // Initialize the zoneType as a list with a single empty string
        val zoneType = mutableListOf(geofenceModel.type!!)

        // Set the item click listener for the AutoCompleteTextView
        zoneTypeInput.onItemClickListener =
            AdapterView.OnItemClickListener { parent, _, position, _ ->
                val zoneTypeItem = parent.getItemAtPosition(position).toString()
                zoneType[0] =
                    if (zoneTypeItem == activity?.getString(R.string.danger_zone)) "danger" else "safe"
            }

        MaterialAlertDialogBuilder(requireActivity())
            .setTitle(activity?.getString(R.string.update_geofence))
            .setView(dialogView)
            .setPositiveButton(activity?.getString(R.string.save)) { _, _ ->
                val label = labelInput.text.toString()

                if (label.isNotEmpty() && zoneType[0].isNotEmpty()) {
                    val intent =
                        Intent(requireActivity(), ParentAddGeofenceMapsActivity::class.java).apply {
                            putExtra(EXTRA_LABEL, label)
                            putExtra(EXTRA_ZONE_TYPE, zoneType[0])
                            putExtra(EXTRA_ID, geofenceModel.id)
                        }
                    launcherIntentMaps.launch(intent)
                } else {
                    showToast(activity?.getString(R.string.fill_all_fields).toString())
                }

//                val geofence = GeofenceModel(
//                    id = geofenceModel.id,
//                    label = label,
//                    type = zoneType[0],
//                    parentId = geofenceModel.parentId,
//                    coordinates = geofenceModel.coordinates
//                )
//                updateGeofence(geofence)
            }
            .setNegativeButton(activity?.getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun deleteGeofenceDialog(geofenceModel: GeofenceModel) {
        MaterialAlertDialogBuilder(requireActivity())
            .setTitle(activity?.getString(R.string.confirmation))
            .setMessage(activity?.getString(R.string.are_you_sure))
            .setPositiveButton(activity?.getString(R.string.yes)) { _, _ ->
                removeGeofence(geofenceModel.id!!)
            }
            .setNegativeButton(activity?.getString(R.string.no)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun removeGeofence(id: String) {
        viewModel.removeGeofence(id).observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Loading -> {
                    binding?.msvParentGeofences?.showLoadingState()
                }

                is Result.Success -> {
                    showToast(result.data)
                    binding?.msvParentGeofences?.showContentState()
                    adapter.notifyDataSetChanged()
                }

                is Result.Error<*> -> {
                    showToast(result.errorData.toString())
                    binding?.msvParentGeofences?.showErrorState()
                }
            }
        }
    }

    private fun updateGeofence(geofence: GeofenceModel) {
        viewModel.updateGeofence(geofence).observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Loading -> {
                    binding?.msvParentGeofences?.showLoadingState()
                }

                is Result.Success -> {
                    showToast(result.data)
                    binding?.msvParentGeofences?.showContentState()
                    adapter.notifyDataSetChanged()
                }

                is Result.Error<*> -> {
                    showToast(result.errorData.toString())
                    binding?.msvParentGeofences?.showErrorState()
                }
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireActivity(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onStateChanged(viewState: MultiStateView.ViewState) {}

    companion object {
        const val MAPS_RESULT_DATA = "maps_result_data"
        const val MAPS_RESULT_LABEL = "maps_result_label"
        const val MAPS_RESULT_ZONE_TYPE = "maps_result_zone_type"
        const val MAPS_RESULT_ID = "maps_result_id"
        const val MAPS_RESULT = 200
        const val EXTRA_LABEL = "extra_label"
        const val EXTRA_ZONE_TYPE = "extra_zone_type"
        const val EXTRA_ID = "extra_id"
    }
}