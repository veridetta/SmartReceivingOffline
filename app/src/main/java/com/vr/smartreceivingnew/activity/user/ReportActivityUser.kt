package com.vr.smartreceivingnew.activity.user

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vr.smartreceivingnew.R
import com.vr.smartreceivingnew.adapter.ReportAdapterUser
import com.vr.smartreceivingnew.helper.DatabaseHelper
import com.vr.smartreceivingnew.model.ReportModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ReportActivityUser : AppCompatActivity() {
    private lateinit var reportAdapter: ReportAdapterUser
    private lateinit var recyclerView: RecyclerView
    private lateinit var contentView: RelativeLayout
    private lateinit var btnBack: ImageView
    private lateinit var progressDialog: ProgressDialog
    private lateinit var databaseHelper: DatabaseHelper
    private var reportList: MutableList<ReportModel> = mutableListOf()

    companion object {
        private const val TAG = "ReportActivityUser"
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report_user)
        databaseHelper = DatabaseHelper(this)
        initView()
        initRc()
        initData()
        initClick()
    }

    private fun initView() {
        recyclerView = findViewById(R.id.rcReport)
        contentView = findViewById(R.id.contentView)
        btnBack = findViewById(R.id.btnBack)
        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Loading...")
        progressDialog.setCancelable(false)
    }

    private fun initRc() {
        recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(this@ReportActivityUser, 1)
            reportAdapter = ReportAdapterUser(reportList, this@ReportActivityUser)
            adapter = reportAdapter
        }
    }

    private fun initData() {
        progressDialog.show()
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val reports = databaseHelper.getCompletedReports()
                Log.d("ReportActivityUser", "Number of completed reports: ${reports.size}")
                withContext(Dispatchers.Main) {
                    reportAdapter.updateList(reports)
                    progressDialog.dismiss()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("ReportActivityUser", "Error loading data: $e")
                    progressDialog.dismiss()
                }
            }
        }
    }


    private fun initClick() {
        btnBack.setOnClickListener {
            finish()
        }
    }
}
