package com.example.tagakaulolearningapp

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.graphics.Path
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import com.airbnb.lottie.LottieAnimationView
import com.example.tagakaulolearningapp.databinding.ActivityNumberTracingBinding
import pk.farimarwat.abckids.AbcdkidsListener
import pk.farimarwat.abckids.TAG

class NumberTracingActivity : AppCompatActivity() {
    lateinit var mContext: Context
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var db: SQLiteDatabase
    private lateinit var binding: ActivityNumberTracingBinding
    private lateinit var topicId: String
    private lateinit var topicName: String
    private lateinit var lessonId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        actionBar?.hide()
        DeviceNavigationClass.hideNavigationBar(this)
        binding = ActivityNumberTracingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences("MyPreferences", MODE_PRIVATE)
        db = openOrCreateDatabase("tagakauloDB", Context.MODE_PRIVATE, null)

        lessonId = intent.getStringExtra("subject").toString()
        topicId = intent.getStringExtra("topic").toString()
        topicName = intent.getStringExtra("topicName").toString()

        mContext = this
        val width = 420
        val height = 420

        val centerX = width.toFloat() / 4
        val centerY = height.toFloat() / 2

        val path = Path()

        when (topicName) {
            "1" -> {
                //1

                path.moveTo(width * 0.6f, height * 1.0f)
                path.lineTo(width * 0.9f, height * 0.51f)

                path.moveTo(width * 0.9f, height * 0.51f)
                path.lineTo(width * 0.9f, height * 1.6f)

                path.moveTo(width * 0.6f, height * 1.6f)
                path.lineTo(width * 1.2f, height * 1.6f)

                binding.tlview.setLetter(path)
            }

            "8" -> {
                //8

                val lowerCaseRadius = 120f
                path.addCircle(centerX + width / 2, centerY, lowerCaseRadius, Path.Direction.CCW)
                path.addCircle(centerX + width / 2, centerY * 2.15f, lowerCaseRadius, Path.Direction.CCW)
                binding.tlview.setLetter(path)
            }
            "7" -> {
                //7

                path.moveTo(width * 0.51f, height * 0.51f)
                path.lineTo(width * 1.1f, height * 0.51f)

                path.moveTo(width * 1.1f, height * 0.51f)
                path.lineTo(width * 0.51f, height * 1.6f)

                binding.tlview.setLetter(path)
            }
            "4" -> {
                //4

                path.moveTo(width * 1.1f, height * 0.51f)
                path.lineTo(width * 0.6f, height * 1.2f)

                path.moveTo(width * 0.6f, height * 1.2f)
                path.lineTo(width * 1.2f, height * 1.2f)

                path.moveTo(width * 1.1f, height * 0.51f)
                path.lineTo(width * 1.1f, height * 1.6f)



                binding.tlview.setLetter(path)
            }

            "9" -> {
                //9

                val lowerCaseRadius = 120f
                path.addCircle(centerX + width / 2, centerY * 1.5f, lowerCaseRadius, Path.Direction.CCW)

                path.moveTo(width * 1.08f, height * 0.48f)
                path.lineTo(width * 1.08f, height * 1.6f)
                binding.tlview.setLetter(path)
            }

            "O" -> {
                val commonRadius = 139.76f
                val lowerCaseRadius = 60.0f

                // Trace the uppercase 'O'
                path.moveTo(centerX, centerY - commonRadius)
                path.cubicTo(
                    centerX + commonRadius, centerY - commonRadius,
                    centerX + commonRadius, centerY + commonRadius,
                    centerX, centerY + commonRadius
                )
                path.cubicTo(
                    centerX - commonRadius, centerY + commonRadius,
                    centerX - commonRadius, centerY - commonRadius,
                    centerX, centerY - commonRadius
                )

                // Trace the lowercase 'o'
                path.moveTo(
                    centerX + width / 2,
                    centerY - commonRadius
                ) // Move to the starting point for the lowercase 'o'
                path.cubicTo(
                    centerX + width / 2 + commonRadius, centerY - commonRadius,
                    centerX + width / 2 + commonRadius, centerY + commonRadius,
                    centerX + width / 2, centerY + commonRadius
                )
                path.cubicTo(
                    centerX + width / 2 - commonRadius, centerY + commonRadius,
                    centerX + width / 2 - commonRadius, centerY - commonRadius,
                    centerX + width / 2, centerY - commonRadius
                )
                path.addCircle(centerX + width / 2, centerY, lowerCaseRadius, Path.Direction.CW)

                binding.tlview.setLetter(path)
            }


        }
        binding.tlview.addListener(object : AbcdkidsListener {
            override fun onDotTouched(progress: Float) {
                Log.e(TAG, "Progress: ${progress}")
            }

            override fun onSegmentFinished() {
                Log.e(TAG, "Segment Finished")
            }

            override fun onTraceFinished() {
                addUserProgress(lessonId, topicId)
                val parentLayout: RelativeLayout = findViewById(R.id.parentLayout)

                val inflater = LayoutInflater.from(this@NumberTracingActivity)
                val quizLayout = inflater.inflate(R.layout.layout_topic_finished, parentLayout, false) as RelativeLayout

                quizLayout.visibility = View.VISIBLE
                parentLayout.addView(quizLayout)
                Handler().postDelayed({
                    parentLayout.removeView(quizLayout)
                    val intent = Intent(this@NumberTracingActivity, LessonActivity::class.java)
                    intent.putExtra("subjectId", lessonId)
                    //intent.putExtra("topic", topicId)
                    //intent.putExtra("topicName", topicName)
                    startActivity(intent)
                }, 2600)
            }

        })
    }
    private fun addUserProgress(subjectId: String, topicId: String) {
        val userType = sharedPreferences.getString("userType", "").toString()

        if (userType == "learner") {
            val firstname = sharedPreferences.getString("firstName", "").toString()
            val lastname = sharedPreferences.getString("lastName", "").toString()
            val studentName =  lastname.uppercase() + ", " + firstname.lowercase().replaceFirstChar { it.uppercaseChar() }
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
}