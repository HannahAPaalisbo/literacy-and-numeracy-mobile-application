package com.example.tagakaulolearningapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView

class ScoreActivity : AppCompatActivity() {
    private lateinit var txtScore: TextView
    private lateinit var txtTotalScore: TextView
    private lateinit var btnRestartQuiz: Button
    private lateinit var btnHome: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        actionBar?.hide()

        setContentView(R.layout.activity_score)

        DeviceNavigationClass.hideNavigationBar(this)

        val lessonId = intent.getStringExtra("subject")
        val quizId = intent.getStringExtra("quizId")
        val score = intent.getIntExtra("score", 0)
        val totalScore = intent.getIntExtra("total score", 0)

        txtScore = findViewById(R.id.txtScore)
        txtTotalScore = findViewById(R.id.txtTotalScore)
        btnRestartQuiz = findViewById(R.id.btnRestartQuiz)
        btnHome = findViewById(R.id.btnHome)

        txtScore.text = score.toString()
        txtTotalScore.text = "out of " + totalScore.toString()

        btnRestartQuiz.setOnClickListener {
            val intent = Intent(this, QuizActivity::class.java)
            intent.putExtra("subject", lessonId)
            intent.putExtra("quizId", quizId)
            startActivity(intent)
            finish()
        }
        btnHome.setOnClickListener {
            val intent = Intent(this, LessonActivity::class.java)
            intent.putExtra("subject", lessonId)
            startActivity(intent)
            finish()
        }
    }
}