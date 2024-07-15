package com.gosty.jejakanak.ui.parent.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.gosty.jejakanak.R
import com.gosty.jejakanak.databinding.FragmentParentProfileBinding
import com.gosty.jejakanak.services.ParentLocationService
import com.gosty.jejakanak.ui.auth.AuthActivity
import com.gosty.jejakanak.utils.Result
import com.gosty.jejakanak.utils.formatIntWithSeparator
import com.gosty.jejakanak.utils.showContentState
import com.gosty.jejakanak.utils.showErrorState
import com.gosty.jejakanak.utils.showLoadingState
import com.kennyc.view.MultiStateView
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ParentProfileFragment : Fragment(), MultiStateView.StateListener {
    private var _binding: FragmentParentProfileBinding? = null
    private val binding get() = _binding
    private val viewModel: ParentProfileViewModel by viewModels()
    private lateinit var multiStateView: MultiStateView

    @Inject
    lateinit var firebaseAuth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentParentProfileBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()

        multiStateView = binding?.msvParentProfile!!
        multiStateView.listener = this@ParentProfileFragment
        multiStateView.getView(MultiStateView.ViewState.ERROR)
            ?.findViewById<Button>(R.id.btn_refresh)
            ?.setOnClickListener {
                initView()
            }

        binding?.btnParentLogout?.setOnClickListener {
            logout()
        }
    }

    private fun initView() {
        viewModel.getUser().observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Loading -> {
                    binding?.msvParentProfile?.showLoadingState()
                }

                is Result.Success -> {
                    binding?.apply {
                        msvParentProfile.showContentState()

                        Glide.with(requireActivity())
                            .load(result.data.photo)
                            .placeholder(R.drawable.ic_image_black)
                            .error(R.drawable.ic_broken_image_black)
                            .centerCrop()
                            .into(ivParentProfileAvatar)

                        tvParentProfileName.text = buildString {
                            append(result.data.firstName)
                            append(" ")
                            append(result.data.lastName)
                        }
                        tvParentProfileEmail.text = result.data.email
                        tvParentProfilePhone.text = result.data.phone
                        tvTotalGeofence.text =
                            result.data.geofences?.size?.formatIntWithSeparator() ?: "0"
                        tvTotalChild.text =
                            result.data.childrenId?.size?.formatIntWithSeparator() ?: "0"
                    }
                }

                is Result.Error<*> -> {
                    binding?.msvParentProfile?.showErrorState()
                    showToast(result.errorData.toString())
                }
            }
        }
    }

    private fun logout() {
        MaterialAlertDialogBuilder(requireActivity())
            .setTitle(activity?.getString(R.string.confirmation))
            .setMessage(activity?.getString(R.string.are_you_sure))
            .setPositiveButton(activity?.getString(R.string.yes)) { _, _ ->

                viewModel.signOut()

                val serviceIntent = Intent(requireActivity(), ParentLocationService::class.java)
                activity?.stopService(serviceIntent)
                toAuthActivity()
            }
            .setNegativeButton(activity?.getString(R.string.no)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun toAuthActivity() {
        val intent = Intent(requireActivity(), AuthActivity::class.java)
        startActivity(intent)
        activity?.finish()
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onStateChanged(viewState: MultiStateView.ViewState) {}
}