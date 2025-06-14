package com.example.tagakaulolearningapp

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.support.v4.os.IResultReceiver._Parcel
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.MediaItem

class HomeActivity : ConnectivityClass() {

    private lateinit var db: SQLiteDatabase
    private var subjectList: ArrayList<String> = ArrayList()
    private lateinit var settings: ImageView
    private lateinit var achievements: ImageView
    private lateinit var txtTeacher: TextView
    private lateinit var sharedPreferences: SharedPreferences
    private var initialProgress: Int = 0
    private var finalProgress: Int = 0
    private var initialQuizProgress: Int = 0
    private var finalQuizProgress: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        actionBar?.hide()
        DeviceNavigationClass.hideNavigationBar(this)
        setContentView(R.layout.activity_home)

        txtTeacher = findViewById(R.id.txtUser)

        sharedPreferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
        val userType = sharedPreferences.getString("userType", "")
        val userId = sharedPreferences.getString("userId", "")
        val firstName = sharedPreferences.getString("firstName", "")
        val lastName = sharedPreferences.getString("lastName", "")
        val adviser = sharedPreferences.getString("adviser", "")

        val mySwipeRefreshLayout = findViewById<SwipeRefreshLayout>(R.id.swiperefresh)
        mySwipeRefreshLayout.setOnRefreshListener {
            if (checkAndShowToast(this@HomeActivity, "homePage")) {
                val parentLayout: GridLayout = findViewById(R.id.parentLayout)
                parentLayout.removeAllViews()

                subjectList.clear()

                val tableName = "tbl_subject"
                val column = "subject"

                db = openOrCreateDatabase("tagakauloDB", Context.MODE_PRIVATE, null)
                val checkQuery = "SELECT * FROM $tableName WHERE addedBy = ? OR addedBy = ? ORDER BY $column ASC"
                val lessonCursor: Cursor = db.rawQuery(checkQuery, arrayOf(adviser, userId))

                if (lessonCursor.moveToFirst()) {
                    val columnIndexSubjectId = lessonCursor.getColumnIndex("subjectId")
                    val columnIndexSubject = lessonCursor.getColumnIndex("subject")
                    val columnIndexCategory = lessonCursor.getColumnIndex("category")

                    if (columnIndexSubjectId != -1) {
                        do {
                            val subjectId = lessonCursor.getString(columnIndexSubjectId)
                            val subject = lessonCursor.getString(columnIndexSubject)
                            val category = lessonCursor.getString(columnIndexCategory)
                            val subjectData = listOf(subjectId, subject, category)
                            subjectList.add(subjectData.toString())
                        } while (lessonCursor.moveToNext())
                    } else {
                    }
                } else {
                    noSubjects()
                }

                lessonCursor.close()


                if (subjectList.isEmpty()) {
                    noSubjects()
                }
                else {
                    val quizData = listOf("000000", "Quiz", "")
                    subjectList.add(quizData.toString())

                }

                loadSubjects()

                mySwipeRefreshLayout.isRefreshing = false
            }
            else {
                Toast.makeText(this@HomeActivity, "Failed to refresh contents", Toast.LENGTH_SHORT).show()
                mySwipeRefreshLayout.isRefreshing = false
            }


        }

        settings = findViewById(R.id.button)
        achievements = findViewById(R.id.btnAchievements)
        if (userType == "learner") {
            txtTeacher.text = "$firstName $lastName"

            settings.setImageResource(R.drawable.logout)

            settings.setOnClickListener {
                val intent = Intent(this@HomeActivity, UserOption::class.java)
                startActivity(intent)
                finish()
            }
        }
        else {
            achievements.visibility = View.GONE
            settings.setImageResource(R.drawable.settings)

            txtTeacher.text = "Teacher $firstName $lastName"

            settings.setOnClickListener {
                val intent = Intent(this@HomeActivity, SettingsActivity::class.java)
                startActivity(intent)
                finish()
            }
        }

        subjectList.clear()

        achievements = findViewById(R.id.btnAchievements)
        achievements.setOnClickListener {
            val intent = Intent(this@HomeActivity, AchievementsActivity::class.java)
            startActivity(intent)
            finish()
        }

        val tableName = "tbl_subject"
        val column = "subject"

