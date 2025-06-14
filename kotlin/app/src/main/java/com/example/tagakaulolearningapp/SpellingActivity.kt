package com.example.tagakaulolearningapp

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.media.Image
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.core.content.ContextCompat
import com.airbnb.lottie.LottieAnimationView
import okhttp3.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class SpellingActivity : AppCompatActivity() {
    private lateinit var playSoundFx: PlaySoundFx
    private lateinit var parentLayout: RelativeLayout
    private lateinit var btnImgView: ImageView
    private lateinit var txtInstruction: TextView
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var db: SQLiteDatabase
    private lateinit var userType: String
    private lateinit var studentName: String
    private lateinit var lessonId: String
    private lateinit var topicId: String
    private lateinit var topicName: String
    private lateinit var quizName: String
    private var userAnswerList: ArrayList<Char> = ArrayList()
    private var currentAnswer: Int = 0
    private var currentIndex: Int = 0
    private var answer: String = ""
    private var ans: String = ""
    private var currentTries: Int = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        actionBar?.hide()
        setContentView(R.layout.activity_spelling)

        DeviceNavigationClass.hideNavigationBar(this)

        sharedPreferences = getSharedPreferences("MyPreferences", MODE_PRIVATE)
        userType = sharedPreferences.getString("userType", "").toString()
        val firstName = sharedPreferences.getString("firstName", "").toString()
        val lastName = sharedPreferences.getString("lastName", "").toString()

        studentName = lastName.uppercase() + ", " + firstName.lowercase().replaceFirstChar { it.uppercaseChar() }
        db = openOrCreateDatabase("tagakauloDB", Context.MODE_PRIVATE, null)
        lessonId = intent.getStringExtra("subjectId").toString()
        topicId = intent.getStringExtra("topicId").toString()
        topicName = intent.getStringExtra("topicName").toString()

        playSoundFx = PlaySoundFx(this@SpellingActivity)

        btnImgView = findViewById(R.id.imgBack)
        btnImgView.setOnClickListener {
            val intent = Intent(this@SpellingActivity, LessonActivity::class.java)
            intent.putExtra("subjectId", lessonId)
            startActivity(intent)
            finish()
        }

        loadActivityDisplay()
    }
    private fun loadActivityDisplay() {
        parentLayout = findViewById(R.id.parentLayout)
        txtInstruction = findViewById(R.id.txtInstruction)
        currentAnswer = 0
        userAnswerList.clear()
        ans = getAnswer().uppercase()
        when (currentIndex) {
            0 -> {
                txtInstruction.text = "Try to spell out the answer with the guide"
                spellingHint(ans)
            }
            1 -> {
                txtInstruction.text = "Try to spell out the answer without any guide"
                spelling(ans)
            }
            else -> {
                addUserProgress(lessonId, topicId)
                updateCentralDBLearnerProgress()
                playSoundFx.requestSFX(this@SpellingActivity, "magaling")
                val inflater = LayoutInflater.from(this@SpellingActivity)
                val quizLayout = inflater.inflate(R.layout.layout_topic_finished, parentLayout, false) as RelativeLayout

                quizLayout.visibility = View.VISIBLE
                parentLayout.addView(quizLayout)
                Handler().postDelayed({
                    parentLayout.removeAllViews()
                    val intent = Intent(this@SpellingActivity, LessonActivity::class.java)
                    intent.putExtra("subjectId", lessonId)
                    startActivity(intent)
                    finish()
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

        if (answer != "") {
            return this.answer
        }
        else {
            val maxLength = 9

            answer = generateAnswer(topicName, maxLength)
            return this.answer
        }
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
    private fun spellingHint(correctAns: String) {
        val letters = getLetters(correctAns)
        val letterView = findViewById<GridLayout>(R.id.letterView)
        val buttonView = findViewById<GridLayout>(R.id.buttonView)

        val inflater = LayoutInflater.from(this@SpellingActivity)

        for (i in 0 until correctAns.toCharArray().size) {
            val inputLayout = inflater.inflate(R.layout.layout_practice_spelling_item, letterView, false) as RelativeLayout
            val letterDisplay = inputLayout.findViewById<TextView>(R.id.txtItem)
            val drawableBg = ContextCompat.getDrawable(this, R.drawable.def_container_bg)
            inputLayout.background = drawableBg
            val userInput = correctAns.toCharArray()
            letterDisplay.text = userInput[i].toString()
            val uniqueId = View.generateViewId()
            inputLayout.id = uniqueId
            letterView.addView(inputLayout)

            inputLayout.setOnClickListener {
                currentAnswer -= 1
                clearInputSpelling(currentIndex, currentAnswer, correctAns)
            }
        }

        for (j in 0 until letters.size) {
            val answerLayout = inflater.inflate(R.layout.layout_practice_spelling_item, buttonView, false) as RelativeLayout
            val letterDisplay = answerLayout.findViewById<TextView>(R.id.txtItem)

            val drawableBg = ContextCompat.getDrawable(this, R.drawable.def_btn_bg)
            val textColor = ContextCompat.getColor(this, R.color.white)
            letterDisplay.setTextColor(textColor)
            answerLayout.background = drawableBg

            letterDisplay.text = letters[j].toString()
            val uniqueId = View.generateViewId()
            answerLayout.id = uniqueId
            buttonView.addView(answerLayout)

            answerLayout.setOnClickListener {
                changeInputSpelling(letters, j, currentAnswer, correctAns)
                currentAnswer += 1
            }
        }
    }

    private fun spelling(correctAns: String) {
        val letters = getLetters(correctAns)
        val letterView = findViewById<GridLayout>(R.id.letterView)
        val buttonView = findViewById<GridLayout>(R.id.buttonView)

        val inflater = LayoutInflater.from(this@SpellingActivity)

        for (i in 0 until correctAns.toCharArray().size) {
            val inputLayout = inflater.inflate(R.layout.layout_practice_spelling_item, letterView, false) as RelativeLayout
            val letterDisplay = inputLayout.findViewById<TextView>(R.id.txtItem)
            val drawableBg = ContextCompat.getDrawable(this, R.drawable.def_container_bg)
            inputLayout.background = drawableBg
            letterDisplay.text = ""
            val uniqueId = View.generateViewId()
            inputLayout.id = uniqueId
            letterView.addView(inputLayout)

            inputLayout.setOnClickListener {
                currentAnswer -= 1
                clearInputSpelling(currentIndex, currentAnswer, correctAns)
            }
        }

        for (j in 0 until letters.size) {
            val answerLayout = inflater.inflate(R.layout.layout_practice_spelling_item, buttonView, false) as RelativeLayout
            val letterDisplay = answerLayout.findViewById<TextView>(R.id.txtItem)

            val drawableBg = ContextCompat.getDrawable(this, R.drawable.def_btn_bg)
            val textColor = ContextCompat.getColor(this, R.color.white)
            letterDisplay.setTextColor(textColor)
            answerLayout.background = drawableBg

            letterDisplay.text = letters[j].toString()
            val uniqueId = View.generateViewId()
            answerLayout.id = uniqueId
            buttonView.addView(answerLayout)

            answerLayout.setOnClickListener {
                changeInputSpelling(letters, j, currentAnswer, correctAns)
                currentAnswer += 1
            }
        }
    }

    private fun changeInputSpelling(letters: List<Char>, position: Int, currentAnswer: Int, correntAns: String) {
        val letterView = findViewById<GridLayout>(R.id.letterView)

        val childLetterView = letterView.getChildAt(currentAnswer)

        if (childLetterView is RelativeLayout) {
            val letterDisplay = childLetterView.findViewById<TextView>(R.id.txtItem)
            val textColor = ContextCompat.getColor(this, R.color.pri_font)
            letterDisplay.setTextColor(textColor)
            letterDisplay.text = letters[position].toString()
        }

        userAnswerList.add(letters[position])

        if ((currentAnswer+1) == correntAns.length) {
            val userAnswer = userAnswerList.joinToString("")
            checkAnswer(userAnswer, correntAns)
        }
    }

    private fun clearInputSpelling(currentIndex: Int, currentAnswer: Int, correctAns: String) {
        when (currentIndex) {
            0 -> {
                val letterView = findViewById<GridLayout>(R.id.letterView)

                val childLetterView = letterView.getChildAt(currentAnswer)
                val correntAnsList = correctAns.toCharArray()
                if (childLetterView is RelativeLayout) {
                    val letterDisplay = childLetterView.findViewById<TextView>(R.id.txtItem)
                    val textColor = ContextCompat.getColor(this, R.color.thr_font)
                    letterDisplay.setTextColor(textColor)

                    var position = userAnswerList.size-1
                    letterDisplay.text = correntAnsList[position].toString()
                }

                userAnswerList.removeAt(userAnswerList.size - 1)
            }
            1 -> {
                val letterView = findViewById<GridLayout>(R.id.letterView)

                val childLetterView = letterView.getChildAt(currentAnswer)
                val correntAnsList = correctAns.toCharArray()
                if (childLetterView is RelativeLayout) {
                    val letterDisplay = childLetterView.findViewById<TextView>(R.id.txtItem)
                    letterDisplay.text = ""
                }

                userAnswerList.removeAt(userAnswerList.size - 1)
            }
        }
    }
    private fun checkAnswer(userAnswer: String, correntAns: String) {
        if (currentIndex == 0) {
            val letterView = findViewById<GridLayout>(R.id.letterView)
            val buttonView = findViewById<GridLayout>(R.id.buttonView)
            if (userAnswer.uppercase() == correntAns.uppercase()) {
                //playSoundFx.requestSFX(this@SpellingActivity, "practiceCorrect")
                currentTries += 1
                //show merit
                guessOutcome(true)
                Handler().postDelayed({
                    letterView.removeAllViews()
                    buttonView.removeAllViews()
                    currentIndex = 1
                    loadActivityDisplay()
                }, 1000)
            }
            else {
                //show merit
                currentTries = 0
                guessOutcome(false)
                Handler().postDelayed({
                    letterView.removeAllViews()
                    buttonView.removeAllViews()
                    currentAnswer = 0
                    loadActivityDisplay()
                }, 1000)
            }
        }
        else {
            val letterView = findViewById<GridLayout>(R.id.letterView)
            val buttonView = findViewById<GridLayout>(R.id.buttonView)
            if (userAnswer.uppercase() == correntAns.uppercase()) {
                currentTries += 1
                currentIndex = 2
                letterView.removeAllViews()
                buttonView.removeAllViews()
                loadActivityDisplay()
            }
            else {
                //show merit
                currentIndex = 0
                currentTries = 0
                guessOutcome(false)
                Handler().postDelayed({
                    letterView.removeAllViews()
                    buttonView.removeAllViews()
                    loadActivityDisplay()
                }, 1000)
            }
        }
    }
    private fun guessOutcome(correct: Boolean) {
        val inflater = LayoutInflater.from(this@SpellingActivity)
        val quizLayout = inflater.inflate(R.layout.layout_practice_show_outcome, parentLayout, false) as RelativeLayout

        val containerLayout = quizLayout.findViewById<RelativeLayout>(R.id.containerLayout)
        val txtOutcome = quizLayout.findViewById<TextView>(R.id.txtOutcome)

        if (correct) {
            playSoundFx.requestSFX(this@SpellingActivity, "finished")
            txtOutcome.text = "You are correct"
            //play correct audio
        }
        else {
            playSoundFx.requestSFX(this@SpellingActivity, "wrong")
            txtOutcome.text = "Try again"
            //screen shake
            //play wrong audio
        }
        parentLayout.addView(quizLayout)
        Handler().postDelayed({
            parentLayout.removeView(quizLayout)
        }, 1000)
    }
    private fun getLetters(correctAns: String): List<Char> {
        val eLettersList = existingLetters()
        val lettersList = mutableListOf<Char>()

        val newCorrectAns = correctAns.toCharArray().asList()
        lettersList.clear()
        val choice = eLettersList.filter { newChoice ->
           newCorrectAns.none { it == newChoice }
        }

        val letterCount = correctAns.toCharArray()
        val newChoice = choice.shuffled().take(1)
        lettersList.addAll(newChoice)
        lettersList.addAll(letterCount.toList())
        lettersList.shuffle()
        return lettersList
    }
    private fun existingLetters(): ArrayList<Char> {
        val eLettersList: ArrayList<Char> = ArrayList()
        eLettersList.clear()
        eLettersList.add('A')
        eLettersList.add('E')
        eLettersList.add('É')
        eLettersList.add('I')
        eLettersList.add('O')
        eLettersList.add('U')
        return eLettersList
    }

    private fun addUserProgress(subjectId: String, topicId: String) {
        val userType = sharedPreferences.getString("userType", "").toString()

        if (userType == "learner") {
            val firstname = sharedPreferences.getString("firstName", "").toString()
            val lastname = sharedPreferences.getString("lastName", "").toString()
            val studentName = lastname.uppercase() + ", " + firstname.lowercase().replaceFirstChar { it.uppercaseChar() }
            val progress = 1
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
    private fun updateCentralDBLearnerProgress() {
        val learnerId = sharedPreferences.getString("learnerId","").toString()
        val fileAccess = "getLearnerProgress.php?learnerId=$learnerId&topicId=$topicId&dateEndTaken=${getDate()}"
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
                    Toast.makeText(this@SpellingActivity, "No connection", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }
    private fun getDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd")
        val currentDate = Date()
        return sdf.format(currentDate)
    }
}