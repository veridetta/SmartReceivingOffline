package com.vr.smartreceivingnew.activity.user.scan

import android.app.ProgressDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
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
import com.vr.smartreceivingnew.model.BarangModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FirstScanActivity : AppCompatActivity() {
    var rackId=""
    var itemNama=""
    var namaRack=""
    var type=""
    var rackDocId=""
    var maxItem = ""
    private lateinit var codeScanner: CodeScanner
    private lateinit var scannerView: CodeScannerView
    private lateinit var progressDialog : ProgressDialog
    private lateinit var databaseHelper: DatabaseHelper

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_first_scan)
        databaseHelper = DatabaseHelper(this)
        Toast.makeText(this@FirstScanActivity, "Calling FirstScan", Toast.LENGTH_SHORT).show()
        initView()
        initIntent()
        initCodeScanner()
    }
    private fun initView(){
        scannerView = findViewById<CodeScannerView>(R.id.scanner_view)
        codeScanner = CodeScanner(this, scannerView)
        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Loading...")
        progressDialog.setCancelable(false)
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
                readData(it.text)
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
        itemNama = intent.getStringExtra("itemNama").toString()
        type = intent.getStringExtra("type").toString()
        namaRack = intent.getStringExtra("namaRack").toString()
        rackDocId = intent.getStringExtra("rackDocId").toString()
        maxItem = intent.getStringExtra("maxItem").toString()
    }
    override fun onResume() {
        super.onResume()
        codeScanner.startPreview()
    }

    override fun onPause() {
        codeScanner.releaseResources()
        super.onPause()
    }

    private fun readData(qrCode: String) {
        progressDialog.show()
        GlobalScope.launch(Dispatchers.IO) {
            val barangs = databaseHelper.getBarangByKode1(qrCode)
            val currentScannedItemCount = databaseHelper.getScannedItemCountByRackId(rackId)
            val maxItemsAllowed = maxItem.toIntOrNull() ?: 0

            var ada = barangs.isNotEmpty()
            var beda = false

            barangs.forEach { barang ->
                if (itemNama.isNotEmpty() && itemNama != barang.nama) {
                    beda = true
                }
            }

            withContext(Dispatchers.Main) {
                progressDialog.dismiss()
                if (currentScannedItemCount >= maxItemsAllowed) {
                    showSnack("Maksimal jumlah item tercapai")
                    codeScanner.startPreview()
                } else if (!ada) {
                    showSnack("Data tidak ditemukan")
                    codeScanner.startPreview()
                } else {
                    if (beda) {
                        showSnack("Data tidak sesuai")
                        codeScanner.startPreview()
                    } else {
                        val barang = barangs.first()
                        navigateToSecondScan(barang)
                    }
                }
            }
        }
    }

    private fun navigateToSecondScan(barang: BarangModel) {
        val intent = Intent(this, SecondScanActivity::class.java).apply {
            putExtra("rackId", rackId)
            putExtra("itemNama", itemNama)
            putExtra("namaRack", namaRack)
            putExtra("rackDocId", rackDocId)
            putExtra("kode1", barang.kode1)
            putExtra("kode2", barang.kode2)
            putExtra("type", type)
        }
        startActivity(intent)
        finish()
    }

    private fun showSnack(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}