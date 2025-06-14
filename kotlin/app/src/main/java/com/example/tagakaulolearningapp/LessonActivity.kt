package com.example.tagakaulolearningapp

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.core.content.ContextCompat

class LessonActivity : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var db: SQLiteDatabase
    private var progress: Int = 0
    private lateinit var lessonId: String
    private lateinit var subject: String
    private var topicList: ArrayList<String> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        actionBar?.hide()
        DeviceNavigationClass.hideNavigationBar(this)
        setContentView(R.layout.activity_lesson)

        sharedPreferences = getSharedPreferences("MyPreferences", MODE_PRIVATE)

        lessonId = intent.getStringExtra("subjectId").toString()

        val tableName1 = "tbl_subject"
        val imgBack = findViewById<ImageView>(R.id.imgBack)
        imgBack.setOnClickListener { goHome() }

        db = openOrCreateDatabase("tagakauloDB", Context.MODE_PRIVATE, null)
        val checkQuery1 = "SELECT * FROM $tableName1 WHERE subjectId = ?"
        val subjectArray = arrayOf(lessonId)
        val subjectCursor: Cursor = db.rawQuery(checkQuery1, subjectArray)

        if (subjectCursor.moveToFirst()) {
            val columnIndexSubjectName = subjectCursor.getColumnIndex("subject")

            if (columnIndexSubjectName != 0) {
                do {
                    subject = subjectCursor.getString(columnIndexSubjectName)
                } while (subjectCursor.moveToNext())
            }

        } else {

        }
        val subjectDisplay = findViewById<TextView>(R.id.txtTeacher)
        subjectDisplay.text = subject
        subjectCursor.close()

        db.close()

        val tableName = "tbl_topic"

        db = openOrCreateDatabase("tagakauloDB", Context.MODE_PRIVATE, null)
        val checkQuery = "SELECT * FROM $tableName WHERE subjectRef LIKE ? ORDER BY topic ASC"
        val topicArray = arrayOf(lessonId)
        val topicCursor: Cursor = db.rawQuery(checkQuery, topicArray)
        var topicId = ""

        if (topicCursor.moveToFirst()) {
            val columnIndexSubjectId = topicCursor.getColumnIndex("topicId")
            val columnIndexSubject = topicCursor.getColumnIndex("topic")
            val columnIndexImagePath = topicCursor.getColumnIndex("imagePath")
            val columnIndexVideoPath = topicCursor.getColumnIndex("videoPath")

            if (columnIndexSubjectId != -1 && columnIndexSubject != -1) {
                do {
                    topicId = topicCursor.getString(columnIndexSubjectId)
                    val topic = topicCursor.getString(columnIndexSubject)
                    val imagePath = topicCursor.getString(columnIndexImagePath)
                    val videoPath = topicCursor.getString(columnIndexVideoPath)

                    val topicData = arrayListOf("$topicId", "$topic", "$imagePath", "$videoPath")
                    topicList.add(topicData.toString())
                } while (topicCursor.moveToNext())
            } else {

            }
        } else {
            noTopic()
        }

        topicCursor.close()
        if(topicList.isEmpty()) {
            noTopic()
        } else {
            loadLesson(subject.lowercase())
        }
    }

    private fun loadLesson(subject: String) {
        val parentLayout: GridLayout = findViewById(R.id.parentLayout)

        val inflater = LayoutInflater.from(this)

        when {
            subject.contains("alphabets") -> {
                for (topic in topicList) {
                    val cleanedTopicData = topic.replace("[", "").replace("]", "")
                    val pieces = cleanedTopicData.split(",")

                    if (pieces.size >= 4) {
                        val topicId = pieces[0].trim()
                        val topicName = pieces[1].trim()
                        val imagePath = pieces[2].trim()
                        val videoPath = pieces[3].trim()

                        val subjectLayout = inflater.inflate(R.layout.layout_alphabet_number_container, parentLayout, false) as RelativeLayout

                        val actualLayout = subjectLayout.findViewById<RelativeLayout>(R.id.actualLayout)
                        val txtSubject = subjectLayout.findViewById<TextView>(R.id.txtSubject)
                        val progressBar = subjectLayout.findViewById<ProgressBar>(R.id.progressBar)
                        val txtProgress = subjectLayout.findViewById<TextView>(R.id.txtProgress)
                        val defDrawableBg = ContextCompat.getDrawable(this, R.drawable.def_container_bg)
                        val drawableBg = ContextCompat.getDrawable(this, R.drawable.alphabets_numbers_finished_bg)

                        progress = getTopicProgress(lessonId, topicId)

                        if (progress == 100) {
                            actualLayout.background = drawableBg
                            val textColor = ContextCompat.getColor(this, R.color.progress_label)
                            txtSubject.setTextColor(textColor)
                        } else {
                            actualLayout.background = defDrawableBg
                            val textColor = ContextCompat.getColor(this, R.color.pri_font)
                            txtSubject.setTextColor(textColor)
                        }
                        txtProgress.visibility = View.GONE

                        txtSubject.text = topicName

                        val uniqueId = View.generateViewId()
                        subjectLayout.id = uniqueId
                        parentLayout.addView(subjectLayout)

                        subjectLayout.setOnClickListener {
                            if (videoPath != "") {
                                addUserProgress(lessonId, topicId)
                                println("New Progress has been added $lessonId")
                                val intent = Intent(this@LessonActivity, VideoLearningActivity::class.java)
                                intent.putExtra("subjectId", lessonId)
                                intent.putExtra("subject", this.subject)
                                intent.putExtra("topic", topicId)
                                intent.putExtra("topicName", topicName)
                                startActivity(intent)
                            }
                            else if (imagePath != "") {
                                val intent = Intent(this@LessonActivity, ImageTracingActivity::class.java)
                                intent.putExtra("subjectId", lessonId)
                                intent.putExtra("subject", this.subject)
                                intent.putExtra("topic", topicId)
                                intent.putExtra("topicName", topicName)
                                startActivity(intent)
                            }
                            else {
                                //Toast.makeText(this@LessonActivity, "No learning materials found", Toast.LENGTH_SHORT).show()
                                val intent = Intent(this@LessonActivity, ImageTracingActivity::class.java)
                                intent.putExtra("subjectId", lessonId)
                                intent.putExtra("topic", topicId)
                                intent.putExtra("topicName", topicName)
                                startActivity(intent)
                            }
                        }
                    }
                }
            }
            subject.contains("numbers") -> {
                for (topic in topicList) {
                    val cleanedTopicData = topic.replace("[", "").replace("]", "")
                    val pieces = cleanedTopicData.split(",")

                    if (pieces.size >= 4) {
                        val topicId = pieces[0].trim()
                        val topicName = pieces[1].trim()
                        val imagePath = pieces[2].trim()
                        val videoPath = pieces[3].trim()

                        val subjectLayout = inflater.inflate(R.layout.layout_alphabet_number_container, parentLayout, false) as RelativeLayout

                        val actualLayout = subjectLayout.findViewById<RelativeLayout>(R.id.actualLayout)
                        val txtSubject = subjectLayout.findViewById<TextView>(R.id.txtSubject)
                        val progressBar = subjectLayout.findViewById<ProgressBar>(R.id.progressBar)
                        val txtProgress = subjectLayout.findViewById<TextView>(R.id.txtProgress)

                        val defDrawableBg = ContextCompat.getDrawable(this, R.drawable.def_container_bg)
                        val drawableBg = ContextCompat.getDrawable(this, R.drawable.alphabets_numbers_finished_bg)

                        progress = getTopicProgress(lessonId, topicId)

                        if (progress == 100) {
                            actualLayout.background = drawableBg
                            val textColor = ContextCompat.getColor(this, R.color.progress_label)
                            txtSubject.setTextColor(textColor)
                        } else {
                            actualLayout.background = defDrawableBg
                            val textColor = ContextCompat.getColor(this, R.color.pri_font)
                            txtSubject.setTextColor(textColor)
                        }

                        txtProgress.visibility = View.GONE

                        txtSubject.text = topicName

                        val uniqueId = View.generateViewId()
                        subjectLayout.id = uniqueId
                        parentLayout.addView(subjectLayout)

                        subjectLayout.setOnClickListener {
                            if (videoPath != "") {
                                addUserProgress(lessonId, topicId)
                                println("New Progress has been added $lessonId")
                                val intent = Intent(this@LessonActivity, VideoLearningActivity::class.java)
                                intent.putExtra("subject", lessonId)
                                intent.putExtra("topic", topicId)
                                intent.putExtra("topicName", topicName)
                                startActivity(intent)
                            }
                            else if (imagePath != "") {
                                val intent = Intent(this@LessonActivity, ImageTracingActivity::class.java)
                                intent.putExtra("subject", lessonId)
                                intent.putExtra("topic", topicId)
                                intent.putExtra("topicName", topicName)
                                startActivity(intent)
                            }
                            else {
                                //Toast.makeText(this@LessonActivity, "No learning materials found", Toast.LENGTH_SHORT).show()
                                val intent = Intent(this@LessonActivity, ImageTracingActivity::class.java)
                                intent.putExtra("subject", lessonId)
                                intent.putExtra("topic", topicId)
                                intent.putExtra("topicName", topicName)
                                startActivity(intent)
                            }
                        }
                    }
                }
            }
            subject.contains("stories") -> {
                for (topic in topicList) {
                    val cleanedTopicData = topic.replace("[", "").replace("]", "")
                    val pieces = cleanedTopicData.split(",")

                    if (pieces.size >= 4) {
                        val topicId = pieces[0].trim()
                        val topicName = pieces[1].trim()
                        val imagePath = pieces[2].trim()
                        val videoPath = pieces[3].trim()

                        val subjectLayout = inflater.inflate(R.layout.layout_home, parentLayout, false) as RelativeLayout
                        val actualLayout = subjectLayout.findViewById<RelativeLayout>(R.id.btnSubject)
                        val imgSubject = subjectLayout.findViewById<ImageView>(R.id.imgSubject)
                        val txtSubject = subjectLayout.findViewById<TextView>(R.id.txtSubject)
                        val txtCategory = subjectLayout.findViewById<TextView>(R.id.txtCategory)
                        val progressBar = subjectLayout.findViewById<ProgressBar>(R.id.progressBar)
                        val txtProgress = subjectLayout.findViewById<TextView>(R.id.txtProgress)

                        txtCategory.visibility = View.GONE
                        progress = getTopicProgress(lessonId, topicId)

                        val defDrawableBg = ContextCompat.getDrawable(this, R.drawable.def_container_bg)
                        val drawableBg = ContextCompat.getDrawable(this, R.drawable.home_finished_container)

                        if (progress == 100) {
                            actualLayout.background = drawableBg
                            val textColor = ContextCompat.getColor(this, R.color.progress_label)
                            val params = txtSubject.layoutParams as RelativeLayout.LayoutParams
                            params.addRule(RelativeLayout.CENTER_IN_PARENT)
                            txtSubject.layoutParams = params
                            imgSubject.visibility = View.GONE
                            txtSubject.setTextColor(textColor)
                        } else {
                            actualLayout.background = defDrawableBg
                            val textColor = ContextCompat.getColor(this, R.color.pri_font)
                            txtSubject.setTextColor(textColor)
                        }

                        progressBar.visibility = View.GONE
                        txtProgress.visibility = View.GONE

                        imgSubject.setImageResource(R.drawable.no_media)
                        txtSubject.text = topicName

                        val uniqueId = View.generateViewId()
                        subjectLayout.id = uniqueId
                        parentLayout.addView(subjectLayout)

                        subjectLayout.setOnClickListener {
                            if (videoPath != "") {
                                addUserProgress(lessonId, topicId)
                                println("New Progress has been added $lessonId")
                                val intent = Intent(this@LessonActivity, VideoLearningActivity::class.java)
                                intent.putExtra("subjectId", lessonId)
                                intent.putExtra("topic", topicId)
                                intent.putExtra("topicName", topicName)
                                startActivity(intent)
                            }
                            else if (imagePath != "") {
                                val intent = Intent(this@LessonActivity, ImageTracingActivity::class.java)
                                intent.putExtra("subjectId", lessonId)
                                intent.putExtra("topic", topicId)
                                intent.putExtra("topicName", topicName)
                                startActivity(intent)
                            }
                            else {
                                //Toast.makeText(this@LessonActivity, "No learning materials found", Toast.LENGTH_SHORT).show()
                                val intent = Intent(this@LessonActivity, ImageTracingActivity::class.java)
                                intent.putExtra("subjectId", lessonId)
                                intent.putExtra("topic", topicId)
                                intent.putExtra("topicName", topicName)
                                startActivity(intent)
                            }
                        }
                    }
                }
            }
            else -> {
                for (topic in topicList) {
                    val cleanedTopicData = topic.replace("[", "").replace("]", "")
                    val pieces = cleanedTopicData.split(",")

                    if (pieces.size >= 4) {
                        val topicId = pieces[0].trim()
                        val topicName = pieces[1].trim()
                        val imagePath = pieces[2].trim()
                        val videoPath = pieces[3].trim()

                        val subjectLayout = inflater.inflate(R.layout.layout_home, parentLayout, false) as RelativeLayout
                        val actualLayout = subjectLayout.findViewById<RelativeLayout>(R.id.btnSubject)
                        val imgSubject = subjectLayout.findViewById<ImageView>(R.id.imgSubject)
                        val txtSubject = subjectLayout.findViewById<TextView>(R.id.txtSubject)
                        val txtCategory = subjectLayout.findViewById<TextView>(R.id.txtCategory)
                        val progressBar = subjectLayout.findViewById<ProgressBar>(R.id.progressBar)
                        val txtProgress = subjectLayout.findViewById<TextView>(R.id.txtProgress)

                        txtCategory.visibility = View.GONE
                        progress = getTopicProgress(lessonId, topicId)

                        val defDrawableBg = ContextCompat.getDrawable(this, R.drawable.def_container_bg)
                        val drawableBg = ContextCompat.getDrawable(this, R.drawable.home_finished_container)

                        if (progress == 100) {
                            actualLayout.background = drawableBg
                            val textColor = ContextCompat.getColor(this, R.color.progress_label)
                            val params = txtSubject.layoutParams as RelativeLayout.LayoutParams
                            params.addRule(RelativeLayout.CENTER_IN_PARENT)
                            txtSubject.layoutParams = params
                            imgSubject.visibility = View.GONE
                            txtSubject.setTextColor(textColor)
                        } else {
                            actualLayout.background = defDrawableBg
                            val textColor = ContextCompat.getColor(this, R.color.pri_font)
                            txtSubject.setTextColor(textColor)
                        }

                        progressBar.visibility = View.GONE
                        txtProgress.visibility = View.GONE

                        imgSubject.setImageResource(R.drawable.no_media)
                        txtSubject.text = topicName

                        val uniqueId = View.generateViewId()
                        subjectLayout.id = uniqueId
                        parentLayout.addView(subjectLayout)

                        subjectLayout.setOnClickListener {
                            if (videoPath != "") {
                                addUserProgress(lessonId, topicId)
                                println("New Progress has been added $lessonId")
                                val intent = Intent(this@LessonActivity, VideoLearningActivity::class.java)
                                intent.putExtra("subjectId", lessonId)
                                intent.putExtra("topic", topicId)
                                intent.putExtra("topicName", topicName)
                                startActivity(intent)
                            }
                            else if (imagePath != "") {
                                val intent = Intent(this@LessonActivity, ImageTracingActivity::class.java)
                                intent.putExtra("subjectId", lessonId)
                                intent.putExtra("topic", topicId)
                                intent.putExtra("topicName", topicName)
                                startActivity(intent)
                            }
                            else {
                                //Toast.makeText(this@LessonActivity, "No learning materials found", Toast.LENGTH_SHORT).show()
                                val intent = Intent(this@LessonActivity, ImageTracingActivity::class.java)
                                intent.putExtra("subjectId", lessonId)
                                intent.putExtra("topic", topicId)
                                intent.putExtra("topicName", topicName)
                                startActivity(intent)
                            }
                        }
                    }
                }
            }
        }
    }
    private fun goHome() {
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish()
    }
    private fun noTopic() {
        Toast.makeText(this@LessonActivity, "There are no topics found", Toast.LENGTH_SHORT).show()
    }
    private fun getTopicProgress(subjectId: String, topicId: String): Int {
        val userType = sharedPreferences.getString("userType", "")

        val userId = sharedPreferences.getString("userId", "")
        var firstName = sharedPreferences.getString("firstName", "").toString()
        var lastName = sharedPreferences.getString("lastName", "").toString()
        var studentName =  lastName.uppercase() + ", " + firstName.lowercase().replaceFirstChar { it.uppercaseChar() }
        if (userType == "learner") {
            val tableName1 = "tbl_learnerProgress"
            val checkQuery1 = "SELECT * FROM $tableName1 WHERE studentName = ? AND subjectId = ? AND topicId = ? AND status = ?"

            val progressArray = arrayOf(studentName, subjectId, topicId, "1")
            val progressCursor: Cursor = db.rawQuery(checkQuery1, progressArray)
            if (progressCursor.moveToFirst()) {
                do {
                    return 100
                } while (progressCursor.moveToNext())
            }
            else {
                return 0
            }
            progressCursor.close()
        }
        else {
            val tableName1 = "tbl_teacherProgress"
            val checkQuery1 = "SELECT * FROM $tableName1 WHERE teacherId = ? AND subjectId = ? AND topicId = ?"
            val progressArray = arrayOf(userId, subjectId, topicId)
            val progressCursor: Cursor = db.rawQuery(checkQuery1, progressArray)
            if (progressCursor.moveToFirst()) {
                do {
                    return 100
                } while (progressCursor.moveToNext())
            }
            else {
                return 0
            }
            progressCursor.close()
        }
    }

    private fun getQuiz() {
        val tableName = "tbl_quiz"
        val query = "SELECT * FROM $tableName"
        val quizCursor: Cursor = db.rawQuery(query, null)
        if (quizCursor.count > 0) {
            val intent = Intent(this@LessonActivity, QuizActivity::class.java)
            intent.putExtra("subject", lessonId)
            quizCursor.close()
            startActivity(intent)
            finish()
        } else {
            noQuiz()
        }
    }
    private fun noQuiz() {
        Toast.makeText(this@LessonActivity, "No quiz to take", Toast.LENGTH_SHORT).show()
    }

    private fun addUserProgress(subjectId: String, topicId: String) {
        val userType = sharedPreferences.getString("userType", "").toString()

        if (userType == "learner") {
            val firstname = sharedPreferences.getString("firstName", "").toString()
            val lastname = sharedPreferences.getString("lastName", "").toString()
            val studentName = lastname + ", " + firstname
            val progress = 1.0
            val tableName = "tbl_learnerProgress"

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

    override fun onBackPressed() {
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish()
    }
}