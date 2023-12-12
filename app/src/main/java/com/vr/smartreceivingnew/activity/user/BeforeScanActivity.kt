package com.vr.smartreceivingnew.activity.user

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vr.smartreceivingnew.R
import com.vr.smartreceivingnew.activity.user.scan.SingleScanActivity
import com.vr.smartreceivingnew.adapter.ScanAdapter
import com.vr.smartreceivingnew.helper.DatabaseHelper
import com.vr.smartreceivingnew.helper.showSnack
import com.vr.smartreceivingnew.model.ReportDetailModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BeforeScanActivity : AppCompatActivity() {
    private lateinit var btnScanRack: CardView
    private lateinit var btnScanQr: CardView
    private lateinit var tvRack: TextView
    private lateinit var tvItemNama: TextView
    private lateinit var tvItemJumlah: TextView
    private lateinit var btnTerima: CardView
    private lateinit var itemAdapter: ScanAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressDialog: ProgressDialog
    private val itemList: MutableList<ReportDetailModel> = mutableListOf()
    private lateinit var databaseHelper: DatabaseHelper
    private var type = ""
    private var lengkap = false
    private var isRack = "false"
    private var namaRack = ""
    private var scannedItem = ""
    private var maxItem = ""
    private var itemNama = ""
    private var rackId = ""
    private var rackDocId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_before_scan)

        databaseHelper = DatabaseHelper(this) // Initialize DatabaseHelper
        initView()
        initIntent()
        setIntent()
        initListener()
        initRc()
        initData()
    }

    private fun initView() {
        recyclerView = findViewById(R.id.rcItem)
        btnScanRack = findViewById(R.id.btnScanRack)
        btnScanQr = findViewById(R.id.btnScanQr)
        tvRack = findViewById(R.id.tvRack)
        tvItemNama = findViewById(R.id.tvItemNama)
        tvItemJumlah = findViewById(R.id.tvItemJumlah)
        btnTerima = findViewById(R.id.btnTerima)
        progressDialog = ProgressDialog(this)
    }

    private fun initListener() {
        btnScanRack.setOnClickListener {
            if (isRack == "true") {
                showSnack(this, "Rack sudah di scan")
            } else {
                val intent = Intent(this, RakScanActivity::class.java).apply {
                    putExtra("type", type)
                    putExtra("namaRack", namaRack)
                }
                startActivity(intent)
                finish()
            }
        }

        btnScanQr.setOnClickListener {
            when {
                lengkap -> showSnack(this, "Semua barang sudah di scan")
                isRack == "false" -> showSnack(this, "Scan rack terlebih dahulu")
                else -> startScanActivity()
            }
        }

        btnTerima.setOnClickListener {
            if (lengkap) {
                progressDialog.setMessage("Loading...")
                terima()
            } else {
                showSnack(this, "Data belum lengkap")
            }
        }
    }

    private fun startScanActivity() {
        val activityClass =  SingleScanActivity::class.java
        val intent = Intent(this, activityClass).apply {
            putExtra("rackId", rackId)
            putExtra("itemNama", itemNama)
            putExtra("maxItem", maxItem)
            putExtra("namaRack", namaRack)
            putExtra("type", type)
            putExtra("rackDocId", rackDocId)
        }
        startActivity(intent)
        finish()
    }

    private fun initIntent() {
        type = intent.getStringExtra("type").toString()
        isRack = intent.getStringExtra("isRack") ?: "false"
        lengkap = intent.getBooleanExtra("lengkap", false)
        namaRack = intent.getStringExtra("namaRack").toString()
        rackId = intent.getStringExtra("rackId").toString()
        itemNama = intent.getStringExtra("itemNama").toString()
        rackDocId = intent.getStringExtra("rackDocId").toString()

        Log.d("DatabaseHelper", "doc Id in initIntent: $rackId")

    }

    private fun setIntent() {
        if (isRack == "true") {
            tvRack.text = "Rack: $rackId"
        }
    }

    private fun initRc() {
        recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(this@BeforeScanActivity, 1)
            itemAdapter = ScanAdapter(itemList, this@BeforeScanActivity) { scan -> hapusBarang(scan) }
            adapter = itemAdapter
        }
    }

    private fun hapusBarang(scan: ReportDetailModel) {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Yakin ingin menghapus ${scan.nama}?")
        builder.setPositiveButton("Ya") { _, _ ->
            progressDialog.show()
            val isSuccess = databaseHelper.deleteReportDetail(scan.docId!!)
            if (isSuccess) {
                showSnack(this, "Berhasil menghapus item")
                refreshActivity()
            } else {
                showSnack(this, "Gagal menghapus item")
            }
            progressDialog.dismiss()
        }
        builder.create().show()
    }

    private fun refreshActivity() {
        val intent = Intent(this, BeforeScanActivity::class.java).apply {
            putExtra("rackId", rackId)
            putExtra("namaRack", namaRack)
            putExtra("aksi", "reload")
            putExtra("type", type)
        }
        startActivity(intent)
        finish()
    }

    private fun initData() {
        readData()
        recyclerView.adapter = itemAdapter
        itemAdapter.filter("")
    }

    private fun readData() {
        progressDialog.show()
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val reports = databaseHelper.getReportDetailsByRakId(rackId)
                updateUiWithData(reports)
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showSnack(this@BeforeScanActivity, "Error loading data")
                    progressDialog.dismiss()
                }
            }
        }
    }

    private suspend fun updateUiWithData(reports: List<ReportDetailModel>) {
        withContext(Dispatchers.Main) {
            if (reports.isNotEmpty()) {
                val firstReport = reports[0]
                maxItem = firstReport.perRak!!
                itemNama = firstReport.itemNama!!
                updateItemDisplay(firstReport, reports.size)
            }
            itemList.addAll(reports)
            itemAdapter.notifyDataSetChanged()
            progressDialog.dismiss()
        }
    }

    private fun updateItemDisplay(report: ReportDetailModel, count: Int) {
        val itemz = StringBuilder().apply {
            append("(").append(report.itemMerek).append(") ").append(report.itemNama)
            if (report.itemJenis!!.isNotEmpty()) {
                append(" - ").append(report.itemJenis)
            }
        }
        tvItemNama.text = "Item: $itemz"
        scannedItem = count.toString()
        tvItemJumlah.text = "Jumlah: $count/$maxItem ${report.satuan}"
        if (count == maxItem.toInt()) {
            lengkap = true
        }
    }

    private fun terima() {
        Log.d("DatabaseHelper", "doc Id in terima: $rackId")
        progressDialog.show()
        GlobalScope.launch(Dispatchers.IO) {
            val isSuccess = databaseHelper.updateReportStatus(rackId, "lengkap")

            withContext(Dispatchers.Main) {
                progressDialog.dismiss()
                if (isSuccess) {
                    showSnack(this@BeforeScanActivity, "Berhasil menyimpan barang")
                    navigateToReportActivity()
                } else {
                    showSnack(this@BeforeScanActivity, "Gagal menyimpan barang")
                }
            }
        }
    }

    private fun navigateToReportActivity() {
        val intent = Intent(this, ReportActivityUser::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(intent)
        finish()
    }

}