        db = openOrCreateDatabase("tagakauloDB", Context.MODE_PRIVATE, null)
        val checkQuery = "SELECT * FROM $tableName WHERE addedBy = ? OR addedBy = ? ORDER BY $column ASC"
        val lessonCursor: Cursor = db.rawQuery(checkQuery, arrayOf(adviser, userId))

        if (lessonCursor.moveToFirst()) {
            val columnIndexSubjectId = lessonCursor.getColumnIndex("subjectId")
            val columnIndexSubject = lessonCursor.getColumnIndex("subject")
            val columnIndexCategory = lessonCursor.getColumnIndex("category")

            if (columnIndexSubjectId != -1) {
                do {
                    val subjectId = lessonCursor.getString(columnIndexSubjectId)
                    val subject = lessonCursor.getString(columnIndexSubject)
                    val category = lessonCursor.getString(columnIndexCategory)
                    val subjectData = listOf(subjectId, subject, category)
                    subjectList.add(subjectData.toString())
                } while (lessonCursor.moveToNext())
            } else {
            }
        } else {
            noSubjects()
        }

        lessonCursor.close()


        if (subjectList.isEmpty()) {
            noSubjects()
        }
        else {
            val quizData = listOf("000000", "Quiz", "")
            subjectList.add(quizData.toString())

        }
        loadSubjects()

    }
    private fun getTotalProgress(subjectId: String) {

        initialProgress = 0
        finalProgress = 0

        val tableName1 = "tbl_topic"
        val topicsArray = ArrayList<String>()

        val column = "topic"
        val checkQuery1 = "SELECT * FROM $tableName1 WHERE subjectRef = ? ORDER BY $column ASC"
        val topicArray = arrayOf(subjectId)
        val topicCursor: Cursor = db.rawQuery(checkQuery1, topicArray)
        if (topicCursor.moveToFirst()) {
            val columnIndexTopicId = topicCursor.getColumnIndex("topicId")

            if (columnIndexTopicId != -1) {
                do {
                    val topicId = topicCursor.getString(columnIndexTopicId)
                    initialProgress += getTopicProgress(subjectId, topicId)

                    topicsArray.add(topicId)

                    finalProgress += 1
                } while (topicCursor.moveToNext())
            }
            else {

            }
        }
        topicCursor.close()
    }
    private fun getTotalQuizProgress(): Int{
        var finalQuizProgress = 0

        val tableName1 = "tbl_quiz"

        val userType = sharedPreferences.getString("userType", "").toString()
        val adviser = if (userType == "learner") sharedPreferences.getString("adviser", "") else sharedPreferences.getString("userId", "")
        val checkQuery1 = "SELECT COUNT(DISTINCT(quizName)) AS count FROM $tableName1 WHERE addedBy = ?"
        val topicCursor: Cursor = db.rawQuery(checkQuery1, arrayOf(adviser))
        if (topicCursor.moveToFirst()) {
            val columnIndexTopicId = topicCursor.getColumnIndex("count")

            if (columnIndexTopicId != -1) {
                val quizCount = topicCursor.getInt(columnIndexTopicId)
                finalQuizProgress = quizCount
            }
            topicCursor.close()
            return finalQuizProgress
        }
        else {
            topicCursor.close()
            return finalQuizProgress
        }

    }
    private fun getProgress(): Int {
        val userType = sharedPreferences.getString("userType", "").toString()
        val userId = sharedPreferences.getString("userId", "").toString()
        val firstName = sharedPreferences.getString("firstName", "").toString()
        val lastName = sharedPreferences.getString("lastName", "").toString()

        if (userType == "learner") {
            val studentName = lastName.uppercase() + ", " + firstName.lowercase().replaceFirstChar { it.uppercaseChar() }

            val tableName2 = "tbl_learnerQuizProgress"
            val createTable = "CREATE TABLE IF NOT EXISTS $tableName2 (studentName TEXT, subjectId TEXT, quizName TEXT, status TEXT, score REAL, attempts INT, dateTaken TEXT);"
            db.execSQL(createTable)
            val checkQuery2 = "SELECT COUNT(DISTINCT(quizName)) AS count FROM $tableName2 WHERE studentName = ?"
            val progressQuizArray = arrayOf(studentName)
            val progressQuizCursor: Cursor = db.rawQuery(checkQuery2, progressQuizArray)
            if (progressQuizCursor.moveToFirst()) {
                val studentColumnIndex = progressQuizCursor.getColumnIndex("count")

                if (studentColumnIndex != -1) {
                    val learnerCount = progressQuizCursor.getInt(studentColumnIndex)

                    return learnerCount
                }
                return 1
            }
            else {
                return 0
            }
            progressQuizCursor.close()
        }
        else {
            val tableName2 = "tbl_teacherQuizProgress"
            val checkQuery2 = "SELECT * FROM $tableName2 WHERE teacherId = ?"
            val progressQuizArray = arrayOf(userId)
            val progressQuizCursor: Cursor = db.rawQuery(checkQuery2, progressQuizArray)
            if (progressQuizCursor.moveToFirst()) {
                return 1
            }
            else {
                return 0
            }
            progressQuizCursor.close()
        }
    }
    private fun getTopicProgress(subjectId: String, topicId: String): Int {
        val userType = sharedPreferences.getString("userType", "")
        val userId = sharedPreferences.getString("userId", "")
        val firstName = sharedPreferences.getString("firstName", "")
        val lastName = sharedPreferences.getString("lastName", "")

        if (userType == "learner") {
            val tableName1 = "tbl_learnerProgress"

            val checkQuery1 = "SELECT * FROM $tableName1 WHERE studentName = ? AND subjectId = ? AND topicId = ? AND status = ?"
            val studentName = lastName.toString().uppercase() + ", " + firstName.toString().lowercase().replaceFirstChar { it.uppercaseChar() }
            val progressArray2 = arrayOf(studentName, subjectId, topicId, "1")
            val progressCursor: Cursor = db.rawQuery(checkQuery1, progressArray2)
            if (progressCursor.moveToFirst()) {
                return 1
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
                    return 1
                } while (progressCursor.moveToNext())
            }
            else {
                return 0
            }

            progressCursor.close()
        }
    }
    private fun loadSubjects() {
        val parentLayout: GridLayout = findViewById(R.id.parentLayout)

        val inflater = LayoutInflater.from(this@HomeActivity)

        for (subject in subjectList.indices) {
            val subjectLayout = inflater.inflate(R.layout.layout_home, parentLayout, false) as RelativeLayout
            val imgSubject = subjectLayout.findViewById<ImageView>(R.id.imgSubject)
            val txtSubject = subjectLayout.findViewById<TextView>(R.id.txtSubject)
            val txtCategory = subjectLayout.findViewById<TextView>(R.id.txtCategory)
            val progressBar = subjectLayout.findViewById<ProgressBar>(R.id.progressBar)
            val txtProgress = subjectLayout.findViewById<TextView>(R.id.txtProgress)

            val topicItem = subjectList[subject]
            val cleanedQuizList = topicItem.replace("[", "").replace("]", "")
            var elements = cleanedQuizList.split(", ")

            val subjectId = elements[0]
            val subjectName = elements[1]
            val category = elements[2]

            txtCategory.text = category
            getTotalProgress(subjectId)

            var bitmap = getVideoImage(subjectId)
            if (bitmap != null.toString()) {
                Glide.with(this@HomeActivity)
                    .load(bitmap)
                    .into(imgSubject)
            }
            else {
                imgSubject.setImageResource(R.drawable.logo)
            }
            txtSubject.text = subjectName

            val uniqueId = View.generateViewId()
            subjectLayout.id = uniqueId
            parentLayout.addView(subjectLayout)

            when (subjectName) {
                /*"Games" -> {
                    progressBar.visibility = View.GONE
                    txtProgress.visibility = View.GONE
                    subjectLayout.setOnClickListener {
                        getGames()
                    }
                }*/
                "Quiz" -> {
                    val userType = sharedPreferences.getString("userType", "")
                    if (userType == "learner") {
                        val adviser = sharedPreferences.getString("adviser", "")
                        initialProgress = getProgress()
                        var currentProgress = initialProgress.toFloat() / getTotalQuizProgress().toFloat()
                        var percentageProgress = (currentProgress * 100).toInt()
                        progressBar.progress = percentageProgress

                        txtProgress.text = percentageProgress.toString() + "%"

                        subjectLayout.setOnClickListener {
                            getQuiz(adviser.toString())
                        }
                    }
                    else {
                        val userId = sharedPreferences.getString("userId", "")
                        initialProgress = getProgress()
                        var currentProgress = initialProgress.toFloat() / getTotalQuizProgress().toFloat()
                        var percentageProgress = (currentProgress * 100).toInt()
                        progressBar.progress = percentageProgress

                        txtProgress.text = percentageProgress.toString() + "%"

                        subjectLayout.setOnClickListener {
                            getQuiz(userId.toString())
                        }
                    }
                }
                else -> {
                    progressBar.visibility = View.VISIBLE
                    txtProgress.visibility = View.VISIBLE

                    var currentProgress = initialProgress.toFloat() / finalProgress.toFloat()
                    var percentageProgress = (currentProgress * 100).toInt()
                    progressBar.progress = percentageProgress

                    txtProgress.text = percentageProgress.toString() + "%"

                    subjectLayout.setOnClickListener {
                        noTopic(subjectId, subjectName)
                    }
                }
            }
        }
    }
    private fun noSubjects() {
        Toast.makeText(this@HomeActivity, "There are no subjects found", Toast.LENGTH_SHORT).show()
    }

    private fun noTopic(subjectId: String, subject: String) {
        val tableName = "tbl_topic"
        val checkQuery = "SELECT * FROM $tableName WHERE subjectRef LIKE ?"
        val topicArray = arrayOf(subjectId)
        val topicCursor: Cursor = db.rawQuery(checkQuery, topicArray)

        if (topicCursor.count > 0) {
            val intent = Intent(this@HomeActivity, LessonActivity::class.java)
            intent.putExtra("subjectId", subjectId)
            intent.putExtra("subject", subject)
            startActivity(intent)
        }
        else {
            Toast.makeText(this@HomeActivity, "No topics found for  this subject", Toast.LENGTH_SHORT).show()
        }
        topicCursor.close()
    }
    private fun getVideoImage(subjectId: String): String {
        var mediaPath = ""
        var image = false
        var video = false
        val tableName = "tbl_topic"
        val query = "SELECT videoPath, imagePath FROM $tableName WHERE subjectRef = ?"
        val videoCursor: Cursor = db.rawQuery(query, arrayOf(subjectId))
        if (videoCursor.moveToFirst()) {
            val videoPathColumnIndex = videoCursor.getColumnIndex("videoPath")
            val imagePathColumnIndex = videoCursor.getColumnIndex("imagePath")
            if (videoPathColumnIndex != -1 || imagePathColumnIndex != -1) {
                val videoPath = videoCursor.getString(videoPathColumnIndex)
                val imagePath = videoCursor.getString(imagePathColumnIndex)

                if (videoPath != "") {
                    mediaPath = videoPath
                    video = true
                    image = false
                }
                else if(imagePath != "") {
                    mediaPath = imagePath
                    image = true
                    video = false
                }
            }
        }
        if (video) {
            return null.toString()
        }
        else if (image) {
            return mediaPath
        }
        else {
            return null.toString()
        }
    }
    /*private fun getGames() {
        val tableName = "tbl_games"
        val checkQuery = "SELECT * FROM $tableName"
        val gameCursor: Cursor = db.rawQuery(checkQuery, null)
        if (gameCursor.count > 0) {
            val intent = Intent(this@HomeActivity, GameActivity::class.java)
            startActivity(intent)
        }
        else {
            Toast.makeText(this@HomeActivity, "No games available", Toast.LENGTH_SHORT).show()
        }
        gameCursor.close()
    }*/
    private fun getQuiz(teacherId: String) {
        val tableName = "tbl_quiz"
        var checkQuery = "SELECT DISTINCT lessonName FROM $tableName WHERE addedBy = ?"
        var quizCursor: Cursor = db.rawQuery(checkQuery, arrayOf(teacherId))
        if (quizCursor.count > 0) {
            val intent = Intent(this@HomeActivity, QuizzesActivity::class.java)
            startActivity(intent)
        }
        else {
            Toast.makeText(this@HomeActivity, "No quiz available", Toast.LENGTH_SHORT).show()
        }
        quizCursor.close()
    }
    override fun onBackPressed() {
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish()
    }
}