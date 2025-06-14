@file:Suppress("DEPRECATION")
package com.example.tagakaulolearningapp
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteQuery
import android.media.MediaPlayer
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.upstream.HttpDataSource
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import okhttp3.*
import org.json.JSONArray
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

open class ConnectivityClass(): AppCompatActivity() {
    private lateinit var httpDataSourceFactory: HttpDataSource.Factory
    private lateinit var defaultDataSourceFactory: DefaultDataSourceFactory
    private lateinit var cacheDataSourceFactory: DataSource.Factory
    private lateinit var simpleExoPlayer: SimpleExoPlayer
    private lateinit var connectionSP: String
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var db: SQLiteDatabase
    private lateinit var state: String
    private val checkIntervalMillis = 10000 // Check every 10 seconds (adjust as needed)
    private val handler = Handler(Looper.getMainLooper())
    private var count = 0
    private var defState = false

    init {
        state = ""
    }

    open fun checkAndShowToast(context: Context, state: String): Boolean {
        val application: TagakauloLearningApp = getApplicationContext() as TagakauloLearningApp
        sharedPreferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
        db = openOrCreateDatabase("tagakauloDB", Context.MODE_PRIVATE, null)
        connectionSP = sharedPreferences.getString("connection", "").toString()
        val hasConnection = isInternetConnected(context)
        if (hasConnection) {
            internetChangedState(true, state)
            return true
        } else {
            return false
        }
    }

    open fun isInternetConnected(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }

    private fun internetChangedState(newState: Boolean, state: String) {
        this.state = state
        val tables = arrayListOf(
            "tbl_batch",
            "tbl_subject",
            "tbl_topic",
            "tbl_language",
            "tbl_quiz",
            "tbl_learner",
            "tbl_learnerProgress",
            "tbl_learnerQuizProgress",
            "tbl_grading",
            "tbl_games",
            "tbl_teacher"
        )

        if (newState) {
            for (i in tables.indices) {
                var tableName = tables[i]

                val tableChecker = "SELECT name FROM sqlite_master WHERE type='table' AND name = ?"
                val tableArray = arrayOf(tableName)
                val tableCursor: Cursor = db.rawQuery(tableChecker, tableArray)

                if (tableCursor.moveToFirst()) {
                    joinChecker(tableName)
                } else {
                    loadResources(tableName)
                }
            }
        }
    }

