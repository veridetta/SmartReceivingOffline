package com.vr.smartreceivingnew.activity.user.scan

import android.app.ProgressDialog
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.budiyev.android.codescanner.AutoFocusMode
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.CodeScannerView
import com.budiyev.android.codescanner.DecodeCallback
import com.budiyev.android.codescanner.ErrorCallback
import com.budiyev.android.codescanner.ScanMode
import com.vr.smartreceivingnew.R
import com.vr.smartreceivingnew.activity.user.BeforeScanActivity
import com.vr.smartreceivingnew.helper.DatabaseHelper
import com.vr.smartreceivingnew.helper.showSnack
import com.vr.smartreceivingnew.model.BarangModel
import com.vr.smartreceivingnew.model.ReportDetailModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID
import kotlin.random.Random

class SingleScanActivity : AppCompatActivity() {
    var rackId = ""
    var namaRack = ""
    var itemNama = ""
    var rackDocId = ""
    var type = ""
    var kode1 = ""
    var total = 2
    var status = true
    var scanned = 0
    var maxItem = ""
    private lateinit var codeScanner: CodeScanner
    private lateinit var scannerView: CodeScannerView
    private lateinit var btnSelesai: LinearLayout
    private lateinit var progressDialog: ProgressDialog
    private lateinit var databaseHelper: DatabaseHelper

