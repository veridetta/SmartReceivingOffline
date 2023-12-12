package com.vr.smartreceivingnew.activity.admin

import android.app.AlertDialog
import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.vr.smartreceivingnew.R
import com.vr.smartreceivingnew.adapter.ReportAdapter
import com.vr.smartreceivingnew.helper.DatabaseHelper
import com.vr.smartreceivingnew.helper.showSnack
import com.vr.smartreceivingnew.model.ReportModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ReportActivity : AppCompatActivity() {
    private lateinit var reportAdapter: ReportAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var contentView: RelativeLayout
    private lateinit var btnBack: ImageView
    private lateinit var btnHapusSemuaLaporan: MaterialButton

    private lateinit var progressDialog: ProgressDialog
    val TAG = "LOAD DATA"
    private val reportList: MutableList<ReportModel> = mutableListOf()
    private lateinit var databaseHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report)
        databaseHelper = DatabaseHelper(this)

        initView()
        initRc()
        initData()
        initClick()
    }
    private fun initView(){
        recyclerView = findViewById(R.id.rcReport)
        contentView = findViewById(R.id.contentView)
        btnBack = findViewById(R.id.btnBack)
        btnHapusSemuaLaporan = findViewById(R.id.btnHapusSemuaLaporan)
        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Loading...")
        progressDialog.setCancelable(false)

    }
    private fun initRc(){
        recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(this@ReportActivity, 1)
            // set the custom adapter to the RecyclerView
            reportAdapter = ReportAdapter(
                reportList,
                this@ReportActivity
            ){ report -> hapusBarang(report) }
        }
    }
    private fun initData(){
        readData()
        recyclerView.adapter = reportAdapter
        reportAdapter.filter("")
    }
    private fun readData() {
        progressDialog.show()
        GlobalScope.launch(Dispatchers.IO) {
            val reports = databaseHelper.getAllReports()
            withContext(Dispatchers.Main) {
                reportAdapter.updateList(reports)
                progressDialog.dismiss()
            }
        }
    }

    private fun initClick(){
        btnBack.setOnClickListener {
            finish()
        }
        btnHapusSemuaLaporan.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Konfirmasi")
                .setMessage("Apakah Anda yakin ingin menghapus semua laporan dan detailnya?")
                .setPositiveButton("Ya") { _, _ -> deleteAllReportsAndDetails() }
                .setNegativeButton("Tidak", null)
                .show()
        }
    }

    private fun deleteAllReportsAndDetails() {
        progressDialog.show()
        GlobalScope.launch(Dispatchers.IO) {
            val isReportsDeleted = databaseHelper.deleteAllReports()
            val isDetailsDeleted = databaseHelper.deleteAllReportDetails()

            withContext(Dispatchers.Main) {
                progressDialog.dismiss()
                if (isReportsDeleted && isDetailsDeleted) {
                    showSnack(this@ReportActivity, "Semua laporan dan detailnya berhasil dihapus")
                    reportList.clear()
                    reportAdapter.updateList(reportList)
                } else {
                    showSnack(this@ReportActivity, "Gagal menghapus laporan dan detailnya")
                }
            }
        }
    }

    private fun hapusBarang(report: ReportModel) {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Yakin ingin menghapus ${report.nama}?")
        builder.setPositiveButton("Ya") { _, _ ->
            progressDialog.show()
            GlobalScope.launch(Dispatchers.IO) {
                Log.d(TAG, "Target Delete ${report.rakId}")
                val isReportDeleted = databaseHelper.deleteReport(report.rakId.toString())
                val isDetailsDeleted = databaseHelper.deleteReportDetails(report.rakId.toString())
                withContext(Dispatchers.Main) {
                    progressDialog.dismiss()
                    if (isReportDeleted && isDetailsDeleted) {
                        Log.d(TAG, "Report and details successfully deleted from database.")
                        showSnack(this@ReportActivity, "Berhasil menghapus laporan dan detailnya")
                        readData() // Reload data
                    } else {
                        Log.d(TAG, "Failed to delete report and/or details from database.")
                        showSnack(this@ReportActivity, "Gagal menghapus laporan dan detailnya")
                    }
                }
            }
        }
        builder.create().show()
    }


}