package com.vr.smartreceivingnew.helper

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.vr.smartreceivingnew.model.BarangModel
import com.vr.smartreceivingnew.model.ReportDetailModel
import com.vr.smartreceivingnew.model.ReportModel
import com.vr.smartreceivingnew.model.UserModel
import java.util.UUID

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase?) {
        // SQL statement to create a table
        val createTableStatement = """
            CREATE TABLE $TABLE_USERS (
                docId TEXT PRIMARY KEY,
                nama TEXT,
                username TEXT,
                password TEXT,
                noHp TEXT,
                role TEXT,
                createdAt TEXT
            )
        """.trimIndent()
        db?.execSQL(createTableStatement)

        val createBarangTableStatement = """
            CREATE TABLE $TABLE_BARANG (
                docId TEXT PRIMARY KEY,
                uid TEXT,
                kode1 TEXT,
                perRak TEXT,
                nama TEXT,
                satuan TEXT,
                jenis TEXT,
                merek TEXT,
                kode2 TEXT,
                banyakQr TEXT,
                scanAt TEXT,
                editAt TEXT,
                createdAt TEXT
            )
        """.trimIndent()
        db?.execSQL(createBarangTableStatement)

        val createReportTableStatement = """
        CREATE TABLE $TABLE_REPORT (
            docId TEXT PRIMARY KEY,
            uid TEXT,
            nama TEXT,
            perRak TEXT,
            rakId TEXT,
            status TEXT,
            itemNama TEXT,
            nomorPenerimaan TEXT,
            itemUid TEXT,
            itemMerek TEXT,
            itemJenis TEXT,
            satuan TEXT,
            petugasNama TEXT,
            petugasUid TEXT,
            jumlah TEXT,
            scanAt TEXT,
            editAt TEXT,
            createdAt TEXT
        )
    """.trimIndent()
        db?.execSQL(createReportTableStatement)

        val createReportDetailTableStatement = """
        CREATE TABLE $TABLE_REPORT_DETAIL (
            docId TEXT PRIMARY KEY,
            uid TEXT,
            nama TEXT,
            perRak TEXT,
            rakId TEXT,
            itemId TEXT,
            itemNama TEXT,
            nomorPenerimaan TEXT,
            itemUid TEXT,
            itemMerek TEXT,
            itemNum TEXT,
            itemJenis TEXT,
            satuan TEXT,
            petugasNama TEXT,
            petugasUid TEXT,
            scanAt TEXT,
            editAt TEXT,
            createdAt TEXT
        )
    """.trimIndent()
        db?.execSQL(createReportDetailTableStatement)


        createDefaultUsers(db)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        // This method is called when the database needs to be upgraded
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        onCreate(db)
    }

    private fun createDefaultUsers(db: SQLiteDatabase?) {
        // Default user and admin details
        val defaultUser = UserModel("1", "1", "Default User", "user", "password", "0000000000", "user", "createdAt")
        val defaultAdmin = UserModel("2", "2", "Default Admin", "admin", "password", "0000000000", "admin", "createdAt")

        // Insert default user and admin into the database
        addUser(defaultUser, db)
        addUser(defaultAdmin, db)
    }

    //user
    fun addUser(user: UserModel, db: SQLiteDatabase? = this.writableDatabase): Boolean {
        val values = ContentValues()

        // Mapping user details to ContentValues
        values.put(COL_DOC_ID, user.docId)
        values.put(COL_NAME, user.nama)
        values.put(COL_USERNAME, user.username)
        values.put(COL_PASSWORD, user.password)
        values.put(COL_PHONE, user.noHp)
        values.put(COL_ROLE, user.role)
        values.put(COL_CREATED_AT, user.createdAt)

        val result = db?.insert(TABLE_USERS, null, values) ?: -1L
        return result != -1L
    }

    fun getUser(username: String, password: String): UserModel? {
        val db = this.readableDatabase
        var user: UserModel? = null

        // SQL query to select the user
        val query = "SELECT * FROM $TABLE_USERS WHERE username = ? AND password = ?"
        val cursor = db.rawQuery(query, arrayOf(username, password))

        // Checking if a user is found
        if (cursor.moveToFirst()) {
            user = UserModel(
                docId = cursor.getString(0),
                nama = cursor.getString(1),
                username = cursor.getString(2),
                password = cursor.getString(3),
                noHp = cursor.getString(4),
                role = cursor.getString(5),
                createdAt = cursor.getString(6)
            )
        }
        cursor.close()
        return user
    }

    fun getAllUsers(): List<UserModel> {
        val db = this.readableDatabase
        val userList = mutableListOf<UserModel>()

        // SQL query to select all users
        val query = "SELECT * FROM $TABLE_USERS"
        val cursor = db.rawQuery(query, null)

        // Iterating over the cursor to create user objects and add them to the list
        if (cursor.moveToFirst()) {
            do {
                val user = UserModel(
                    docId = cursor.getString(cursor.getColumnIndexOrThrow(COL_DOC_ID)),
                    nama = cursor.getString(cursor.getColumnIndexOrThrow(COL_NAME)),
                    username = cursor.getString(cursor.getColumnIndexOrThrow(COL_USERNAME)),
                    password = cursor.getString(cursor.getColumnIndexOrThrow(COL_PASSWORD)),
                    noHp = cursor.getString(cursor.getColumnIndexOrThrow(COL_PHONE)),
                    role = cursor.getString(cursor.getColumnIndexOrThrow(COL_ROLE)),
                    createdAt = cursor.getString(cursor.getColumnIndexOrThrow(COL_CREATED_AT))
                )
                userList.add(user)
            } while (cursor.moveToNext())
        }

        cursor.close()
        return userList
    }

    fun updateUser(user: UserModel): Boolean {
        val db = this.writableDatabase
        val values = ContentValues()

        // Assign values for each column from the UserModel object
        values.put(COL_NAME, user.nama)
        values.put(COL_USERNAME, user.username)
        values.put(COL_PASSWORD, user.password)
        values.put(COL_PHONE, user.noHp)
        values.put(COL_ROLE, user.role)
        values.put(COL_CREATED_AT, user.createdAt)

        // Update the record in the database where the docId matches
        val result = db.update(TABLE_USERS, values, "docId = ?", arrayOf(user.docId.toString()))

        // If result is greater than 0, it means the update was successful
        return result > 0
    }

    fun deleteUser(docId: String): Boolean {
        val db = this.writableDatabase
        val result = db.delete(TABLE_USERS, "docId = ?", arrayOf(docId))
        return result > 0
    }

    //barang
    fun addBarang(barang: BarangModel): Boolean {
        val db = this.writableDatabase
        val values = ContentValues()

        values.put(COL_DOC_ID, barang.docId)
        values.put(COL_UID, barang.uid)
        values.put(COL_KODE1, barang.kode1)
        values.put(COL_PER_RAK, barang.perRak)
        values.put(COL_NAME, barang.nama)
        values.put(COL_SATUAN, barang.satuan)
        values.put(COL_JENIS, barang.jenis)
        values.put(COL_MEREK, barang.merek)
        values.put(COL_KODE2, barang.kode2)
        values.put(COL_BANYAK_QR, barang.banyakQr)
        values.put(COL_SCAN_AT, barang.scanAt)
        values.put(COL_EDIT_AT, barang.editAt)
        values.put(COL_CREATED_AT, barang.createdAt)

        val result = db.insert(TABLE_BARANG, null, values)
        return result != -1L
    }

    fun getAllBarang(): List<BarangModel> {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_BARANG", null)
        val barangs = mutableListOf<BarangModel>()

        if (cursor.moveToFirst()) {
            do {
                val barang = BarangModel(
                    uid = cursor.getString(cursor.getColumnIndexOrThrow(COL_UID)),
                    docId = cursor.getString(cursor.getColumnIndexOrThrow(COL_DOC_ID)),
                    kode1 = cursor.getString(cursor.getColumnIndexOrThrow(COL_KODE1)),
                    perRak = cursor.getString(cursor.getColumnIndexOrThrow(COL_PER_RAK)),
                    nama = cursor.getString(cursor.getColumnIndexOrThrow(COL_NAME)),
                    satuan = cursor.getString(cursor.getColumnIndexOrThrow(COL_SATUAN)),
                    jenis = cursor.getString(cursor.getColumnIndexOrThrow(COL_JENIS)),
                    merek = cursor.getString(cursor.getColumnIndexOrThrow(COL_MEREK)),
                    kode2 = cursor.getString(cursor.getColumnIndexOrThrow(COL_KODE2)),
                    banyakQr = cursor.getString(cursor.getColumnIndexOrThrow(COL_BANYAK_QR)),
                    scanAt = cursor.getString(cursor.getColumnIndexOrThrow(COL_SCAN_AT)),
                    editAt = cursor.getString(cursor.getColumnIndexOrThrow(COL_EDIT_AT)),
                    createdAt = cursor.getString(cursor.getColumnIndexOrThrow(COL_CREATED_AT))
                )
                barangs.add(barang)
            } while (cursor.moveToNext())
        }

        cursor.close()
        return barangs
    }


    fun updateBarang(barang: BarangModel): Boolean {
        val db = this.writableDatabase
        val values = ContentValues()

        // Assign values for each column from the BarangModel object
        values.put(COL_UID, barang.uid)
        values.put(COL_KODE1, barang.kode1)
        values.put(COL_PER_RAK, barang.perRak)
        values.put(COL_NAME, barang.nama)
        values.put(COL_SATUAN, barang.satuan)
        values.put(COL_JENIS, barang.jenis)
        values.put(COL_MEREK, barang.merek)
        values.put(COL_KODE2, barang.kode2)
        values.put(COL_BANYAK_QR, barang.banyakQr)
        values.put(COL_SCAN_AT, barang.scanAt)
        values.put(COL_EDIT_AT, barang.editAt)
        values.put(COL_CREATED_AT, barang.createdAt)

        // Update the record in the database where the docId matches
        val result = db.update(TABLE_BARANG, values, "docId = ?", arrayOf(barang.docId.toString()))

        // If result is greater than 0, it means the update was successful
        return result > 0
    }

    fun getBarangByKode1(kode1: String): List<BarangModel> {
        val db = this.readableDatabase
        val barangList = mutableListOf<BarangModel>()

        // SQL query to select barang by kode1
        val query = "SELECT * FROM $TABLE_BARANG WHERE kode1 = ?"
        val cursor = db.rawQuery(query, arrayOf(kode1))

        // Iterating over the cursor to create barang objects and add them to the list
        if (cursor.moveToFirst()) {
            do {
                val barang = BarangModel(
                    docId = cursor.getString(cursor.getColumnIndexOrThrow(COL_DOC_ID)),
                    uid = cursor.getString(cursor.getColumnIndexOrThrow(COL_UID)),
                    kode1 = cursor.getString(cursor.getColumnIndexOrThrow(COL_KODE1)),
                    perRak = cursor.getString(cursor.getColumnIndexOrThrow(COL_PER_RAK)),
                    nama = cursor.getString(cursor.getColumnIndexOrThrow(COL_NAME)),
                    satuan = cursor.getString(cursor.getColumnIndexOrThrow(COL_SATUAN)),
                    jenis = cursor.getString(cursor.getColumnIndexOrThrow(COL_JENIS)),
                    merek = cursor.getString(cursor.getColumnIndexOrThrow(COL_MEREK)),
                    kode2 = cursor.getString(cursor.getColumnIndexOrThrow(COL_KODE2)),
                    banyakQr = cursor.getString(cursor.getColumnIndexOrThrow(COL_BANYAK_QR)),
                    scanAt = cursor.getString(cursor.getColumnIndexOrThrow(COL_SCAN_AT)),
                    editAt = cursor.getString(cursor.getColumnIndexOrThrow(COL_EDIT_AT)),
                    createdAt = cursor.getString(cursor.getColumnIndexOrThrow(COL_CREATED_AT))
                )
                barangList.add(barang)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return barangList
    }

    fun getBarangByKode2Kode1(kode2: String, kode1: String): List<BarangModel> {
        val db = this.readableDatabase
        val barangList = mutableListOf<BarangModel>()

        // SQL query to select barang by kode2 and kode1
        val query = "SELECT * FROM $TABLE_BARANG WHERE kode2 = ? AND kode1 = ?"
        val cursor = db.rawQuery(query, arrayOf(kode2, kode1))

        // Iterating over the cursor to create barang objects and add them to the list
        if (cursor.moveToFirst()) {
            do {
                val barang = BarangModel(
                    docId = cursor.getString(cursor.getColumnIndexOrThrow(COL_DOC_ID)),
                    uid = cursor.getString(cursor.getColumnIndexOrThrow(COL_UID)),
                    kode1 = cursor.getString(cursor.getColumnIndexOrThrow(COL_KODE1)),
                    perRak = cursor.getString(cursor.getColumnIndexOrThrow(COL_PER_RAK)),
                    nama = cursor.getString(cursor.getColumnIndexOrThrow(COL_NAME)),
                    satuan = cursor.getString(cursor.getColumnIndexOrThrow(COL_SATUAN)),
                    jenis = cursor.getString(cursor.getColumnIndexOrThrow(COL_JENIS)),
                    merek = cursor.getString(cursor.getColumnIndexOrThrow(COL_MEREK)),
                    kode2 = cursor.getString(cursor.getColumnIndexOrThrow(COL_KODE2)),
                    banyakQr = cursor.getString(cursor.getColumnIndexOrThrow(COL_BANYAK_QR)),
                    scanAt = cursor.getString(cursor.getColumnIndexOrThrow(COL_SCAN_AT)),
                    editAt = cursor.getString(cursor.getColumnIndexOrThrow(COL_EDIT_AT)),
                    createdAt = cursor.getString(cursor.getColumnIndexOrThrow(COL_CREATED_AT))
                )
                barangList.add(barang)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return barangList
    }


    fun deleteBarang(docId: String): Boolean {
        val db = this.writableDatabase
        val result = db.delete(TABLE_BARANG, "docId = ?", arrayOf(docId))
        return result > 0
    }

    //report
    fun addReport(report: ReportModel): Boolean {
        val db = this.writableDatabase
        val docId = report.docId ?: UUID.randomUUID().toString()
        val values = ContentValues().apply {
            put(COL_DOC_ID, docId)
            put("uid", report.uid)
            put("nama", report.nama)
            put("perRak", report.perRak)
            put("rakId", report.rakId)
            put("status", report.status)
            put("itemNama", report.itemNama)
            put("nomorPenerimaan", report.nomorPenerimaan)
            put("itemUid", report.itemUid)
            put("itemMerek", report.itemMerek)
            put("itemJenis", report.itemJenis)
            put("satuan", report.satuan)
            put("petugasNama", report.petugasNama)
            put("petugasUid", report.petugasUid)
            put("jumlah", report.jumlah)
            put("scanAt", report.scanAt)
            put("editAt", report.editAt)
            put("createdAt", report.createdAt)
        }
        val result = db.insert(TABLE_REPORT, null, values)
        return result != -1L
    }

    fun getAllReports(): List<ReportModel> {
        val db = this.readableDatabase
        val reportsList = mutableListOf<ReportModel>()

        // SQL query to select all reports
        val query = "SELECT * FROM $TABLE_REPORT"
        val cursor = db.rawQuery(query, null)

        // Iterating over the cursor to create report objects and add them to the list
        if (cursor.moveToFirst()) {
            do {
                val report = ReportModel(
                    uid = cursor.getString(cursor.getColumnIndexOrThrow("uid")),
                    docId = cursor.getString(cursor.getColumnIndexOrThrow(COL_DOC_ID)),
                    nama = cursor.getString(cursor.getColumnIndexOrThrow("nama")),
                    perRak = cursor.getString(cursor.getColumnIndexOrThrow("perRak")),
                    rakId = cursor.getString(cursor.getColumnIndexOrThrow(COL_RAK_ID)),
                    status = cursor.getString(cursor.getColumnIndexOrThrow(COL_STATUS)),
                    itemNama = cursor.getString(cursor.getColumnIndexOrThrow("itemNama")),
                    nomorPenerimaan = cursor.getString(cursor.getColumnIndexOrThrow("nomorPenerimaan")),
                    itemUid = cursor.getString(cursor.getColumnIndexOrThrow("itemUid")),
                    itemMerek = cursor.getString(cursor.getColumnIndexOrThrow("itemMerek")),
                    itemJenis = cursor.getString(cursor.getColumnIndexOrThrow("itemJenis")),
                    satuan = cursor.getString(cursor.getColumnIndexOrThrow(COL_SATUAN)),
                    petugasNama = cursor.getString(cursor.getColumnIndexOrThrow("petugasNama")),
                    petugasUid = cursor.getString(cursor.getColumnIndexOrThrow("petugasUid")),
                    jumlah = cursor.getString(cursor.getColumnIndexOrThrow(COL_JUMLAH)),
                    scanAt = cursor.getString(cursor.getColumnIndexOrThrow(COL_SCAN_AT)),
                    editAt = cursor.getString(cursor.getColumnIndexOrThrow(COL_EDIT_AT)),
                    createdAt = cursor.getString(cursor.getColumnIndexOrThrow("createdAt"))
                )
                reportsList.add(report)
            } while (cursor.moveToNext())
        }

        cursor.close()
        return reportsList
    }

    fun getReportsByRakId(rakId: String): List<ReportModel> {
        val db = this.readableDatabase
        val reportList = mutableListOf<ReportModel>()

        // SQL query to select reports based on the rakId
        val query = "SELECT * FROM $TABLE_REPORT WHERE rakId = ?"
        val cursor = db.rawQuery(query, arrayOf(rakId))

        // Iterating over the cursor to create report objects and add them to the list
        if (cursor.moveToFirst()) {
            do {
                val report = ReportModel(
                    uid = cursor.getString(cursor.getColumnIndexOrThrow("uid")),
                    docId = cursor.getString(cursor.getColumnIndexOrThrow(COL_DOC_ID)),
                    nama = cursor.getString(cursor.getColumnIndexOrThrow("nama")),
                    perRak = cursor.getString(cursor.getColumnIndexOrThrow("perRak")),
                    rakId = cursor.getString(cursor.getColumnIndexOrThrow("rakId")),
                    status = cursor.getString(cursor.getColumnIndexOrThrow(COL_STATUS)),
                    itemNama = cursor.getString(cursor.getColumnIndexOrThrow("itemNama")),
                    nomorPenerimaan = cursor.getString(cursor.getColumnIndexOrThrow("nomorPenerimaan")),
                    itemUid = cursor.getString(cursor.getColumnIndexOrThrow("itemUid")),
                    itemMerek = cursor.getString(cursor.getColumnIndexOrThrow("itemMerek")),
                    itemJenis = cursor.getString(cursor.getColumnIndexOrThrow("itemJenis")),
                    satuan = cursor.getString(cursor.getColumnIndexOrThrow("satuan")),
                    petugasNama = cursor.getString(cursor.getColumnIndexOrThrow("petugasNama")),
                    petugasUid = cursor.getString(cursor.getColumnIndexOrThrow("petugasUid")),
                    jumlah = cursor.getString(cursor.getColumnIndexOrThrow("jumlah")),
                    scanAt = cursor.getString(cursor.getColumnIndexOrThrow("scanAt")),
                    editAt = cursor.getString(cursor.getColumnIndexOrThrow("editAt")),
                    createdAt = cursor.getString(cursor.getColumnIndexOrThrow("createdAt"))
                )
                reportList.add(report)
            } while (cursor.moveToNext())
        }

        cursor.close()
        return reportList
    }

    fun updateReportWithRakId(rakId: String, itemNama: String, perRak: String, itemUid: String, itemMerek: String, itemJenis: String, satuan: String): Boolean {
        val db = this.writableDatabase
        val contentValues = ContentValues().apply {
            put("itemNama", itemNama)
            put("perRak", perRak)
            put("itemUid", itemUid)
            put("itemMerek", itemMerek)
            put("itemJenis", itemJenis)
            put("satuan", satuan)
        }

        val rowsAffected = db.update(TABLE_REPORT, contentValues, "rakId=?", arrayOf(rakId))
        db.close()
        return rowsAffected > 0
    }

    fun deleteReport(rakId: String): Boolean {
        val db = this.writableDatabase
        val result = db.delete(TABLE_REPORT, "rakId = ?", arrayOf(rakId))
        return result > 0
    }
    fun deleteReportDetails(rakId: String): Boolean {
        val db = this.writableDatabase
        val result = db.delete(TABLE_REPORT_DETAIL, "rakId = ?", arrayOf(rakId))
        return result > 0
    }

    //report detail
    fun addReportDetail(reportDetail: ReportDetailModel): Boolean {
        val db = this.writableDatabase
        val docId = reportDetail.docId ?: UUID.randomUUID().toString()
        val values = ContentValues().apply {
            put("docId", docId)
            put("uid", reportDetail.uid)
            put("nama", reportDetail.nama)
            put("perRak", reportDetail.perRak)
            put("rakId", reportDetail.rackId)
            put("itemId", reportDetail.itemId)
            put("itemNama", reportDetail.itemNama)
            put("nomorPenerimaan", reportDetail.nomorPenerimaan)
            put("itemUid", reportDetail.itemUid)
            put("itemMerek", reportDetail.itemMerek)
            put("itemNum", reportDetail.itemNum)
            put("itemJenis", reportDetail.itemJenis)
            put("satuan", reportDetail.satuan)
            put("petugasNama", reportDetail.petugasNama)
            put("petugasUid", reportDetail.petugasUid)
            put("scanAt", reportDetail.scanAt)
            put("editAt", reportDetail.editAt)
            put("createdAt", reportDetail.createdAt)
        }
        val result = db.insert(TABLE_REPORT_DETAIL, null, values)
        return result != -1L
    }

    fun getReportDetailsByRakId(rakId: String): List<ReportDetailModel> {
        val db = this.readableDatabase
        val reportDetailsList = mutableListOf<ReportDetailModel>()

        // SQL query to select report details by RakId
        val query = "SELECT * FROM $TABLE_REPORT_DETAIL WHERE rakId = ?"
        val cursor = db.rawQuery(query, arrayOf(rakId))

        // Iterating over the cursor to create report detail objects and add them to the list
        if (cursor.moveToFirst()) {
            do {
                val reportDetail = ReportDetailModel(
                    uid = cursor.getString(cursor.getColumnIndexOrThrow("uid")),
                    docId = cursor.getString(cursor.getColumnIndexOrThrow("docId")),
                    nama = cursor.getString(cursor.getColumnIndexOrThrow("nama")),
                    perRak = cursor.getString(cursor.getColumnIndexOrThrow("perRak")),
                    rackId = cursor.getString(cursor.getColumnIndexOrThrow("rakId")),
                    itemId = cursor.getString(cursor.getColumnIndexOrThrow("itemId")),
                    itemNama = cursor.getString(cursor.getColumnIndexOrThrow("itemNama")),
                    nomorPenerimaan = cursor.getString(cursor.getColumnIndexOrThrow("nomorPenerimaan")),
                    itemUid = cursor.getString(cursor.getColumnIndexOrThrow("itemUid")),
                    itemMerek = cursor.getString(cursor.getColumnIndexOrThrow("itemMerek")),
                    itemNum = cursor.getString(cursor.getColumnIndexOrThrow("itemNum")),
                    itemJenis = cursor.getString(cursor.getColumnIndexOrThrow("itemJenis")),
                    satuan = cursor.getString(cursor.getColumnIndexOrThrow("satuan")),
                    petugasNama = cursor.getString(cursor.getColumnIndexOrThrow("petugasNama")),
                    petugasUid = cursor.getString(cursor.getColumnIndexOrThrow("petugasUid")),
                    scanAt = cursor.getString(cursor.getColumnIndexOrThrow("scanAt")),
                    editAt = cursor.getString(cursor.getColumnIndexOrThrow("editAt")),
                    createdAt = cursor.getString(cursor.getColumnIndexOrThrow("createdAt"))
                )
                reportDetailsList.add(reportDetail)
            } while (cursor.moveToNext())
        }

        cursor.close()
        return reportDetailsList
    }

    fun getScannedItemCountByRackId(rackId: String): Int {
        val db = this.readableDatabase
        val query = "SELECT COUNT(*) FROM $TABLE_REPORT_DETAIL WHERE rakId = ?"
        val cursor = db.rawQuery(query, arrayOf(rackId))
        var count = 0
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0)
        }
        cursor.close()
        return count
    }

    fun getCompletedReports(): List<ReportModel> {
        val db = this.readableDatabase
        val reportsList = mutableListOf<ReportModel>()

        // Define a selection argument to specify the criteria for the rows we want
        val selection = "$COL_STATUS = ?"
        val selectionArgs = arrayOf("lengkap")

        // Query the database for all completed reports
        val cursor = db.query(
            TABLE_REPORT,
            null, // null for all columns
            selection,
            selectionArgs,
            null,
            null,
            null
        )

        // Iterate over the cursor to create report objects and add them to the list
        if (cursor.moveToFirst()) {
            do {
                val report = ReportModel(
                    docId = cursor.getString(cursor.getColumnIndexOrThrow(COL_DOC_ID)),
                    uid = cursor.getString(cursor.getColumnIndexOrThrow("uid")),
                    nama = cursor.getString(cursor.getColumnIndexOrThrow("nama")),
                    perRak = cursor.getString(cursor.getColumnIndexOrThrow("perRak")),
                    rakId = cursor.getString(cursor.getColumnIndexOrThrow(COL_RAK_ID)),
                    status = cursor.getString(cursor.getColumnIndexOrThrow(COL_STATUS)),
                    itemNama = cursor.getString(cursor.getColumnIndexOrThrow("itemNama")),
                    nomorPenerimaan = cursor.getString(cursor.getColumnIndexOrThrow("nomorPenerimaan")),
                    itemUid = cursor.getString(cursor.getColumnIndexOrThrow(COL_ITEM_UID)),
                    itemMerek = cursor.getString(cursor.getColumnIndexOrThrow("itemMerek")),
                    itemJenis = cursor.getString(cursor.getColumnIndexOrThrow(COL_ITEM_JENIS)),
                    satuan = cursor.getString(cursor.getColumnIndexOrThrow(COL_SATUAN)),
                    petugasNama = cursor.getString(cursor.getColumnIndexOrThrow("petugasNama")),
                    petugasUid = cursor.getString(cursor.getColumnIndexOrThrow("petugasUid")),
                    jumlah = cursor.getString(cursor.getColumnIndexOrThrow(COL_JUMLAH)),
                    scanAt = cursor.getString(cursor.getColumnIndexOrThrow(COL_SCAN_AT)),
                    editAt = cursor.getString(cursor.getColumnIndexOrThrow(COL_EDIT_AT)),
                    createdAt = cursor.getString(cursor.getColumnIndexOrThrow("createdAt"))
                )
                reportsList.add(report)
            } while (cursor.moveToNext())
        }

        cursor.close()
        return reportsList
    }



    fun deleteReportDetail(docId: String): Boolean {
        val db = writableDatabase
        val result = db.delete(TABLE_REPORT_DETAIL, "docId = ?", arrayOf(docId))
        db.close()
        return result > 0
    }

    fun updateReportStatus(rackId: String, newStatus: String): Boolean {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(COL_STATUS, newStatus)

        // First check if the docId exists
        val cursor = db.query(TABLE_REPORT, arrayOf(COL_RAK_ID), "rakId=?", arrayOf(rackId), null, null, null)
        val exists = cursor.moveToFirst()
        cursor.close()

        if (!exists) {
            Log.d("DatabaseHelper", "No record found with docId: $rackId")
            return false
        }

        return try {
            val rowsAffected = db.update(TABLE_REPORT, contentValues, "rakId=?", arrayOf(rackId))
            if (rowsAffected > 0) {
                Log.d("DatabaseHelper", "Update successful, $rowsAffected row(s) affected.")
                true
            } else {
                Log.d("DatabaseHelper", "Update failed, no rows affected.")
                false
            }
        } catch (e: SQLiteException) {
            Log.e("DatabaseHelper", "Update failed", e)
            false
        } finally {
            db.close()
        }
    }

    fun deleteAllReports(): Boolean {
        val db = this.writableDatabase
        val result = db.delete(TABLE_REPORT, null, null)
        db.close()
        return result >= 0 // True if deletion is successful
    }

    fun deleteAllReportDetails(): Boolean {
        val db = this.writableDatabase
        val result = db.delete(TABLE_REPORT_DETAIL, null, null)
        db.close()
        return result >= 0 // True if deletion is successful
    }



    fun isReportDetailDuplicate(itemId: String): Boolean {
        val db = this.readableDatabase
        val query = "SELECT * FROM $TABLE_REPORT_DETAIL WHERE itemId = ?"
        val cursor = db.rawQuery(query, arrayOf(itemId))
        val exists = cursor.count > 0
        return exists
    }



    companion object {
        private const val DATABASE_NAME = "userDatabase"
        private const val DATABASE_VERSION = 1

        // Table and columns names
        private const val TABLE_USERS = "users"
        private const val COL_DOC_ID = "docId"
        private const val COL_NAME = "nama"
        private const val COL_USERNAME = "username"
        private const val COL_PASSWORD = "password"
        private const val COL_PHONE = "noHp"
        private const val COL_ROLE = "role"
        private const val COL_CREATED_AT = "createdAt"

        // Table and columns names for BarangModel
        private const val COL_UID = "uid"
        private const val TABLE_BARANG = "barang"
        private const val COL_KODE1 = "kode1"
        private const val COL_PER_RAK = "perRak"
        private const val COL_SATUAN = "satuan"
        private const val COL_JENIS = "jenis"
        private const val COL_MEREK = "merek"
        private const val COL_KODE2 = "kode2"
        private const val COL_BANYAK_QR = "banyakQr"
        private const val COL_SCAN_AT = "scanAt"
        private const val COL_EDIT_AT = "editAt"

        // Table and columns names for ReportModel
        private const val TABLE_REPORT = "report"
        private const val COL_RAK_ID = "rakId"
        private const val COL_STATUS = "status"
        private const val COL_NO_PENERIMAAN = "noPenerimaan"
        private const val COL_ITEM_UID = "itemUid"
        private const val COL_ITEM_MEREK = "itemMere"
        private const val COL_ITEM_JENIS = "itemJenis"
        private const val COL_PETUGAS_NAMA = "namaPetugas"
        private const val COL_PETUGAS_UID = "uidPetugas"
        private const val COL_JUMLAH= "jumlah"

        private const val TABLE_REPORT_DETAIL = "reportDetail"
    }
}
