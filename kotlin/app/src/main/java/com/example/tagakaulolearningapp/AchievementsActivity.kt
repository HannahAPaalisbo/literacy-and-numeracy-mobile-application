package com.example.tagakaulolearningapp

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import java.util.zip.Inflater
import kotlin.collections.ArrayList

class AchievementsActivity : AppCompatActivity() {
    private lateinit var btnBack: ImageView
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var lessonId: String
    private lateinit var studentName: String
    private lateinit var userId: String
    private lateinit var adviser: String
    private lateinit var db: SQLiteDatabase
    private lateinit var parentLayout: LinearLayout
    private var subjectList: ArrayList<String> = ArrayList()
    private var quizArray: ArrayList<String> = ArrayList()
    private var gameArray: ArrayList<String> = ArrayList()
    private var proArray: ArrayList<String> = ArrayList()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        actionBar?.hide()
        setContentView(R.layout.activity_achievements)

        sharedPreferences = getSharedPreferences("MyPreferences", MODE_PRIVATE)
        val firstName = sharedPreferences.getString("firstName", "").toString()
        val lastName = sharedPreferences.getString("lastName", "").toString()
        studentName = lastName.uppercase() + ", " + firstName.lowercase().replaceFirstChar { it.uppercaseChar() }
        userId = sharedPreferences.getString("userId", "").toString()
        adviser = sharedPreferences.getString("adviser", "").toString()

        db = openOrCreateDatabase("tagakauloDB", Context.MODE_PRIVATE, null)

        quizArray.clear()
        subjectList.clear()

        btnBack = findViewById(R.id.imgBack)
        btnBack.setOnClickListener {
            subjectList.clear()
            quizArray.clear()
            val intent = Intent(this@AchievementsActivity, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }

        val tableName = "tbl_subject"
        val query = "SELECT * FROM $tableName WHERE addedBy = ? OR addedBy = ?"
        val subjectCursor: Cursor = db.rawQuery(query, arrayOf(adviser, "USR100001"))

        if (subjectCursor.moveToFirst()) {
            val columnSubjectIdIndex = subjectCursor.getColumnIndex("subjectId")
            if (columnSubjectIdIndex != -1) {
                do {
                    val subjectId = subjectCursor.getString(columnSubjectIdIndex)
                    subjectList.add(subjectId)
                } while (subjectCursor.moveToNext())
            }
        }
        subjectCursor.close()

        val userType = sharedPreferences.getString("userType", "").toString()
        val teacherId = if (userType == "learner") sharedPreferences.getString("adviser", "") else sharedPreferences.getString("userId", "")
        val tableName1 = "tbl_quiz"
        val query1 = "SELECT DISTINCT quizName FROM $tableName1 WHERE addedBy = ?"
        val quizCursor: Cursor = db.rawQuery(query1, arrayOf(teacherId))

        if (quizCursor.count > 0) {
            quizCursor.moveToFirst()
            val columnSubjectIdIndex = quizCursor.getColumnIndex("quizName")
            if (columnSubjectIdIndex != -1) {
                do {
                    val quizId = quizCursor.getString(columnSubjectIdIndex)
                    quizArray.add(quizId)
                } while (quizCursor.moveToNext())
            }
        }

        quizCursor.close()
        loadAchievements()
    }

