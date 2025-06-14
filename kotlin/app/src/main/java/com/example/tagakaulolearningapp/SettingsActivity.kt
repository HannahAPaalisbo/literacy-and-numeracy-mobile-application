package com.example.tagakaulolearningapp

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bumptech.glide.Glide
import android.os.Process
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.view.get

class SettingsActivity : AppCompatActivity() {
    private lateinit var db: SQLiteDatabase
    private lateinit var muteBGM: Button
    private lateinit var muteSFX: Button
    private lateinit var exportGrade: Button
    private lateinit var clearCache: Button
    private lateinit var btnBack: ImageView
    private lateinit var btnLogout: ImageView
    private lateinit var userId: String
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var playSoundFx: PlaySoundFx

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        actionBar?.hide()
        DeviceNavigationClass.hideNavigationBar(this)
        setContentView(R.layout.activity_settings)

        playSoundFx = PlaySoundFx(this@SettingsActivity)

        sharedPreferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
        val userType = sharedPreferences.getString("userType", "")
        userId = sharedPreferences.getString("userId", "").toString()

        db = openOrCreateDatabase("tagakauloDB", Context.MODE_PRIVATE, null)

        muteBGM = findViewById(R.id.button2)

        exportGrade = findViewById(R.id.btnExport)
        clearCache = findViewById(R.id.button5)
        btnLogout = findViewById(R.id.btnLogout)
        btnBack = findViewById(R.id.btnBack)

        if (userType == "learner" ) {
            exportGrade.visibility = View.GONE
        }
        else {
            exportGrade.visibility = View.VISIBLE
        }
        btnBack.setOnClickListener{
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }
        btnLogout.setOnClickListener {
            val intent = Intent(this, UserOption::class.java)
            startActivity(intent)
            finish()
        }
        muteBGM.setOnClickListener {  }
        exportGrade.setOnClickListener {
            val intent = Intent(this, ExcelExportActivity::class.java)
            startActivity(intent)
        }
        clearCache.setOnClickListener {
            showBatchDialog()
        }
    }
    private fun deleteBatchData(batch: String) {
        val tableName = "tbl_grading"

        val checkQuery = "SELECT * FROM $tableName WHERE batch = ?"
        val teacherArray = arrayOf(batch)
        val teacherCursor: Cursor = db.rawQuery(checkQuery, teacherArray)

        if (teacherCursor.count > 0) {
            val deleteQuery = "DELETE FROM $tableName WHERE batch = '$batch'"
            db.execSQL(deleteQuery)
            Toast.makeText(this@SettingsActivity, "Successfully deleted $batch", Toast.LENGTH_SHORT).show()
            teacherCursor.close()
        } else {
            Toast.makeText(this@SettingsActivity, "No records of $batch", Toast.LENGTH_SHORT).show()
            teacherCursor.close()
        }
    }

    private fun showBatchDialog(){
        val parentLayout: RelativeLayout = findViewById(R.id.btnSubject)
        val inflater = LayoutInflater.from(this)

        val batchDialog = inflater.inflate(R.layout.layout_settings_batch_delete, parentLayout, false) as RelativeLayout

        val childLayout = batchDialog.findViewById<RelativeLayout>(R.id.childLayout)
        val spBatch = batchDialog.findViewById<Spinner>(R.id.spBatch)
        val btnAgree = batchDialog.findViewById<TextView>(R.id.btnAgree)

        childLayout.setOnClickListener {
            parentLayout.removeView(batchDialog)
        }

        val adapter = ArrayAdapter(this@SettingsActivity, android.R.layout.simple_spinner_item, getBatchList())
        spBatch.adapter = adapter
        parentLayout.addView(batchDialog)

        btnAgree.setOnClickListener {
            val batch = spBatch.getSelectedItem().toString()
            deleteBatchData(batch)
            parentLayout.removeView(batchDialog)
        }
    }

    private fun getBatchList(): ArrayList<String> {
        var batchList: ArrayList<String> = ArrayList()

        batchList.add(getString(R.string.spinner3_prompt))

        val tableName = "tbl_batch"
        val checkQuery = "SELECT * FROM $tableName WHERE teacherId = '$userId' ORDER BY batch"
        val batchCursor: Cursor = db.rawQuery(checkQuery, null)
        if (batchCursor.count > 0) {
            if (batchCursor.moveToFirst()){
                val batchColumnIndex = batchCursor.getColumnIndex("batch")
                if (batchColumnIndex != -1) {
                    do {
                        val batch = batchCursor.getString(batchColumnIndex)
                        batchList.add(batch)
                    } while (batchCursor.moveToNext())
                }
            }
        }
        return batchList
    }

    override fun onBackPressed() {
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish()
    }
}