package com.example.tagakaulolearningapp

import android.content.Context
import android.database.Cursor
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.media.Image
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.ScrollView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy

class PlayGameActivity : AppCompatActivity() {
    private lateinit var gameTitle: String
    private lateinit var db: SQLiteDatabase
    private lateinit var description: String
    private var lives: Int = 3
    private var score: Int = 0
    private var currentIndex = 0
    private var totalItems = 0
    private var mediaList: ArrayList<String> = ArrayList()
    private var shadowList: ArrayList<String> = ArrayList()
    private var answerList: ArrayList<String> = ArrayList()
    private var questionList: ArrayList<String> = ArrayList()
    private var hintList: ArrayList<String> = ArrayList()
    private lateinit var parentLayout: RelativeLayout
    private lateinit var back: Button
    private lateinit var heart1: ImageView
    private lateinit var heart2: ImageView
    private lateinit var heart3: ImageView
    private lateinit var gameTextView: TextView
    var choices = mutableListOf<String>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        actionBar?.hide()
        DeviceNavigationClass.hideNavigationBar(this)
        setContentView(R.layout.activity_play_game)

        db = openOrCreateDatabase("tagakauloDB", Context.MODE_PRIVATE, null)

        parentLayout = findViewById(R.id.parent_layout)
        gameTextView = findViewById(R.id.txtDisplay)
        back = findViewById(R.id.btnBack)
        heart1 = findViewById(R.id.heart1)
        heart2 = findViewById(R.id.heart2)
        heart3 = findViewById(R.id.heart3)

        heart1.setImageResource(R.drawable.heart)
        heart2.setImageResource(R.drawable.heart)
        heart3.setImageResource(R.drawable.heart)

        gameTitle = intent.getStringExtra("gameTitle").toString()
        getGameTitle(gameTitle)