    private fun loadResources(tableName: String) {
        val client = OkHttpClient()
        val baseUrl = ConnectionClass()
        var fileAccess = "getLesson.php"
        var url = baseUrl + fileAccess
        var request = Request.Builder().url(url).build()
        var taskInProgress = false

        when (tableName) {
            "tbl_batch" -> {
                fileAccess = "getTeacherClass.php"
                url = baseUrl + fileAccess
                request = Request.Builder().url(url).build()
                client.newCall(request).enqueue(object : Callback {
                    override fun onResponse(call: Call, response: Response) {
                        val responseBody = response.body?.string()
                        val jsonArray = JSONArray(responseBody)

                        if (jsonArray.length() > 0) {
                            for (i in 0 until jsonArray.length()) {
                                val item = jsonArray.getJSONObject(i)
                                val batchId = item.getString("classId")
                                val batch = item.getString("className")
                                val teacherId = item.getString("teacherId")

                                runOnUiThread {
                                    insertBatch(batchId, batch, teacherId)
                                }
                            }
                        }
                        else {
                            runOnUiThread {
                                val tableNewName = "tbl_batch"
                                val createTable = "CREATE TABLE IF NOT EXISTS $tableNewName (batchId TEXT, batch TEXT, teacherId TEXT)"
                                db.execSQL(createTable)
                            }
                        }

                        runOnUiThread {
                            
                        }
                    }
                    override fun onFailure(call: Call, e: IOException) {
                        runOnUiThread {
                            val tableNewName = "tbl_batch"
                            val createTable = "CREATE TABLE IF NOT EXISTS $tableNewName (batchId TEXT, batch TEXT, teacherId TEXT)"
                            db.execSQL(createTable)

                            
                        }
                    }

                })
            }
            "tbl_subject" -> {
                fileAccess = "getLesson.php"
                url = baseUrl + fileAccess
                request = Request.Builder().url(url).build()

                client.newCall(request).enqueue(object : Callback {
                    override fun onResponse(call: Call, response: Response) {
                        val responseBody = response.body?.string()
                        val jsonArray = JSONArray(responseBody)

                        if (jsonArray.length() > 0) {
                            for (i in 0 until jsonArray.length()) {
                                val item = jsonArray.getJSONObject(i)
                                val lessonId = item.getString("subjectId")
                                val lesson = item.getString("subject")
                                val category = item.getString("category")
                                val addedBy = item.getString("addedBy")
                                runOnUiThread {
                                    insertLesson(category, lessonId, lesson.lowercase().replaceFirstChar { it.uppercaseChar() }, addedBy)
                                }
                            }
                        }
                        else {
                            runOnUiThread {
                                val tableName = "tbl_subject"
                                val createTable = "CREATE TABLE IF NOT EXISTS $tableName (subjectId TEXT, category TEXT, subject TEXT, addedBy TEXT)"
                                db.execSQL(createTable)
                            }
                        }

                        runOnUiThread {
                            
                        }
                    }
                    override fun onFailure(call: Call, e: java.io.IOException) {
                        runOnUiThread {
                            val tableName = "tbl_subject"
                            val createTable = "CREATE TABLE IF NOT EXISTS $tableName (subjectId TEXT, category TEXT, subject TEXT, addedBy TEXT)"
                            db.execSQL(createTable)

                            

                        }
                    }
                })
            }
            "tbl_topic" -> {
                fileAccess = "getTopic.php"
                url = baseUrl + fileAccess
                request = Request.Builder().url(url).build()
                client.newCall(request).enqueue(object : Callback {
                    override fun onResponse(call: Call, response: Response) {
                        val responseBody = response.body?.string()
                        val jsonArray = JSONArray(responseBody)

                        if (jsonArray.length() > 0) {
                            for (i in 0 until jsonArray.length()) {
                                val item = jsonArray.getJSONObject(i)

                                val topicId = item.getString("topicId")
                                val topic = item.getString("topic")
                                val topicSubjectRef = item.getString("subjectIdRef")

                                val imagePath = item.getString("imagePath")
                                val audioPath = item.getString("audioPath")
                                val videoPath = item.getString("videoPath")
                                val pdfPath = item.getString("pdfPath")

                                runOnUiThread {
                                    getImage(imagePath)
                                    insertTopic(topicId, topic.lowercase().replaceFirstChar { it.uppercaseChar() }, topicSubjectRef, getImage(imagePath), getVideo(videoPath), getAudio(audioPath), pdfPath)
                                }
                            }
                        }
                        else {
                            runOnUiThread {
                                val tableName = "tbl_topic"
                                val createTable = "CREATE TABLE IF NOT EXISTS $tableName (topicId TEXT, topic TEXT, subjectRef TEXT, imagePath TEXT, videoPath TEXT, audioPath TEXT, pdfPath TEXT)"
                                db.execSQL(createTable)
                            }
                        }
                        runOnUiThread {
                            
                        }
                    }
                    override fun onFailure(call: Call, e: java.io.IOException) {
                        runOnUiThread {
                            val tableName = "tbl_topic"
                            val createTable = "CREATE TABLE IF NOT EXISTS $tableName (topicId TEXT, topic TEXT, subjectRef TEXT, imagePath TEXT, videoPath TEXT, audioPath TEXT, pdfPath TEXT)"
                            db.execSQL(createTable)

                            
                        }
                    }


                })
            }
            "tbl_language" -> {
                fileAccess = "getLanguage.php"
                url = baseUrl + fileAccess
                request = Request.Builder().url(url).build()
                client.newCall(request).enqueue(object : Callback {
                    override fun onResponse(call: Call, response: Response) {
                        val responseBody = response.body?.string()
                        val jsonArray = JSONArray(responseBody)

                        if (jsonArray.length() > 0) {
                            for (i in 0 until jsonArray.length()) {
                                val item = jsonArray.getJSONObject(i)
                                val languageId = item.getString("languageId")
                                val kalagan = item.getString("kalagan")
                                val filipino = item.getString("filipino")
                                val english = item.getString("english")
                                val langTopicRef = item.getString("langTopicRef")

                                runOnUiThread {
                                    insertLanguage(languageId, kalagan.lowercase().replaceFirstChar { it.uppercaseChar() }, filipino.lowercase().replaceFirstChar { it.uppercaseChar() }, english.lowercase().replaceFirstChar { it.uppercaseChar() }, langTopicRef)
                                }
                            }
                        }
                        else {
                            runOnUiThread {
                                val tableName = "tbl_language"
                                val createTable = "CREATE TABLE IF NOT EXISTS $tableName (langId TEXT, kalagan TEXT, filipino TEXT, english TEXT, topicRef TEXT)"
                                db.execSQL(createTable)
                            }
                        }
                        runOnUiThread {
                            
                        }
                    }
                    override fun onFailure(call: Call, e: java.io.IOException) {
                        runOnUiThread {
                            val tableName = "tbl_language"
                            val createTable = "CREATE TABLE IF NOT EXISTS $tableName (langId TEXT, kalagan TEXT, filipino TEXT, english TEXT, topicRef TEXT)"
                            db.execSQL(createTable)

                            
                        }
                    }

                })
            }
            "tbl_quiz" -> {
                fileAccess = "getQuiz.php"
                url = baseUrl + fileAccess
                request = Request.Builder().url(url).build()
                client.newCall(request).enqueue(object : Callback {
                    override fun onResponse(call: Call, response: Response) {
                        val responseBody = response.body?.string()
                        val jsonArray = JSONArray(responseBody)

                        if (jsonArray.length() > 0) {
                            for (i in 0 until jsonArray.length()) {
                                val item = jsonArray.getJSONObject(i)
                                val quizId = item.getString("quizId")
                                val quizName = item.getString("quizName")
                                val question = item.getString("question")
                                val quizImg = item.getString("quizImg")
                                val selectionA = item.getString("selectionA")
                                val selectionB = item.getString("selectionB")
                                val selectionC = item.getString("selectionC")
                                val selectionD = item.getString("selectionD")
                                val score = item.getString("score")
                                val quizLessonIdRef = item.getString("quizLessonIdRef")
                                val quizLessonNameRef = item.getString("quizLessonNameRef")
                                val attempts = item.getString("attempts")
                                val addedBy = item.getString("addedByID")

                                runOnUiThread {
                                    insertQuiz(quizId, quizName, question, getImage(quizImg), selectionA, selectionB, selectionC, selectionD, score.toDouble(), quizLessonIdRef, quizLessonNameRef, attempts.toInt(), addedBy)
                                }
                            }
                        }
                        else {
                            runOnUiThread {
                                val tableName = "tbl_quiz"
                                val createTable = "CREATE TABLE IF NOT EXISTS $tableName (quizId TEXT, quizName TEXT, question TEXT, imagePath TEXT, selectionA TEXT, selectionB TEXT, selectionC TEXT, selectionD TEXT, score REAL, lessonId TEXT, lessonName TEXT, attempts INT, addedBy TEXT)"
                                db.execSQL(createTable)
                            }
                        }
                        runOnUiThread {
                            
                        }
                    }
                    override fun onFailure(call: Call, e: java.io.IOException) {
                        runOnUiThread {
                            val tableName = "tbl_quiz"
                            val createTable = "CREATE TABLE IF NOT EXISTS $tableName (quizId TEXT, quizName TEXT, question TEXT, imagePath TEXT, selectionA TEXT, selectionB TEXT, selectionC TEXT, selectionD TEXT, score REAL, lessonId TEXT, lessonName TEXT, attempts INT, addedBy TEXT)"
                            db.execSQL(createTable)

                            
                        }
                    }


                })
            }
            "tbl_games" -> {
                fileAccess = "getGames.php"
                url = baseUrl + fileAccess
                request = Request.Builder().url(url).build()
                client.newCall(request).enqueue(object : Callback {
                    override fun onResponse(call: Call, response: Response) {
                        val responseBody = response.body?.string()
                        val jsonArray = JSONArray(responseBody)

                        if (jsonArray.length() > 0) {
                            for (i in 0 until jsonArray.length()) {
                                val item = jsonArray.getJSONObject(i)
                                val gameId = item.getString("gameId")
                                val answer = item.getString("answer")
                                val question = item.getString("question")
                                val hint = item.getString("hint")
                                val type = item.getString("type")
                                val imagePath1 = item.getString("imagePath1")
                                val imagePath2 = item.getString("imagePath2")
                                val audioPath = item.getString("audioPath")

                                runOnUiThread {
                                    insertGames(gameId, answer, question, hint, type, getImage(imagePath1), getImage(imagePath2), audioPath)
                                }
                            }
                        }
                        else {
                            runOnUiThread {
                                val tableName = "tbl_games"
                                val createTable = "CREATE TABLE IF NOT EXISTS $tableName (gameId TEXT, answer TEXT, question TEXT, hint TEXT, type TEXT, imagePath1 TEXT, imagePath2 TEXT, audioPath TEXT)"
                                db.execSQL(createTable)
                            }
                        }
                        runOnUiThread {
                            
                        }
                    }
                    override fun onFailure(call: Call, e: java.io.IOException) {
                        runOnUiThread {
                            val tableName = "tbl_games"
                            val createTable = "CREATE TABLE IF NOT EXISTS $tableName (gameId TEXT, answer TEXT, question TEXT, hint TEXT, type TEXT, imagePath1 TEXT, imagePath2 TEXT, audioPath TEXT)"
                            db.execSQL(createTable)

                            
                        }
                    }

                })
            }
            "tbl_learner" -> {
                fileAccess = "getLearner.php"
                url = baseUrl + fileAccess
                request = Request.Builder().url(url).build()
                client.newCall(request).enqueue(object : Callback {
                    override fun onResponse(call: Call, response: Response) {
                        val responseBody = response.body?.string()
                        val jsonArray = JSONArray(responseBody)

                        if (jsonArray.length() > 0) {
                            for (i in 0 until jsonArray.length()) {
                                val item = jsonArray.getJSONObject(i)
                                val learnerId = item.getString("learnerId")
                                val firstName = item.getString("firstName")
                                val lastName = item.getString("lastName")
                                val className = item.getString("classId")
                                val addedBy = item.getString("addedBy")

                                runOnUiThread {
                                    insertLearner(learnerId, firstName.lowercase().replaceFirstChar { it.uppercaseChar() }, lastName.lowercase().replaceFirstChar { it.uppercaseChar() }, className, addedBy)
                                }
                            }
                        }
                        else {
                            runOnUiThread {
                                val tableName = "tbl_learner"
                                val createTable = "CREATE TABLE IF NOT EXISTS $tableName (learnerId TEXT, firstName TEXT, lastName TEXT, className TEXT, addedBy TEXT)"
                                db.execSQL(createTable)
                            }
                        }

                        runOnUiThread {
                            
                        }
                    }
                    override fun onFailure(call: Call, e: java.io.IOException) {
                        runOnUiThread {
                            val tableName = "tbl_learner"
                            val createTable = "CREATE TABLE IF NOT EXISTS $tableName (learnerId TEXT, firstName TEXT, lastName TEXT, className TEXT, addedBy TEXT)"
                            db.execSQL(createTable)

                            
                        }
                    }

                })
            }
            "tbl_learnerProgress" -> {
                fileAccess = "getLearnerProgress.php"
                url = baseUrl + fileAccess
                request = Request.Builder().url(url).build()
                client.newCall(request).enqueue(object : Callback {
                    override fun onResponse(call: Call, response: Response) {
                        val responseBody = response.body?.string()
                        val jsonArray = JSONArray(responseBody)

                        if (jsonArray.length() > 0) {
                            runOnUiThread {
                                val tableName = "tbl_learnerProgress"
                                val createTable = "CREATE TABLE IF NOT EXISTS $tableName (studentName TEXT, subjectId TEXT, topicId TEXT, status TEXT);"
                                db.execSQL(createTable)
                            }

                            for (i in 0 until jsonArray.length()) {
                                val item = jsonArray.getJSONObject(i)
                                val studentName = item.getString("studentName")
                                val subjectId = item.getString("subjectId")
                                val topicId = item.getString("topicId")
                                val status = item.getString("status")

                                runOnUiThread {
                                    insertLearnerProgress(studentName, subjectId, topicId, status)
                                }
                            }
                        }
                        else {
                            runOnUiThread {
                                val tableName = "tbl_learnerProgress"
                                val createTable = "CREATE TABLE IF NOT EXISTS $tableName (studentName TEXT, subjectId TEXT, topicId TEXT, status TEXT);"
                                db.execSQL(createTable)
                            }
                        }
                        runOnUiThread {
                            
                        }
                    }
                    override fun onFailure(call: Call, e: java.io.IOException) {
                        runOnUiThread {
                            val tableName = "tbl_learnerProgress"
                            val createTable = "CREATE TABLE IF NOT EXISTS $tableName (studentName TEXT, subjectId TEXT, topicId TEXT, status TEXT);"
                            db.execSQL(createTable)

                            
                        }
                    }
                })
            }
            "tbl_learnerQuizProgress" -> {
                fileAccess = "getLearnerQuiz.php"
                url = baseUrl + fileAccess
                request = Request.Builder().url(url).build()
                client.newCall(request).enqueue(object : Callback {
                    override fun onResponse(call: Call, response: Response) {
                        val responseBody = response.body?.string()
                        val jsonArray = JSONArray(responseBody)

                        if (jsonArray.length() > 0) {
                            for (i in 0 until jsonArray.length()) {
                                val item = jsonArray.getJSONObject(i)
                                val studentName = item.getString("studentName")
                                val subjectId = item.getString("subjectId")
                                val quizName = item.getString("quizName")
                                val score = item.getString("score")
                                val attempt = item.getString("attempt")
                                val dateTaken = item.getString("dateTaken")

                                runOnUiThread {
                                    insertLearnerQuizProgress(studentName, subjectId, quizName, score.toDouble(), attempt.toInt(), dateTaken)
                                }
                            }
                        }
                        else {
                            runOnUiThread {
                                val tableName = "tl_learnerQuizProgress"
                                val createTable = "CREATE TABLE IF NOT EXISTS $tableName (studentName TEXT, subjectId TEXT, quizName TEXT, status TEXT, score REAL, attempts INT, dateTaken TEXT);"
                                db.execSQL(createTable)
                            }
                        }
                        runOnUiThread {
                            
                        }
                    }
                    override fun onFailure(call: Call, e: java.io.IOException) {
                        runOnUiThread {
                            val tableName = "tl_learnerQuizProgress"
                            val createTable = "CREATE TABLE IF NOT EXISTS $tableName (studentName TEXT, subjectId TEXT, quizName TEXT, status TEXT, score REAL, attempts INT, dateTaken TEXT);"
                            db.execSQL(createTable)

                            
                        }
                    }
                })
            }
            "tbl_grading" -> {
                fileAccess = "getLearnerQuiz.php"
                url = baseUrl + fileAccess
                request = Request.Builder().url(url).build()
                client.newCall(request).enqueue(object : Callback {
                    override fun onResponse(call: Call, response: Response) {
                        val responseBody = response.body?.string()
                        val jsonArray = JSONArray(responseBody)

                        if (jsonArray.length() > 0) {
                            for (i in 0 until jsonArray.length()) {
                                val item = jsonArray.getJSONObject(i)
                                val studentName = item.getString("studentName")
                                val subjectId = item.getString("subjectId")
                                val score = item.getString("score")

                                runOnUiThread {
                                    insertGradeRecords(studentName, subjectId, score.toDouble())
                                }
                            }
                        }
                        else {
                            runOnUiThread {
                                val tableNewName = "tbl_grading"
                                val createTable = "CREATE TABLE IF NOT EXISTS $tableNewName (batch TEXT, studentName TEXT, quarter TEXT, record TEXT, subject TEXT, score REAL, addedBy TEXT);"
                                db.execSQL(createTable)
                            }
                        }
                        runOnUiThread {
                            
                        }
                    }
                    override fun onFailure(call: Call, e: java.io.IOException) {
                        runOnUiThread {
                            val tableNewName = "tbl_grading"
                            val createTable = "CREATE TABLE IF NOT EXISTS $tableNewName (batch TEXT, studentName TEXT, quarter TEXT, record TEXT, subject TEXT, score REAL, addedBy TEXT);"
                            db.execSQL(createTable)

                            
                        }
                    }


                })
            }
            "tbl_teacher" -> {
                fileAccess = "getTeacher.php"
                url = baseUrl + fileAccess
                request = Request.Builder().url(url).build()
                client.newCall(request).enqueue(object : Callback {
                    override fun onResponse(call: Call, response: Response) {
                        val responseBody = response.body?.string()
                        val jsonArray = JSONArray(responseBody)

                        if (jsonArray.length() > 0) {
                            for (i in 0 until jsonArray.length()) {
                                val item = jsonArray.getJSONObject(i)
                                val teacherId = item.getString("teacherId")
                                val firstName = item.getString("firstName")
                                val lastName = item.getString("lastName")
                                val password = item.getString("password")

                                runOnUiThread {
                                    insertTeacher(teacherId, firstName.lowercase().replaceFirstChar { it.uppercaseChar() }, lastName.lowercase().replaceFirstChar { it.uppercaseChar() }, password)
                                }
                            }
                        }
                        else {
                            val tableName = "tbl_teacher"
                            val createTable = "CREATE TABLE IF NOT EXISTS $tableName (teacherId TEXT, firstName TEXT, lastName TEXT, password TEXT)"
                            db.execSQL(createTable)
                        }

                        runOnUiThread {
                            taskInProgress = true

                            if (taskInProgress) {
                                Toast.makeText(this@ConnectivityClass, "You are back online", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(this@ConnectivityClass, "Failed to download resources", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    override fun onFailure(call: Call, e: java.io.IOException) {
                        runOnUiThread {
                            getLesson(100)
                            getTopic(100)
                            getLanguage(100)
                            getQuiz(100)
                            getGame(100)
                            getBatch(100)
                            getTeacher(100)

                            val tableName = "tbl_teacher"
                            val createTable = "CREATE TABLE IF NOT EXISTS $tableName (teacherId TEXT, firstName TEXT, lastName TEXT, password TEXT)"
                            db.execSQL(createTable)

                            taskInProgress = true

                            if (taskInProgress) {
                                Toast.makeText(this@ConnectivityClass, "Failed to download resources", Toast.LENGTH_SHORT).show()
                            }
                            else {
                                Toast.makeText(this@ConnectivityClass, "Failed to download resources", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                })
            }
        }

    }
    private fun joinChecker(tableName: String) {
        val client = OkHttpClient()
        val baseUrl = ConnectionClass()
        var fileAccess: String
        var url: String
        var request: Request
        var taskInProgress = false
        when (tableName) {
            "tbl_batch" -> {
                fileAccess = "getTeacherClass.php"
                url = baseUrl + fileAccess
                request = Request.Builder().url(url).build()
                
                client.newCall(request).enqueue(object : Callback {
                    override fun onResponse(call: Call, response: Response) {
                        
                        val responseBody = response.body?.string()
                        val jsonArray = JSONArray(responseBody)

                        runOnUiThread {
                            val tableNewName = "tbl_batch"
                            val delTable = "DROP TABLE IF EXISTS $tableNewName"
                            db.execSQL(delTable)
                        }
                        if (jsonArray.length() > 0) {
                            for (i in 0 until jsonArray.length()) {
                                val item = jsonArray.getJSONObject(i)
                                val batchId = item.getString("classId")
                                val batch = item.getString("className")
                                val teacherId = item.getString("teacherId")
                                runOnUiThread {
                                    insertBatch(batchId, batch, teacherId)
                                }
                            }
                        }
                        else {
                            runOnUiThread {
                                val tableName = "tbl_batch"
                                val createTable = "CREATE TABLE IF NOT EXISTS $tableName (batchId TEXT, batch TEXT, teacherId TEXT)"
                                db.execSQL(createTable)
                            }
                        }
                        runOnUiThread {
                            
                        }
                    }
                    override fun onFailure(call: Call, e: IOException) {
                        runOnUiThread {
                            val tableName = "tbl_batch"
                            val createTable = "CREATE TABLE IF NOT EXISTS $tableName (batchId TEXT, batch TEXT, teacherId TEXT)"
                            db.execSQL(createTable)

                            
                        }
                    }
                })
            }
            "tbl_subject" -> {
                fileAccess = "getLesson.php"
                url = baseUrl + fileAccess
                request = Request.Builder().url(url).build()

                client.newCall(request).enqueue(object : Callback {
                    override fun onResponse(call: Call, response: Response) {
                        
                        val responseBody = response.body?.string()
                        val jsonArray = JSONArray(responseBody)

                        runOnUiThread {
                            val tableName = "tbl_subject"
                            val delQuery = "DROP TABLE IF EXISTS $tableName"
                            db.execSQL(delQuery)
                        }

                        if (jsonArray.length() > 0) {
                            for (i in 0 until jsonArray.length()) {
                                val item = jsonArray.getJSONObject(i)
                                val lessonId = item.getString("subjectId")
                                val lesson = item.getString("subject")
                                val category = item.getString("category")
                                val addedBy = item.getString("addedBy")

                                runOnUiThread {
                                    insertLesson(category, lessonId, lesson.lowercase().replaceFirstChar { it.uppercaseChar() }, addedBy)
                                }
                            }
                        }
                        else {
                            runOnUiThread {
                                val tableName = "tbl_subject"
                                val createTable = "CREATE TABLE IF NOT EXISTS $tableName (subjectId TEXT, category TEXT, subject TEXT, addedBy TEXT)"
                                db.execSQL(createTable)
                            }
                        }
                        runOnUiThread {
                            
                        }
                    }
                    override fun onFailure(call: Call, e: IOException) {
                        runOnUiThread {
                            val tableName = "tbl_subject"
                            val createTable = "CREATE TABLE IF NOT EXISTS $tableName (subjectId TEXT, category TEXT, subject TEXT, addedBy TEXT)"
                            db.execSQL(createTable)

                            
                        }
                    }
                })
            }
            "tbl_topic" -> {
                fileAccess = "getTopic.php"
                url = baseUrl + fileAccess
                request = Request.Builder().url(url).build()
                client.newCall(request).enqueue(object : Callback {
                    override fun onResponse(call: Call, response: Response) {
                        
                        val responseBody = response.body?.string()
                        val jsonArray = JSONArray(responseBody)

                        runOnUiThread {
                            val tableName = "tbl_topic"
                            val delQuery = "DROP TABLE IF EXISTS $tableName"
                            db.execSQL(delQuery)
                        }

                        if (jsonArray.length() > 0) {
                            for (i in 0 until jsonArray.length()) {
                                val item = jsonArray.getJSONObject(i)
                                val topicId = item.getString("topicId")
                                val topic = item.getString("topic")
                                val subjectRef = item.getString("subjectIdRef")
                                val imagePath = item.getString("imagePath")
                                val videoPath = item.getString("videoPath")
                                val audioPath = item.getString("audioPath")
                                val pdfPath = item.getString("pdfPath")

                                runOnUiThread {
                                    insertTopic(topicId, topic.lowercase().replaceFirstChar { it.uppercaseChar() }, subjectRef, getImage(imagePath), getVideo(videoPath), getAudio(audioPath), pdfPath)
                                }
                            }
                        }
                        else {
                            runOnUiThread {
                                val tableName = "tbl_topic"
                                val createTable = "CREATE TABLE IF NOT EXISTS $tableName (topicId TEXT, topic TEXT, subjectRef TEXT, imagePath TEXT, videoPath TEXT, audioPath TEXT, pdfPath TEXT)"
                                db.execSQL(createTable)
                            }
                        }
                        runOnUiThread {
                            
                        }
                    }
                    override fun onFailure(call: Call, e: IOException) {
                        runOnUiThread {
                            val tableName = "tbl_topic"
                            val createTable = "CREATE TABLE IF NOT EXISTS $tableName (topicId TEXT, topic TEXT, subjectRef TEXT, imagePath TEXT, videoPath TEXT, audioPath TEXT, pdfPath TEXT)"
                            db.execSQL(createTable)

                            
                        }
                    }


                })
            }
            "tbl_language" -> {
                fileAccess = "getLanguage.php"
                url = baseUrl + fileAccess
                request = Request.Builder().url(url).build()
                
                client.newCall(request).enqueue(object : Callback {
                    override fun onResponse(call: Call, response: Response) {
                        
                        val responseBody = response.body?.string()
                        val jsonArray = JSONArray(responseBody)


                        runOnUiThread {
                            val tableName = "tbl_language"
                            val delQuery = "DROP TABLE IF EXISTS $tableName"
                            db.execSQL(delQuery)
                        }

                        if (jsonArray.length() > 0) {
                            for (i in 0 until jsonArray.length()) {
                                val item = jsonArray.getJSONObject(i)
                                val languageId = item.getString("languageId")
                                val kalagan = item.getString("kalagan")
                                val filipino = item.getString("filipino")
                                val english = item.getString("english")
                                val langTopicRef = item.getString("langTopicRef")

                                runOnUiThread {
                                    insertLanguage(languageId, kalagan.lowercase().replaceFirstChar { it.uppercaseChar() }, filipino.lowercase().replaceFirstChar { it.uppercaseChar() }, english.lowercase().replaceFirstChar { it.uppercaseChar() }, langTopicRef)
                                }
                            }
                        }
                        else {
                            runOnUiThread {
                                val tableName = "tbl_language"
                                val createTable = "CREATE TABLE IF NOT EXISTS $tableName (langId TEXT, kalagan TEXT, filipino TEXT, english TEXT, topicRef TEXT)"
                                db.execSQL(createTable)
                            }
                        }
                        runOnUiThread {
                            
                        }
                    }
                    override fun onFailure(call: Call, e: IOException) {
                        runOnUiThread {
                            val tableName = "tbl_language"
                            val createTable = "CREATE TABLE IF NOT EXISTS $tableName (langId TEXT, kalagan TEXT, filipino TEXT, english TEXT, topicRef TEXT)"
                            db.execSQL(createTable)

                            
                        }
                    }

                })
            }
            "tbl_quiz" -> {
                fileAccess = "getQuiz.php"
                url = baseUrl + fileAccess
                request = Request.Builder().url(url).build()
                
                client.newCall(request).enqueue(object : Callback {
                    override fun onResponse(call: Call, response: Response) {
                        
                        val responseBody = response.body?.string()
                        val jsonArray = JSONArray(responseBody)

                        runOnUiThread {
                            val tableName = "tbl_quiz"
                            val delQuery = "DROP TABLE IF EXISTS $tableName"
                            db.execSQL(delQuery)
                        }

                        if (jsonArray.length() > 0) {
                            for (i in 0 until jsonArray.length()) {
                                val item = jsonArray.getJSONObject(i)
                                val quizId = item.getString("quizId")
                                val quizName = item.getString("quizName")
                                val question = item.getString("question")
                                val quizImg = item.getString("quizImg")
                                val selectionA = item.getString("selectionA")
                                val selectionB = item.getString("selectionB")
                                val selectionC = item.getString("selectionC")
                                val selectionD = item.getString("selectionD")
                                val score = item.getString("score")
                                val quizLessonIdRef = item.getString("quizLessonIdRef")
                                val quizLessonNameRef = item.getString("quizLessonNameRef")
                                val attempts = item.getString("attempts")
                                val addedBy = item.getString("addedByID")

                                runOnUiThread {
                                    insertQuiz(quizId, quizName, question, getImage(quizImg), selectionA, selectionB, selectionC, selectionD, score.toDouble(), quizLessonIdRef, quizLessonNameRef, attempts.toInt(), addedBy)
                                }
                            }
                        }
                        else {
                            runOnUiThread {
                                val tableName = "tbl_quiz"
                                val createTable = "CREATE TABLE IF NOT EXISTS $tableName (quizId TEXT, quizName TEXT, question TEXT, imagePath TEXT, selectionA TEXT, selectionB TEXT, selectionC TEXT, selectionD TEXT, score REAL, lessonId TEXT, lessonName TEXT, attempts INT, addedBy TEXT)"
                                db.execSQL(createTable)
                            }
                        }
                        runOnUiThread {
                            
                        }
                    }
                    override fun onFailure(call: Call, e: IOException) {
                        runOnUiThread {
                            val tableName = "tbl_quiz"
                            val createTable = "CREATE TABLE IF NOT EXISTS $tableName (quizId TEXT, quizName TEXT, question TEXT, imagePath TEXT, selectionA TEXT, selectionB TEXT, selectionC TEXT, selectionD TEXT, score REAL, lessonId TEXT, lessonName TEXT, attempts INT, addedBy TEXT)"
                            db.execSQL(createTable)

                            
                        }
                    }


                })
                val tableNewName = "tbl_new_quiz"
                val delTable = "DROP TABLE IF EXISTS $tableNewName"
                db.execSQL(delTable)
            }
            "tbl_learner" -> {
                fileAccess = "getLearner.php"
                url = baseUrl + fileAccess
                request = Request.Builder().url(url).build()
                
                client.newCall(request).enqueue(object : Callback {
                    override fun onResponse(call: Call, response: Response) {
                        
                        val responseBody = response.body?.string()
                        val jsonArray = JSONArray(responseBody)

                        runOnUiThread {
                            val tableNewName1 = "tbl_learner"
                            val createTable1 = "DROP TABLE IF EXISTS $tableNewName1"
                            db.execSQL(createTable1)
                        }

                        if (jsonArray.length() > 0) {
                            for (i in 0 until jsonArray.length()) {
                                val item = jsonArray.getJSONObject(i)
                                val learnerId = item.getString("learnerId")
                                val firstName = item.getString("firstName")
                                val lastName = item.getString("lastName")
                                val className = item.getString("classId")
                                val addedBy = item.getString("addedBy")

                                runOnUiThread {
                                    insertLearner(learnerId, firstName.lowercase().replaceFirstChar { it.uppercaseChar() }, lastName.lowercase().replaceFirstChar { it.uppercaseChar() }, className, addedBy)
                                }
                            }
                        }
                        else {
                            runOnUiThread {
                                val tableName = "tbl_learner"
                                val createTable = "CREATE TABLE IF NOT EXISTS $tableName (learnerId TEXT, firstName TEXT, lastName TEXT, className TEXT, addedBy TEXT)"
                                db.execSQL(createTable)
                            }
                        }
                        runOnUiThread {
                            
                        }
                    }
                    override fun onFailure(call: Call, e: IOException) {
                        runOnUiThread {
                            val tableName = "tbl_learner"
                            val createTable = "CREATE TABLE IF NOT EXISTS $tableName (learnerId TEXT, firstName TEXT, lastName TEXT, className TEXT, addedBy TEXT)"
                            db.execSQL(createTable)

                            
                        }
                    }


                })
            }
            "tbl_learnerProgress" -> {
                fileAccess = "getLearnerProgress.php"
                url = baseUrl + fileAccess
                request = Request.Builder().url(url).build()
                
                client.newCall(request).enqueue(object : Callback {
                    override fun onResponse(call: Call, response: Response) {
                        
                        val responseBody = response.body?.string()
                        val jsonArray = JSONArray(responseBody)

                        runOnUiThread {
                            val tableName = "tbl_learnerProgress"
                            val delQuery = "DROP TABLE IF EXISTS $tableName"
                            db.execSQL(delQuery)
                        }

                        if (jsonArray.length() > 0) {
                            for (i in 0 until jsonArray.length()) {
                                val item = jsonArray.getJSONObject(i)
                                val studentName = item.getString("studentName")
                                val subjectId = item.getString("subjectId")
                                val topicId = item.getString("topicId")
                                val status = item.getString("status")

                                runOnUiThread {
                                    insertLearnerProgress(studentName, subjectId, topicId, status)
                                }
                            }
                        }
                        else {
                            runOnUiThread {
                                val tableName = "tbl_learnerProgress"
                                val createTable = "CREATE TABLE IF NOT EXISTS $tableName (studentName TEXT, subjectId TEXT, topicId TEXT, status TEXT);"
                                db.execSQL(createTable)
                            }
                        }

                        runOnUiThread {
                            
                        }
                    }
                    override fun onFailure(call: Call, e: java.io.IOException) {
                        runOnUiThread {
                            val tableName = "tbl_learnerProgress"
                            val createTable = "CREATE TABLE IF NOT EXISTS $tableName (studentName TEXT, subjectId TEXT, topicId TEXT, status TEXT);"
                            db.execSQL(createTable)

                            

                        }
                    }
                })
            }
            "tbl_learnerQuizProgress" -> {
                fileAccess = "getLearnerQuiz.php"
                url = baseUrl + fileAccess
                request = Request.Builder().url(url).build()
                
                client.newCall(request).enqueue(object : Callback {
                    override fun onResponse(call: Call, response: Response) {
                        
                        val responseBody = response.body?.string()
                        val jsonArray = JSONArray(responseBody)

                        runOnUiThread {
                            val tableName = "tbl_learnerQuizProgress"
                            val delQuery = "DROP TABLE IF EXISTS $tableName"
                            db.execSQL(delQuery)
                        }

                        if (jsonArray.length() > 0) {
                            for (i in 0 until jsonArray.length()) {
                                val item = jsonArray.getJSONObject(i)
                                val studentName = item.getString("studentName")
                                val subjectId = item.getString("subjectId")
                                val quizName = item.getString("quizName")
                                val score = item.getString("score")
                                val attempt = item.getString("attempt")
                                val dateTaken = item.getString("dateTaken")

                                runOnUiThread {
                                    insertLearnerQuizProgress(studentName, subjectId, quizName, score.toDouble(), attempt.toInt(), dateTaken)
                                }
                            }
                        }
                        else {
                            runOnUiThread {
                                val tableName = "tl_learnerQuizProgress"
                                val createTable = "CREATE TABLE IF NOT EXISTS $tableName (studentName TEXT, subjectId TEXT, quizName TEXT, status TEXT, score REAL, attempts INT, dateTaken TEXT);"
                                db.execSQL(createTable)
                            }
                        }
                        runOnUiThread {
                            
                        }
                    }
                    override fun onFailure(call: Call, e: java.io.IOException) {
                        runOnUiThread {
                            val tableName = "tl_learnerQuizProgress"
                            val createTable = "CREATE TABLE IF NOT EXISTS $tableName (studentName TEXT, subjectId TEXT, quizName TEXT, status TEXT, score REAL, attempts INT, dateTaken TEXT);"
                            db.execSQL(createTable)

                            
                        }
                    }
                })
                val tableNewName = "tbl_new_learnerQuizProgress"
                val delTable = "DROP TABLE IF EXISTS $tableNewName"
                db.execSQL(delTable)
            }
            "tbl_grading" -> {
                fileAccess = "getLearnerQuiz.php"
                url = baseUrl + fileAccess
                request = Request.Builder().url(url).build()
                
                client.newCall(request).enqueue(object : Callback {
                    override fun onResponse(call: Call, response: Response) {
                        
                        val responseBody = response.body?.string()
                        val jsonArray = JSONArray(responseBody)

                        runOnUiThread {
                            val tableName = "tbl_grading"
                            val delQuery = "DROP TABLE IF EXISTS $tableName"
                            db.execSQL(delQuery)
                        }

                        if (jsonArray.length() > 0) {
                            for (i in 0 until jsonArray.length()) {
                                val item = jsonArray.getJSONObject(i)
                                val studentName = item.getString("studentName")
                                val subjectId = item.getString("subjectId")
                                val quizName = item.getString("quizName")
                                val score = item.getString("score")
                                val attempt = item.getString("attempt")
                                val dateTaken = item.getString("dateTaken")

                                runOnUiThread {
                                    insertGradeRecords(studentName, subjectId, score.toDouble())
                                }
                            }
                        }
                        else {
                            runOnUiThread {
                                val tableNewName = "tbl_grading"
                                val createTable = "CREATE TABLE IF NOT EXISTS $tableNewName (batch TEXT, studentName TEXT, quarter TEXT, record TEXT, subject TEXT, score REAL, addedBy TEXT);"
                                db.execSQL(createTable)
                            }
                        }
                        runOnUiThread {
                            
                        }
                    }
                    override fun onFailure(call: Call, e: java.io.IOException) {
                        runOnUiThread {
                            val tableNewName = "tbl_grading"
                            val createTable = "CREATE TABLE IF NOT EXISTS $tableNewName (batch TEXT, studentName TEXT, quarter TEXT, record TEXT, subject TEXT, score REAL, addedBy TEXT);"
                            db.execSQL(createTable)

                            
                        }
                    }
                })
            }
            "tbl_games" -> {
                fileAccess = "getGames.php"
                url = baseUrl + fileAccess
                request = Request.Builder().url(url).build()
                
                client.newCall(request).enqueue(object : Callback {
                    override fun onResponse(call: Call, response: Response) {
                        
                        val responseBody = response.body?.string()
                        val jsonArray = JSONArray(responseBody)

                        runOnUiThread {
                            val tableName = "tbl_grading"
                            val delQuery = "DROP TABLE IF EXISTS $tableName"
                            db.execSQL(delQuery)
                        }

                        if (jsonArray.length() > 0) {
                            for (i in 0 until jsonArray.length()) {
                                val item = jsonArray.getJSONObject(i)
                                val gameId = item.getString("gameId")
                                val answer = item.getString("answer")
                                val question = item.getString("question")
                                val hint = item.getString("hint")
                                val type = item.getString("type")
                                val imagePath1 = item.getString("imagePath1")
                                val imagePath2 = item.getString("imagePath2")
                                val audioPath = item.getString("audioPath")

                                runOnUiThread {
                                    insertGames(gameId, answer, question, hint, type, getImage(imagePath1), getImage(imagePath2), audioPath)
                                }
                            }
                        }
                        else {
                            runOnUiThread {
                                val tableName = "tbl_games"
                                val createTable = "CREATE TABLE IF NOT EXISTS $tableName (gameId TEXT, answer TEXT, question TEXT, hint TEXT, type TEXT, imagePath1 TEXT, imagePath2 TEXT, audioPath TEXT)"
                                db.execSQL(createTable)
                            }
                        }
                        runOnUiThread {
                            
                        }
                    }
                    override fun onFailure(call: Call, e: IOException) {
                        runOnUiThread {
                            
                        }
                    }

                })
            }
            "tbl_teacher" -> {
                fileAccess = "getTeacher.php"
                url = baseUrl + fileAccess
                request = Request.Builder().url(url).build()
                
                client.newCall(request).enqueue(object : Callback {
                    override fun onResponse(call: Call, response: Response) {
                        
                        val responseBody = response.body?.string()
                        val jsonArray = JSONArray(responseBody)

                        runOnUiThread {
                            val tableName = "tbl_grading"
                            val delQuery = "DROP TABLE IF EXISTS $tableName"
                            db.execSQL(delQuery)
                        }

                        if (jsonArray.length() > 0) {
                            for (i in 0 until jsonArray.length()) {
                                val item = jsonArray.getJSONObject(i)
                                val teacherId = item.getString("teacherId")
                                val firstName = item.getString("firstName")
                                val lastName = item.getString("lastName")
                                val password = item.getString("password")

                                runOnUiThread {
                                    insertTeacher(teacherId, firstName.lowercase().replaceFirstChar { it.uppercaseChar() }, lastName.lowercase().replaceFirstChar { it.uppercaseChar() }, password)
                                }
                            }
                        }
                        else {
                            runOnUiThread {
                                val tableName = "tbl_teacher"
                                val createTable = "CREATE TABLE IF NOT EXISTS $tableName (teacherId TEXT, firstName TEXT, lastName TEXT, password TEXT)"
                                db.execSQL(createTable)
                            }
                        }

                        runOnUiThread {

                            taskInProgress = true
                            if (taskInProgress) {
                                Toast.makeText(this@ConnectivityClass, "You are back online", Toast.LENGTH_SHORT).show()
                                taskInProgress = true
                            } else {
                                Toast.makeText(this@ConnectivityClass, "Failed to download resources", Toast.LENGTH_SHORT).show()
                                taskInProgress = true
                            }
                        }
                    }
                    override fun onFailure(call: Call, e: IOException) {
                        runOnUiThread {
                            val tableName = "tbl_teacher"
                            val createTable = "CREATE TABLE IF NOT EXISTS $tableName (teacherId TEXT, firstName TEXT, lastName TEXT, password TEXT)"
                            db.execSQL(createTable)

                            getLesson(100)
                            getTopic(100)
                            getLanguage(100)
                            getQuiz(100)
                            getGame(100)
                            getBatch(100)
                            getTeacher(100)
                            
                            taskInProgress = true

                            if (taskInProgress) {
                                Toast.makeText(this@ConnectivityClass, "Failed to download resources", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }

                })
            }
        }
    }

    private fun getImage(mediaPath: String): String {
        if (mediaPath != "") {
            Glide.with(applicationContext)
                .load(mediaPath)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .preload()
            return mediaPath
        }
        else {
            Glide.with(applicationContext)
                .load(R.drawable.logo)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .preload()
            return ""
        }
    }
    private fun getVideo(mediaPath: String): String {
        if (mediaPath != "") {
            return mediaPath
        }
        else {
            return ""
        }
    }
    private fun getAudio(mediaPath: String): String{
        if (mediaPath != "") {
            val filename = mediaPath.substringAfterLast("/")
            val videoCacheFile = File(cacheDir, filename)
            if (videoCacheFile.exists()) {
                println("Audio file already exists.")
            }
            else {
                val client = OkHttpClient()
                val request = Request.Builder().url(mediaPath).build()
                client.newCall(request).enqueue(object : Callback {
                    override fun onResponse(call: Call, response: Response) {
                        val responseBody = response.body
                        responseBody?.byteStream()?.use { input ->
                            videoCacheFile.outputStream().use { output ->
                                input.copyTo(output)
                            }
                        }

                        runOnUiThread {
                            println("Successfully downloaded audio file.")
                        }
                    }

                    override fun onFailure(call: Call, e: IOException) {
                        runOnUiThread {
                            Toast.makeText(this@ConnectivityClass, "Unable to download audio", Toast.LENGTH_SHORT).show()
                        }
                    }
                })

            }
            return mediaPath
        }
        else {
            return ""
        }
    }

    private fun downloadAndCacheVideo(videoUrl: String) {
        val client = OkHttpClient()
        val request = Request.Builder().url(videoUrl).build()

        try {
            val response: Response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val responseBody: ResponseBody? = response.body
                if (responseBody != null) {
                    // Specify the file path where you want to save the video
                    val cacheFile = File(cacheDir, videoUrl)
                    val output = FileOutputStream(cacheFile)
                    responseBody.byteStream().use { input ->
                        output.use { output ->
                            input.copyTo(output)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            // Handle any exceptions that may occur during the download.
        }
    }

    private fun insertBatch(batchId: String, batch: String, teacherId: String) {
        val tableName = "tbl_batch"
        val createTable = "CREATE TABLE IF NOT EXISTS $tableName (batchId TEXT, batch TEXT, teacherId TEXT)"
        db.execSQL(createTable)

        val checkQuery = "SELECT * FROM $tableName WHERE batchId = ?"
        val lessonArray = arrayOf(batchId)
        val lessonCursor: Cursor = db.rawQuery(checkQuery, lessonArray)

        if (lessonCursor.count > 0) {
            val insertQuery = "UPDATE $tableName SET batch = ?, teacherId = ? WHERE batchId = ?"
            val lessonArray = arrayOf(batch, teacherId, batchId)
            db.execSQL(insertQuery, lessonArray)
            lessonCursor.close()
        } else {
            val insertQuery = "INSERT INTO $tableName (batchId, batch, teacherId) VALUES (?,?,?)"
            val lessonArray = arrayOf(batchId, batch, teacherId)
            db.execSQL(insertQuery, lessonArray)
            lessonCursor.close()
        }
        println("Added Subject: $batchId, $batch")
    }
    private fun insertLesson(category: String, subjectId: String, subject: String, addedBy: String) {
        val tableName = "tbl_subject"

        val createTable = "CREATE TABLE IF NOT EXISTS $tableName (subjectId TEXT, category TEXT, subject TEXT, addedBy TEXT)"
        db.execSQL(createTable)

        val checkQuery = "SELECT * FROM $tableName WHERE subjectId = ?"
        val lessonArray = arrayOf(subjectId)
        val lessonCursor: Cursor = db.rawQuery(checkQuery, lessonArray)

        if (lessonCursor.count > 0) {
            val insertQuery = "UPDATE $tableName SET subject = ?, category = ?, addedBy = ? WHERE subjectId = ?"
            val lessonArray = arrayOf(subject, category, addedBy, subjectId)
            db.execSQL(insertQuery, lessonArray)
            lessonCursor.close()
        } else {
            val insertQuery = "INSERT INTO $tableName (subjectId, category, subject, addedBy) VALUES (?,?,?,?)"
            val lessonArray = arrayOf(subjectId, category, subject, addedBy)
            db.execSQL(insertQuery, lessonArray)
            lessonCursor.close()
        }
        println("Added Subject: $subjectId, $subject, $addedBy")
    }
    private fun insertTopic(topicId: String, topic: String, topicSubjectRef: String, imagePath: String, videoPath: String, audioPath: String, pdfPath: String) {
        val tableName = "tbl_topic"

        val createTable = "CREATE TABLE IF NOT EXISTS $tableName (topicId TEXT, topic TEXT, subjectRef TEXT, imagePath TEXT, videoPath TEXT, audioPath TEXT, pdfPath TEXT)"
        db.execSQL(createTable)

        val checkQuery = "SELECT * FROM $tableName WHERE topicId = ?"
        val topicArray = arrayOf(topicId)
        val topicCursor:Cursor = db.rawQuery(checkQuery, topicArray)

        if (topicCursor.count > 0) {
            val insertQuery = "UPDATE $tableName SET topic = ?, subjectRef = ?, imagePath = ?, videoPath = ?, audioPath = ?, pdfPath = ? WHERE topicId = ?"
            val topicArray2 = arrayOf(topic, topicSubjectRef, imagePath, videoPath, audioPath, pdfPath, topicId)
            db.execSQL(insertQuery, topicArray2)
            topicCursor.close()
        } else {
            val insertQuery = "INSERT INTO $tableName (topicId, topic, SubjectRef, imagePath, videoPath, audioPath, pdfPath) VALUES (?, ?, ?, ?, ?, ?, ?)"
            val topicArray2 = arrayOf(topicId, topic, topicSubjectRef, imagePath, videoPath, audioPath, pdfPath)
            db.execSQL(insertQuery, topicArray2)
            topicCursor.close()
        }
        println("Added Topic: $topicId, $topic, $topicSubjectRef, $imagePath, $videoPath, $audioPath, $pdfPath")
    }
    private fun insertLanguage(langId: String, kalagan: String, filipino: String, english: String, languageTopicRef: String) {
        val tableName = "tbl_language"

        val createTable = "CREATE TABLE IF NOT EXISTS $tableName (langId TEXT, kalagan TEXT, filipino TEXT, english TEXT, topicRef TEXT)"
        db.execSQL(createTable)

        val checkQuery = "SELECT * FROM $tableName WHERE langId = ?"
        val langArray = arrayOf(langId)
        val langCursor:Cursor = db.rawQuery(checkQuery, langArray)

        if (langCursor.count > 0) {
            val insertQuery = "UPDATE $tableName SET kalagan = ?, filipino = ?, english = ?, topicRef = ? WHERE langId = ?"
            val langArray = arrayOf(kalagan, filipino, english, languageTopicRef, langId)
            db.execSQL(insertQuery, langArray)
            langCursor.close()
        } else {
            val insertQuery = "INSERT INTO $tableName (langId, kalagan, filipino, english, topicRef) VALUES (?, ?, ?, ?, ?)"
            val langArray = arrayOf(langId, kalagan, filipino, english, languageTopicRef)
            db.execSQL(insertQuery, langArray)
            langCursor.close()
        }
        println("Added Language: $langId, $kalagan, $filipino, $english, $languageTopicRef")
    }
    private fun insertQuiz(quizId: String, quizName: String, quizQuestion: String, imgPath: String, selectionA: String, selectionB: String, selectionC: String, selectionD: String, score: Double, quizLessonIdRef: String, quizLessonNameRef: String, attempts: Int, addedBy: String) {
        val tableName = "tbl_quiz"
        val createTable = "CREATE TABLE IF NOT EXISTS $tableName (quizId TEXT, quizName TEXT, question TEXT, imagePath TEXT, selectionA TEXT, selectionB TEXT, selectionC TEXT, selectionD TEXT, score REAL, lessonId TEXT, lessonName TEXT, attempts INT, addedBy TEXT)"
        db.execSQL(createTable)

        val checkQuery = "SELECT * FROM $tableName WHERE quizId = ? AND quizName = ?"
        val quizArray = arrayOf(quizId, quizName)
        val quizCursor:Cursor = db.rawQuery(checkQuery, quizArray)

        if(quizCursor.count > 0) {
            val insertQuery = "UPDATE $tableName SET quizName = ?, question = ?, imagePath = ?, selectionA = ?, selectionB = ?, selectionC = ?, selectionD = ?, score = ?, lessonId = ?, lessonName = ?, attempts = ?, addedBy = ? WHERE quizId = ?"
            val quizArray = arrayOf(quizQuestion, imgPath, selectionA, selectionB, selectionC, selectionD, score, quizLessonIdRef, quizLessonNameRef, attempts, addedBy, quizId)
            db.execSQL(insertQuery, quizArray)
            quizCursor.close()
        } else {
            val insertQuery = "INSERT INTO $tableName (quizId, quizName, question, imagePath, selectionA, selectionB, selectionC, selectionD, score, lessonId, lessonName, attempts, addedBy) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
            val inQueryArray = arrayOf(quizId, quizName, quizQuestion, imgPath, selectionA, selectionB, selectionC, selectionD, score, quizLessonIdRef, quizLessonNameRef, attempts, addedBy)
            db.execSQL(insertQuery, inQueryArray)
            quizCursor.close()
        }
        println("Added Quiz by $addedBy : $quizId, $quizName, $quizQuestion, $imgPath, $quizLessonNameRef")
    }
    private fun insertLearner(learnerId: String, firstName: String, lastName: String, className: String, addedBy: String) {
        val tableName = "tbl_learner"
        val createTable = "CREATE TABLE IF NOT EXISTS $tableName (learnerId TEXT, firstName TEXT, lastName TEXT, className TEXT, addedBy TEXT)"
        db.execSQL(createTable)

        val checkQuery = "SELECT * FROM $tableName WHERE learnerId = ?"
        val learnerArray = arrayOf(learnerId)
        val learnerCursor: Cursor = db.rawQuery(checkQuery, learnerArray)

        if (learnerCursor.count > 0) {
            val insertQuery = "UPDATE $tableName SET firstName = ?, lastName = ?, className = ?, addedBy = ? WHERE learnerId = ?"
            val learnerArray = arrayOf(firstName, lastName, className, learnerId, addedBy)
            db.execSQL(insertQuery, learnerArray)
            learnerCursor.close()
        } else {
            val insertQuery = "INSERT INTO $tableName (learnerId, firstName, lastName, className, addedBy) VALUES (?, ?, ?, ?, ?)"
            val inQueryArray = arrayOf(learnerId, firstName, lastName, className, addedBy)
            db.execSQL(insertQuery, inQueryArray)
            learnerCursor.close()
        }
        println("Added Learner: $learnerId, $firstName, $lastName, $className")
    }
    private fun insertLearnerProgress(studentName: String, subjectId: String, topicId: String, status: String) {
        val tableName = "tbl_learnerProgress"
        val createTable = "CREATE TABLE IF NOT EXISTS $tableName (studentName TEXT, subjectId TEXT, topicId TEXT, status TEXT);"
        db.execSQL(createTable)

        val checkQuery = "SELECT * FROM $tableName WHERE studentName = ? AND subjectId = ? AND topicId = ?"
        val learnerQuizProgressArray = arrayOf(studentName, subjectId, topicId)
        val learnerQuizProgressCursor: Cursor = db.rawQuery(checkQuery, learnerQuizProgressArray)

        if (learnerQuizProgressCursor.count > 0) {
            val insertQuery = "UPDATE $tableName SET status = ? WHERE studentName = ? AND subjectId = ? AND topicId = ?"
            val lessonArray = arrayOf(status, studentName, subjectId, topicId)
            db.execSQL(insertQuery, lessonArray)
            learnerQuizProgressCursor.close()
        } else {
            val insertQuery = "INSERT INTO $tableName (studentName, subjectId, topicId, status) VALUES (?,?,?,?)"
            val lessonArray = arrayOf(studentName, subjectId, topicId, status)
            db.execSQL(insertQuery, lessonArray)
            learnerQuizProgressCursor.close()
        }
        println("Added Existing Quiz Progress: $studentName, $subjectId, $topicId, $status")
    }
    private fun insertLearnerQuizProgress(studentName: String, subjectId: String, quizName: String, score: Double, attempts: Int, dateTaken: String) {
        val tableName = "tbl_learnerQuizProgress"
        val createTable = "CREATE TABLE IF NOT EXISTS $tableName (studentName TEXT, subjectId TEXT, quizName TEXT, status TEXT, score REAL, attempts INT, dateTaken TEXT);"
        db.execSQL(createTable)

        val checkQuery = "SELECT * FROM $tableName WHERE studentName = ? AND subjectId = ? AND quizName = ?"
        val learnerQuizProgressArray = arrayOf(studentName, subjectId, quizName)
        val learnerQuizProgressCursor: Cursor = db.rawQuery(checkQuery, learnerQuizProgressArray)

        if (learnerQuizProgressCursor.count > 0) {
            val insertQuery = "UPDATE $tableName SET score = ?, attempts = ?, dateTaken = ? WHERE studentName = ? AND subjectId = ? AND quizName = ?"
            val lessonArray = arrayOf(score, attempts, dateTaken, studentName, subjectId, quizName)
            db.execSQL(insertQuery, lessonArray)
            learnerQuizProgressCursor.close()
        } else {
            val insertQuery = "INSERT INTO $tableName (studentName, subjectId, quizName, status, score, attempts, dateTaken) VALUES (?,?,?,?,?,?,?)"
            val lessonArray = arrayOf(studentName, subjectId, quizName, "1", score, attempts, dateTaken)
            db.execSQL(insertQuery, lessonArray)
            learnerQuizProgressCursor.close()
        }
        println("Added Existing Quiz Progress: $studentName, $quizName, $score, $dateTaken, $attempts")
    }
    private fun insertGradeRecords(studentName: String, subjectId: String, score: Double) {
        val tableName = "tbl_grading"
        val createTable = "CREATE TABLE IF NOT EXISTS $tableName (batch TEXT, studentName TEXT, quarter TEXT, record TEXT, subject TEXT, score REAL, addedBy TEXT);"
        db.execSQL(createTable)

        val (lastName, firstName) = studentName.split(", ")
        val tableName1 = "tbl_learner"
        val studentQuery = "SELECT * FROM $tableName1 WHERE firstName = ? AND lastName = ?;"
        val batchCursor: Cursor = db.rawQuery(studentQuery, arrayOf(firstName.trim().lowercase().replaceFirstChar { it.uppercaseChar() }, lastName.trim().lowercase().replaceFirstChar { it.uppercaseChar() }))

        var batch = ""
        if (batchCursor.moveToFirst()) {
            val columnIndexBatch = batchCursor.getColumnIndex("className")
            if (columnIndexBatch != -1) { batch = batchCursor.getString(columnIndexBatch) }
            else { batch = "" }
        }
        batchCursor.close()

        val tableName2 = "tbl_subject"
        val subjectQuery = "SELECT * FROM $tableName2 WHERE subjectId = ?"
        val subjectCursor: Cursor = db.rawQuery(subjectQuery, arrayOf(subjectId))

        var subject = ""
        if (subjectCursor.moveToFirst()) {
            val columnIndexSubject = subjectCursor.getColumnIndex("subject")
            if (columnIndexSubject != -1) { subject = subjectCursor.getString(columnIndexSubject) }
            else { subject = "" }
        }

        val gradingQuery = "SELECT * FROM $tableName WHERE studentName = ? AND subject = ?"
        val gradingArray = arrayOf(studentName, subject)
        val gradingCursor: Cursor = db.rawQuery(gradingQuery, gradingArray)

        if (gradingCursor.count > 0) {
            val insertQuery = "UPDATE $tableName SET batch = ?, quarter = ?, record = ?, score = ? WHERE studentName = ? AND subject = ?"
            val lessonArray = arrayOf(batch, "0", "Quiz", score, studentName, subject)
            db.execSQL(insertQuery, lessonArray)
            gradingCursor.close()
        } else {
            val insertQuery = "INSERT INTO $tableName (batch, studentName, quarter, record, subject, score, addedBy) VALUES (?,?,?,?,?,?,?)"
            val lessonArray = arrayOf(batch, studentName, "0", "Quiz", subject, score, "SYS")
            db.execSQL(insertQuery, lessonArray)
            gradingCursor.close()
        }
        println("Added Existing Quiz Progress to Grade: $studentName, $batch, $score, $subject")
    }
    private fun insertGames(gameId: String, answer: String, question: String, hint: String, type: String, imagePath1: String, imagePath2: String, audioPath: String) {
        val tableName = "tbl_games"

        val createTable = "CREATE TABLE IF NOT EXISTS $tableName (gameId TEXT, answer TEXT, question TEXT, hint TEXT, type TEXT, imagePath1 TEXT, imagePath2 TEXT, audioPath TEXT)"
        db.execSQL(createTable)

        val checkQuery = "SELECT * FROM $tableName WHERE gameId = ?"
        val gameArray = arrayOf(gameId)
        val gameCursor: Cursor = db.rawQuery(checkQuery, gameArray)
        if (gameCursor.count > 0) {
            val insertQuery = "UPDATE $tableName SET answer = ?, question = ?, hint = ?, type = ?, imagePath1 = ?, imagePath2 = ?, audioPath = ? WHERE gameId = ?"
            val gameArray = arrayOf(answer, question, hint, type, imagePath1, imagePath2, audioPath, gameId)
            db.execSQL(insertQuery, gameArray)
            gameCursor.close()
        } else {
            val insertQuery = "INSERT INTO $tableName (gameId, answer, question, hint, type, imagePath1, imagePath2, audioPath) VALUES (?, ?, ?, ?, ?, ?, ?, ?)"
            val inQueryArray = arrayOf(gameId, answer, question, hint, type, imagePath1, imagePath2, audioPath)
            db.execSQL(insertQuery, inQueryArray)
            gameCursor.close()
        }
        println("Added Game: $gameId, $question, $type")
    }
    private fun insertTeacher(teacherId: String, firstName: String, lastName: String, password: String) {
        val tableName = "tbl_teacher"

        val createTable = "CREATE TABLE IF NOT EXISTS $tableName (teacherId TEXT, firstName TEXT, lastName TEXT, password TEXT)"
        db.execSQL(createTable)

        val checkQuery = "SELECT * FROM $tableName WHERE teacherId = ?"
        val teacherArray = arrayOf(teacherId)
        val teacherCursor: Cursor = db.rawQuery(checkQuery, teacherArray)

        if (teacherCursor.count > 0) {
            val insertQuery =
                "UPDATE $tableName SET firstName = ?, lastName = ?, password = ? WHERE teacherId = ?"
            val teacherArray = arrayOf(firstName, lastName, password, teacherId)
            db.execSQL(insertQuery, teacherArray)
            teacherCursor.close()
        } else {
            val insertQuery =
                "INSERT INTO $tableName (teacherId, firstName, lastName, password) VALUES (?, ?, ?,?)"
            val inQueryArray = arrayOf(teacherId, firstName, lastName, password)
            db.execSQL(insertQuery, inQueryArray)
            teacherCursor.close()
        }
        println("Added Teacher: $teacherId, $firstName, $lastName, $password")
    }
    private fun createLearner() {
    }
    private fun createLearnerProgress() {
        val tableName1 = "tbl_learnerProgress"
        val tableName2 = "tbl_learnerQuizProgress"
        val tableName3 = "tbl_learnerGameProgress"

        val createTable1 = "CREATE TABLE IF NOT EXISTS $tableName1 (studentName TEXT, subjectId TEXT, topicId TEXT, status REAL);"
        val createTable2 = "CREATE TABLE IF NOT EXISTS $tableName2 (studentName TEXT, subjectId TEXT, quizName TEXT, status TEXT, score REAL);"
        val createTable3 = "CREATE TABLE IF NOT EXISTS $tableName3 (studentName TEXT, gameType TEXT, status TEXT);"

        db.execSQL(createTable1)
        db.execSQL(createTable2)
        db.execSQL(createTable3)


    }
    private fun createTeacherProgress() {
        val tableName1 = "tbl_teacherProgress"
        val tableName2 = "tbl_teacherQuizProgress"
        val tableName3 = "tbl_learnerGameProgress"

        val createTable1 = "CREATE TABLE IF NOT EXISTS $tableName1 (teacherId TEXT, subjectId TEXT, topicId TEXT, status REAL);"
        val createTable2 = "CREATE TABLE IF NOT EXISTS $tableName2 (teacherId TEXT, subjectId TEXT, quizName TEXT, status TEXT, score REAL);"
        val createTable3 = "CREATE TABLE IF NOT EXISTS $tableName3 (teacherId TEXT, gameType TEXT, status TEXT);"

        db.execSQL(createTable1)
        db.execSQL(createTable2)
        db.execSQL(createTable3)


    }
    private fun createTableGrading() {
        val tableName = "tbl_grading"

        val createTable1 = "CREATE TABLE IF NOT EXISTS $tableName (batch TEXT, studentName TEXT, quarter TEXT, record TEXT, subject TEXT, score REAL, addedBy TEXT);"
        db.execSQL(createTable1)
    }
    private fun getLesson(initialProgress: Int) {
        val tableName = "tbl_subject"

        val checkQuery = "SELECT * FROM $tableName"
        val lessonCursor: Cursor = db.rawQuery(checkQuery, null)

        if (lessonCursor.count > 0) {
            Log.d("Previous Lesson", "Loaded")
            Log.d("Offline Access", "Allow")
        } else {
            Log.d("Offline Access", "Deny")
        }
    }
    private fun getTopic(initialProgress: Int) {
        val tableName = "tbl_topic"

        val checkQuery = "SELECT * FROM $tableName"
        val lessonCursor: Cursor = db.rawQuery(checkQuery, null)

        if (lessonCursor.count > 0) {
            Log.d("Previous Topic", "Loaded")
            Log.d("Offline Access", "Allow")
        } else {
            Log.d("Offline Access", "Deny")
        }
    }
    private fun getLanguage(initialProgress: Int) {
        val tableName = "tbl_language"

        val checkQuery = "SELECT * FROM $tableName"
        val lessonCursor: Cursor = db.rawQuery(checkQuery, null)

        if (lessonCursor.count > 0) {
            Log.d("Previous Language", "Loaded")
            Log.d("Offline Access", "Allow")
        } else {
            Log.d("Offline Access", "Deny")
        }
    }
    private fun getQuiz(initialProgress: Int) {
        val tableName = "tbl_quiz"

        val checkQuery = "SELECT * FROM $tableName"
        val lessonCursor: Cursor = db.rawQuery(checkQuery, null)

        if (lessonCursor.count > 0) {
            Log.d("Previous Quiz", "Loaded")
            Log.d("Offline Access", "Allow")
        } else {
            Log.d("Offline Access", "Deny")
        }
    }
    private fun getGame(initialProgress: Int) {
        val tableName = "tbl_games"

        val checkQuery = "SELECT * FROM $tableName"
        val gameCursor: Cursor = db.rawQuery(checkQuery, null)

        if (gameCursor.count > 0) {
            Log.d("Previous Accounts", "Loaded")
            Log.d("Offline Access", "Allow")
        } else {
            Log.d("Offline Access", "Deny")
        }
    }
    private fun getBatch(initialProgress: Int) {
        val tableName = "tbl_batch"

        val checkQuery = "SELECT * FROM $tableName"
        val lessonCursor: Cursor = db.rawQuery(checkQuery, null)

        if (lessonCursor.count > 0) {
            Log.d("Previous Accounts", "Loaded")
            Log.d("Offline Access", "Allow")
        } else {
            Log.d("Offline Access", "Deny")
        }
    }
    private fun getTeacher(initialProgress: Int) {
        val tableName = "tbl_teacher"

        val checkQuery = "SELECT * FROM $tableName"
        val lessonCursor: Cursor = db.rawQuery(checkQuery, null)

        if (lessonCursor.count > 0) {
            Log.d("Previous Accounts", "Loaded")
            Log.d("Offline Access", "Allow")
        } else {
            Log.d("Offline Access", "Deny")
        }
    }
}
