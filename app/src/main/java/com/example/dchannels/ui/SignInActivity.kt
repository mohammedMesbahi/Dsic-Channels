package com.example.dchannels.ui

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.widget.Toast
import com.example.dchannels.Constants
import com.example.dchannels.Models.Admin
import com.example.dchannels.databinding.ActivitySignInBinding
import com.example.dchannels.utilities.Authentication
import com.example.dchannels.utilities.MyPreferences
import com.example.dchannels.utilities.Utilities

class SignInActivity : AppCompatActivity() {

    private lateinit var mainLayout: View
    private val hideHandler = Handler(Looper.getMainLooper())
    private var isFullscreen: Boolean = false


    lateinit var binding: ActivitySignInBinding
    private lateinit var preferenceManager: MyPreferences

    var isLoading = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferenceManager = MyPreferences(this)

        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mainLayout = binding.mainLayout // replace with your layout ID
        isFullscreen = true
        // Set up the user interaction to manually show or hide the system UI.
        mainLayout.setOnClickListener { toggle() }
        // Trigger the initial hide() shortly after the activity has been created.
        delayedHide(100)

        setListeners()
    }

    fun setListeners() {
        binding.signInButton.setOnClickListener {
            if (isValidSingInDetails()) {
                signIn()
            }
        }
    }

    fun signIn() {
        isLoading = true
        Utilities.toggleLoading(isLoading, binding.signInButton, binding.signInProgressBar)
        val email = binding.emailEditText.text.toString()
        val password = binding.passwordEditText.text.toString()
        Authentication.signIn(email, password)
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    Utilities.showToast(
                        this,
                        "email or password is incorrect"
                    )
                } else {
                    val admin = Admin(
                        documents.documents[0].id,
                        documents.documents[0].data?.get(Constants.USER_NAME_FIELD).toString(),
                        documents.documents[0].data?.get(Constants.USER_EMAIL_FIELD).toString(),
                        documents.documents[0].data?.get(Constants.USER_PASSWORD_FIELD).toString()
                    )
                    admin.profileImage = documents.documents[0].data?.get(Constants.USER_PROFILE_IMAGE_FIELD).toString()
                    Log.d("Constants.TAG", documents.documents[0].data?.get(Constants.USER_PROFILE_IMAGE_FIELD).toString())
                            preferenceManager.id = admin.id
                            preferenceManager.name = admin.name
                            preferenceManager.email = admin.email
                            preferenceManager.profileImage = admin.profileImage as String

                            val intent = Intent(this, HomeActivity::class.java)
                            startActivity(intent)
                            finish()
                    }
                isLoading = false
                Utilities.toggleLoading(isLoading, binding.signInButton, binding.signInProgressBar)
                }
            .addOnFailureListener { exception ->
                Utilities.showToast(this, "Error: ${exception.message}")
            }
    }

    fun isValidSingInDetails(): Boolean {
        if (binding.emailEditText.text.toString().isEmpty()
            || !android.util.Patterns.EMAIL_ADDRESS.matcher(binding.emailEditText.text.toString())
                .matches()
        ) {
            binding.emailEditText.error = "Please enter a valid email"
            return false
        }
        if (binding.passwordEditText.text.toString().isEmpty()) {
            binding.passwordEditText.error = "Please enter password"
            return false
        }
        return true
    }


    private fun toggle() {
        if (isFullscreen) {
            hide()
        } else {
            show()
        }
    }

    private fun hide() {
        // Hide UI first
        supportActionBar?.hide()
        isFullscreen = false

        if (Build.VERSION.SDK_INT >= 30) {
            // Use WindowInsetsController for API 30 and above
            mainLayout.windowInsetsController?.let {
                it.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                it.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            // Use deprecated methods for older API versions
            mainLayout.systemUiVisibility = (View.SYSTEM_UI_FLAG_LOW_PROFILE
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
        }
    }

    private fun show() {
        // Show the system bar
        if (Build.VERSION.SDK_INT >= 30) {
            mainLayout.windowInsetsController?.show(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
        } else {
            mainLayout.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION)
        }
        isFullscreen = true

        // Show the ActionBar
        supportActionBar?.show()
    }

    /**
     * Schedules a call to hide() in delayMillis, canceling any
     * previously scheduled calls.
     */
    private fun delayedHide(delayMillis: Int) {
        hideHandler.removeCallbacksAndMessages(null)
        hideHandler.postDelayed({ hide() }, delayMillis.toLong())
    }

    companion object {
        private const val UI_ANIMATION_DELAY = 300
    }
}