    private fun getLearnerScore(progressType: String, lessonId: String): Double {
        var initialScore = 0.0
        if (progressType == "quizProgress") {
            val tableName = "tbl_learnerQuizProgress"

            val query = "SELECT COUNT(studentName) AS count FROM $tableName WHERE studentName = ? and quizName = ?"
            val quizArray = arrayOf(studentName, lessonId)
            val quizCursor: Cursor = db.rawQuery(query, quizArray)

            if (quizCursor.moveToFirst()) {
                val columnIndexQuizScore = quizCursor.getColumnIndex("count")
                if (columnIndexQuizScore != -1) {
                    initialScore += quizCursor.getInt(columnIndexQuizScore)
                    return initialScore.toDouble()
                }
                else {
                    initialScore += 0.0
                }
            } else {
                initialScore = 0.0
            }
            return initialScore
        }
        else if (progressType == "pro") {
            when (lessonId) {
                "Outstanding Learner" -> {
                    for (i in 0 until subjectList.size) {
                        val tableName = "tbl_learnerProgress"
                        var query ="SELECT COUNT(DISTINCT subjectId) AS score FROM $tableName WHERE subjectId = ? AND studentName = ?"
                        var quizArray = arrayOf(subjectList[i], studentName)
                        var quizCursor: Cursor = db.rawQuery(query, quizArray)

                        if (quizCursor.moveToFirst()) {
                            var columnIndexQuizScore = quizCursor.getColumnIndex("score")

                            if (columnIndexQuizScore != -1) {
                                var score = quizCursor.getDouble(columnIndexQuizScore)
                                initialScore += score
                            } else {
                                initialScore = 0.0
                            }
                        }
                    }
                }
                "Quiz Conqueror" -> {
                    val tableName = "tbl_learnerQuizProgress"
                    val query = "SELECT SUM(score) AS score FROM $tableName WHERE studentName = ?"
                    val quizArray = arrayOf(studentName)
                    val quizCursor: Cursor = db.rawQuery(query, quizArray)

                    if (quizCursor.moveToFirst()) {
                        val columnIndexQuizScore = quizCursor.getColumnIndex("score")
                        if (columnIndexQuizScore != -1) {
                            val score = quizCursor.getDouble(columnIndexQuizScore)
                            initialScore += score
                        } else {
                            initialScore = 0.0
                        }
                    }
                }
            }
            return initialScore
        }
        else {
            val tableName = "tbl_learnerProgress"

            val query ="SELECT DISTINCT subjectId, SUM(status) AS score FROM $tableName WHERE subjectId = ? AND studentName = ?"
            val quizArray = arrayOf(lessonId, studentName)
            val quizCursor: Cursor = db.rawQuery(query, quizArray)

            if (quizCursor.moveToFirst()) {
                val columnIndexQuizScore = quizCursor.getColumnIndex("score")

                if (columnIndexQuizScore != -1) {
                    val score = quizCursor.getDouble(columnIndexQuizScore)
                    initialScore += score
                } else {
                    initialScore = 0.0
                }
                return initialScore
            } else {
                return initialScore
            }
            return initialScore
        }
    }

    private fun getTeacherScore(progressType: String, lessonId: String): Double {
        var initialScore = 0.0
        if (progressType == "quizProgress") {
            val tableName = "tbl_teacherQuizProgress"

            val query =
                "SELECT SUM(score) AS score FROM $tableName WHERE subjectId = ? AND teacherId = ?"
            val quizArray = arrayOf(lessonId, userId)
            val quizCursor: Cursor = db.rawQuery(query, quizArray)

            if (quizCursor.moveToFirst()) {
                val columnIndexQuizScore = quizCursor.getColumnIndex("score")
                if (columnIndexQuizScore != -1) {
                    val score = quizCursor.getDouble(columnIndexQuizScore)
                    initialScore += 1.0
                } else {
                    initialScore = 0.0
                }
            } else {
                initialScore = 0.0
            }
            return initialScore
        }
        else {
            val tableName = "tbl_teacherProgress"

            val query = "SELECT SUM(status) AS score FROM $tableName WHERE subjectId = ? AND teacherId = ?"
            val quizArray = arrayOf(lessonId, userId)
            val quizCursor: Cursor = db.rawQuery(query, quizArray)

            if (quizCursor.moveToFirst()) {
                val columnIndexQuizScore = quizCursor.getColumnIndex("score")

                if (columnIndexQuizScore != -1) {
                    val score = quizCursor.getDouble(columnIndexQuizScore)
                    initialScore += score
                } else {
                    initialScore = 0.0
                }
                return initialScore
            } else {
                return initialScore
            }
        }
    }

    private fun getTotal(progressType: String, lessonId: String): Double {
        if (progressType == "quizProgress") {
            var total = 0.0

            val tableName = "tbl_quiz"
            val query = "SELECT COUNT(*) AS count FROM $tableName"
            val topicCursor: Cursor = db.rawQuery(query, null)

            if (topicCursor.moveToFirst()) {
                val quizNameIndex= topicCursor.getColumnIndex("count")
                if (quizNameIndex != -1) {
                    val quizzes = topicCursor.getInt(quizNameIndex)
                    return quizzes.toDouble()
                }
            } else {
                total = 0.0
            }
            return total
        }
        else {
            var total = 0.0

            val tableName = "tbl_topic"
            val query = "SELECT COUNT(*) AS score FROM $tableName WHERE subjectRef = ?"
            val quizArray = arrayOf(lessonId)
            val quizCursor: Cursor = db.rawQuery(query, quizArray)

            if (quizCursor.moveToFirst()) {
                val quizScoreIndex = quizCursor.getColumnIndex("score")
                if (quizScoreIndex != -1) {
                    val quizScore = quizCursor.getDouble(quizScoreIndex)
                    total += quizScore
                }
            } else {
                total = 0.0
            }
            return total
        }
    }

    private fun getReaderMedal(currentIndex: Int): Boolean {
        val userType = sharedPreferences.getString("userType", "")
        var hasMedal = false
        if (userType == "learner") {
            val initialScore = getLearnerScore("learnerProgress", subjectList[currentIndex])
            val totalScore = getTotal("", subjectList[currentIndex])
            val finalScore = initialScore / totalScore

            if (finalScore >= 1.0) {
                hasMedal = true
            } else {
                hasMedal = false
            }
            return hasMedal
        } else {
            val initialScore = getTeacherScore("teacherProgress", subjectList[currentIndex])
            val totalScore = getTotal("", subjectList[currentIndex])
            val finalScore = initialScore / totalScore
            if (finalScore == 1.0) {
                hasMedal = true
            } else {
                hasMedal = false
            }
            return hasMedal
        }
    }

