package com.example.tagakaulolearningapp

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*

class GameActivity : AppCompatActivity() {
    private lateinit var db: SQLiteDatabase
    private var gamesArray: ArrayList<String> = ArrayList()
    private lateinit var display: TextView
    private lateinit var parentLayout: GridLayout
    private lateinit var back: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        actionBar?.hide()
        DeviceNavigationClass.hideNavigationBar(this)
        setContentView(R.layout.activity_game)

        display = findViewById(R.id.txtDisplay)
        back = findViewById(R.id.btnBack)
        parentLayout = findViewById(R.id.parent_layout)

        back.setOnClickListener {
            val intent = Intent(this@GameActivity, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }

        db = openOrCreateDatabase("tagakauloDB", Context.MODE_PRIVATE, null)

        gamesArray.clear()
        getGames()
        if (gamesArray.isNullOrEmpty()) {
            noDisplayGames()
        }
        else {
            displayGames()
        }
    }
    private fun getGames() {
        val tableName = "tbl_games"
        val checkQuery = "SELECT DISTINCT type FROM $tableName"
        val gameCursor: Cursor = db.rawQuery(checkQuery, null)
        if (gameCursor.moveToFirst()) {
            do {
                val gameColumnIndex = gameCursor.getColumnIndex("type")
                if (gameColumnIndex != -1) {
                    val game = gameCursor.getString(gameColumnIndex).lowercase()
                    gamesArray.add(game)
                }
            } while(gameCursor.moveToNext())
        }
        gameCursor.close()
    }
    private fun displayGames() {
        parentLayout.visibility = View.VISIBLE
        display.setText("Games")

        val inflater = LayoutInflater.from(this)

        for (subject in gamesArray.indices) {
            val gameLayout = inflater.inflate(R.layout.layout_games, parentLayout, false) as RelativeLayout
            val imgGame = gameLayout.findViewById<ImageView>(R.id.imgGame)
            val txtGame = gameLayout.findViewById<TextView>(R.id.txtGame)

            var gameTitle = gamesArray[subject]
            imgGame.setImageResource(R.drawable.no_media)
            txtGame.text = gameTitle

            val uniqueId = View.generateViewId()
            gameLayout.id = uniqueId
            parentLayout.addView(gameLayout)

            val supportedGames = arrayListOf("shadow", "sound", "vocabulary", "math", "spelling")

            if (supportedGames.contains(gameTitle)) {
                gameLayout.setOnClickListener {
                    hasGame(gameTitle)
                }
            }
            else {
                gameLayout.setOnClickListener {
                    noDisplayGames()
                    unsupportedGame()
                }
            }
        }
    }
    private fun noDisplayGames() {
        parentLayout.visibility = View.GONE
        display.setText("No games available")
    }
    private fun unsupportedGame() {
        Toast.makeText(this, "The game isn't available yet", Toast.LENGTH_LONG).show()
    }
    private fun hasGame(gameTitle: String) {
        val intent = Intent(this, PlayGameActivity::class.java)
        intent.putExtra("gameTitle", gameTitle)
        startActivity(intent)
        finish()
    }
}