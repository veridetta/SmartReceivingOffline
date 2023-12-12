package com.vr.smartreceivingnew

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ProgressBar
import android.widget.TextView
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.ktx.appCheck
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.initialize
import com.vr.smartreceivingnew.activity.LoginActivity
import com.vr.smartreceivingnew.activity.admin.AdminActivity
import com.vr.smartreceivingnew.activity.user.UserActivity

class SplashActivity : AppCompatActivity() {
    private var progressBar: ProgressBar? = null
    private var loadingText: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Inisialisasi elemen UI
        progressBar = findViewById<ProgressBar>(R.id.progressBar)
        loadingText = findViewById<TextView>(R.id.loadingText)

        // Simulasikan proses loading
        simulateLoading()

        // Periksa apakah pengguna sudah login
        val sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE)
        val isLoggedIn = sharedPreferences.getBoolean("isLogin", false)
        // Initialize Firebase
        Firebase.initialize(context = this)
        Firebase.appCheck.installAppCheckProviderFactory(
            DebugAppCheckProviderFactory.getInstance(),
        )
        Handler(Looper.getMainLooper()).postDelayed({
            checkUserLogin()
        }, 1000) // Jeda selama 1 detik
    }
    private fun simulateLoading() {
        // Tampilkan progress bar dan teks "Loading..."
        progressBar!!.visibility = ProgressBar.VISIBLE
        loadingText!!.visibility = TextView.VISIBLE

        Handler(Looper.getMainLooper()).postDelayed({

            // Setelah simulasi loading selesai, sembunyikan progress bar dan teks
            progressBar!!.visibility = ProgressBar.GONE
            loadingText!!.visibility = TextView.GONE
        }, 1000) // Simulasi loading selama 1 detik
    }
    private fun checkUserLogin() {
        val sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE)
        val isLoggedIn = sharedPreferences.getBoolean("isLogin", false)
        val userRole = sharedPreferences.getString("userRole", "")

        val targetActivity = when {
            isLoggedIn -> {
                //cek token firebase
                when (userRole) {
                    "admin" -> AdminActivity::class.java
                    "user" -> UserActivity::class.java
                    else -> LoginActivity::class.java // Handle unknown roles
                }
            }
            else -> LoginActivity::class.java
        }
        //jika berhasil login maka update token
        if (isLoggedIn) {
            //cek jika token kosong maka update token
            val tokken = sharedPreferences.getString("token", "")
            if (tokken.isNullOrEmpty()) {
                val uid = sharedPreferences.getString("userUid", "")
            }
        }
        startActivity(Intent(this@SplashActivity, targetActivity))
        finish()
    }
}