    private fun getQuizzerMedal(currentIndex: Int): Boolean {
        val userType = sharedPreferences.getString("userType", "")
        var hasMedal = false
        if (userType == "learner") {
            val initialScore = getLearnerScore("quizProgress", quizArray[currentIndex])
            val totalScore = getTotal("quizProgress", "")
            val finalScore = initialScore - totalScore
            println("cheeckign final score achievements: $initialScore")
            if (initialScore != 0.0) {
                hasMedal = true
            } else {
                hasMedal = false
            }
            return hasMedal
        } else {
            val initialScore = getTeacherScore("quizProgress", subjectList[currentIndex])
            val totalScore = getTotal("quizProgress", subjectList[currentIndex])
            val finalScore = initialScore / totalScore
            if (finalScore == 1.0) {
                hasMedal = true
            } else {
                hasMedal = false
            }
            return hasMedal
        }
    }

    private fun getGamerMedal(currentIndex: Int): Boolean {
        val userType = sharedPreferences.getString("userType", "")
        var hasMedal = false
        if (userType == "learner") {
            for (i in 0 until gameArray.size) {
                val initialScore = getLearnerScore("learnerProgress", gameArray[currentIndex])
                val totalScore = getTotal("", gameArray[i])
                val finalScore = initialScore / totalScore
                if (finalScore == 1.0) {
                    hasMedal = true
                } else {
                    hasMedal = false
                }
            }

        } else {
            for (i in 0 until gameArray.size) {
                val initialScore = getTeacherScore("teacherProgress", gameArray[currentIndex])
                val totalScore = getTotal("", gameArray[i])
                val finalScore = initialScore / totalScore
                if (finalScore == 1.0) {
                    hasMedal = true
                } else {
                    hasMedal = false
                }
            }
        }
        return hasMedal
    }

    private fun getProMedal(currentIndex: Int): Boolean {
        val userType = sharedPreferences.getString("userType", "")
        var hasMedal = false

        if (userType == "learner") {
            when (proArray[currentIndex]) {
                "Outstanding Learner" -> {
                    val initialScore = getLearnerScore("pro", proArray[currentIndex])
                    val finalScore = initialScore / subjectList.size
                    if (finalScore == 1.0) {
                        hasMedal = true
                    } else {
                        hasMedal = false
                    }
                }
                "Quiz Conqueror" -> {
                    val initialScore = getLearnerScore("pro", proArray[currentIndex])
                    val finalScore = initialScore / quizArray.size
                    if (finalScore == 1.0) {
                        hasMedal = true
                    } else {
                        hasMedal = false
                    }
                }
                "Bi-lingual Learner" -> {
                    val readerScore = getLearnerScore("pro", "Outstanding Learner")
                    val quizScore = getLearnerScore("pro", "Quiz Conqueror")
                    val initialScore = readerScore + quizScore

                    val finalScore = initialScore / (quizArray.size + subjectList.size)
                    if (finalScore >= 1.0) {
                        hasMedal = true
                    } else {
                        hasMedal = false
                    }
                }
                "All-Around Learner" -> {
                    //Lesson + quiz + games
                }
                "Problem Solver" -> {
                    //All games
                }

            }
            return hasMedal
        } else {
            for (i in 0 until proArray.size) {
                val initialScore = getTeacherScore("teacherProgress", proArray[currentIndex])
                val totalScore = getTotal("", proArray[i])
                val finalScore = initialScore / totalScore
                if (finalScore == 1.0) {
                    hasMedal = true
                } else {
                    hasMedal = false
                }
            }
        }

        return hasMedal
    }