        back.setOnClickListener {
            val intent = Intent(this@PlayGameActivity, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
    private fun getGameTitle(gameTitle: String) {
        val tableName = "tbl_games"
        val checkQuery = "SELECT * FROM $tableName WHERE type = ?"
        val gameArray = arrayOf(gameTitle)
        val gameCursor: Cursor = db.rawQuery(checkQuery, gameArray)

        when (gameTitle) {
            "shadow" -> {
                if (gameCursor.moveToFirst()) {
                    val descColumnIndex = gameCursor.getColumnIndex("question")
                    val imagePath1ColumnIndex = gameCursor.getColumnIndex("imagePath1")
                    val imagePath2ColumnIndex = gameCursor.getColumnIndex("imagePath2")

                    if (descColumnIndex != -1) {
                        do {
                            val desc = gameCursor.getString(descColumnIndex)
                            val imagePath = gameCursor.getString(imagePath1ColumnIndex)
                            val shadowPath = gameCursor.getString(imagePath2ColumnIndex)

                            description = desc
                            mediaList.add(imagePath)
                            if (shadowPath.isNullOrEmpty()) { shadowList.add("null") }
                            else { shadowList.add(shadowPath) }
                            totalItems++
                        } while (gameCursor.moveToNext())

                        val existingShadow = listOf(
                            "alimango_shadow",
                            "baboy_shadow",
                            "paruparo_shadow",
                            "pusa_shadow"
                        )
                        val existingDisplay = listOf(
                            "alimango_display",
                            "baboy_display",
                            "paruparo_display",
                            "pusa_display"
                        )
                        val totalItems = existingShadow.size
                        var existingShadowIndex = totalItems - 1
                        for (i in 0 until shadowList.size) {
                            val trimmedValue = shadowList[i].trim()
                            if (shadowList[i] == "null" || trimmedValue.isEmpty()) {
                                shadowList[i] = existingShadow[existingShadowIndex]
                                mediaList[i] = existingDisplay[existingShadowIndex]
                                existingShadowIndex--
                            }
                        }
                        val shadowSize = shadowList.size
                        if (shadowSize < 4) {
                            shadowList.addAll(existingShadow)
                            for (i in shadowList.size until shadowList.size) {
                                mediaList.removeAt(i)
                            }
                            mediaList.addAll(existingDisplay)
                        }
                        if (shadowList.size > 4) {
                            shadowList = ArrayList(shadowList.take(5))
                            mediaList = ArrayList(mediaList.take(5))
                        }

                        choices.clear()
                        choices.addAll(mediaList)
                        choices.shuffle()
                    }
                }
            }
            "sounds" -> {
                if (gameCursor.moveToFirst()) {
                    val answerColumnIndex = gameCursor.getColumnIndex("")
                    val descColumnIndex = gameCursor.getColumnIndex("")
                    val audioPathColumnIndex = gameCursor.getColumnIndex("")

                    if (answerColumnIndex != -1) {
                        do {
                            val answer = gameCursor.getString(answerColumnIndex)
                            val desc = gameCursor.getString(descColumnIndex)
                            val audioPath = gameCursor.getString(audioPathColumnIndex)

                            description = desc
                            answerList.add(answer)
                            mediaList.add(audioPath)
                        } while(gameCursor.moveToNext())
                    }
                }
            }
            "vocabulary" -> {
                if (gameCursor.moveToFirst()) {
                    val answerColumnIndex = gameCursor.getColumnIndex("")
                    val descColumnIndex = gameCursor.getColumnIndex("")
                    val imagePath1ColumnIndex = gameCursor.getColumnIndex("")

                    if (answerColumnIndex != -1) {
                        do {
                            val answer = gameCursor.getString(answerColumnIndex)
                            val desc = gameCursor.getString(descColumnIndex)
                            val imagePath = gameCursor.getString(imagePath1ColumnIndex)

                            answerList.add(answer)
                            questionList.add(desc)
                            mediaList.add(imagePath)
                        } while(gameCursor.moveToNext())
                    }
                }
            }
            "spelling" -> {
                if (gameCursor.moveToFirst()) {
                    val answerColumnIndex = gameCursor.getColumnIndex("")
                    val descColumnIndex = gameCursor.getColumnIndex("")
                    val hintColumnIndex = gameCursor.getColumnIndex("")
                    val imagePath1ColumnIndex = gameCursor.getColumnIndex("")

                    if (answerColumnIndex != -1) {
                        do {
                            val answer = gameCursor.getString(answerColumnIndex)
                            val desc = gameCursor.getString(descColumnIndex)
                            val hint = gameCursor.getString(hintColumnIndex)
                            val imagePath = gameCursor.getString(imagePath1ColumnIndex)

                            answerList.add(answer)
                            questionList.add(desc)
                            hintList.add(hint)
                            mediaList.add(imagePath)
                        } while(gameCursor.moveToNext())
                    }
                }
            }
            "math" -> {
                if (gameCursor.moveToFirst()) {
                    val answerColumnIndex = gameCursor.getColumnIndex("")
                    val descColumnIndex = gameCursor.getColumnIndex("")

                    if (answerColumnIndex != -1) {
                        do {
                            val answer = gameCursor.getString(answerColumnIndex)
                            val desc = gameCursor.getString(descColumnIndex)

                            answerList.add(answer)
                            questionList.add(desc)
                        } while(gameCursor.moveToNext())
                    }
                }
            }
        }
        loadGameItems()
    }
    private fun loadGameItems() {
        if (gameTitle == "shadow" && currentIndex <= (shadowList.size-1) && lives > 0) {
            displayShadowGame(currentIndex)
        }
        else {
            goHome()
        }
    }
    private fun displayShadowGame(index: Int) {
        val correct = mediaList[index]
        choices.shuffle()
        var choice = choices.filter { newChoice -> newChoice != correct }
        var newChoices = listOf("$correct", choice[0])
        newChoices = newChoices.shuffled()
        if (index <= (shadowList.size-1) || lives != 0) {
            gameTextView.setText("guess based on silhouette")

            val parentLayout = findViewById(R.id.parent_layout) as RelativeLayout
            val inflater = LayoutInflater.from(this)

            val gameLayout = inflater.inflate(R.layout.layout_game_shadow, parentLayout, false) as RelativeLayout
            var imgShadow = gameLayout.findViewById<ImageView>(R.id.imgGame)
            var imgChoice1 = gameLayout.findViewById<ImageView>(R.id.imgChoice1)
            var imgChoice2 = gameLayout.findViewById<ImageView>(R.id.imgChoice2)

            if (shadowList[index].startsWith("http") || shadowList[index].startsWith("https")) {
                // Load image from a URL
                Glide.with(this@PlayGameActivity)
                    .load(shadowList[index])
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(imgShadow)
            } else {
                // Load image based on resource name
                Glide.with(this@PlayGameActivity)
                    .load(resources.getIdentifier(shadowList[index], "drawable", packageName))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(imgShadow)
            }

            var choiceShow1 = newChoices[0]
            var choiceShow2 = newChoices[1]

            if (choiceShow1 == "") {
                Glide.with(this@PlayGameActivity)
                    .load(R.drawable.broken_media)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(imgChoice1)
            }
            else {
                if (choiceShow1.startsWith("http") || choiceShow1.startsWith("https")) {
                    // Load image from a URL
                    Glide.with(this@PlayGameActivity)
                        .load(choiceShow1)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(imgChoice1)
                } else {
                    // Load image based on resource name
                    Glide.with(this@PlayGameActivity)
                        .load(resources.getIdentifier(choiceShow1, "drawable", packageName))
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(imgChoice1)
                }
            }
            if (choiceShow2 == "") {
                Glide.with(this@PlayGameActivity)
                    .load(R.drawable.broken_media)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(imgChoice2)
            }
            else {
                if (choiceShow2.startsWith("http") || choiceShow2.startsWith("https")) {
                    // Load image from a URL
                    Glide.with(this@PlayGameActivity)
                        .load(choiceShow2)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(imgChoice2)
                } else {
                    // Load image based on resource name
                    Glide.with(this@PlayGameActivity)
                        .load(resources.getIdentifier(choiceShow2, "drawable", packageName))
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(imgChoice2)
                }
            }

            val uniqueId = View.generateViewId()
            gameLayout.id = uniqueId
            parentLayout.addView(gameLayout)

            imgChoice1.setOnClickListener {
                if (choiceShow1 == correct) {
                    score++
                    println("shadow: You are correct")
                    currentIndex += 1
                } else {
                    lives--
                    showLives(lives)
                    println("shadow: You have $lives left")
                }
                parentLayout.removeView(gameLayout)
                loadGameItems()


            }

            imgChoice2.setOnClickListener {
                if (choiceShow2 == correct) {
                    score++
                    println("shadow: You are correct")
                    currentIndex += 1
                } else {
                    lives--
                    showLives(lives)
                    println("shadow: You have $lives left")
                }
                // Continue the game if not over
                parentLayout.removeView(gameLayout)
                loadGameItems()
            }

        }
        else if(lives == 0) {
            //show you have failed
        }
        else if (lives == 3) {
            //perfect and show achievement
        }
        else {
            //you have cleared
            goHome()
        }

    }
    private fun displaySoundGame() {

    }
    private fun displayVocabularyGame() {

    }
    private fun displaySpellingImageGame() {

    }
    private fun displaySpellingNoImageGame() {

    }
    private fun displayMathGame() {

    }
    private fun restartGame() {
    }
    private fun goHome() {
        mediaList.clear()
        shadowList.clear()
        answerList.clear()
        questionList.clear()
        hintList.clear()
        lives = 3
        val intent = Intent(this@PlayGameActivity, HomeActivity::class.java)
        startActivity(intent)
        finish()
    }
    private fun showLives(currentLives: Int) {
        when (currentLives.toString()) {
            "0" -> {
                heart1.setImageResource(R.drawable.no_heart)
            }
            "1" -> {
                heart2.setImageResource(R.drawable.no_heart)
            }
            "2" -> {
                heart3.setImageResource(R.drawable.no_heart)
            }
        }
    }
    private fun showCoins() {

    }
    private fun useCoins() {
        //if coins < hint price, text = Money not enough
        //if coins > hint price, text = show verification box
    }
    private fun showVerification() {
        //show text = You will spend $hint price for the hint
        //show display hint price
        //if yes button = coins-- && close showVerification()
        //if no button = close showVerification()
    }
    private fun showAchievement() {
        //show text = you have earned an achievement
    }
    private fun showFailedGame() {
        //show text = no lives left. Try again!
    }
    private fun showCompletedGame() {
        //if perfect, show text = Perfect!
        //if lives < 3, show text = You have potential
    }
    private fun recordProgress() {

    }
}