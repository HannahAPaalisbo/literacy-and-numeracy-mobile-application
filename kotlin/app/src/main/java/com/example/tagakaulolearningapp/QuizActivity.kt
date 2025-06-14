package com.example.tagakaulolearningapp

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import okhttp3.*
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class QuizActivity : AppCompatActivity() {
    private lateinit var lessonId: String
    lateinit var topicId: String
    private lateinit var quizId: String
    private lateinit var quizName: String
    private lateinit var userType: String
    private lateinit var studentName: String
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var db: SQLiteDatabase
    private var number: Int = 0
    var totalScore: Double = 0.0
    private var userScore: Double = 0.0
    private var attempt = 0
    private var quizList: ArrayList<String> = ArrayList()
    private var userAnswerList: ArrayList<String> = ArrayList()
    private var correctAnswerList: ArrayList<String> = ArrayList()
    private var checkedAnswerList: ArrayList<String> = ArrayList()
    private var currentQuestionIndex: Int = 0
    private var recitationList: ArrayList<String> = ArrayList()
    private var recitationQuestionList: ArrayList<String> = ArrayList()
    private var recitationScoreList: ArrayList<Double> = ArrayList()
    private var hasAnswers: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        actionBar?.hide()

        setContentView(R.layout.activity_quiz)

        DeviceNavigationClass.hideNavigationBar(this)

        totalScore = 0.0
        recitationScoreList.clear()
        sharedPreferences = getSharedPreferences("MyPreferences", MODE_PRIVATE)
        userType = sharedPreferences.getString("userType", "").toString()
        val firstName = sharedPreferences.getString("firstName", "").toString()
        val lastName = sharedPreferences.getString("lastName", "").toString()
        studentName = lastName.uppercase() + ", " + firstName.lowercase().replaceFirstChar { it.uppercaseChar() }
        db = openOrCreateDatabase("tagakauloDB", Context.MODE_PRIVATE, null)
        lessonId = intent.getStringExtra("subjectId").toString()
        quizName = intent.getStringExtra("quizName").toString()

        getQuiz(quizName.toString())
        getTopics(lessonId)
        if (quizList.isNullOrEmpty()) {
            Toast.makeText(this, "No Quiz to take", Toast.LENGTH_SHORT).show()
            val intent = Intent(this@QuizActivity, QuizzesActivity::class.java)
            intent.putExtra("subject", lessonId)
            startActivity(intent)
            finish()
        }

        if (userType == "learner") {
            attempt = getAttempt()

            var tableName = "tbl_quiz"
            var checkQuery = "SELECT * FROM $tableName WHERE quizName = ?"
            var quizArray = arrayOf(quizName)
            val quizCursor: Cursor = db.rawQuery(checkQuery, quizArray)
            var quizAttempt = 0
            if (quizCursor.moveToFirst()) {
                val attemptColumnIndex = quizCursor.getColumnIndex("attempts")
                var thisQuizAttempt = 0
                if (attemptColumnIndex != -1) {
                    thisQuizAttempt = quizCursor.getInt(attemptColumnIndex)
                }
                quizAttempt = thisQuizAttempt
            }

            if (attempt < quizAttempt) {
                getQuizOption()
            }
            else {
                Toast.makeText(this, "No Quiz to take", Toast.LENGTH_SHORT).show()
                val intent = Intent(this@QuizActivity, QuizzesActivity::class.java)
                intent.putExtra("subject", lessonId)
                startActivity(intent)
                finish()
            }
        }
        else {
            getQuizOption()
        }
    }
    private fun getTopics(lessonId: String) {
        val tableName = "tbl_topic"

        val query = "SELECT * FROM $tableName WHERE subjectRef LIKE ?"
        val topicArray = arrayOf(lessonId)
        val topicCursor: Cursor = db.rawQuery(query, topicArray)

        if (topicCursor.moveToFirst()) {
            val columnIndexTopicId = topicCursor.getColumnIndex("topicId")

            if (columnIndexTopicId != -1) {
                do {
                    topicId = topicCursor.getString(columnIndexTopicId)
                    getQuiz(topicId)
                } while (topicCursor.moveToNext())

            }
        }
        topicCursor.close()
    }
    private fun getQuiz(quizName: String) {
        val tableName = "tbl_quiz"

        val query = "SELECT * FROM $tableName WHERE quizName = ?"
        var quizArray = arrayOf(quizName)
        val quizCursor: Cursor = db.rawQuery(query, quizArray)

        if (quizCursor.moveToFirst()) {
            val columnIndexQuizId = quizCursor.getColumnIndex("quizId")
            val columnIndexQuestion = quizCursor.getColumnIndex("question")
            val columnIndexImage = quizCursor.getColumnIndex("imagePath")
            val columnIndexSelectionA = quizCursor.getColumnIndex("selectionA")
            val columnIndexSelectionB = quizCursor.getColumnIndex("selectionB")
            val columnIndexSelectionC = quizCursor.getColumnIndex("selectionC")
            val columnIndexSelectionD = quizCursor.getColumnIndex("selectionD")
            val columnIndexScore = quizCursor.getColumnIndex("score")

            if (columnIndexQuizId != -1) {
                do {
                    number += 1
                    quizId = quizCursor.getString(columnIndexQuizId)
                    val question = quizCursor.getString(columnIndexQuestion)
                    var imagePath  = quizCursor.getString(columnIndexImage)
                    val selectionA = quizCursor.getString(columnIndexSelectionA)
                    val selectionB = quizCursor.getString(columnIndexSelectionB)
                    val selectionC = quizCursor.getString(columnIndexSelectionC)
                    val selectionD = quizCursor.getString(columnIndexSelectionD)
                    val score = quizCursor.getDouble(columnIndexScore)
                    totalScore += score
                    val quizData = arrayListOf("$number, $question, $imagePath, $selectionA, $selectionB, $selectionC, $selectionD, $score")
                    quizList.add(quizData.toString())
                } while (quizCursor.moveToNext())
            }
            else {

            }
        } else {

        }
        quizCursor.close()
    }
    private fun loadQuiz(quizOption: String) {
        when (quizOption) {
            "default" -> {
                if (currentQuestionIndex < quizList.size) {
                    val quizItem = quizList[currentQuestionIndex]
                    val cleanedQuizList = quizItem.replace("[", "").replace("]", "")
                    var elements = cleanedQuizList.split(", ")

                    val number = elements[0]
                    val question = elements[1]
                    val imgPath = elements[2]
                    val selectionA = elements[3].uppercase()
                    val selectionB = elements[4].uppercase()
                    val selectionC = elements[5].uppercase()
                    val selectionD = elements[6].uppercase()
                    val score = elements[7].toDouble()

                    if (selectionA != "" && selectionB != "") {
                        multipleChoice(quizOption, number, question, imgPath, selectionA, selectionB, selectionC, selectionD, score)
                    }
                    else if (selectionA == "TRUE" || selectionA == "FALSE" || selectionA == "TAMA" || selectionA == "MALI") {
                        trueOrFalse(quizOption, number, question, imgPath, selectionA, score)
                    }
                    else if (question != "" && selectionA == "") {
                        openEnded(quizOption, number, question, score)
                    }
                    else if (selectionA != "") {
                        identifcation(quizOption, number, question, imgPath, selectionA, score)
                    }
                    else {
                        noQuizType()
                    }
                }
                else {
                    Log.d("Taking Quiz Status", "checking answer")
                    editableAnswer()
                }
            }
            "display only" -> {
                if (currentQuestionIndex < quizList.size) {
                    val quizItem = quizList[currentQuestionIndex]
                    val cleanedQuizList = quizItem.replace("[", "").replace("]", "")
                    var elements = cleanedQuizList.split(", ")

                    val number = elements[0]
                    val question = elements[1]
                    val imgPath = elements[2]
                    val selectionA = elements[3].uppercase()
                    val selectionB = elements[4].uppercase()
                    val selectionC = elements[5].uppercase()
                    val selectionD = elements[6].uppercase()
                    val score = elements[7].toDouble()

                    if (selectionA != "" && selectionB != "") {
                        multipleChoice(quizOption, number, question, imgPath, selectionA, selectionB, selectionC, selectionD, score)
                    }
                    else if (selectionA == "TRUE" || selectionA == "FALSE" || selectionA == "TAMA" || selectionA == "MALI") {
                        trueOrFalse(quizOption, number, question, imgPath, selectionA, score)
                    }
                    else if (question != "" && selectionA == "") {
                        openEnded(quizOption, number, question, score)
                    }
                    else if (selectionA != "") {
                        identifcation(quizOption, number, question, imgPath, selectionA, score)
                    }
                    else {
                        noQuizType()
                    }
                }
                else {
                    Log.d("Taking Quiz Status", "finished")
                    showScore(quizOption)
                }
            }
            "recitation" -> {
                if (currentQuestionIndex < quizList.size) {
                    val quizItem = quizList[currentQuestionIndex]
                    val cleanedQuizList = quizItem.replace("[", "").replace("]", "")
                    var elements = cleanedQuizList.split(", ")

                    val number = elements[0]
                    val question = elements[1]
                    val imgPath = elements[2]
                    val selectionA = elements[3].uppercase()
                    val selectionB = elements[4].uppercase()
                    val selectionC = elements[5].uppercase()
                    val selectionD = elements[6].uppercase()
                    val score = elements[7].toDouble()

                    if (selectionA != "" && selectionB != "") {
                        multipleChoice(quizOption, number, question, imgPath, selectionA, selectionB, selectionC, selectionD, score)
                    }
                    else if (selectionA == "TRUE" || selectionA == "FALSE" || selectionA == "TAMA" || selectionA == "MALI") {
                        trueOrFalse(quizOption, number, question, imgPath, selectionA, score)
                    }
                    else if (question != "" && selectionA == "") {
                        openEnded(quizOption, number, question, score)
                    }
                    else if (selectionA != "") {
                        identifcation(quizOption, number, question, imgPath, selectionA, score)
                    }
                    else {
                        noQuizType()
                    }
                }
                else {
                    Log.d("Taking Quiz Status", "finished")
                    showScore(quizOption)
                }
            }
        }
    }
    private fun multipleChoice(quizOption: String, number: String, question: String, imgPath:String, selectionA: String, selectionB: String, selectionC: String, selectionD: String, points: Double) {
        val correctAns = selectionA

        val choices = mutableListOf<String>()
        choices.clear()
        choices.add(selectionA)
        choices.add(selectionB)
        choices.add(selectionC)
        //choices.add(selectionD)
        choices.shuffle()

        val parentLayout: RelativeLayout = findViewById(R.id.parentLayout)

        val inflater = LayoutInflater.from(this)

        val quizLayout = inflater.inflate(R.layout.layout_quiz_multiple_choice, parentLayout, false) as RelativeLayout
        val btnBack = quizLayout.findViewById<ImageView>(R.id.btnBack)
        val txtNumber = quizLayout.findViewById<TextView>(R.id.txtNumber)
        val txtQuestion = quizLayout.findViewById<TextView>(R.id.txtQuestion)
        val imgQuiz = quizLayout.findViewById<ImageView>(R.id.imgQuiz)
        val btnA = quizLayout.findViewById<Button>(R.id.btnSelectionA)
        val btnB = quizLayout.findViewById<Button>(R.id.btnSelectionB)
        val btnC = quizLayout.findViewById<Button>(R.id.btnSelectionC)

        txtNumber.text = number
        txtQuestion.text = question

        btnBack.setOnClickListener {
            showMessageBox("multipleChoice")
        }

        val imageUrl = getImage(imgPath)
        Glide.with(this@QuizActivity)
            .load(imageUrl)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(imgQuiz)

        btnA.text = choices[0]
        btnB.text = choices[1]
        btnC.text = choices[2]

        if (quizOption == "display only" && currentQuestionIndex > 0) {
            parentLayout.addView(quizLayout)

            val previousLayout = RelativeLayout(this)
            previousLayout.layoutParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            )
            val btnPrevious = ImageView(this)
            Glide.with(this)
                .load(R.drawable.quiz_display_only_prev)
                .into(btnPrevious)
            val layoutParams = RelativeLayout.LayoutParams(
                162,
                162
            )
            layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL)
            layoutParams.addRule(RelativeLayout.CENTER_VERTICAL)
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT)
            btnPrevious.layoutParams = layoutParams

            parentLayout.addView(btnPrevious)

            val btnNext = ImageView(this)
            Glide.with(this)
                .load(R.drawable.quiz_display_only_next)
                .into(btnNext)
            val nextParams = RelativeLayout.LayoutParams(
                162,
                162
            )
            nextParams.addRule(RelativeLayout.CENTER_HORIZONTAL)
            nextParams.addRule(RelativeLayout.CENTER_VERTICAL)
            nextParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
            btnNext.layoutParams = nextParams

            parentLayout.addView(btnNext)

            btnPrevious.setOnClickListener {
                currentQuestionIndex -= 1

                displayOnly()

                parentLayout.removeView(btnPrevious)
                parentLayout.removeView(btnNext)
                parentLayout.removeView(quizLayout)
            }
            btnNext.setOnClickListener {
                currentQuestionIndex += 1

                displayOnly()

                parentLayout.removeView(btnPrevious)
                parentLayout.removeView(btnNext)
                parentLayout.removeView(quizLayout)
            }
        }
        else if (quizOption == "display only" && currentQuestionIndex == 0) {
            parentLayout.addView(quizLayout)

            val previousLayout = RelativeLayout(this)
            previousLayout.layoutParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            )

            val btnNext = ImageView(this)
            Glide.with(this)
                .load(R.drawable.quiz_display_only_next)
                .into(btnNext)
            val nextParams = RelativeLayout.LayoutParams(
                162,
                162
            )
            nextParams.addRule(RelativeLayout.CENTER_HORIZONTAL)
            nextParams.addRule(RelativeLayout.CENTER_VERTICAL)
            nextParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
            btnNext.layoutParams = nextParams

            parentLayout.addView(btnNext)

            btnNext.setOnClickListener {
                currentQuestionIndex += 1

                displayOnly()

                parentLayout.removeView(btnNext)
                parentLayout.removeView(quizLayout)
            }
        }
        else if (quizOption == "recitation") {
            parentLayout.addView(quizLayout)
            btnA.setOnClickListener {
                recitationQuestionList.add(question)
                checkAnswer(quizOption, correctAns, choices[0].uppercase(), points)
            }
            btnB.setOnClickListener {
                recitationQuestionList.add(question)
                checkAnswer(quizOption, correctAns, choices[1].uppercase(), points)
            }
            btnC.setOnClickListener {
                recitationQuestionList.add(question)
                checkAnswer(quizOption, correctAns, choices[2].uppercase(), points)
            }
        }
        else {
            parentLayout.addView(quizLayout)
            var userAnswer: String
            btnA.setOnClickListener {
                recitationQuestionList.add(question)
                userAnswer = choices[0].uppercase()
                val recordAns = arrayListOf("$userAnswer", "$correctAns", "$points")
                userAnswerList.add(currentQuestionIndex, recordAns.toString())
                currentQuestionIndex += 1
                parentLayout.removeView(quizLayout)
                if (!hasAnswers) { default() }
                else { editableAnswer() }
            }
            btnB.setOnClickListener {
                recitationQuestionList.add(question)
                userAnswer = choices[1].uppercase()
                val recordAns = arrayListOf("$userAnswer", "$correctAns", "$points")
                userAnswerList.add(currentQuestionIndex, recordAns.toString())
                currentQuestionIndex += 1
                parentLayout.removeView(quizLayout)
                if (!hasAnswers) { default() }
                else { editableAnswer() }
            }
            btnC.setOnClickListener {
                recitationQuestionList.add(question)
                userAnswer = choices[2].uppercase()
                val recordAns = arrayListOf("$userAnswer", "$correctAns", "$points")
                userAnswerList.add(currentQuestionIndex, recordAns.toString())
                currentQuestionIndex += 1
                parentLayout.removeView(quizLayout)
                if (!hasAnswers) { default() }
                else { editableAnswer() }
            }
        }
    }
    private fun trueOrFalse(quizOption: String, number: String, question: String, imgPath: String, correctAnswer: String, points: Double) {
        val correctAns = correctAnswer

        val parentLayout: RelativeLayout = findViewById(R.id.parentLayout)

        val inflater = LayoutInflater.from(this)

        val quizLayout = inflater.inflate(R.layout.layout_quiz_true_or_false, parentLayout, false) as RelativeLayout
        val btnBack = quizLayout.findViewById<ImageView>(R.id.btnBack)
        val txtNumber = quizLayout.findViewById<TextView>(R.id.txtNumber)
        val txtQuestion = quizLayout.findViewById<TextView>(R.id.txtQuestion)
        val imgQuiz = quizLayout.findViewById<ImageView>(R.id.imgQuiz)
        val btnSubmit = quizLayout.findViewById<Button>(R.id.btnTrue)
        val btnFalse = quizLayout.findViewById<Button>(R.id.btnFalse)

        txtNumber.text = number
        txtQuestion.text = question

        btnBack.setOnClickListener {
            showMessageBox("trueOrFalse")
        }

        val imageUrl = getImage(imgPath)
        Glide.with(this@QuizActivity)
            .load(imageUrl)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(imgQuiz)

        if (quizOption == "display only" && currentQuestionIndex > 0) {
            parentLayout.addView(quizLayout)

            val previousLayout = RelativeLayout(this)
            previousLayout.layoutParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            )
            val btnPrevious = ImageView(this)
            Glide.with(this)
                .load(R.drawable.quiz_display_only_prev)
                .into(btnPrevious)
            val layoutParams = RelativeLayout.LayoutParams(
                162,
                162
            )
            layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL)
            layoutParams.addRule(RelativeLayout.CENTER_VERTICAL)
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT)
            btnPrevious.layoutParams = layoutParams

            parentLayout.addView(btnPrevious)

            val btnNext = ImageView(this)
            Glide.with(this)
                .load(R.drawable.quiz_display_only_next)
                .into(btnNext)
            val nextParams = RelativeLayout.LayoutParams(
                162,
                162
            )
            nextParams.addRule(RelativeLayout.CENTER_HORIZONTAL)
            nextParams.addRule(RelativeLayout.CENTER_VERTICAL)
            nextParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
            btnNext.layoutParams = nextParams

            parentLayout.addView(btnNext)

            btnPrevious.setOnClickListener {
                currentQuestionIndex -= 1

                displayOnly()

                parentLayout.removeView(btnPrevious)
                parentLayout.removeView(btnNext)
                parentLayout.removeView(quizLayout)
            }
            btnNext.setOnClickListener {
                currentQuestionIndex += 1

                displayOnly()

                parentLayout.removeView(btnPrevious)
                parentLayout.removeView(btnNext)
                parentLayout.removeView(quizLayout)
            }
        }
        else if (quizOption == "display only" && currentQuestionIndex == 0) {
            parentLayout.addView(quizLayout)

            val previousLayout = RelativeLayout(this)
            previousLayout.layoutParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            )

            val btnNext = ImageView(this)
            Glide.with(this)
                .load(R.drawable.quiz_display_only_next)
                .into(btnNext)
            val nextParams = RelativeLayout.LayoutParams(
                162,
                162
            )
            nextParams.addRule(RelativeLayout.CENTER_HORIZONTAL)
            nextParams.addRule(RelativeLayout.CENTER_VERTICAL)
            nextParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
            btnNext.layoutParams = nextParams

            parentLayout.addView(btnNext)
            btnNext.setOnClickListener {
                currentQuestionIndex += 1

                displayOnly()

                parentLayout.removeView(btnNext)
                parentLayout.removeView(quizLayout)
            }
        }
        else if (quizOption == "recitation") {
            parentLayout.addView(quizLayout)
            btnSubmit.setOnClickListener {
                recitationQuestionList.add(question)
                checkAnswer(quizOption, correctAns, "TRUE", points)
            }
            btnFalse.setOnClickListener {
                recitationQuestionList.add(question)
                checkAnswer(quizOption, correctAns, "FALSE", points)
            }
        }
        else {
            parentLayout.addView(quizLayout)
            var userAnswer: String
            btnSubmit.setOnClickListener {
                recitationQuestionList.add(question)
                userAnswer = "TRUE"
                val recordAns = arrayListOf("$userAnswer", "$correctAns", "$points")
                userAnswerList.add(currentQuestionIndex, recordAns.toString())
                currentQuestionIndex += 1
                parentLayout.removeView(quizLayout)
                if (!hasAnswers) { default() }
                else { editableAnswer() }
            }
            btnFalse.setOnClickListener {
                recitationQuestionList.add(question)
                userAnswer = "FALSE"
                val recordAns = arrayListOf("$userAnswer", "$correctAns", "$points")
                userAnswerList.add(currentQuestionIndex, recordAns.toString())
                currentQuestionIndex += 1
                parentLayout.removeView(quizLayout)
                if (!hasAnswers) { default() }
                else { editableAnswer() }
            }
        }
    }
    private fun identifcation(quizOption: String, number: String, question: String, imgPath: String, correctAnswer: String, points: Double) {
        val correctAns = correctAnswer.lowercase().trim()

        val parentLayout: RelativeLayout = findViewById(R.id.parentLayout)

        val inflater = LayoutInflater.from(this)

        val quizLayout = inflater.inflate(R.layout.layout_quiz_identification, parentLayout, false) as RelativeLayout
        val btnBack = quizLayout.findViewById<ImageView>(R.id.btnBack)
        val txtNumber = quizLayout.findViewById<TextView>(R.id.txtNumber)
        val txtQuestion = quizLayout.findViewById<TextView>(R.id.txtQuestion)
        val imgQuiz = quizLayout.findViewById<ImageView>(R.id.imgQuiz)
        val etAnswer = quizLayout.findViewById<EditText>(R.id.etAnswer)
        val btnSubmit = quizLayout.findViewById<Button>(R.id.btnSubmit)

        txtNumber.text = number
        txtQuestion.text = question

        btnBack.setOnClickListener {
            showMessageBox("identification")
        }

        val imageUrl = getImage(imgPath)
        Glide.with(this@QuizActivity)
            .load(imageUrl)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(imgQuiz)

        if (quizOption == "display only" && currentQuestionIndex > 0) {
            parentLayout.addView(quizLayout)

            val previousLayout = RelativeLayout(this)
            previousLayout.layoutParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            )
            val btnPrevious = ImageView(this)
            Glide.with(this)
                .load(R.drawable.quiz_display_only_prev)
                .into(btnPrevious)
            val layoutParams = RelativeLayout.LayoutParams(
                162,
                162
            )
            layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL)
            layoutParams.addRule(RelativeLayout.CENTER_VERTICAL)
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT)
            btnPrevious.layoutParams = layoutParams

            parentLayout.addView(btnPrevious)

            val btnNext = ImageView(this)
            Glide.with(this)
                .load(R.drawable.quiz_display_only_next)
                .into(btnNext)
            val nextParams = RelativeLayout.LayoutParams(
                162,
                162
            )
            nextParams.addRule(RelativeLayout.CENTER_HORIZONTAL)
            nextParams.addRule(RelativeLayout.CENTER_VERTICAL)
            nextParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
            btnNext.layoutParams = nextParams

            parentLayout.addView(btnNext)

            btnPrevious.setOnClickListener {
                currentQuestionIndex -= 1

                displayOnly()

                parentLayout.removeView(btnPrevious)
                parentLayout.removeView(btnNext)
                parentLayout.removeView(quizLayout)
            }
            btnNext.setOnClickListener {
                currentQuestionIndex += 1

                displayOnly()

                parentLayout.removeView(btnPrevious)
                parentLayout.removeView(btnNext)
                parentLayout.removeView(quizLayout)
            }
        }
        else if (quizOption == "display only" && currentQuestionIndex == 0) {
            parentLayout.addView(quizLayout)

            val previousLayout = RelativeLayout(this)
            previousLayout.layoutParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            )

            val btnNext = ImageView(this)
            Glide.with(this)
                .load(R.drawable.quiz_display_only_next)
                .into(btnNext)
            val nextParams = RelativeLayout.LayoutParams(
                162,
                162
            )
            nextParams.addRule(RelativeLayout.CENTER_HORIZONTAL)
            nextParams.addRule(RelativeLayout.CENTER_VERTICAL)
            nextParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
            btnNext.layoutParams = nextParams

            parentLayout.addView(btnNext)

            btnNext.setOnClickListener {
                currentQuestionIndex += 1

                displayOnly()

                parentLayout.removeView(btnNext)
                parentLayout.removeView(quizLayout)
            }
        }
        else if (quizOption == "recitation") {
            parentLayout.addView(quizLayout)
            val answer = etAnswer.text
            btnSubmit.setOnClickListener {
                recitationQuestionList.add(question)
                checkAnswer(quizOption, correctAnswer, (answer.toString().lowercase()), points)
            }
        }
        else {
            btnBack.setOnClickListener {
                showMessageBox("identification")
            }

            parentLayout.addView(quizLayout)
            var userAnswer: String
            val answer = etAnswer.text
            btnSubmit.setOnClickListener {
                recitationQuestionList.add(question)
                userAnswer = answer.toString().lowercase()
                val recordAns = arrayListOf("$userAnswer", "$correctAns", "$points")
                userAnswerList.add(currentQuestionIndex, recordAns.toString())
                parentLayout.removeView(quizLayout)
                currentQuestionIndex += 1
                if (!hasAnswers) { default() }
                else { editableAnswer() }
            }
        }
    }
    private fun openEnded(quizOption: String, number: String, question: String, points: Double) {
        val parentLayout: RelativeLayout = findViewById(R.id.parentLayout)

        val inflater = LayoutInflater.from(this)

        val quizLayout = inflater.inflate(R.layout.layout_quiz_open_ended, parentLayout, false) as RelativeLayout
        val txtNumber = quizLayout.findViewById<TextView>(R.id.txtNumber)
        val txtQuestion = quizLayout.findViewById<TextView>(R.id.txtQuestion)
        val btnContinue = quizLayout.findViewById<Button>(R.id.btnContinue)

        txtNumber.text = number
        txtQuestion.text = question

        if (quizOption == "display only" && currentQuestionIndex > 0) {
            val previousLayout = RelativeLayout(this)
            previousLayout.layoutParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            )
            val btnPrevious = Button(this)
            btnPrevious.text = "Previous Question"
            btnPrevious.layoutParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            )
            val nextLayout = RelativeLayout(this)
            nextLayout.layoutParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            )
            val btnNext = Button(this)

            btnNext.text = "Next Question"
            val buttonParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            )
            btnNext.layoutParams = buttonParams
            buttonParams.setMargins(25, 16, 16, 16)

            previousLayout.addView(btnPrevious)
            nextLayout.addView(btnNext)
            parentLayout.addView(previousLayout)
            parentLayout.addView(quizLayout)
            parentLayout.addView(nextLayout)

            btnPrevious.setOnClickListener {
                currentQuestionIndex -= 1

                displayOnly()

                parentLayout.removeView(previousLayout)
                parentLayout.removeView(quizLayout)
                parentLayout.removeView(nextLayout)
            }
            btnNext.setOnClickListener {
                currentQuestionIndex += 1

                displayOnly()

                parentLayout.removeView(previousLayout)
                parentLayout.removeView(quizLayout)
                parentLayout.removeView(nextLayout)
            }
        }
        else if (quizOption == "display only" && currentQuestionIndex == 0) {
            val nextLayout = RelativeLayout(this)
            nextLayout.layoutParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            )
            val btnNext = Button(this)
            btnNext.text = "Next Question"
            btnNext.layoutParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            )
            nextLayout.addView(btnNext)
            parentLayout.addView(quizLayout)
            parentLayout.addView(nextLayout)

            btnNext.setOnClickListener {
                currentQuestionIndex += 1

                displayOnly()

                parentLayout.removeView(quizLayout)
                parentLayout.removeView(nextLayout)
            }
        }
        else if (quizOption == "recitation") {
            parentLayout.addView(quizLayout)
            btnContinue.setOnClickListener {
                recitationQuestionList.add(question)
                checkAnswer(quizOption, "", "", points)
                parentLayout.removeView(quizLayout)
            }
        }
        else {
            parentLayout.addView(quizLayout)
            var userAnswer: String
            btnContinue.setOnClickListener {
                recitationQuestionList.add(question)
                userAnswer = ""
                val recordAns = arrayListOf("$userAnswer", "", "$points")
                userAnswerList.add(recordAns.toString())
                parentLayout.removeView(quizLayout)
                currentQuestionIndex += 1
                if (!hasAnswers) { default() }
                else { editableAnswer() }
            }
        }
    }
    private fun noQuizType() {
        Toast.makeText(this, "no Quiz", Toast.LENGTH_SHORT).show()
    }
    private fun getImage(imagePath: String): String {
        var newImagePath = ""
        if (imagePath == "") {
            newImagePath = "R.drawable.no_media"
            return newImagePath
        }

        else {
            val url = imagePath
            newImagePath = url
            return newImagePath
        }


    }
    private fun checkAnswer(quizOption: String, correctAns: String, selectedAnswer: String, points: Double) {
        correctAns.uppercase()
        selectedAnswer.uppercase()

        if (quizOption == "default") {
            var isCorrect = false
            currentQuestionIndex += 1
            if (selectedAnswer == correctAns) {
                userScore += points
                isCorrect = true
            }
            val checkedAnswer = arrayListOf("$selectedAnswer", "$isCorrect")
            checkedAnswerList.add(checkedAnswer.toString())
        }
        else if (quizOption == "display only") {
            currentQuestionIndex += 1
            recitationScoreList.add(points)
            displayOnly()
        }
        else if (quizOption == "recitation") {
            if (correctAns == "" && selectedAnswer == "") {
                val parentLayout: RelativeLayout = findViewById(R.id.parentLayout)
                if (selectedAnswer == correctAns) {
                    currentQuestionIndex += 1
                    recitationScoreList.add(points)
                    parentLayout.removeAllViews()
                    showMerit(true)
                } else {
                    currentQuestionIndex += 1
                    recitationScoreList.add(points)
                    parentLayout.removeAllViews()
                    showMerit(false)
                }
            }
            else {
                if (selectedAnswer.uppercase() == correctAns.uppercase()) {
                    currentQuestionIndex += 1
                    recitationScoreList.add(points)
                    showVerifyBox(quizOption, true)
                }
                else {
                    currentQuestionIndex
                    recitationScoreList.add(0.0)
                    showVerifyBox(quizOption, false)
                }
            }
        }
        else {
            //do nothing
        }
    }
    private fun getLearner() {
        if (currentQuestionIndex < quizList.size) {
            val parentLayout: RelativeLayout = findViewById(R.id.parentLayout)

            val inflater = LayoutInflater.from(this)

            val learnerInputLayout = inflater.inflate(R.layout.layout_quiz_recitation_learner, parentLayout, false) as RelativeLayout
            val etFirstName = learnerInputLayout.findViewById<EditText>(R.id.etFirstName)
            val etLastName = learnerInputLayout.findViewById<EditText>(R.id.etLastName)
            val btnSubmit = learnerInputLayout.findViewById<Button>(R.id.btnAgree)

            parentLayout.addView(learnerInputLayout)

            btnSubmit.setOnClickListener {
                val firstName = etFirstName.text.toString()
                val lastName = etLastName.text.toString()
                if (firstName.isEmpty() || lastName.isEmpty() ) {
                    Toast.makeText(this, "Please provide your name", Toast.LENGTH_LONG).show()
                } else {
                    val lastname = etLastName.text.toString().trim().uppercase()
                    val firstname = etFirstName.text.toString().trim().lowercase().replaceFirstChar { it.uppercaseChar() }
                    val cleanedName = lastname + ", " + firstname
                    recitationList.add(cleanedName)
                    parentLayout.removeView(learnerInputLayout)
                }

            }
        }
        else {
            recitationType()
        }
    }
    private fun default() { loadQuiz("default") }
    private fun displayOnly() { loadQuiz("display only") }
    private fun recitationType() { loadQuiz("recitation") }
    private fun editableAnswer() {
        hasAnswers = true
        val parentLayout: RelativeLayout = findViewById(R.id.parentLayout)

        val inflater = LayoutInflater.from(this)
        val quizLayout = inflater.inflate(R.layout.layout_quiz_edit, parentLayout, false) as RelativeLayout
        val checkLayout = quizLayout.findViewById<ScrollView>(R.id.edit_layout)
        val btnSubmit = quizLayout.findViewById<Button>(R.id.btnSubmit)
        val containerLayout = LinearLayout(this)
        containerLayout.orientation = LinearLayout.VERTICAL
        for (i in 0 until quizList.size) {
            val quizItem = quizList[i]
            val cleanedQuizList = quizItem.replace("[", "").replace("]", "")
            var elements = cleanedQuizList.split(", ")

            val ansItem = userAnswerList[i]
            val cleanedAnswerList = ansItem.replace("[", "").replace("]", "")
            var elements1 = cleanedAnswerList.split(", ")

            val question = elements[1]
            var userAnswer = elements1[0]

            if (userAnswer == "Bonus") {
                val txtQuestion = TextView(this)
                txtQuestion.text = question
                txtQuestion.layoutParams = RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT
                )

                val txtAnswer = TextView(this)
                txtAnswer.text = userAnswer
                txtAnswer.layoutParams = RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT
                )
                checkLayout.addView(txtQuestion)
                checkLayout.addView(txtAnswer)
            }
            else {
                val quizItemLayout = inflater.inflate(R.layout.layout_quiz_edit_item, parentLayout, false) as RelativeLayout
                val txtNumber = quizItemLayout.findViewById<TextView>(R.id.txtNumber)
                val txtQuestion = quizItemLayout.findViewById<TextView>(R.id.txtQuestion)
                val txtAnswer = quizItemLayout.findViewById<TextView>(R.id.txtAnswer)
                val btnSubmit = quizItemLayout.findViewById<Button>(R.id.btnSubmit)

                txtNumber.text = (i+1).toString()
                txtQuestion.text = question
                txtAnswer.text = userAnswer

                btnSubmit.setOnClickListener {
                    currentQuestionIndex = i
                    userAnswerList.removeAt(i)
                    parentLayout.removeView(quizLayout)
                    default()
                }

                containerLayout.addView(quizItemLayout)
            }
        }

        btnSubmit.setOnClickListener {
            showVerifyBox("default", false)
        }
        checkLayout.addView(containerLayout)
        parentLayout.addView(quizLayout)
    }
    private fun showScore(quizOption: String) {
        if (quizOption == "default")    {
            addUserQuizProgress(userType, lessonId, quizName, userScore, 1)
            updateCentralDBLearnerProgress(userScore.toString())
            val parentLayout: RelativeLayout = findViewById(R.id.parentLayout)

            val inflater = LayoutInflater.from(this)
            val quizLayout = inflater.inflate(R.layout.layout_quiz_score, parentLayout, false) as RelativeLayout
            val txtScore = quizLayout.findViewById<TextView>(R.id.txtScore)
            val txtLabel2 = quizLayout.findViewById<TextView>(R.id.txtLabel2)
            val btnRestartQuiz = quizLayout.findViewById<ImageView>(R.id.btnRestart)
            val btnViewAnswers = quizLayout.findViewById<ImageView>(R.id.btnView)
            val btnHome = quizLayout.findViewById<ImageView>(R.id.btnHome)

            txtScore.text = userScore.toInt().toString()
            txtLabel2.text = "out of " + totalScore.toInt().toString()

            btnRestartQuiz.setOnClickListener {
                println("checking attempt: $attempt")

                var tableName = "tbl_quiz"
                var checkQuery = "SELECT * FROM $tableName WHERE quizName = ?"
                var quizArray = arrayOf(quizName)
                val quizCursor: Cursor = db.rawQuery(checkQuery, quizArray)
                var quizAttempt = 0
                if (quizCursor.moveToFirst()) {
                    val attemptColumnIndex = quizCursor.getColumnIndex("attempts")
                    var thisQuizAttempt = 0
                    if (attemptColumnIndex != -1) {
                        thisQuizAttempt = quizCursor.getInt(attemptColumnIndex)
                    }
                    quizAttempt = thisQuizAttempt
                }

                if (attempt == quizAttempt) {
                    showMessageBox("restart")
                }
                else {
                    currentQuestionIndex = 0
                    userScore = 0.0
                    default()
                }
            }

            btnViewAnswers.setOnClickListener {
                parentLayout.removeView(quizLayout)
                showCheckedAnswers("default")
            }

            btnHome.setOnClickListener {
                showMessageBox("home")
            }

            parentLayout.addView(quizLayout)
        }

        else if (quizOption == "display only") {
            addUserQuizProgress(userType, lessonId, quizName, userScore, 0)
            val parentLayout: RelativeLayout = findViewById(R.id.parentLayout)

            val inflater = LayoutInflater.from(this)
            val quizLayout = inflater.inflate(R.layout.layout_quiz_score, parentLayout, false) as RelativeLayout
            val txtLabel = quizLayout.findViewById<TextView>(R.id.txtLabel)
            val txtScore = quizLayout.findViewById<TextView>(R.id.txtScore)
            val txtLabel2 = quizLayout.findViewById<TextView>(R.id.txtLabel2)
            val btnAddScores = quizLayout.findViewById<ImageView>(R.id.btnRestart)
            val btnViewAnswers = quizLayout.findViewById<ImageView>(R.id.btnView)
            val btnHome = quizLayout.findViewById<ImageView>(R.id.btnHome)

            txtLabel2.visibility = View.GONE
            btnViewAnswers.visibility = View.GONE

            txtLabel.text = "Total Score is"
            txtScore.text = totalScore.toInt().toString()

            Glide.with(this)
                .load(R.drawable.ic_add_grade)
                .into(btnAddScores)

            btnAddScores.setOnClickListener {
                addDataToGrades(lessonId, "", "", "")
            }

            btnHome.setOnClickListener {
                val intent = Intent(this, QuizzesActivity::class.java)
                intent.putExtra("subject", lessonId)
                startActivity(intent)
                finish()
            }

            parentLayout.addView(quizLayout)
        }
        else {
            addUserQuizProgress(userType, lessonId, quizName, userScore, 0)

            for (i in 0 until recitationScoreList.size) {
                addDataToGrades(lessonId, recitationList[i], recitationScoreList[i].toString(), "Recitation")
            }

            val parentLayout: RelativeLayout = findViewById(R.id.parentLayout)

            val inflater = LayoutInflater.from(this)
            val quizLayout = inflater.inflate(R.layout.layout_quiz_score, parentLayout, false) as RelativeLayout
            val txtLabel = quizLayout.findViewById<TextView>(R.id.txtLabel)
            val txtScore = quizLayout.findViewById<TextView>(R.id.txtScore)
            val txtLabel2 = quizLayout.findViewById<TextView>(R.id.txtLabel2)
            val btnRestartQuiz = quizLayout.findViewById<ImageView>(R.id.btnRestart)
            val btnViewAnswers = quizLayout.findViewById<ImageView>(R.id.btnView)
            val btnHome = quizLayout.findViewById<ImageView>(R.id.btnHome)

            txtLabel2.visibility = View.GONE

            txtLabel.text = "No. of students participated"
            txtScore.text = recitationList.distinct().size.toString()

            btnRestartQuiz.setOnClickListener {
                currentQuestionIndex = 0
                recitationList.clear()
                recitationScoreList.clear()
                recitationType()
                getLearner()
            }

            btnViewAnswers.setOnClickListener {
                parentLayout.removeView(quizLayout)
                showCheckedAnswers("recitation")
            }

            btnHome.setOnClickListener {
                showMessageBox("home")
            }

            parentLayout.addView(quizLayout)
        }
    }
    private fun getQuizOption(){
        if (userType == "learner") {
            default()
        }
        else {
            val parentLayout: RelativeLayout = findViewById(R.id.parentLayout)

            val inflater = LayoutInflater.from(this)
            val quizLayout = inflater.inflate(R.layout.layout_quiz_option, parentLayout, false) as RelativeLayout
            val btnBack = quizLayout.findViewById<ImageView>(R.id.imgBack)
            val txtDisplay = quizLayout.findViewById<TextView>(R.id.txtTeacher)
            val btnDisplayOnly = quizLayout.findViewById<Button>(R.id.btnDisplayOnly)
            val btnRecitation = quizLayout.findViewById<Button>(R.id.btnRecitation)

            btnBack.setOnClickListener {
                val intent = Intent(this, QuizzesActivity::class.java)
                startActivity(intent)
                finish()
            }

            txtDisplay.text = "Quiz: " + "$quizName"

            val uniqueId = View.generateViewId()
            quizLayout.id = uniqueId
            parentLayout.addView(quizLayout)

            btnDisplayOnly.setOnClickListener {
                displayOnly()
                parentLayout.removeView(quizLayout)
            }
            btnRecitation.setOnClickListener {
                recitationType()
                getLearner()
                parentLayout.removeView(quizLayout)
            }
        }
    }
    private fun showMerit(correctAns: Boolean) {
        if (correctAns) {
            val currentIndex = (recitationList.size) - 1
            val learner = recitationList[currentIndex]
            val name = learner.split(Regex("[,\\s]+"))
            val firstname = name[1].lowercase().replaceFirstChar { it.uppercaseChar() }
            val parentLayout: RelativeLayout = findViewById(R.id.parentLayout)

            val inflater = LayoutInflater.from(this)
            val quizLayout = inflater.inflate(R.layout.layout_quiz_show_merit, parentLayout, false) as LinearLayout
            val txtDisplay = quizLayout.findViewById<TextView>(R.id.txtLabel)
            val btnDisplayOnly = quizLayout.findViewById<TextView>(R.id.txtTamaOMali)
            val btnContinue = quizLayout.findViewById<Button>(R.id.btnContinue)

            txtDisplay.text = "Ang sagot ni " + firstname + " ay"

            btnDisplayOnly.text = "TAMA"

            parentLayout.addView(quizLayout)

            btnContinue.setOnClickListener {
                recitationType()
                getLearner()
                parentLayout.removeView(quizLayout)
            }


        }
        else {
            val currentIndex = (recitationList.size) - 1
            val learner = recitationList[currentIndex]
            val name = learner.split(Regex("[,\\s]+"))
            val firstname = name[1].lowercase().replaceFirstChar { it.uppercaseChar() }
            val parentLayout: RelativeLayout = findViewById(R.id.parentLayout)

            val inflater = LayoutInflater.from(this)
            val quizLayout = inflater.inflate(R.layout.layout_quiz_show_merit, parentLayout, false) as LinearLayout
            val txtDisplay = quizLayout.findViewById<TextView>(R.id.txtLabel)
            val btnDisplayOnly = quizLayout.findViewById<TextView>(R.id.txtTamaOMali)
            val btnContinue = quizLayout.findViewById<Button>(R.id.btnContinue)

            txtDisplay.text = "Ang sagot ni " + firstname + " ay"

            btnDisplayOnly.setTextColor((Color.parseColor("#B92900")))
            btnDisplayOnly.text = "MALI"

            parentLayout.addView(quizLayout)

            btnContinue.setOnClickListener {
                recitationType()
                getLearner()
                parentLayout.removeView(quizLayout)
            }
        }
    }
    private fun showCheckedAnswers(quizType: String) {
        val parentLayout: RelativeLayout = findViewById(R.id.parentLayout)

        val inflater = LayoutInflater.from(this)
        val quizLayout = inflater.inflate(R.layout.layout_quiz_edit, parentLayout, false) as RelativeLayout
        val checkLayout = quizLayout.findViewById<ScrollView>(R.id.edit_layout)
        val btnSubmit = quizLayout.findViewById<Button>(R.id.btnSubmit)
        val containerLayout = LinearLayout(this)
        containerLayout.orientation = LinearLayout.VERTICAL
        if (quizType == "default") {

            btnSubmit.text = "Back"
            for (i in 0 until checkedAnswerList.size) {
                val quizItem = quizList[i]
                val cleanedQuizList = quizItem.replace("[", "").replace("]", "")
                var elements = cleanedQuizList.split(", ")

                val answerItem = checkedAnswerList[i]
                val cleanedAnswerList = answerItem.replace("[", "").replace("]", "")
                var elements1 = cleanedAnswerList.split(", ")

                val question = elements[1]
                var userAnswer = elements1[0]
                var isCorrect = elements1[1].toBoolean()

                val answerItemLayout = inflater.inflate(R.layout.layout_quiz_check_answer, parentLayout, false) as RelativeLayout
                val txtNumber = answerItemLayout.findViewById<TextView>(R.id.txtNumber)
                val txtQuestion = answerItemLayout.findViewById<TextView>(R.id.txtQuestion)
                val txtAnswer = answerItemLayout.findViewById<TextView>(R.id.txtAnswer)

                if (userAnswer == "" || userAnswer == null) {
                    userAnswer = "No Answer"
                }
                txtNumber.text = (i+1).toString()
                txtQuestion.text = question
                txtAnswer.text = userAnswer

                if (isCorrect) {
                    txtAnswer.setTextColor((Color.parseColor("#11820F")))
                }
                else {
                    txtAnswer.setTextColor((Color.parseColor("#B92900")))
                }
                containerLayout.addView(answerItemLayout)
            }
            checkLayout.addView(containerLayout)
            parentLayout.addView(quizLayout)

            btnSubmit.setOnClickListener {
                parentLayout.removeView(quizLayout)
                showScore("default")
            }
        }
        else {

            btnSubmit.text = "Back"
            for (i in 0 until recitationList.size) {

                val answerItemLayout = inflater.inflate(R.layout.layout_quiz_recitation_learner_answers, parentLayout, false) as RelativeLayout
                val txtNumber = answerItemLayout.findViewById<TextView>(R.id.txtNumber)
                val txtQuestion = answerItemLayout.findViewById<TextView>(R.id.txtQuestion)
                val txtAnswer = answerItemLayout.findViewById<TextView>(R.id.txtAnswer)
                val txtUser = answerItemLayout.findViewById<TextView>(R.id.txtUser)

                txtUser.text = recitationScoreList[i].toString()

                txtNumber.text = (i+1).toString()

                var currentQuizI = 0
                if (recitationScoreList[i] > 0.0) {
                    val quizItem = quizList[currentQuizI]
                    val cleanedQuizList = quizItem.replace("[", "").replace("]", "")
                    var elements = cleanedQuizList.split(", ")
                    txtQuestion.text = elements[1]

                    currentQuizI += 1
                }
                else {
                    val quizItem = quizList[currentQuizI]
                    val cleanedQuizList = quizItem.replace("[", "").replace("]", "")
                    var elements = cleanedQuizList.split(", ")
                    txtQuestion.text = elements[1]
                }
                txtAnswer.text = recitationList[i].toString()

                if (recitationScoreList[i].toInt() > 0 ) {
                    txtUser.setBackgroundColor((Color.parseColor("#78D917")))
                    txtUser.setTextColor((Color.parseColor("#09B605")))
                }
                else {
                    txtUser.setBackgroundColor((Color.parseColor("#E37E7E")))
                    txtUser.setTextColor((Color.parseColor("#B92900")))
                }

                containerLayout.addView(answerItemLayout)
            }
            checkLayout.addView(containerLayout)
            parentLayout.addView(quizLayout)

            btnSubmit.setOnClickListener {
                parentLayout.removeView(quizLayout)
                showScore("recitation")
            }
        }
    }
    private fun addUserQuizProgress(userType: String, subjectId: String, quizName: String, score: Double, attempt: Int) {
        if (userType == "learner") {
            val firstname = sharedPreferences.getString("firstName", "").toString()
            val lastname = sharedPreferences.getString("lastName", "").toString()
            val studentName = lastname.uppercase() + ", " + firstname.lowercase().replaceFirstChar { it.uppercaseChar() }
            val progress = 1
            val tableName = "tbl_learnerQuizProgress"

            val query = "SELECT * FROM $tableName WHERE studentName = ? AND subjectId = ? AND quizName = ?"
            val topicArray = arrayOf(studentName, subjectId, quizName)
            val topicCursor: Cursor = db.rawQuery(query, topicArray)
            if (topicCursor.moveToFirst()) {
                val columnScoreIndex = topicCursor.getColumnIndex("score")
                val score = topicCursor.getDouble(columnScoreIndex)
                if (columnScoreIndex != -1) {
                    val insertQuery = "UPDATE $tableName SET score = ?, attempts = ? WHERE studentName = ? AND subjectId = ? AND quizName = ?"
                    val progressArray = arrayOf(score, (attempt+1), studentName, subjectId, quizName)
                    db.execSQL(insertQuery, progressArray)
                    println("Updated learner quiz progress $score")
                }
            }
            else {
                val insertQuery = "INSERT INTO $tableName (status, studentName, quizName, subjectId, score, attempts) VALUES (?,?,?,?,?,?)"
                val progressArray = arrayOf(progress, studentName, quizName, subjectId, score, attempt)
                db.execSQL(insertQuery, progressArray)
                println("Added learner quiz progress $score")
            }

        }
        else {
            sharedPreferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
            val teacherId = sharedPreferences.getString("userId", "")
            val progress = 1
            val tableName1 = "tbl_teacherQuizProgress"

            val query = "SELECT * FROM $tableName1 WHERE teacherId = ? AND subjectId = ? AND quizName = ?"
            val topicArray = arrayOf(teacherId, subjectId, quizName)
            val topicCursor: Cursor = db.rawQuery(query, topicArray)
            if (topicCursor.moveToFirst()) {
                val columnScoreIndex = topicCursor.getColumnIndex("score")
                val score = topicCursor.getDouble(columnScoreIndex)
                if (score == totalScore) {
                    val insertQuery = "UPDATE $tableName1 SET score = ? WHERE teacherId = ? AND subjectId = ? AND quizName = ?"
                    val progressArray = arrayOf(score, teacherId, subjectId, quizName)
                    db.execSQL(insertQuery, progressArray)
                    println("Updated learner quiz progress $score")
                } else {
                    val insertQuery = "UPDATE $tableName1 SET score = ? WHERE teacherId = ? AND subjectId = ? AND quizName = ?"
                    val progressArray = arrayOf(score, teacherId, subjectId, quizName)
                    db.execSQL(insertQuery, progressArray)
                }
            }
            else {
                val insertQuery = "INSERT INTO $tableName1 (status, teacherId, quizName, subjectId, score) VALUES (?,?,?,?,?)"
                val progressArray = arrayOf(progress, teacherId, quizName, subjectId, score)
                db.execSQL(insertQuery, progressArray)
                println("This is the progress of teacher ${progressArray.toString()}")
            }
        }
    }
    private fun updateCentralDBLearnerProgress(score: String) {
        val learnerId = sharedPreferences.getString("learnerId","").toString()
        val newAttempt = 1
        val fileAccess = "getLearnerQuiz.php?learnerId=$learnerId&quizId=$quizId&score=$score&attempt=$newAttempt&dateTaken=${getDate()}"
        val values = ""
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
                    Toast.makeText(this@QuizActivity, "No connection", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }
    private fun getDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd")
        val currentDate = Date()
        return sdf.format(currentDate)
    }
    private fun getAttempt(): Int {
        var tableName = "tbl_quiz"
        var checkQuery = "SELECT * FROM $tableName WHERE quizName = ?"
        var quizArray = arrayOf(quizName)
        val quizCursor: Cursor = db.rawQuery(checkQuery, quizArray)
        var quizAttempt = 0
        if (quizCursor.moveToFirst()) {
            val attemptColumnIndex = quizCursor.getColumnIndex("attempts")
            var thisQuizAttempt = 0
            if (attemptColumnIndex != -1) {
                thisQuizAttempt = quizCursor.getInt(attemptColumnIndex)
            }
            quizAttempt = thisQuizAttempt
        }

        val tableName2 = "tbl_learnerQuizProgress"
        val checkQuery2 = "SELECT * FROM $tableName2 WHERE studentName = ? AND quizName = ? AND subjectId = ?"
        val quizArray2 = arrayOf(studentName, quizName, lessonId)
        val quizUserCursor: Cursor = db.rawQuery(checkQuery2, quizArray2)
        var userAttempt = 0
        if (quizUserCursor.moveToFirst()) {
            val attemptColumnIndex = quizUserCursor.getColumnIndex("attempts")
            var thisUserAttempt = 0
            if (attemptColumnIndex != -1) {
                thisUserAttempt = quizUserCursor.getInt(attemptColumnIndex)
            }
            userAttempt = thisUserAttempt
        }
        //var attempt = quizAttempt - userAttempt
        println("This is the new attempts: $userAttempt")

        return userAttempt
    }
    private fun addDataToGrades(lessonId: String, studentName: String, score: String, quizType: String) {
        var cleanStudentName = ""
        if (studentName == "") {

        }
        else {
            val name = studentName.split(Regex("[,\\s]+"))
            val lastName = name[0]
            val firstname = name[1].lowercase().replaceFirstChar { it.uppercaseChar() }

            cleanStudentName = lastName.uppercase() + ", " + firstname
        }

        var currentSubject = ""
        val tableName = "tbl_subject"
        val checkQuery1 = "SELECT * FROM $tableName WHERE subjectId = ?"
        val subjectArray = arrayOf(lessonId)
        val subjectCursor: Cursor = db.rawQuery(checkQuery1, subjectArray)

        if (subjectCursor.moveToFirst()) {
            val columnIndexSubjectName = subjectCursor.getColumnIndex("subject")

            if (columnIndexSubjectName != -1) {
                do {
                    currentSubject = subjectCursor.getString(columnIndexSubjectName)
                } while (subjectCursor.moveToNext())
            }

        } else {

        }

        if (quizType.isEmpty()) {
            val intent = Intent(this, ExcelExportActivity::class.java)
            val editor = sharedPreferences.edit()
            editor.putString("quizId", quizId)
            editor.putString("quizType", "Quiz")
            editor.putString("currentSubject", currentSubject)
            editor.apply()
            startActivity(intent)
            finish()
        }
        else {
            val record = quizType
            val tableName = "tbl_grading"
            val query = "SELECT * FROM $tableName WHERE studentName = ? AND record = ? AND subject = ?"
            val existArray = arrayOf(cleanStudentName, record, currentSubject)
            val existCursor: Cursor = db.rawQuery(query, existArray)

            if (existCursor.moveToFirst()) {
                val updateQuery = "UPDATE $tableName SET score = score + ? WHERE studentName = ? AND record = ? AND subject = ?"
                val scoreArray = arrayOf(score.toDouble(), studentName, record, currentSubject)
                db.execSQL(updateQuery, scoreArray)
                println("Updated $studentName with a score of $score under the subject $currentSubject ($record)")
            } else {
                val insertQuery = "INSERT INTO $tableName (studentName, record, subject, score) VALUES (?,?,?,?)"
                val scoreArray = arrayOf(cleanStudentName, record, currentSubject, score.toDouble())
                db.execSQL(insertQuery, scoreArray)
                println("Added $cleanStudentName with a score of $score under the subject $currentSubject ($record)")
            }
        }

    }
    private fun showVerifyBox(quizType: String, answer: Boolean) {
        val parentLayout: RelativeLayout = findViewById(R.id.parentLayout)

        val inflater = LayoutInflater.from(this)
        val verifyBoxLayout = inflater.inflate(R.layout.layout_quiz_verify, parentLayout, false) as RelativeLayout

        ViewCompat.setElevation(verifyBoxLayout, 8f)

        val relativeLayout: RelativeLayout = verifyBoxLayout.findViewById(R.id.relativeLayout)
        val txtDisplay: TextView = relativeLayout.findViewById(R.id.txtDisplay)
        val agree: Button = relativeLayout.findViewById(R.id.btnAgree)
        val cancel: Button = relativeLayout.findViewById(R.id.btnCancel)

        txtDisplay.text = "Is this your final answer?"

        if (quizType == "default") {
            agree.setOnClickListener {
                for (i in 0 until userAnswerList.size) {
                    val ansItem = userAnswerList[i]
                    val cleanedAnswerList = ansItem.replace("[", "").replace("]", "")
                    var elements = cleanedAnswerList.split(", ")

                    val userAnswer = elements[0]
                    val correctAns = elements[1]
                    val points = elements[2].toDouble()

                    checkAnswer("default", correctAns, userAnswer, points)
                }

                attempt++
                parentLayout.removeAllViews()

                showScore("default")
            }
        }
        else {
            agree.setOnClickListener {
                parentLayout.removeAllViews()
                if (answer) {
                    showMerit(true)
                    parentLayout.removeView(verifyBoxLayout)
                } else {
                    showMerit(false)
                    parentLayout.removeView(verifyBoxLayout)
                }
            }
        }

        cancel.setOnClickListener {
            parentLayout.removeView(verifyBoxLayout)
        }

        parentLayout.addView(verifyBoxLayout)
    }
    private fun showMessageBox(state: String) {
        val parentLayout: RelativeLayout = findViewById(R.id.parentLayout)

        val inflater = LayoutInflater.from(this)
        val verifyBoxLayout = inflater.inflate(R.layout.layout_quiz_verify, parentLayout, false) as RelativeLayout

        ViewCompat.setElevation(verifyBoxLayout, 8f)

        val relativeLayout: RelativeLayout = verifyBoxLayout.findViewById(R.id.relativeLayout)
        val txtDisplay: TextView = relativeLayout.findViewById(R.id.txtDisplay)
        val agree: Button = relativeLayout.findViewById(R.id.btnAgree)
        val cancel: Button = relativeLayout.findViewById(R.id.btnCancel)

        if (userType == "learner") {
            when (state) {
                "restart" -> {
                    txtDisplay.text = "You have used up your attempts"
                    agree.visibility = View.GONE
                    cancel.text = "Okay"
                    cancel.setOnClickListener {
                        parentLayout.removeView(verifyBoxLayout)
                    }
                }
                "home" -> {
                    txtDisplay.text = "Go back to home?"
                    agree.text = "Yes"
                    agree.setOnClickListener {
                        hasAnswers = false
                        userAnswerList.clear()
                        userScore = 0.0
                        totalScore = 0.0
                        val intent = Intent(this, QuizzesActivity::class.java)
                        parentLayout.removeAllViews()
                        startActivity(intent)
                        finish()
                    }
                    cancel.text = "No"
                    cancel.setOnClickListener {
                        parentLayout.removeView(verifyBoxLayout)
                    }
                }
                else -> {
                    txtDisplay.text = "Go to home?\nYou'll lose your progress"
                    agree.text = "Yes"
                    agree.setOnClickListener {
                        hasAnswers = false
                        userAnswerList.clear()
                        userScore = 0.0
                        totalScore = 0.0
                        val intent = Intent(this, QuizzesActivity::class.java)
                        parentLayout.removeAllViews()
                        startActivity(intent)
                        finish()
                    }
                    cancel.text = "No"
                    cancel.setOnClickListener {
                        parentLayout.removeView(verifyBoxLayout)
                    }
                }
            }
        }
        else {
            when (state) {
                "restart" -> {
                    txtDisplay.text = "You have used up your attempts"
                    agree.visibility = View.GONE
                    cancel.text = "Okay"
                    cancel.setOnClickListener {
                        parentLayout.removeView(verifyBoxLayout)
                    }
                }
                "home" -> {
                    txtDisplay.text = "Go back to home?"
                    agree.text = "Yes"
                    agree.setOnClickListener {
                        hasAnswers = false
                        userAnswerList.clear()
                        userScore = 0.0
                        totalScore = 0.0
                        val intent = Intent(this, QuizzesActivity::class.java)
                        parentLayout.removeAllViews()
                        startActivity(intent)
                        finish()
                    }
                    cancel.text = "No"
                    cancel.setOnClickListener {
                        parentLayout.removeView(verifyBoxLayout)
                    }
                }
                else -> {
                    txtDisplay.text = "Go to home?\nYou'll lose your progress"
                    agree.text = "Yes"
                    agree.setOnClickListener {
                        hasAnswers = false
                        userAnswerList.clear()
                        userScore = 0.0
                        totalScore = 0.0
                        val intent = Intent(this, QuizzesActivity::class.java)
                        parentLayout.removeAllViews()
                        startActivity(intent)
                        finish()
                    }
                    cancel.text = "No"
                    cancel.setOnClickListener {
                        parentLayout.removeView(verifyBoxLayout)
                    }
                }
            }
        }

        parentLayout.addView(verifyBoxLayout)
    }

    override fun onBackPressed() {
        val intent = Intent(this, QuizzesActivity::class.java)
        intent.putExtra("subject", lessonId)
        startActivity(intent)
        finish()
    }
}