package com.example.tagakaulolearningapp

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import com.airbnb.lottie.model.content.ShapeTrimPath
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy

class PracticeQuizActivity : AppCompatActivity() {
    private lateinit var parentLayout: RelativeLayout
    private lateinit var btnBack: ImageView
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var db: SQLiteDatabase
    private lateinit var userType: String
    private lateinit var studentName: String
    private lateinit var lessonId: String
    private lateinit var topicId: String
    private lateinit var topicName: String
    private lateinit var quizName: String
    private lateinit var playSoundFx: PlaySoundFx
    private var imagePath: String = ""
    private var currentIndex: Int = 0
    private var answer: String = ""
    private var ans: String = ""
    private var currentTries: Int = 0
    private var attempts: Int = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        actionBar?.hide()
        setContentView(R.layout.activity_practice_quiz)
        DeviceNavigationClass.hideNavigationBar(this)

        sharedPreferences = getSharedPreferences("MyPreferences", MODE_PRIVATE)
        userType = sharedPreferences.getString("userType", "").toString()
        val firstName = sharedPreferences.getString("firstName", "").toString()
        val lastName = sharedPreferences.getString("lastName", "").toString()

        playSoundFx = PlaySoundFx(this@PracticeQuizActivity)

        studentName = lastName.uppercase() + ", " + firstName.lowercase().replaceFirstChar { it.uppercaseChar() }
        db = openOrCreateDatabase("tagakauloDB", Context.MODE_PRIVATE, null)
        lessonId = intent.getStringExtra("subjectId").toString()
        topicId = intent.getStringExtra("topic").toString()
        topicName = intent.getStringExtra("topicName").toString()
        parentLayout = findViewById(R.id.parentLayout)
        getImage()
        loadActivityDisplay()
        //get the necessary topic
        //get the word
    }
    private fun loadActivityDisplay() {
        ans = getAnswer()
        when (currentIndex) {
            0 -> {
                multipleChoice(ans)
            }
            1 -> {
                playSoundFx.requestSFX(this@PracticeQuizActivity, "magaling")
                val inflater = LayoutInflater.from(this@PracticeQuizActivity)
                val quizLayout = inflater.inflate(R.layout.layout_topic_finished, parentLayout, false) as RelativeLayout

                quizLayout.visibility = View.VISIBLE
                parentLayout.addView(quizLayout)
                Handler().postDelayed({
                    parentLayout.removeAllViews()
                    spellingHint()
                }, 1620)

            }
            2 -> {
                showAnswer(ans)
                Handler().postDelayed({
                    spellingHint()
                }, 1620)
            }
        }
    }
    private fun getAnswer(): String {
        val tableName = "tbl_language"

        val query = "SELECT * FROM $tableName WHERE topicRef = ?"
        val topicArray = arrayOf(topicId)
        val topicCursor: Cursor = db.rawQuery(query, topicArray)

        if (topicCursor.moveToFirst()) {
            val columnIndexTopicId = topicCursor.getColumnIndex("kalagan")

            if (columnIndexTopicId != -1) {
                do {
                    answer = topicCursor.getString(columnIndexTopicId)
                } while (topicCursor.moveToNext())

            }
        }
        topicCursor.close()

        if (answer.trim() == "") {
            if (topicName.length == 1 ) {
                Toast.makeText(this@PracticeQuizActivity, "Not suitable for quiz practice", Toast.LENGTH_SHORT).show()
                addUserProgress(lessonId, topicId)
                val intent = Intent(this@PracticeQuizActivity, LessonActivity::class.java)
                intent.putExtra("subjectId", lessonId)
                startActivity(intent)
                finish()
            }
            else {
                val maxLength = 9
                answer = generateAnswer(topicName, maxLength)
            }
        }

        return this.answer
    }
    private fun getImage() {
        val tableName2 = "tbl_topic"

        val checkQuery2 = "SELECT * FROM $tableName2 WHERE topicId = '$topicId'"
        val topicCursor: Cursor = db.rawQuery(checkQuery2, null)

        if (topicCursor.moveToFirst()) {
            val columnIndexImagePath = topicCursor.getColumnIndex("imagePath")

            if (columnIndexImagePath != -1) {
                do {
                    this.imagePath = topicCursor.getString(columnIndexImagePath)
                    // Process the retrieved data here
                } while (topicCursor.moveToNext())
            }

        } else {

        }
        topicCursor.close()
    }
    private fun generateAnswer(topicName: String, maxLength: Int): String {
        val words = topicName.split(" ")
        val truncatedWords = mutableListOf<String>()

        var currentLength = 0
        for (word in words) {
            if (currentLength + word.length <= maxLength) {
                truncatedWords.add(word)
                currentLength += word.length + 1 // Add 1 for the space between words
            } else {
                break
            }
        }

        return truncatedWords.joinToString(" ")
    }
    private fun multipleChoice(correntAns: String) {
        val choices = getChoices(correntAns)
        val inflater = LayoutInflater.from(this@PracticeQuizActivity)
        val quizLayout = inflater.inflate(R.layout.layout_practice_multiple_choice, parentLayout, false) as RelativeLayout

        val btnBack = quizLayout.findViewById<ImageView>(R.id.btnBack)
        val imgDisplay = quizLayout.findViewById<ImageView>(R.id.imgDisplay)
        val choice1 = quizLayout.findViewById<Button>(R.id.btnChoice1)
        val choice2 = quizLayout.findViewById<Button>(R.id.btnChoice2)
        val choice3 = quizLayout.findViewById<Button>(R.id.btnChoice3)

        if(this.imagePath != "") {
            Glide.with(this@PracticeQuizActivity)
                .load(this.imagePath)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imgDisplay)
        }
        else {
            Glide.with(this@PracticeQuizActivity)
                .load(R.drawable.broken_media)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imgDisplay)
        }

        choice1.text = choices[0]
        choice2.text = choices[1]
        choice3.text = choices[2]

        parentLayout.addView(quizLayout)

        btnBack.setOnClickListener {
            val intent = Intent(this@PracticeQuizActivity, LessonActivity::class.java)
            intent.putExtra("subjectId", lessonId)
            startActivity(intent)
            finish()
        }

        var userAnswer = ""

        if (currentTries == 2) {
            parentLayout.removeAllViews()
            currentIndex = 2
            loadActivityDisplay()
        }
        else {
            choice1.setOnClickListener {
                userAnswer = choices[0]
                checkAnswer(userAnswer, correntAns)
                currentTries += 1
                attempts += 1
            }
            choice2.setOnClickListener {
                userAnswer = choices[1]
                checkAnswer(userAnswer, correntAns)
                currentTries += 1
                attempts += 1
            }
            choice3.setOnClickListener {
                userAnswer = choices[2]
                checkAnswer(userAnswer, correntAns)
                currentTries += 1
                attempts += 1
            }
        }
    }
    private fun spellingHint() {
        val intent = Intent(this@PracticeQuizActivity, SpellingActivity::class.java)
        intent.putExtra("subjectId", lessonId)
        intent.putExtra("topicId", topicId)
        intent.putExtra("topicName", topicName)
        startActivity(intent)
        finish()
    }
    private fun checkAnswer(userAnswer: String, correntAns: String) {
        if (userAnswer.uppercase() == correntAns.uppercase()) {
            currentIndex += 1
            //show merit
            guessOutcome(true)
            Handler().postDelayed({
                parentLayout.removeAllViews()
                loadActivityDisplay()
            }, 1000)
        }
        else {
            //show merit
            guessOutcome(false)
            Handler().postDelayed({
                parentLayout.removeAllViews()
                loadActivityDisplay()
            }, 1000)
        }
    }
    private fun guessOutcome(correct: Boolean) {
        val inflater = LayoutInflater.from(this@PracticeQuizActivity)
        val quizLayout = inflater.inflate(R.layout.layout_practice_show_outcome, parentLayout, false) as RelativeLayout

        val containerLayout = quizLayout.findViewById<RelativeLayout>(R.id.containerLayout)
        val txtOutcome = quizLayout.findViewById<TextView>(R.id.txtOutcome)

        if (correct) {
            playSoundFx.requestSFX(this@PracticeQuizActivity, "finished")
            txtOutcome.text = "You are correct!"
            //play correct audio
        }
        else {
            playSoundFx.requestSFX(this@PracticeQuizActivity, "wrong")
            txtOutcome.text = "Try again."
            //play wrong audio
        }
        parentLayout.addView(quizLayout)
    }
    private fun showAnswer(correntAns: String) {
        val inflater = LayoutInflater.from(this@PracticeQuizActivity)
        val quizLayout = inflater.inflate(R.layout.layout_practice_show_answer, parentLayout, false) as LinearLayout

        val txtDisplay = quizLayout.findViewById<TextView>(R.id.txtDisplay)
        val txtAnswer = quizLayout.findViewById<TextView>(R.id.txtAnswer)

        txtAnswer.text = correntAns

        parentLayout.addView(quizLayout)
        currentTries = 0
    }
    private fun getChoices(correctAns: String): List<String> {
        val eChoicesList = existingChoices()
        val choicesList = mutableListOf<String>()

        choicesList.clear()
        var choice = eChoicesList.filter { newChoice -> newChoice != correctAns }
        choicesList.addAll(choice.take(2))
        choicesList.add(correctAns)
        choicesList.shuffle()
        return choicesList
    }
    private fun existingChoices(): ArrayList<String> {
        val eChoiceList: ArrayList<String> = ArrayList()
        eChoiceList.clear()
        eChoiceList.add("Ambak")
        eChoiceList.add("Dilek")
        eChoiceList.add("Balyeg")
        eChoiceList.add("Labun")
        eChoiceList.add("Miyaw")
        eChoiceList.add("Nanag")
        return eChoiceList
    }
    private fun addUserProgress(subjectId: String, topicId: String) {
        val userType = sharedPreferences.getString("userType", "").toString()

        if (userType == "learner") {
            val firstname = sharedPreferences.getString("firstName", "").toString()
            val lastname = sharedPreferences.getString("lastName", "").toString()
            val studentName = lastname.uppercase() + ", " + firstname.lowercase().replaceFirstChar { it.uppercaseChar() }
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