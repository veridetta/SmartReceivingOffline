package com.vr.smartreceivingnew.activity.admin

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vr.smartreceivingnew.R
import com.vr.smartreceivingnew.activity.LoginActivity
import com.vr.smartreceivingnew.adapter.BarangAdapter
import com.vr.smartreceivingnew.helper.DatabaseHelper
import com.vr.smartreceivingnew.helper.showSnack
import com.vr.smartreceivingnew.model.BarangModel
import com.vr.smartreceivingnew.model.ReportModel
import io.github.evanrupert.excelkt.workbook
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    private lateinit var btnExport: CardView
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
        btnExport = findViewById(R.id.btnExport)
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
        btnExport.setOnClickListener {
            export()
        }
    }

    private fun export() {
        // ambil data dari databaseHelper
        val reportList: MutableList<ReportModel> = mutableListOf()
        progressDialog.show()

        GlobalScope.launch(Dispatchers.IO) {
            try {
                val data = databaseHelper.getAllReports()
                withContext(Dispatchers.Main) {
                    reportList.clear()
                    reportList.addAll(data)

                    progressDialog.dismiss()

                    // Export data to Excel
                    exportToExcel(reportList)
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error reading data : $e")
                progressDialog.dismiss()
            }
        }
    }

    private fun exportToExcel(reportList: List<ReportModel>) {
        val dateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())
        val currentDate = Date()
        val formattedDate = dateFormat.format(currentDate)
        var filename = "report-"+formattedDate+".xlsx"
        var path = File(getExternalFilesDir(null),"DataFolder")
        if (!path.exists()) {
            path.mkdirs()
        }
        var file: File? = null
        try {
            file = File(
                path,
                filename
            ) // Ubah ekstensi file sesuai kebutuhan (misalnya, .txt, .csv)
            val writer = FileWriter(file)
            writer.append()
            writer.flush()
            writer.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        // Create a new workbook
        workbook {
            sheet("Reports") {
                row {
                    cell("UID")
                    cell("Doc ID")
                    cell("Nama Rak")
                    cell("Per Rak")
                    cell("Rak ID")
                    cell("Status")
                    cell("Item Nama")
                    cell("Nomor Penerimaan")
                    cell("Item UID")
                    cell("Item Merek")
                    cell("Item Jenis")
                    cell("Satuan")
                    cell("Petugas Nama")
                    cell("Petugas UID")
                    cell("Jumlah")
                    cell("Scan At")
                    cell("Edit At")
                    cell("Created At")
                }

                for (report in reportList) {
                    row {
                        report.uid?.let { cell(it) }
                        report.docId?.let { cell(it) }
                        report.nama?.let { cell(it) }
                        report.perRak?.let { cell(it) }
                        report.rakId?.let { cell(it) }
                        report.status?.let { cell(it) }
                        report.itemNama?.let { cell(it) }
                        report.nomorPenerimaan?.let { cell(it) }
                        report.itemUid?.let { cell(it) }
                        report.itemMerek?.let { cell(it) }
                        report.itemJenis?.let { cell(it) }
                        report.satuan?.let { cell(it) }
                        report.petugasNama?.let { cell(it) }
                        report.petugasUid?.let { cell(it) }
                        report.jumlah?.let { cell(it) }
                        report.scanAt?.let { cell(it) }
                        report.editAt?.let { cell(it) }
                        report.createdAt?.let { cell(it) }
                    }
                }
            }
        }
            .write(path.toString()+"/"+filename)
        // Show a success message or handle the file as needed
        Log.d(TAG, "Exported to: $path")
        shareFile(file)
    }
    private fun shareFile(file: File?) {
        if (file != null) {
            val fileUri = FileProvider.getUriForFile(
                this,
                this.applicationContext.packageName + ".provider",
                file
            )
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.setType("text/*")
            shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri)
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) // Penting untuk memberikan izin baca
            startActivity(Intent.createChooser(shareIntent, "Bagikan File via"))
        }
    }
    private fun saveDataToFile(data: String): File? {
        var file: File? = null
        try {
            val root =
                File(getExternalFilesDir(null), "DataFolder") // Ubah nama folder sesuai kebutuhan
            if (!root.exists()) {
                root.mkdirs()
            }
            file =
                File(root, "data.csv") // Ubah ekstensi file sesuai kebutuhan (misalnya, .txt, .csv)
            val writer = FileWriter(file)
            writer.append(data)
            writer.flush()
            writer.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return file
    }

    override fun onResume() {
        super.onResume()
        readData() // Reload the data every time AdminActivity resumes
    }
}