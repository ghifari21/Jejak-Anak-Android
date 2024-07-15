package com.gosty.jejakanak.ui.parent.main

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.gosty.jejakanak.R
import com.gosty.jejakanak.core.domain.models.ChildModel
import com.gosty.jejakanak.databinding.ActivityParentBinding
import com.gosty.jejakanak.services.ParentLocationService
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ParentActivity : AppCompatActivity() {
    private lateinit var binding: ActivityParentBinding

    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityParentBinding.inflate(layoutInflater)

        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupBottomNav()
        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
//        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        if (intent.action == ParentLocationService.ACTION_OPEN_MAP_FRAGMENT) {
            val childModel: ChildModel? =
                intent.getParcelableExtra(ParentLocationService.EXTRA_CHILD_SERVICE)

            if (childModel != null) {
                val bundle = Bundle().apply {
                    putParcelable(EXTRA_CHILD_PARCELABLE, childModel)
                }
                navController.navigate(R.id.parent_navigation_map, bundle)
            }
        }
    }

    private fun setupBottomNav() {
        val navView: BottomNavigationView = binding.navView
        navController = findNavController(R.id.parent_nav_host_fragment)

        navView.setupWithNavController(navController)
    }

    companion object {
        const val EXTRA_CHILD_PARCELABLE = "childModel_parcelable"
    }
}