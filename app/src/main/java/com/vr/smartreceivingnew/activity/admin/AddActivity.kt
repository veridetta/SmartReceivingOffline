package com.vr.smartreceivingnew.activity.admin

import android.app.ProgressDialog
import android.os.Build
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.vr.smartreceivingnew.R
import com.vr.smartreceivingnew.databinding.ActivityAddBinding
import com.vr.smartreceivingnew.helper.DatabaseHelper
import com.vr.smartreceivingnew.helper.showSnack
import com.vr.smartreceivingnew.model.BarangModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

class AddActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddBinding

    private lateinit var type: String //edit atau add
    private lateinit var uid : String
    private lateinit var docId : String
    private lateinit var kode1 : String
    private lateinit var perRak : String
    private lateinit var jenis : String
    private lateinit var merek : String
    private lateinit var nama : String
    private lateinit var satuan : String
    private lateinit var kode2 : String
    private lateinit var banyakQr : String
    private lateinit var scanAt : String
    private lateinit var editAt : String
    private lateinit var createdAt : String

    lateinit var progressDialog: ProgressDialog

    private lateinit var databaseHelper: DatabaseHelper


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Loading...")
        progressDialog.setCancelable(false)

        initSpinner()
        initIntent()
        setIntent()
        initClick()

        databaseHelper = DatabaseHelper(this)

    }

    private fun initSpinner() {
        val arJenis = resources.getStringArray(R.array.jenis)
        val arMerek = resources.getStringArray(R.array.merek)
        val adapterJenis = ArrayAdapter(this, android.R.layout.simple_spinner_item, arJenis)
        val adapterMerek = ArrayAdapter(this, android.R.layout.simple_spinner_item, arMerek)

        binding.spJenis.adapter = adapterJenis
        binding.spMerek.adapter = adapterMerek
    }
    private fun initIntent(){
        type = intent.getStringExtra("type").toString()
        uid = intent.getStringExtra("uid").toString()
        docId = intent.getStringExtra("docId").toString()
        kode1 = intent.getStringExtra("kode1").toString()
        perRak = intent.getStringExtra("perRak").toString()
        jenis = intent.getStringExtra("jenis").toString()
        merek = intent.getStringExtra("merek").toString()
        nama = intent.getStringExtra("nama").toString()
        satuan = intent.getStringExtra("satuan").toString()
        kode2 = intent.getStringExtra("kode2").toString()
        banyakQr = intent.getStringExtra("banyakQr").toString()
        scanAt = intent.getStringExtra("scanAt").toString()
        editAt = intent.getStringExtra("editAt").toString()
        createdAt = intent.getStringExtra("createdAt").toString()
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun setIntent(){
        if(type == "edit"){
            binding.etNama.setText(nama)
            binding.etSatuan.setText(satuan)
            binding.etPerRak.setText(perRak)
            binding.etQr1.setText(kode1)
            binding.etQr2.setText(kode2)
            binding.tvJudul.text = "Edit Barang"
            binding.btnSimpan.text = "Simpan"
            // Set spinner berdasarkan nilai yang telah Anda terima
            val jenisPosition = (binding.spJenis.adapter as ArrayAdapter<String>).getPosition(jenis)
            binding.spJenis.setSelection(jenisPosition)

            val merekPosition = (binding.spMerek.adapter as ArrayAdapter<String>).getPosition(merek)
            binding.spMerek.setSelection(merekPosition)
            //tanggal sekarang dan jam
            val currentDateTime = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")
            val formatted = currentDateTime.format(formatter)
            editAt = formatted
        }else{
            binding.tvJudul.text = "Tambah Barang"
            binding.btnSimpan.text = "Tambah"
            //tanggal sekarang dan jam
            val currentDateTime = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")
            val formatted = currentDateTime.format(formatter)
            createdAt = formatted
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun chekData(){
        if(binding.etNama.text.toString().isEmpty()){
            binding.etNama.error = "Nama barang tidak boleh kosong"
            binding.etNama.requestFocus()
            return
        }
        if(binding.etSatuan.text.toString().isEmpty()){
            binding.etSatuan.error = "Satuan barang tidak boleh kosong"
            binding.etSatuan.requestFocus()
            return
        }
        if(binding.etPerRak.text.toString().isEmpty()){
            binding.etPerRak.error = "Per rak tidak boleh kosong"
            binding.etPerRak.requestFocus()
            return
        }
        if(binding.etQr1.text.toString().isEmpty()) {
            binding.etQr1.error = "Kode 1 tidak boleh kosong"
            binding.etQr1.requestFocus()
            return
        }
        //tambah atau edit data
        if(type == "edit"){
            editData()
        }else{
            tambahData()
        }

    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun initClick(){
        binding.btnBack.setOnClickListener {
            finish()
        }
        binding.btnSimpan.setOnClickListener {
            chekData()
        }
    }
    private fun tambahData() {
        // Check if kode2 is empty and set banyakQr accordingly
        banyakQr = if(binding.etQr2.text.toString().isEmpty()) {
            "1"
        } else {
            "2"
        }

        progressDialog.show()

        // Create a new instance of BarangModel with the data from the UI
        val newBarang = BarangModel(
            uid = UUID.randomUUID().toString(),
            docId = UUID.randomUUID().toString(), // Generate a new unique ID for docId
            kode1 = binding.etQr1.text.toString(),
            perRak = binding.etPerRak.text.toString(),
            jenis = binding.spJenis.selectedItem.toString(),
            merek = binding.spMerek.selectedItem.toString(),
            nama = binding.etNama.text.toString(),
            satuan = binding.etSatuan.text.toString(),
            kode2 = binding.etQr2.text.toString(),
            banyakQr = banyakQr,
            scanAt = scanAt,
            editAt = editAt,
            createdAt = createdAt
        )

        // Use DatabaseHelper to add the new barang to the database
        val isSuccess = databaseHelper.addBarang(newBarang)
        if (isSuccess) {
            showSnack(this, "Berhasil menyimpan barang")
            // Optionally, redirect to another activity or update the UI accordingly
        } else {
            showSnack(this, "Gagal menyimpan barang")
        }
        progressDialog.dismiss()
        finish()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun editData() {
        // Determine the value for 'banyakQr' based on the contents of 'binding.etQr2'
        banyakQr = if (binding.etQr2.text.toString().isEmpty()) {
            "1"
        } else {
            "2"
        }

        progressDialog.show()

        // Create an updated instance of BarangModel with the data from the UI
        val updatedBarang = BarangModel(
            uid = this.uid, // Preserve the existing UID
            docId = this.docId, // Use the existing document ID
            kode1 = binding.etQr1.text.toString(),
            perRak = binding.etPerRak.text.toString(),
            jenis = binding.spJenis.selectedItem.toString(),
            merek = binding.spMerek.selectedItem.toString(),
            nama = binding.etNama.text.toString(),
            satuan = binding.etSatuan.text.toString(),
            kode2 = binding.etQr2.text.toString(),
            banyakQr = this.banyakQr,
            scanAt = this.scanAt, // Preserve the existing 'scanAt', if necessary
            editAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")),
            createdAt = this.createdAt // Preserve the existing 'createdAt'
        )

        // Use DatabaseHelper to update the barang in the database
        val isSuccess = databaseHelper.updateBarang(updatedBarang)
        if (isSuccess) {
            showSnack(this, "Berhasil memperbarui barang")
            // Optionally, redirect to another activity or update the UI accordingly
        } else {
            showSnack(this, "Gagal memperbarui barang")
        }

        progressDialog.dismiss()
        finish()
    }
}