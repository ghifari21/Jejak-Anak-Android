package com.gosty.jejakanak.ui.child.profile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.gosty.jejakanak.R
import com.gosty.jejakanak.core.domain.models.ParentModel
import com.gosty.jejakanak.core.ui.RvParentListAdapter
import com.gosty.jejakanak.databinding.FragmentChildProfileBinding
import com.gosty.jejakanak.services.ChildLocationService
import com.gosty.jejakanak.ui.auth.AuthActivity
import com.gosty.jejakanak.utils.Result
import com.gosty.jejakanak.utils.showContentState
import com.gosty.jejakanak.utils.showErrorState
import com.gosty.jejakanak.utils.showLoadingState
import com.kennyc.view.MultiStateView
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ChildProfileFragment : Fragment(), MultiStateView.StateListener {

    private var _binding: FragmentChildProfileBinding? = null
    private val binding get() = _binding
    private val viewModel: ChildProfileViewModel by viewModels()
    private val adapter = RvParentListAdapter()
    private lateinit var multiStateView: MultiStateView

    @Inject
    lateinit var firebaseAuth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentChildProfileBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initProfile()
        initRecycleView()
        initListView()

        multiStateView = binding?.msvChildProfile!!
        multiStateView.listener = this@ChildProfileFragment
        multiStateView.getView(MultiStateView.ViewState.ERROR)
            ?.findViewById<Button>(R.id.btn_refresh)
            ?.setOnClickListener {
                initProfile()
                initListView()
            }

        binding?.btnChildLogout?.setOnClickListener {
            userLogout()
        }
    }

    private fun initProfile() {
        viewModel.getChildProfile().observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Loading -> {
                    binding?.msvChildProfile?.showLoadingState()
                }

                is Result.Success -> {
                    binding?.msvChildProfile?.showContentState()
                    binding?.tvChildName?.text = buildString {
                        append(result.data.firstName)
                        append(" ")
                        append(result.data.lastName)
                    }
                    binding?.tvChildEmail?.text = result.data.email
                    binding?.tvChildUniqueCode?.text = result.data.uniqueCode

                    Glide.with(requireActivity())
                        .load(result.data.photo)
                        .placeholder(R.drawable.ic_image_black)
                        .error(R.drawable.ic_broken_image_black)
                        .centerCrop()
                        .into(binding?.ivChildAvatar!!)
                }

                is Result.Error<*> -> {
                    binding?.msvChildProfile?.showErrorState()
                    Toast.makeText(
                        requireActivity(),
                        result.errorData.toString(),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun initRecycleView() {
        val layoutManager = LinearLayoutManager(requireActivity())
        binding?.apply {
            rvParentProfile.adapter = adapter
            rvParentProfile.layoutManager = layoutManager
            rvParentProfile.setHasFixedSize(true)

            adapter.setOnItemClickCallback(
                object : RvParentListAdapter.OnItemClickCallback {
                    override fun onItemClicked(parentModel: ParentModel) {
                        if (parentModel.phone != null) {
                            val intent = Intent(Intent.ACTION_DIAL)
                            intent.data = Uri.parse("tel:${parentModel.phone}")
                            startActivity(intent)
                        }
                    }
                }
            )
        }
    }

    private fun initListView() {
        viewModel.getParentsProfile().observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Loading -> {
                    binding?.msvChildProfile?.showLoadingState()
                }

                is Result.Success -> {
                    binding?.msvChildProfile?.showContentState()
                    if (result.data.isNotEmpty()) {
                        binding?.rvParentProfile?.visibility = View.VISIBLE
                        binding?.tvEmptyParentProfile?.visibility = View.GONE
                        adapter.submitList(result.data)
                    } else {
                        binding?.rvParentProfile?.visibility = View.GONE
                        binding?.tvEmptyParentProfile?.visibility = View.VISIBLE
                    }
                }

                is Result.Error<*> -> {
                    binding?.msvChildProfile?.showErrorState()
                    Toast.makeText(
                        requireActivity(),
                        result.errorData.toString(),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun userLogout() {
        MaterialAlertDialogBuilder(requireActivity())
            .setTitle(activity?.getString(R.string.confirmation))
            .setMessage(activity?.getString(R.string.are_you_sure))
            .setPositiveButton(activity?.getString(R.string.yes)) { _, _ ->
                viewModel.signOut()

                val serviceIntent = Intent(requireActivity(), ChildLocationService::class.java)
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

    override fun onStateChanged(viewState: MultiStateView.ViewState) {}

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    companion object {
        private val TAG = ChildProfileFragment::class.java.simpleName
    }
}