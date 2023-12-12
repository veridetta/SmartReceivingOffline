package com.vr.smartreceivingnew.activity.admin

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vr.smartreceivingnew.R
import com.vr.smartreceivingnew.adapter.UserAdapter
import com.vr.smartreceivingnew.helper.DatabaseHelper
import com.vr.smartreceivingnew.helper.showSnack
import com.vr.smartreceivingnew.model.UserModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ListUserActivity : AppCompatActivity() {
    private lateinit var userAdapter: UserAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var contentView: RelativeLayout
    private lateinit var searchLayout: LinearLayout
    private lateinit var btnCari: EditText
    private lateinit var btnBack: ImageView
    private lateinit var btnAdd: Button
    private lateinit var progressDialog: ProgressDialog
    val TAG = "LOAD DATA"
    private val userList: MutableList<UserModel> = mutableListOf()
    private lateinit var databaseHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_user)
        databaseHelper = DatabaseHelper(this)

        initView()
        initRc()
        initData()
        initCari()
        initClick()

    }
    private fun initView(){
        recyclerView = findViewById(R.id.rcUser)
        contentView = findViewById(R.id.contentView)
        searchLayout = findViewById(R.id.searchLayout)
        btnCari = findViewById(R.id.btnCari)
        btnAdd = findViewById(R.id.btnAdd)
        btnBack = findViewById(R.id.btnBack)
        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Loading...")
        progressDialog.setCancelable(false)

    }
    private fun initRc(){
        recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(this@ListUserActivity, 1)
            // set the custom adapter to the RecyclerView
            userAdapter = UserAdapter(
                userList,
                this@ListUserActivity,
                { user -> editUser(user) },
                { user -> hapusUser(user) }
            )
        }
    }
    private fun initData(){
        readData()
        recyclerView.adapter = userAdapter
    }
    private fun initCari(){
        userAdapter.filter("")
        btnCari.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                userAdapter.filter(s.toString())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }
    private fun readData() {
        Log.d(TAG, "Fetched users: read user called")
        progressDialog.show()
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val users = databaseHelper.getAllUsers() // Fetch users from the database
                Log.d(TAG, "Fetched users: ${users.size}")
                withContext(Dispatchers.Main) {
                    userAdapter.updateUsers(users) // Update the adapter's data
                    progressDialog.dismiss()
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error reading data List User: $e")
                progressDialog.dismiss()
            }
        }
    }


    private fun editUser(user: UserModel) {
        //intent ke homeActivity fragment add
        val intent = Intent(this, AddUserActivity::class.java)
        intent.putExtra("type", "edit")
        intent.putExtra("docId", user.docId)
        intent.putExtra("uid", user.uid)
        intent.putExtra("nama", user.nama)
        intent.putExtra("username", user.username)
        intent.putExtra("password", user.password)
        intent.putExtra("noHp", user.noHp)

        startActivity(intent)
    }
    private fun hapusUser(user: UserModel) {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Yakin ingin menghapus ${user.nama}?")
        builder.setPositiveButton("Ya") { dialog, which ->
            progressDialog.show()
            val isSuccess = databaseHelper.deleteUser(user.docId.toString())
            if (isSuccess) {
                showSnack(this, "Berhasil menghapus user")
                readData() // Reload data
            } else {
                showSnack(this, "Gagal menghapus user")
            }
            progressDialog.dismiss()
        }
        builder.create().show()
    }
    private fun initClick(){
        btnBack.setOnClickListener {
            finish()
        }
        btnAdd.setOnClickListener {
            //intent ke homeActivity fragment add
            val intent = Intent(this, AddUserActivity::class.java)
            intent.putExtra("type", "add")
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        readData()
    }
}