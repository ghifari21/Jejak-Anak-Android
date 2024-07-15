package com.gosty.jejakanak.ui.child.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.gosty.jejakanak.R
import com.gosty.jejakanak.databinding.ActivityChildBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChildActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChildBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityChildBinding.inflate(layoutInflater)

        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupBottomNav()
    }

    private fun setupBottomNav() {
        val navView: BottomNavigationView = binding.navView
        val navController = findNavController(R.id.child_nav_host_fragment)

        navView.setupWithNavController(navController)
    }
}