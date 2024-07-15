package com.gosty.jejakanak.ui.parent.manage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayoutMediator
import com.gosty.jejakanak.R
import com.gosty.jejakanak.core.ui.TabLayoutParentManageAdapter
import com.gosty.jejakanak.databinding.FragmentParentManageBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ParentManageFragment : Fragment() {
    private var _binding: FragmentParentManageBinding? = null
    private val binding get() = _binding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentParentManageBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupTabLayout()
    }

    private fun setupTabLayout() {
        val tabLayoutAdapter = TabLayoutParentManageAdapter(requireActivity())
        binding?.viewPager?.adapter = tabLayoutAdapter
        TabLayoutMediator(binding?.tabLayout!!, binding?.viewPager!!) { tab, position ->
            tab.text = resources.getString(TAB_TITLES[position])
        }.attach()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    companion object {
        @StringRes
        private val TAB_TITLES = intArrayOf(
            R.string.geofence,
            R.string.children
        )
    }
}