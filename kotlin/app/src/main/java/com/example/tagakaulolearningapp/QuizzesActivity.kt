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
import android.widget.*
import androidx.cardview.widget.CardView

class QuizzesActivity : AppCompatActivity() {
    private lateinit var db: SQLiteDatabase
    private var quizList: ArrayList<String> = ArrayList()
    private var subjectList: ArrayList<String> = ArrayList()
    private lateinit var btnBack: ImageView
    private lateinit var txtDisplay: TextView
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var lessonId: String
    private lateinit var quizName: String
    private var initialProgress: Int = 0
    private var finalProgress: Int = 0
    private var initialQuizProgress: Int = 0
    private var finalQuizProgress: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        actionBar?.hide()
        DeviceNavigationClass.hideNavigationBar(this)
        setContentView(R.layout.activity_quizzes)

        quizList.clear()
        subjectList.clear()

        db = openOrCreateDatabase("tagakauloDB", Context.MODE_PRIVATE, null)

        sharedPreferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
        lessonId = sharedPreferences.getString("lessonId", "").toString()

        btnBack = findViewById(R.id.btnBack)
        btnBack.setOnClickListener {
            val intent = Intent(this@QuizzesActivity, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }

        db.execSQL("DROP TABLE IF EXISTS tbl_new_quiz")

        createTempQuiz()

        for (i in quizList.indices) {
            val topicItem = quizList[i]
            val cleanedQuizList = topicItem.replace("[", "").replace("]", "")
            var elements = cleanedQuizList.split(", ")

            quizName = elements[0]
            var subjectRef = elements[2]
            val tableName = "tbl_subject"

            val adviser = sharedPreferences.getString("adviser", "").toString()
            val teacherId = sharedPreferences.getString("userId", "").toString()
            val checkQuery = "SELECT * FROM $tableName WHERE subjectId = ? AND addedBy = ? OR addedBy = ?"
            val lessonCursor: Cursor = db.rawQuery(checkQuery, arrayOf(subjectRef, adviser, teacherId))

            if (lessonCursor.moveToFirst()) {
                val columnIndexSubject = lessonCursor.getColumnIndex("subject")
                if (columnIndexSubject != -1) {
                    do {
                        val subject = lessonCursor.getString(columnIndexSubject)
                        subjectList.add(subject)
                    } while (lessonCursor.moveToNext())
                } else {
                }
            }
            else {
                subjectList.add("")
            }

            lessonCursor.close()
        }

        if(quizList.isEmpty()) {
            val intent = Intent(this@QuizzesActivity, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }
        else {
            loadSubjects()
        }
    }
    private fun createTempQuiz() {
        val adviser = sharedPreferences.getString("adviser", "")
        val teacherId = sharedPreferences.getString("userId", "")

        val tableName = "tbl_new_quiz"

        val quizTable = "tbl_quiz"

        val retrieveQuery = "SELECT * FROM $quizTable WHERE addedBy = ? OR addedBy = ?"
        val quizCursor: Cursor = db.rawQuery(retrieveQuery, arrayOf(adviser, teacherId))

        if (quizCursor.moveToFirst()) {
            val quizIdColIndex = quizCursor.getColumnIndex("quizId")
            val quizNameColIndex = quizCursor.getColumnIndex("quizName")
            val subjectRefColIndex = quizCursor.getColumnIndex("lessonId")
            if (quizNameColIndex != -1) {
                do {
                    val quizId = quizCursor.getString(quizIdColIndex)
                    val quizName = quizCursor.getString(quizNameColIndex)
                    val subjectRef = quizCursor.getString(subjectRefColIndex)

                    val quizData = listOf(quizId, quizName, subjectRef)
                    quizList.add(quizData.toString())
                } while(quizCursor.moveToNext())
            }
        }
    }
    private fun getProgress(subjectId: String, quizName: String): Int {
        val userType = sharedPreferences.getString("userType", "").toString()
        val userId = sharedPreferences.getString("userId", "").toString()
        val firstName = sharedPreferences.getString("firstName", "").toString()
        val lastName = sharedPreferences.getString("lastName", "").toString()

        if (userType == "learner") {
            val studentName = lastName.uppercase() + ", " + firstName.lowercase().replaceFirstChar { it.uppercaseChar() }

            val tableName2 = "tbl_learnerQuizProgress"
            val checkQuery2 = "SELECT * FROM $tableName2 WHERE studentName = ? AND quizName = ? AND subjectId = ?"
            val progressQuizArray = arrayOf(studentName, quizName, subjectId)
            val progressQuizCursor: Cursor = db.rawQuery(checkQuery2, progressQuizArray)
            if (progressQuizCursor.moveToFirst()) {
                val studentColumnIndex = progressQuizCursor.getColumnIndex("attempts")

                if (studentColumnIndex != -1) {
                    var learnerCount = 0
                    do {
                        learnerCount += progressQuizCursor.getInt(studentColumnIndex)
                    } while (progressQuizCursor.moveToNext())

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
            val checkQuery2 = "SELECT COUNT(teacherId) AS count FROM $tableName2 WHERE teacherId = ? AND quizName = ? AND subjectId = ?"
            val progressQuizArray = arrayOf(userId, quizName, subjectId)
            val progressQuizCursor: Cursor = db.rawQuery(checkQuery2, progressQuizArray)
            if (progressQuizCursor.moveToFirst()) {
                val teacherColumnIndex = progressQuizCursor.getColumnIndex("count")

                if (teacherColumnIndex != -1) {
                    val teacherCount = progressQuizCursor.getInt(teacherColumnIndex)

                    return teacherCount
                }
                return 1
            }
            else {
                return 0
            }
            progressQuizCursor.close()
        }
    }

    private fun getFinalProgress(quizId: String, quizName: String): Int {
        val userType = sharedPreferences.getString("userType", "").toString()
        val userId = sharedPreferences.getString("userId", "").toString()
        val firstName = sharedPreferences.getString("firstName", "").toString()
        val lastName = sharedPreferences.getString("lastName", "").toString()
        val adviser = sharedPreferences.getString("adviser", "")
        val teacherId = sharedPreferences.getString("userId", "")
        if (userType == "learner") {
            var quizCount = 0

            val tableName2 = "tbl_quiz"
            val checkQuery2 = "SELECT * FROM $tableName2 WHERE quizName = ? AND lessonName = ? AND addedBy = ? OR addedBy = ?"
            val progressQuizArray = arrayOf(quizName, quizId, adviser, teacherId)
            val progressQuizCursor: Cursor = db.rawQuery(checkQuery2, progressQuizArray)
            if (progressQuizCursor.moveToFirst()) {
                val studentColumnIndex = progressQuizCursor.getColumnIndex("attempts")

                if (studentColumnIndex != -1) {
                    var learnerCount = progressQuizCursor.getInt(studentColumnIndex)
                    quizCount = learnerCount
                    return quizCount
                } else {
                    quizCount = 1
                    return quizCount
                }
            }
            else {
                quizCount = 0
                return quizCount
            }
            progressQuizCursor.close()
        }
        else {
            val quizCount = 0
            return quizCount
        }
    }
    private fun loadSubjects() {
        val parentLayout: GridLayout = findViewById(R.id.parent_layout)

        val inflater = LayoutInflater.from(this)

        var sizeWithoutDuplicates = 0

        val userType = sharedPreferences.getString("userType", "").toString()
        val teacherId = if (userType == "learner") sharedPreferences.getString("adviser", "") else sharedPreferences.getString("userId", "")
        val checkQuiz = "SELECT COUNT(DISTINCT quizName) AS quizName FROM tbl_quiz WHERE addedBy = ?;"
        val quizCursor: Cursor = db.rawQuery(checkQuiz, arrayOf(teacherId))

        if (quizCursor.moveToFirst()) {
            val quizNameColIndex = quizCursor.getColumnIndex("quizName")
            if (quizNameColIndex != -1) {
                val quizName = quizCursor.getString(quizNameColIndex)
                sizeWithoutDuplicates = quizName.toInt()
            }
        }
        println("this is the quiz size: $sizeWithoutDuplicates")
        for (subject in 0 until sizeWithoutDuplicates) {

            val topicItem = quizList[subject]
            val cleanedQuizList = topicItem.replace("[", "").replace("]", "")
            var elements = cleanedQuizList.split(", ")

            val quizId = elements[0]
            val quizName = elements[1]
            val subjectId = elements[2]
            var subjectName = subjectList[subject]

            val initialProgress = getProgress(subjectId, subjectId)
            finalProgress = getFinalProgress(quizId, subjectName)

                val userType = sharedPreferences.getString("userType", "").toString()
                if (userType == "learner") {
                    if (initialProgress == finalProgress) {
                        val subjectLayout = inflater.inflate(R.layout.layout_home_finished, parentLayout, false) as CardView
                        val txtSubject = subjectLayout.findViewById<TextView>(R.id.txtSubject)
                        val txtCategory = subjectLayout.findViewById<TextView>(R.id.txtCategory)

                        txtCategory.visibility = View.VISIBLE
                        txtSubject.text = quizName
                        txtCategory.text = subjectName

                        parentLayout.addView(subjectLayout)
                    }
                    else {
                        val subjectLayout = inflater.inflate(R.layout.layout_home, parentLayout, false) as RelativeLayout
                        val imgSubject = subjectLayout.findViewById<ImageView>(R.id.imgSubject)
                        val txtSubject = subjectLayout.findViewById<TextView>(R.id.txtSubject)
                        val txtCategory = subjectLayout.findViewById<TextView>(R.id.txtCategory)
                        val progressBar = subjectLayout.findViewById<ProgressBar>(R.id.progressBar)
                        val txtProgress = subjectLayout.findViewById<TextView>(R.id.txtProgress)

                        txtCategory.visibility = View.VISIBLE
                        txtSubject.text = quizName
                        txtCategory.text = subjectName

                        var currentProgress = initialProgress.toFloat() / 1.0
                        var percentageProgress = (currentProgress * 100).toInt()
                        progressBar.progress = percentageProgress

                        txtProgress.text = percentageProgress.toString() + "%"

                        imgSubject.setImageResource(R.drawable.logo)

                        val uniqueId = View.generateViewId()
                        subjectLayout.id = uniqueId
                        parentLayout.addView(subjectLayout)

                        progressBar.visibility = View.VISIBLE
                        txtProgress.visibility = View.VISIBLE
                        subjectLayout.setOnClickListener {
                            loadQuiz(subjectId, quizName)
                        }
                    }
                }
                else {
                    if (initialProgress == 1) {
                        val subjectLayout = inflater.inflate(R.layout.layout_home_finished, parentLayout, false) as CardView
                        val btnSubject = subjectLayout.findViewById<RelativeLayout>(R.id.btnSubject)
                        val txtSubject = subjectLayout.findViewById<TextView>(R.id.txtSubject)
                        val txtCategory = subjectLayout.findViewById<TextView>(R.id.txtCategory)

                        txtCategory.visibility = View.VISIBLE
                        txtSubject.text = quizName
                        txtCategory.text = subjectName

                        val uniqueId = View.generateViewId()
                        btnSubject.id = uniqueId

                        parentLayout.addView(subjectLayout)

                        btnSubject.setOnClickListener {
                            println("Clicked finished quiz")
                            loadQuiz(subjectId, quizName)
                        }
                    }
                    else {
                        val subjectLayout = inflater.inflate(R.layout.layout_home, parentLayout, false) as RelativeLayout
                        val imgSubject = subjectLayout.findViewById<ImageView>(R.id.imgSubject)
                        val txtSubject = subjectLayout.findViewById<TextView>(R.id.txtSubject)
                        val txtCategory = subjectLayout.findViewById<TextView>(R.id.txtCategory)
                        val progressBar = subjectLayout.findViewById<ProgressBar>(R.id.progressBar)
                        val txtProgress = subjectLayout.findViewById<TextView>(R.id.txtProgress)

                        txtCategory.visibility = View.VISIBLE
                        txtSubject.text = quizName
                        txtCategory.text = subjectName

                        var currentProgress = initialProgress.toFloat() / 1.0
                        var percentageProgress = (currentProgress * 100).toInt()
                        progressBar.progress = percentageProgress

                        txtProgress.text = percentageProgress.toString() + "%"

                        imgSubject.setImageResource(R.drawable.logo)
                        txtSubject.text = quizName

                        val uniqueId = View.generateViewId()
                        subjectLayout.id = uniqueId
                        parentLayout.addView(subjectLayout)

                        progressBar.visibility = View.VISIBLE
                        txtProgress.visibility = View.VISIBLE
                        subjectLayout.setOnClickListener {
                            loadQuiz(subjectId, quizName)
                        }
                    }
                }

        }
    }
    private fun loadQuiz(subjectId: String, quizName: String) {
        val tableName = "tbl_quiz"
        val query = "SELECT * FROM $tableName WHERE quizName = ? AND lessonId = ?"
        val quizCursor: Cursor = db.rawQuery(query, arrayOf(quizName, subjectId))
        if (quizCursor.count > 0) {
            val intent = Intent(this@QuizzesActivity, QuizActivity::class.java)
            intent.putExtra("subjectId", subjectId)
            intent.putExtra("quizName", quizName)
            quizCursor.close()
            startActivity(intent)
            finish()
        } else {
            Toast.makeText(this, "No quiz available", Toast.LENGTH_SHORT).show()
        }

    }
}