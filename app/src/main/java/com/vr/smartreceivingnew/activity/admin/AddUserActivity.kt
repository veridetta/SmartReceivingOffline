package com.vr.smartreceivingnew.activity.admin

import android.app.ProgressDialog
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import com.vr.smartreceivingnew.R
import com.vr.smartreceivingnew.helper.DatabaseHelper
import com.vr.smartreceivingnew.helper.showSnack
import com.vr.smartreceivingnew.model.UserModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

class AddUserActivity : AppCompatActivity() {
    private lateinit var etNama:EditText
    private lateinit var etEmail:EditText
    private lateinit var tvJudul: TextView
    private lateinit var etPassword:EditText
    private lateinit var etNoHp:EditText
    private lateinit var btnSimpan: Button
    private lateinit var btnBack: ImageView
    private lateinit var progressDialog: ProgressDialog

    private lateinit var databaseHelper: DatabaseHelper

    private var role ="user"
    private var type =""
    private var docId =""
    private var uid =""
    private var nama =""
    private var username =""
    private var password =""
    private var noHp =""
    private var createdAt =""

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_user)
        databaseHelper = DatabaseHelper(this)

        initView()
        initIntent()
        setIntent()
        initClick()
    }
    private fun initView(){
        etNama = findViewById(R.id.etNama)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        etNoHp = findViewById(R.id.etNoHp)
        btnSimpan = findViewById(R.id.btnSimpan)
        btnBack = findViewById(R.id.btnBack)
        tvJudul = findViewById(R.id.tvJudul)
        progressDialog = ProgressDialog(this)
        progressDialog.setCancelable(false)
        progressDialog.setMessage("Loading...")
    }
    private fun initIntent(){
        type = intent.getStringExtra("type").toString()
        if (type == "edit"){
            docId = intent.getStringExtra("docId").toString()
            uid = intent.getStringExtra("uid").toString()
            nama = intent.getStringExtra("nama").toString()
            username = intent.getStringExtra("username").toString()
            password = intent.getStringExtra("password").toString()
            noHp = intent.getStringExtra("noHp").toString()
            createdAt = intent.getStringExtra("createdAt").toString()
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun setIntent(){
        if (type == "edit"){
            etNama.setText(nama)
            etEmail.setText(username)
            etPassword.setText(password)
            etNoHp.setText(noHp)
            tvJudul.text = "Edit User"
        }else{
            tvJudul.text = "Tambah User"
            //tanggal sekarang dan jam
            val currentDateTime = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")
            val formatted = currentDateTime.format(formatter)
            createdAt = formatted
        }
    }
    private fun initClick(){
        btnBack.setOnClickListener {
            onBackPressed()
        }
        btnSimpan.setOnClickListener {
            if (type == "edit"){
                editUser()
            }else{
                addUser()
            }
        }
    }
    private fun editUser() {
        val nama = etNama.text.toString()
        val username = etEmail.text.toString()
        val password = etPassword.text.toString()
        val noHp = etNoHp.text.toString()

        if (nama.isEmpty() || username.isEmpty() || password.isEmpty() || noHp.isEmpty()) {
            showSnack(this, "Data tidak boleh kosong")
        } else {
            progressDialog.show()
            val user = UserModel(uid, docId, nama, username, password, noHp, role, createdAt)
            val isSuccess = databaseHelper.updateUser(user)
            if (isSuccess) {
                showSnack(this, "Berhasil menyimpan user")
            } else {
                showSnack(this, "Gagal menyimpan user")
            }
            progressDialog.dismiss()
            finish()
        }
    }
    private fun addUser() {
        val nama = etNama.text.toString()
        val username = etEmail.text.toString()
        val password = etPassword.text.toString()
        val noHp = etNoHp.text.toString()

        if (nama.isEmpty() || username.isEmpty() || password.isEmpty() || noHp.isEmpty()) {
            showSnack(this, "Data tidak boleh kosong")
        } else {
            progressDialog.show()
            val newUser = UserModel(UUID.randomUUID().toString(), null, nama, username, password, noHp, role, createdAt)
            val isSuccess = databaseHelper.addUser(newUser)
            if (isSuccess) {
                showSnack(this, "Berhasil menyimpan user")
            } else {
                showSnack(this, "Gagal menyimpan user")
            }
            progressDialog.dismiss()
            finish()
        }
    }
}