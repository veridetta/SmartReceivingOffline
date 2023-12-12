package com.vr.smartreceivingnew.activity.user

import android.app.ProgressDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.budiyev.android.codescanner.AutoFocusMode
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.CodeScannerView
import com.budiyev.android.codescanner.DecodeCallback
import com.budiyev.android.codescanner.ErrorCallback
import com.budiyev.android.codescanner.ScanMode
import com.vr.smartreceivingnew.R
import com.vr.smartreceivingnew.helper.DatabaseHelper
import com.vr.smartreceivingnew.helper.showSnack
import com.vr.smartreceivingnew.model.ReportModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

class RakScanActivity : AppCompatActivity() {
    var rackId=""
    var type=""
    private lateinit var codeScanner: CodeScanner
    private lateinit var scannerView: CodeScannerView
    private lateinit var progressDialog : ProgressDialog
    private lateinit var databaseHelper: DatabaseHelper
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rak_scan)
        databaseHelper = DatabaseHelper(this)
        initView()
        initIntent()
        initCodeScanner()
    }
    private fun initView() {
        scannerView = findViewById(R.id.scanner_view)
        codeScanner = CodeScanner(this, scannerView)
        progressDialog = ProgressDialog(this).apply {
            setMessage("Loading...")
            setCancelable(false)
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun initCodeScanner(){
        codeScanner.camera = CodeScanner.CAMERA_BACK
        codeScanner.formats = CodeScanner.ALL_FORMATS // list of type BarcodeFormat,
        codeScanner.autoFocusMode = AutoFocusMode.SAFE // or CONTINUOUS
        codeScanner.scanMode = ScanMode.SINGLE // or CONTINUOUS or PREVIEW
        codeScanner.isAutoFocusEnabled = true // Whether to enable auto focus or not
        codeScanner.isFlashEnabled = false // Whether to enable flash or not
        codeScanner.decodeCallback = DecodeCallback {
            runOnUiThread {
                showSnack(this, "Scan result: ${it.text}")
                //val input = it.text
                //val startIndex = input.indexOf("rackId:") + "rackId:".length
                //val endIndex = input.indexOf(",", startIndex)
                //val rackIdValue = input.substring(startIndex, endIndex)
                //val rackId = rackIdValue.toInt()
                Log.d("Rak Code","Rak "+it.text)
                val jj = it.text
                val jsonString = "{$jj"
                try {
                    // Buat objek JSON dari string JSON
                    val jsonObject = JSONObject(jsonString)
                    // Dapatkan nilai "itemNum" dan "id" dari objek JSON
                    val rid = jsonObject.getString("rackId")
                    cekData(rid)
                    showSnack(this, "Scan result: ${rid}")
                } catch (e: Exception) {
                    // Tangani kesalahan jika parsing JSON gagal
                    e.printStackTrace()
                }
            }
        }
        codeScanner.errorCallback = ErrorCallback { // or ErrorCallback.SUPPRESS
            runOnUiThread {
                Toast.makeText(this, "Camera initialization error: ${it.message}",
                    Toast.LENGTH_LONG).show()
            }
        }
        scannerView.setOnClickListener {
            codeScanner.startPreview()
        }
    }
    private fun initIntent() {
        rackId = intent.getStringExtra("rackId").toString()
        type = intent.getStringExtra("type").toString()
    }

    override fun onResume() {
        super.onResume()
        codeScanner.startPreview()
    }

    override fun onPause() {
        codeScanner.releaseResources()
        super.onPause()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun cekData(rid: String) {
        GlobalScope.launch(Dispatchers.IO) {
            val reports = databaseHelper.getReportsByRakId(rid)
            withContext(Dispatchers.Main) {
                if (reports.isNotEmpty()) {
                    val report = reports.first()
                    showSnack("Rak sudah terpakai")
                    navigateToBeforeScanActivity(report, rid)
                } else {
                    createNewReport(rid)
                }
            }
        }
    }

    private fun navigateToBeforeScanActivity(report: ReportModel, rid: String) {
        val intent = Intent(this, BeforeScanActivity::class.java).apply {
            putExtra("rackId", rid)
            putExtra("rackDocId", report.docId)
            putExtra("namaRack", report.nama)
            putExtra("isRack", "true")
            putExtra("itemNama", report.itemNama)
            putExtra("type", type)
        }
        Log.d("DatabaseHelper", "doc Id in RakScanActivity to beforescan: $rid")
        startActivity(intent)
        finish()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNewReport(rid: String) {
        val createdAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"))
        val nama = "ra" + generateRandomString(7)
        val sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE)
        val petugasUid = sharedPreferences.getString("userUid", "")
        val petugasNama = sharedPreferences.getString("userName", "")
        val nomorPenerimaan = "np" + generateRandomString(8)

        val newReport = ReportModel(
            uid = UUID.randomUUID().toString(),
            nama = nama,
            perRak = "",
            itemJenis = "",
            itemMerek = "",
            rakId = rid,
            satuan = "",
            status = "proses",
            itemNama = "",
            itemUid = "",
            petugasUid = petugasUid,
            petugasNama = petugasNama,
            nomorPenerimaan = nomorPenerimaan,
            jumlah = "1",
            createdAt = createdAt
        )

        val isSuccess = databaseHelper.addReport(newReport)
        if (isSuccess) {
            navigateToBeforeScanActivity(newReport, rid)
        } else {
            showSnack("Gagal menyimpan rak")
        }
    }

    private fun showSnack(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    fun generateRandomString(jumlah: Int): String {
        val allowedChars = "0123456789"
        return (1..jumlah)
            .map { allowedChars.random() }
            .joinToString("")
    }
}