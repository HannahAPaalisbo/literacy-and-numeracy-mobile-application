package com.example.tagakaulolearningapp

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.cardview.widget.CardView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import okhttp3.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ImageTracingActivity : ConnectivityClass(){
    private lateinit var topicId: String
    private lateinit var lessonId: String
    private lateinit var topicName: String
    private lateinit var sharedPreferences: SharedPreferences
    private val userType: String = ""
    private lateinit var subject: String
    private lateinit var db: SQLiteDatabase
    private var topicList: ArrayList<String> = ArrayList()
    private lateinit var imagePath: String
    private lateinit var topic: String
    private lateinit var playSoundFx: PlaySoundFx

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        actionBar?.hide()
        DeviceNavigationClass.hideNavigationBar(this)
        setContentView(R.layout.activity_image_tracing)

        playSoundFx = PlaySoundFx(this@ImageTracingActivity)

        sharedPreferences = getSharedPreferences("MyPreferences", MODE_PRIVATE)

        subject = intent.getStringExtra("subject").toString()
        topicId = intent.getStringExtra("topic").toString()
        lessonId = intent.getStringExtra("subjectId").toString()
        topicName = intent.getStringExtra("topicName").toString()

        val tableName1 = "tbl_subject"

        db = openOrCreateDatabase("tagakauloDB", Context.MODE_PRIVATE, null)
        val checkQuery1 = "SELECT * FROM $tableName1 WHERE subjectId = '$lessonId'"
        val subjectCursor: Cursor = db.rawQuery(checkQuery1, null)

        if (subjectCursor.moveToFirst()) {
            val columnIndexSubjectName = subjectCursor.getColumnIndex("subject")

            if (columnIndexSubjectName != -1) {
                do {
                    subject = subjectCursor.getString(columnIndexSubjectName)
                } while (subjectCursor.moveToNext())
            }

        } else {

        }

        subjectCursor.close()

        val tableName2 = "tbl_topic"

        val checkQuery2 = "SELECT * FROM $tableName2 WHERE topicId = '$topicId'"
        val topicCursor: Cursor = db.rawQuery(checkQuery2, null)

        if (topicCursor.moveToFirst()) {
            val columnIndexTopicName = topicCursor.getColumnIndex("topic")
            val columnIndexImagePath = topicCursor.getColumnIndex("imagePath")

            if (columnIndexTopicName != -1 && columnIndexImagePath != -1) {
                do {
                    topic = topicCursor.getString(columnIndexTopicName)
                    imagePath = topicCursor.getString(columnIndexImagePath)
                    // Process the retrieved data here
                } while (topicCursor.moveToNext())
            }

        } else {

        }
        topicCursor.close()

        val tableName3 = "tbl_language"
        val checkQuery3 = "SELECT * FROM $tableName3 WHERE topicRef = '$topicId'"

        val langCursor: Cursor = db.rawQuery(checkQuery3, null)

        if (langCursor.moveToFirst()) {
            val columnIndexKalagan = langCursor.getColumnIndex("kalagan")
            val columnIndexFilipino = langCursor.getColumnIndex("filipino")
            val columnIndexEnglish = langCursor.getColumnIndex("english")

            if (columnIndexKalagan != -1 && columnIndexFilipino != -1 && columnIndexEnglish != -1) {
                do {
                    val kalagan = langCursor.getString(columnIndexKalagan)
                    val filipino = langCursor.getString(columnIndexFilipino)
                    val english = langCursor.getString(columnIndexEnglish)

                    val topicData = arrayListOf("$kalagan", "$filipino", "$english", "$imagePath")

                    topicList.add(topicData.toString())
                } while (langCursor.moveToNext())

            } else {

            }
        } else {
            val topicData = arrayListOf("", "", "", "$imagePath")
            topicList.add(topicData.toString())
        }

        langCursor.close()

        if(topicList.isEmpty()) {
            noLearningMaterials()
        } else {
            loadTopic()
        }
    }

    private fun loadTopic() {
        val parentLayout: LinearLayout = findViewById(R.id.parentLayout)
        val inflater = LayoutInflater.from(this)

        for (topicData in topicList) {
            val cleanedTopicData = topicData.replace("[", "").replace("]", "")
            val pieces = cleanedTopicData.split(",")

            if (pieces.size >= 4) {
                val kalagan = pieces[0].trim()
                val filipino = pieces[1].trim()
                val english = pieces[2].trim()
                val imagePath = pieces[3].trim()

                val symbolLayout = inflater.inflate(R.layout.layout_alphabets_numbers_firstcard, parentLayout, false) as RelativeLayout
                val cardView = symbolLayout.findViewById<CardView>(R.id.imgContainer)
                val btnBack = symbolLayout.findViewById<ImageView>(R.id.imgBack)
                val txtTopic = symbolLayout.findViewById<TextView>(R.id.txtSubject)
                val btnPractice = symbolLayout.findViewById<Button>(R.id.btnPractice)
                val img1 = symbolLayout.findViewById<ImageView>(R.id.imgDisplay)
                val btnAudio = symbolLayout.findViewById<ImageView>(R.id.btnAudio)
                val txtKalaganDisplay = symbolLayout.findViewById<TextView>(R.id.txtKalagan)
                val txtFilipinoDisplay = symbolLayout.findViewById<TextView>(R.id.txtFilipino)
                val txtEnglishDisplay = symbolLayout.findViewById<TextView>(R.id.txtEnglish)
                val txtKalagan = symbolLayout.findViewById<TextView>(R.id.txtKalaganT)
                val txtFilipino = symbolLayout.findViewById<TextView>(R.id.txtFilipinoT)
                val txtEnglish = symbolLayout.findViewById<TextView>(R.id.txtEnglishT)

                if (kalagan == "" && filipino == "" && english == "") {
                    val cardViewParams = cardView.layoutParams as RelativeLayout.LayoutParams  // or LinearLayout.LayoutParams
                    cardViewParams.addRule(RelativeLayout.CENTER_IN_PARENT) // or Gravity.CENTER for LinearLayout
                    cardViewParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 0)
                    cardView.layoutParams = cardViewParams
                }
                txtTopic.text = subject + ": " + topicName
                if(imagePath != "") {
                    Glide.with(this@ImageTracingActivity)
                        .load(imagePath)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(img1)
                }
                else {
                    Glide.with(this@ImageTracingActivity)
                        .load(R.drawable.broken_media)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(img1)
                }

                if (kalagan == "") {
                    txtKalagan.visibility = View.GONE
                    txtKalaganDisplay.visibility = View.GONE
                }
                else {
                    txtKalaganDisplay.visibility = View.VISIBLE
                    txtKalagan.visibility = View.VISIBLE
                    txtKalagan.text = kalagan
                }

                if (filipino == "") {
                    txtFilipino.visibility = View.GONE
                    txtFilipinoDisplay.visibility = View.GONE
                }
                else {
                    txtFilipinoDisplay.visibility = View.VISIBLE
                    txtFilipino.visibility = View.VISIBLE
                    txtFilipino.text = filipino
                }

                if (english == "") {
                    txtEnglish.visibility = View.GONE
                    txtEnglishDisplay.visibility = View.GONE
                }
                else {
                    txtEnglishDisplay.visibility = View.VISIBLE
                    txtEnglish.visibility = View.VISIBLE
                    txtEnglish.text = english
                }

                val uniqueId = View.generateViewId()
                symbolLayout.id = uniqueId
                parentLayout.addView(symbolLayout)

                btnBack.setOnClickListener {
                    val intent = Intent(this, LessonActivity::class.java)
                    intent.putExtra("subjectId", lessonId)
                    startActivity(intent)
                    finish()
                }

                btnAudio.setOnClickListener {
                    playSoundFx.getAudioPath(topicId, this@ImageTracingActivity)
                    //Toast.makeText(this, "Supposedly audio plays", Toast.LENGTH_SHORT).show()
                }

                btnPractice.setOnClickListener {
                    if (subject.lowercase().contains("alphabets") || subject.lowercase().contains("letters")) {
                        updateCentralDBLearnerProgress()
                        loadLetterTracing()
                    }
                    else if (subject.lowercase().contains("numbers")) {
                        updateCentralDBLearnerProgress()
                        loadNumberTracing()
                    }
                    else {
                        Log.d("User Activity: ", "Finished Lesson")
                        updateCentralDBLearnerProgress()
                        val intent = Intent(this@ImageTracingActivity, PracticeQuizActivity::class.java)
                        intent.putExtra("subjectId", lessonId)
                        intent.putExtra("topic", topicId)
                        intent.putExtra("topicName", topicName)
                        startActivity(intent)
                    }
                }
            } else {
            }
        }
    }

    private fun loadLetterTracing() {
        val intent = Intent(this@ImageTracingActivity, LetterTracingActivity::class.java)
        intent.putExtra("subject", lessonId)
        intent.putExtra("topic", topicId)
        intent.putExtra("topicName", topicName)
        startActivity(intent)
        finish()
    }

    private fun loadNumberTracing() {
        val intent = Intent(this@ImageTracingActivity, NumberTracingActivity::class.java)
        intent.putExtra("subject", lessonId)
        intent.putExtra("topic", topicId)
        intent.putExtra("topicName", topicName)
        startActivity(intent)
        finish()
    }

    private fun noLearningMaterials() {
        val intent = Intent(this@ImageTracingActivity, LessonActivity::class.java)
        intent.putExtra("subject", lessonId)
        startActivity(intent)
    }

    private fun updateCentralDBLearnerProgress() {
        val learnerId = sharedPreferences.getString("learnerId","").toString()
        val fileAccess = "getLearnerProgress.php?learnerId=$learnerId&topicId=$topicId&dateStartTaken=${getDate()}"
        val baseUrl = ConnectionClass()
        val url = baseUrl + fileAccess
        val request = Request.Builder().url(url).build()
        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
            }
            override fun onFailure(call: Call, e: java.io.IOException) {
                runOnUiThread {
                    Toast.makeText(this@ImageTracingActivity, "No connection", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }
    private fun getDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd")
        val currentDate = Date()
        return sdf.format(currentDate)
    }

    private fun addUserProgress(subjectId: String, topicId: String) {
        val userType = sharedPreferences.getString("userType", "").toString()

        if (userType == "learner") {
            val firstname = sharedPreferences.getString("firstName", "").toString()
            val lastname = sharedPreferences.getString("lastName", "").toString()
            val studentName = lastname.uppercase() + ", " + firstname.lowercase().replaceFirstChar { it.uppercaseChar() }
            val progress = 1.0
            val tableName = "tbl_learnerProgress"

            println("New Progress has been added $subjectId")
            val query = "SELECT * FROM $tableName WHERE studentName = ? AND subjectId = ? AND topicId = ?"
            val topicArray = arrayOf(studentName, subjectId, topicId)
            val topicCursor: Cursor = db.rawQuery(query, topicArray)
            if (topicCursor.moveToFirst()) {

            }
            else {
                val insertQuery = "INSERT INTO $tableName (status, studentName, topicId, subjectId) VALUES (?,?,?,?)"
                val progressArray = arrayOf(progress, studentName, topicId, subjectId)
                db.execSQL(insertQuery, progressArray)
            }
            topicCursor.close()
        }
        else {
            sharedPreferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
            val teacherId = sharedPreferences.getString("userId", "")
            val progress = 1.0
            val tableName = "tbl_teacherProgress"
            val query = "SELECT * FROM $tableName WHERE teacherId = ? AND subjectId = ? AND topicId = ?"
            val topicArray = arrayOf(teacherId, subjectId, topicId)
            val topicCursor: Cursor = db.rawQuery(query, topicArray)

            if (topicCursor.moveToFirst()) {
            }
            else {
                val insertQuery = "INSERT INTO $tableName (status, teacherId, topicId, subjectId) VALUES (?,?,?,?)"
                val progressArray = arrayOf(progress, teacherId, topicId, subjectId)
                db.execSQL(insertQuery, progressArray)
            }
            topicCursor.close()
        }
    }
}