    private var mediaPlayer: MediaPlayer? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_single_scan)
        databaseHelper = DatabaseHelper(this)
        initView()
        initIntent()
        onClick()
        initCodeScanner()
    }

    private fun initView() {
        scannerView = findViewById(R.id.scanner_view)
        codeScanner = CodeScanner(this, scannerView)
        btnSelesai = findViewById(R.id.btnSelesai)
        progressDialog = ProgressDialog(this).apply {
            setMessage("Loading...")
            setCancelable(false)
        }
        // Inisialisasi MediaPlayer
        mediaPlayer = MediaPlayer.create(this, R.raw.sound)
    }

    private fun onClick() {
        btnSelesai.setOnClickListener {
            val intent = Intent(this@SingleScanActivity, BeforeScanActivity::class.java)
            intent.putExtra("rackId", rackId)
            intent.putExtra("namaRack", namaRack)
            intent.putExtra("aksi", "reload")
            intent.putExtra("rackDocId", rackDocId)
            intent.putExtra("type", type)
            intent.putExtra("isRack", "true")
            startActivity(intent)
            finish()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initCodeScanner() {
        codeScanner.apply {
            camera = CodeScanner.CAMERA_BACK
            formats = CodeScanner.ALL_FORMATS
            autoFocusMode = AutoFocusMode.SAFE
            scanMode = ScanMode.CONTINUOUS
            isAutoFocusEnabled = true
            isFlashEnabled = false

            decodeCallback = DecodeCallback {
                runOnUiThread {
                    handleScanResult(it.text)
                }
            }

            errorCallback = ErrorCallback {
                runOnUiThread {
                    Toast.makeText(
                        this@SingleScanActivity,
                        "Camera initialization error: ${it.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            scannerView.setOnClickListener {
                startPreview()
            }
        }
    }

    private fun initIntent() {
        rackId = intent.getStringExtra("rackId").toString()
        itemNama = intent.getStringExtra("itemNama").toString()
        rackDocId = intent.getStringExtra("rackDocId").toString()
        type = intent.getStringExtra("type").toString()
        namaRack = intent.getStringExtra("namaRack").toString()
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

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun handleScanResult(scanResult: String) {
        val jsonString = scanResult
        try {
            val jsonObject = JSONObject(jsonString)
            kode1 = jsonObject.getString("itemNum")
            val itemId = jsonObject.getString("id")
            if (status) {
                checkMaxItemsAllowed(itemId)
            } else {
                showSnack(this, "Masih mengirim data..")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun checkMaxItemsAllowed(itemId: String) {
        progressDialog.show()
        GlobalScope.launch(Dispatchers.IO) {
            val barangs = databaseHelper.getBarangByKode1(kode1)
            if (barangs.isNotEmpty()) {
                val barang = barangs.first()
                total = barang.perRak?.toIntOrNull() ?: 0
                Log.d("SingleScanActivity", "checkMaxItemsAllowed: $total")

                val currentScannedItemCount = databaseHelper.getScannedItemCountByRackId(rackId)
                withContext(Dispatchers.Main) {
                    progressDialog.dismiss()
                    if (currentScannedItemCount >= total) {
                        showSnack(this@SingleScanActivity, "Maksimal jumlah item tercapai")
                        codeScanner.startPreview()
                    } else {
                        // If maximum items not reached, proceed to read data
                        readData(itemId)
                    }
                }
            } else {
                withContext(Dispatchers.Main) {
                    progressDialog.dismiss()
                    showSnack(this@SingleScanActivity, "Data tidak ditemukan")
                    codeScanner.startPreview()
                }
            }
        }
    }

    private fun navigateBackToBeforeScanActivity() {
        val intent = Intent(this, BeforeScanActivity::class.java).apply {
            putExtra("rackId", rackId)
            putExtra("namaRack", namaRack)
            putExtra("aksi", "reload")
            putExtra("rackDocId", rackDocId)
            putExtra("type", type)
            putExtra("isRack", "true")
        }
        startActivity(intent)
        finish()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun readData(itemId: String) {
        progressDialog.show()
        GlobalScope.launch(Dispatchers.IO) {
            val barangs = databaseHelper.getBarangByKode1(kode1)
            var ada = barangs.isNotEmpty()
            var beda = false

            if (ada) {
                val barang = barangs.first()
                total = barang.perRak?.toIntOrNull() ?: 0 // Update total with perRak value
                beda = itemNama.isNotEmpty() && itemNama != barang.nama
            }

            val currentScannedItemCount = databaseHelper.getScannedItemCountByRackId(rackId)

            // Check if the item is a duplicate
            val isDuplicate = databaseHelper.isReportDetailDuplicate(itemId)

            withContext(Dispatchers.Main) {
                progressDialog.dismiss()
                if (currentScannedItemCount >= total) {
                    showSnack(this@SingleScanActivity, "Maksimal jumlah item tercapai")
                } else if (isDuplicate) {
                    showSnack(this@SingleScanActivity, "Item sudah di scan")
                } else if (beda) {
                    showSnack(this@SingleScanActivity, "Data tidak sesuai")
                } else if (ada) {
                    // Implement logic to save data to SQLite
                    saveDataToSQLite(itemId, barangs[0])
                } else {
                    showSnack(this@SingleScanActivity, "Data tidak ditemukan")
                }
                codeScanner.startPreview()
            }
        }
    }





    @RequiresApi(Build.VERSION_CODES.O)
    private fun saveDataToSQLite(itemId: String, barang: BarangModel) {
        val currentDateTime =
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"))
        val sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE)
        val petugasUid = sharedPreferences.getString("userUid", "")
        val petugasNama = sharedPreferences.getString("userName", "")
        val nomorPenerimaan = "np" + generateRandomString(8)

        val reportDetail = ReportDetailModel(
            docId = UUID.randomUUID().toString(),
            uid = barang.uid,
            nama = barang.nama,
            perRak = barang.perRak,
            rackId = rackId,
            itemId = itemId,
            itemNama = barang.nama,
            nomorPenerimaan = nomorPenerimaan,
            itemUid = barang.uid,
            itemMerek = barang.merek,
            itemNum = kode1,
            itemJenis = barang.jenis,
            satuan = barang.satuan,
            petugasNama = petugasNama,
            petugasUid = petugasUid,
            scanAt = currentDateTime,
            editAt = currentDateTime,
            createdAt = currentDateTime
        )

        val isSuccess = databaseHelper.addReportDetail(reportDetail)
        runOnUiThread {
            if (isSuccess) {
                showSnack(this@SingleScanActivity, "Berhasil menyimpan barang")
                if (mediaPlayer != null && !mediaPlayer!!.isPlaying) {
                    mediaPlayer?.start()
                }
                databaseHelper.updateReportWithRakId(
                    rackId,
                    barang.nama.toString(),
                    barang.perRak.toString(),
                    itemId,
                    barang.merek.toString(),
                    barang.jenis.toString(),
                    barang.satuan.toString()

                )
                scanned += 1
            } else {
                showSnack(this@SingleScanActivity, "Gagal menyimpan barang")
            }
            status = true
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun cekDuplicate(itemId: String) {
        progressDialog.show()
        GlobalScope.launch(Dispatchers.IO) {
            val isDuplicate = databaseHelper.isReportDetailDuplicate(itemId)
            withContext(Dispatchers.Main) {
                progressDialog.dismiss()
                if (isDuplicate) {
                    showSnack(this@SingleScanActivity, "Item sudah di scan")
                    status = true
                    codeScanner.startPreview()
                } else {
                    readData(itemId)
                }
            }
        }
    }

    // Fungsi untuk menghasilkan string acak dengan angka
    fun generateRandomString(jumlah: Int): String {
        val karakter = CharArray(jumlah) { ' ' }

        // Daftar karakter yang diizinkan untuk string acak (hanya angka)
        val karakterDiizinkan = "0123456789"

        for (i in karakter.indices) {
            karakter[i] = karakterDiizinkan[Random.nextInt(0, karakterDiizinkan.length)]
        }

        return String(karakter)
    }
}