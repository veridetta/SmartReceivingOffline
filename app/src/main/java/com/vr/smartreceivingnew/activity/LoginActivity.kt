package com.vr.smartreceivingnew.activity

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.vr.smartreceivingnew.R
import com.vr.smartreceivingnew.activity.admin.AdminActivity
import com.vr.smartreceivingnew.activity.user.UserActivity
import com.vr.smartreceivingnew.helper.DatabaseHelper
import com.vr.smartreceivingnew.helper.showSnack

class LoginActivity : AppCompatActivity() {

    // Declare UI elements
    private lateinit var buttonLogin: Button
    private lateinit var editTextEmail: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var progressDialog: ProgressDialog
    private lateinit var databaseHelper: DatabaseHelper // SQLite database helper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        initView()
        clickView()
    }

    fun initView(){
        // Initialize UI elements
        buttonLogin = findViewById(R.id.buttonLogin)
        editTextEmail = findViewById(R.id.editTextEmail)
        editTextPassword = findViewById(R.id.editTextPassword)
        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Please wait...")
        progressDialog.setCancelable(false)

        databaseHelper = DatabaseHelper(this) // Initialize DatabaseHelper
    }

    fun clickView(){
        buttonLogin.setOnClickListener {
            progressDialog.show()
            val email = editTextEmail.text.toString()
            val password = editTextPassword.text.toString()

            // Validate login credentials using SQLite
            val user = databaseHelper.getUser(email, password)
            progressDialog.dismiss()
            if (user != null) {
                // Save user role to SharedPreferences
                val sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE)
                val editor = sharedPreferences.edit()
                editor.putBoolean("isLogin", true)
                editor.putString("userRole", user.role)
                editor.putString("userUid", user.docId)
                editor.putString("userName", user.nama)
                editor.putString("userUsername", user.username)
                editor.apply()
                Log.d("Login","Role ${user.role}")

                // Redirect to appropriate activity based on user role
                when (user.role) {
                    "admin" -> startActivity(Intent(this, AdminActivity::class.java))
                    "user" -> startActivity(Intent(this, UserActivity::class.java))
                }
                finish()
            } else {
                showSnack(this, "Login failed. Please check your credentials.")
            }
        }
    }
}