    private fun loadAchievements() {
        val categoryArray = arrayOf("reader", "quizzer") //add gamer and pro soon

        gameArray = arrayListOf("Shadow Artist", "Good Listener", "Vocabulary Voyager", "Problem Solver")
        //proArray = arrayListOf("Outstanding Learner", "Quiz Conqueror","Bi-lingual Learner", "All-Around Learner", "Problem Solver")
        parentLayout = findViewById(R.id.parentLayout)

        val inflater = LayoutInflater.from(this)
        for (category in categoryArray.indices) {
            val mainLayout = inflater.inflate(R.layout.layout_medal_category, parentLayout, false) as RelativeLayout
            val categoryLayout = mainLayout.findViewById<GridLayout>(R.id.categoryLayout)

            parentLayout.addView(mainLayout)
            when (categoryArray[category]) {
                "reader" -> {
                    val medalLayout = inflater.inflate(R.layout.layout_medal, categoryLayout, false) as RelativeLayout

                    val txtDate = medalLayout.findViewById<TextView>(R.id.txtDate)
                    val imgMedal = medalLayout.findViewById<ImageView>(R.id.imgMedal)
                    val txtSubject = medalLayout.findViewById<TextView>(R.id.txtSubject)
                    var medals = 0
                    var totalMedals = 0

                    txtDate.text = categoryArray[category].lowercase().replaceFirstChar { it.uppercaseChar() }
                    for (medal in subjectList.indices) {
                        val hasMedal = getReaderMedal(medal)
                        if (hasMedal) {
                            medals++
                        }
                        totalMedals++
                    }
                    if (medals > 0) {
                        Glide.with(this@AchievementsActivity)
                            .load(R.drawable.read_has_medal)
                            .into(imgMedal)
                    }
                    else {
                        Glide.with(this@AchievementsActivity)
                            .load(R.drawable.no_medal)
                            .into(imgMedal)
                    }
                    txtSubject.text = "$medals / $totalMedals"
                    categoryLayout.addView(medalLayout)
                }
                "quizzer" -> {
                    val medalLayout = inflater.inflate(R.layout.layout_medal, categoryLayout, false) as RelativeLayout

                    val txtDate = medalLayout.findViewById<TextView>(R.id.txtDate)
                    val imgMedal = medalLayout.findViewById<ImageView>(R.id.imgMedal)
                    val txtSubject = medalLayout.findViewById<TextView>(R.id.txtSubject)
                    var medals = 0
                    var totalMedals = 0

                    txtDate.text = categoryArray[category].lowercase().replaceFirstChar { it.uppercaseChar() }
                    for (medal in quizArray.indices) {
                        val hasMedal = getQuizzerMedal(medal)
                        if (hasMedal) {
                            medals++
                        }
                        totalMedals++
                    }
                    if (medals > 0) {
                        Glide.with(this@AchievementsActivity)
                            .load(R.drawable.quiz_has_medal)
                            .into(imgMedal)

                    } else {
                        Glide.with(this@AchievementsActivity)
                            .load(R.drawable.no_medal)
                            .into(imgMedal)
                    }
                    txtSubject.text = "$medals / $totalMedals"
                    categoryLayout.addView(medalLayout)
                }
                "gamer" -> {
                    val medalLayout = inflater.inflate(R.layout.layout_medal, categoryLayout, false) as RelativeLayout

                    val txtDate = medalLayout.findViewById<TextView>(R.id.txtDate)
                    val imgMedal = medalLayout.findViewById<ImageView>(R.id.imgMedal)
                    val txtSubject = medalLayout.findViewById<TextView>(R.id.txtSubject)
                    var medals = 0
                    var totalMedals = 0

                    txtDate.text = categoryArray[category].lowercase().replaceFirstChar { it.uppercaseChar() }

                    for (medal in gameArray.indices) {
                        val hasMedal = getGamerMedal(medal)
                        if (hasMedal) {
                            medals++
                        }
                        totalMedals++
                    }

                    if (medals > 0) {
                        Glide.with(this@AchievementsActivity)
                            .load(R.drawable.quiz_has_medal)
                            .into(imgMedal)

                    } else {
                        Glide.with(this@AchievementsActivity)
                            .load(R.drawable.no_medal)
                            .into(imgMedal)
                    }

                    txtSubject.text = "$medals / $totalMedals"
                    categoryLayout.addView(medalLayout)
                }
                "pro" -> {
                    val medalLayout = inflater.inflate(R.layout.layout_medal, categoryLayout, false) as RelativeLayout

                    val txtDate = medalLayout.findViewById<TextView>(R.id.txtDate)
                    val imgMedal = medalLayout.findViewById<ImageView>(R.id.imgMedal)
                    val txtSubject = medalLayout.findViewById<TextView>(R.id.txtSubject)
                    var medals = 0
                    var totalMedals = 0

                    txtDate.text = categoryArray[category].lowercase().replaceFirstChar { it.uppercaseChar() }

                    for (medal in proArray.indices) {
                        val hasMedal = getProMedal(medal)
                        if (hasMedal) {
                            medals++
                        }
                        totalMedals++
                    }

                    if (medals > 0) {
                        Glide.with(this@AchievementsActivity)
                            .load(R.drawable.quiz_has_medal)
                            .into(imgMedal)

                    } else {
                        Glide.with(this@AchievementsActivity)
                            .load(R.drawable.no_medal)
                            .into(imgMedal)
                    }

                    txtSubject.text = "$medals / $totalMedals"
                    categoryLayout.addView(medalLayout)
                }
            }
        }
    }
    override fun onBackPressed() {
        subjectList.clear()
        quizArray.clear()
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish()
    }
}