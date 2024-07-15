package com.gosty.jejakanak.ui.auth

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.lifecycleScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.gosty.jejakanak.R
import com.gosty.jejakanak.databinding.ActivityAuthBinding
import com.gosty.jejakanak.ui.child.main.ChildActivity
import com.gosty.jejakanak.ui.parent.main.ParentActivity
import com.gosty.jejakanak.utils.Result
import com.gosty.jejakanak.utils.showContentState
import com.gosty.jejakanak.utils.showLoadingState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AuthActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAuthBinding
    private val viewModel: AuthViewModel by viewModels()

    @Inject
    lateinit var firebaseAuth: FirebaseAuth

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean? ->
        if (!isGranted!!)
            Toast.makeText(
                this@AuthActivity,
                "Unable to display Foreground service notification due to permission decline",
                Toast.LENGTH_LONG
            )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityAuthBinding.inflate(layoutInflater)

        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.authStateView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this@AuthActivity,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        binding.btnLogin.setOnClickListener {
            // TODO input phone number
            confirmationDialog()
        }
    }

    private fun confirmationDialog() {
        val userRoles =
            arrayOf(resources.getString(R.string.parent), resources.getString(R.string.child))
        var checkedItem = 0

        MaterialAlertDialogBuilder(this@AuthActivity)
            .setTitle(resources.getString(R.string.choose_your_role))
            .setNegativeButton(resources.getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton(resources.getString(R.string.save)) { _, _ ->
                if (checkedItem == 0) {
                    signIn(true)
                } else {
                    signIn(false)
                }
            }
            .setSingleChoiceItems(userRoles, checkedItem) { _, which ->
                checkedItem = which
            }
            .show()
    }

    private fun phoneNumberDialog(isParent: Boolean) {
        val dialogView =
            LayoutInflater.from(this@AuthActivity).inflate(R.layout.phone_dialog_layout, null)
        val phoneInput = dialogView.findViewById<TextInputEditText>(R.id.tiet_add_phone)

        MaterialAlertDialogBuilder(this@AuthActivity)
            .setTitle(R.string.input_phone)
            .setView(dialogView)
            .setPositiveButton(R.string.save) { _, _ ->
                val phoneNumber = phoneInput.text.toString()
                if (phoneNumber.isNotEmpty()) {
                    inputPhoneNumber(phoneNumber, isParent)
                } else {
                    Toast.makeText(
                        this@AuthActivity,
                        getString(R.string.fill_all_fields),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                viewModel.signOut()
                dialog.dismiss()
            }
            .setOnCancelListener {
                viewModel.signOut()
            }
            .show()
    }

    private fun signIn(isParent: Boolean) {
        val credentialManager = CredentialManager.create(this@AuthActivity)

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setAutoSelectEnabled(false)
            .setServerClientId(getString(R.string.default_web_client_id))
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        lifecycleScope.launch {
            try {
                Log.d(TAG, "Attempting to get credentials")
                val result: GetCredentialResponse = credentialManager.getCredential(
                    request = request,
                    context = this@AuthActivity
                )
                Log.d(TAG, "Credential response received: $result")
                handleSignIn(result, isParent)
            } catch (e: GetCredentialException) {
                Log.e("Error", "Credential exception: ${e.message}")
            } catch (e: Exception) {
                Log.e("Error", "Unexpected error: ${e.message}")
            }
        }
    }

    private fun handleSignIn(result: GetCredentialResponse, isParent: Boolean) {
        when (val credential = result.credential) {
            is CustomCredential -> {
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    try {
                        // Use googleIdTokenCredential and extract id to validate and authenticate on your server.
                        val googleIdTokenCredential =
                            GoogleIdTokenCredential.createFrom(credential.data)
                        firebaseAuthWithGoogle(googleIdTokenCredential.idToken, isParent)
                    } catch (e: GoogleIdTokenParsingException) {
                        Log.e(TAG, "Received an invalid google id token response", e)
                    }
                } else {
                    // Catch any unrecognized custom credential type here.
                    Log.e(TAG, "Unexpected type of credential")
                }
            }

            else -> {
                // Catch any unrecognized credential type here.
                Log.e(TAG, "Unexpected type of credential")
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String, isParent: Boolean) {
        val credential: AuthCredential = GoogleAuthProvider.getCredential(idToken, null)
        viewModel.signIn(credential, isParent).observe(this@AuthActivity) {
            when (it) {
                is Result.Loading -> {
                    binding.authStateView.showLoadingState()
                }

                is Result.Success -> {
                    binding.authStateView.showContentState()
                    if (isParent) {
                        viewModel.setUserRole("Parent")
                    } else {
                        viewModel.setUserRole("Child")
                    }
                    isPhoneNumberExist(isParent)
                }

                is Result.Error<*> -> {
                    binding.authStateView.showContentState()
                    Toast.makeText(this@AuthActivity, it.errorData.toString(), Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    private fun isPhoneNumberExist(isParent: Boolean) {
        viewModel.isUserPhoneNumberExist().observe(this@AuthActivity) {
            when (it) {
                is Result.Loading -> {
                    binding.authStateView.showLoadingState()
                }

                is Result.Success -> {
                    binding.authStateView.showContentState()
                    if (it.data) {
                        toHomePage(isParent)
                    } else {
                        phoneNumberDialog(isParent)
                    }
                }

                is Result.Error<*> -> {
                    binding.authStateView.showContentState()
                    Toast.makeText(this@AuthActivity, it.errorData.toString(), Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    private fun toHomePage(isParent: Boolean) {
        val intent = if (isParent) {
            Intent(this@AuthActivity, ParentActivity::class.java)
        } else {
            Intent(this@AuthActivity, ChildActivity::class.java)
        }
        startActivity(intent)
        finish()
    }

    private fun inputPhoneNumber(phoneNumber: String, isParent: Boolean) {
        viewModel.inputUserPhoneNumber(phoneNumber).observe(this@AuthActivity) {
            when (it) {
                is Result.Loading -> {
                    binding.authStateView.showLoadingState()
                }

                is Result.Success -> {
                    binding.authStateView.showContentState()
                    val intent = if (isParent) {
                        Intent(this@AuthActivity, ParentActivity::class.java)
                    } else {
                        Intent(this@AuthActivity, ChildActivity::class.java)
                    }
                    startActivity(intent)
                    finish()
                }

                is Result.Error<*> -> {
                    binding.authStateView.showContentState()
                    Toast.makeText(this@AuthActivity, it.errorData.toString(), Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    companion object {
        private val TAG = AuthActivity::class.java.simpleName
    }
}