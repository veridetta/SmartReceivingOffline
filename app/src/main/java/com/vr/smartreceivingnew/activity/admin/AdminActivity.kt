package com.vr.smartreceivingnew.activity.admin

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vr.smartreceivingnew.R
import com.vr.smartreceivingnew.activity.LoginActivity
import com.vr.smartreceivingnew.adapter.BarangAdapter
import com.vr.smartreceivingnew.helper.DatabaseHelper
import com.vr.smartreceivingnew.helper.showSnack
import com.vr.smartreceivingnew.model.BarangModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AdminActivity : AppCompatActivity() {
    private lateinit var plantAdapter: BarangAdapter

    private lateinit var recyclerView: RecyclerView
    private lateinit var contentView: RelativeLayout
    private lateinit var searchLayout: LinearLayout
    private lateinit var btnCari: EditText
    private lateinit var btnLogout: CardView
    private lateinit var btnAdd: CardView
    private lateinit var btnUser: CardView
    private lateinit var btnReport: CardView
    private lateinit var progressDialog: ProgressDialog

    val TAG = "LOAD DATA AdminActvy"
    private val plantList: MutableList<BarangModel> = mutableListOf()

    private lateinit var databaseHelper: DatabaseHelper


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)
        initView()
        initRc()
        initData()
        initCari()
        initClick()

        databaseHelper = DatabaseHelper(this)

    }
    private fun initView(){
        recyclerView = findViewById(R.id.rcBarang)
        contentView = findViewById(R.id.contentView)
        searchLayout = findViewById(R.id.searchLayout)
        btnCari = findViewById(R.id.btnCari)
        btnAdd = findViewById(R.id.btnAdd)
        btnLogout = findViewById(R.id.btnLogout)
        btnUser = findViewById(R.id.btnUser)
        btnReport = findViewById(R.id.btnReport)
        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Loading...")
        progressDialog.setCancelable(false)

    }
    private fun initRc(){
        recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(this@AdminActivity, 1)
            // set the custom adapter to the RecyclerView
            plantAdapter = BarangAdapter(
                plantList,
                this@AdminActivity,
                { barang -> editBarang(barang) },
                { barang -> hapusBarang(barang) }
            )
        }
    }
    private fun initData(){
        readData()
        recyclerView.adapter = plantAdapter
    }
    private fun initCari(){
        plantAdapter.filter("")
        btnCari.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                plantAdapter.filter(s.toString())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }
    private fun readData() {
        progressDialog.show()
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val plants = databaseHelper.getAllBarang()
                withContext(Dispatchers.Main) {
                    plantList.clear()
                    plantList.addAll(plants)
                    plantAdapter.barangList = plantList // Update the main list
                    plantAdapter.filter("") // Update and refresh the filtered list
                    progressDialog.dismiss()
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error reading data : $e")
                progressDialog.dismiss()
            }
        }
    }



    private fun editBarang(barang: BarangModel) {
        //intent ke homeActivity fragment add
        val intent = Intent(this, AddActivity::class.java)
        intent.putExtra("type", "edit")
        intent.putExtra("docId", barang.docId)
        intent.putExtra("kode1", barang.kode1)
        intent.putExtra("uid", barang.uid)
        intent.putExtra("perRak", barang.perRak)
        intent.putExtra("jenis", barang.perRak)
        intent.putExtra("merek", barang.perRak)
        intent.putExtra("nama", barang.nama)
        intent.putExtra("satuan", barang.satuan)
        intent.putExtra("kode2", barang.kode2)
        intent.putExtra("banyakQr", barang.banyakQr)

        startActivity(intent)
    }
    private fun hapusBarang(barang: BarangModel) {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Yakin ingin menghapus ${barang.nama}?")
        builder.setPositiveButton("Ya") { dialog, which ->
            progressDialog.show()
            if (databaseHelper.deleteBarang(barang.docId.toString())) {
                showSnack(this, "Berhasil menghapus barang")
                readData() // Reload data
            } else {
                showSnack(this, "Gagal menghapus barang")
            }
            progressDialog.dismiss()
        }
        builder.create().show()
    }

    private fun initClick(){
        btnLogout.setOnClickListener {
            // Hapus shared preferences
            val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.clear()
            editor.apply()

            // Arahkan ke MainActivity dengan membersihkan stack aktivitas
            val intent = Intent(this, LoginActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
            finish()
        }
        btnAdd.setOnClickListener {
            //intent ke homeActivity fragment add
            val intent = Intent(this, AddActivity::class.java)
            intent.putExtra("type", "tambah")
            startActivity(intent)
        }
        btnUser.setOnClickListener {
            //intent ke homeActivity fragment add
            val intent = Intent(this, ListUserActivity::class.java)
            startActivity(intent)
        }
        btnReport.setOnClickListener {
            //intent ke homeActivity fragment add
            val intent = Intent(this, ReportActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        readData() // Reload the data every time AdminActivity resumes
    }
}