package com.gosty.jejakanak.core.ui

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.gosty.jejakanak.ui.parent.manage.children.ParentChildrenListFragment
import com.gosty.jejakanak.ui.parent.manage.geofence.ParentGeofencesFragment

class TabLayoutParentManageAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment =
        if (position == 0) {
            ParentGeofencesFragment()
        } else {
            ParentChildrenListFragment()
        }
}