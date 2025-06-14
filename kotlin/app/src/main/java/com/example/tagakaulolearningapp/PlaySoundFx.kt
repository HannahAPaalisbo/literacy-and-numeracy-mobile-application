package com.example.tagakaulolearningapp

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import okhttp3.*
import java.io.File
import java.io.IOException

class PlaySoundFx(context: Context) {
    private lateinit var sfxFilename: String
    private lateinit var bgMusic: MediaPlayer
    private lateinit var db: SQLiteDatabase
    private lateinit var customCacheDir: File
    private lateinit var audioPath: String
    private val uiHandler = Handler(Looper.getMainLooper())
    init {
        db = context.openOrCreateDatabase("tagakauloDB", Context.MODE_PRIVATE, null)
    }

    open fun requestSFX(context: Context, state: String) {
        val resId = getSFX(state)

        if (resId != 0) {
            bgMusic = MediaPlayer.create(context, resId)
            bgMusic.start()
        }
    }

    private fun getSFX(state: String): Int {
        return when (state) {
            "click" -> R.raw.click
            "magaling" -> R.raw.wow
            "wrong" -> R.raw.wrong
            "finished" -> R.raw.correct
            else -> 0
        }
    }

    open fun getAudioPath(topicId: String, context: Context){
        val tableName = "tbl_topic"
        val checkQuery2 = "SELECT * FROM $tableName WHERE topicId = '$topicId'"
        val topicCursor: Cursor = db.rawQuery(checkQuery2, null)

        if (topicCursor.moveToFirst()) {
            val columnIndexTopicName = topicCursor.getColumnIndex("topic")
            val columnIndexaudioPath = topicCursor.getColumnIndex("audioPath")

            if (columnIndexTopicName != -1 && columnIndexaudioPath != -1) {
                do {
                    this.audioPath = topicCursor.getString(columnIndexaudioPath)

                } while (topicCursor.moveToNext())
            }

        } else {

        }

        val filename = audioPath.substringAfterLast("/")

        var videoCacheFile = File(context.cacheDir, filename)
        downloadCacheAudio(videoCacheFile, context)
    }

    private fun downloadCacheAudio(filename: File, context: Context) {
        if (filename.exists()) {
            Toast.makeText(context, "audio loading...", Toast.LENGTH_SHORT).show()
            playAudio(context, filename)
        }
        else {
            val client = OkHttpClient()
            val request = Request.Builder().url(audioPath).build()
            client.newCall(request).enqueue(object : Callback {
                override fun onResponse(call: Call, response: Response) {

                    val responseBody = response.body
                    responseBody?.byteStream()?.use { input ->
                        filename.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }

                    uiHandler.post {
                        Toast.makeText(context, "audio loading...", Toast.LENGTH_SHORT).show()
                        playAudio(context, filename)
                    }
                }

                override fun onFailure(call: Call, e: IOException) {
                    println("Request unsuccessful. Status code: $e")
                    uiHandler.post {
                        Toast.makeText(context, "Unable to download audio", Toast.LENGTH_SHORT).show()
                    }
                }
            })

        }
    }

    private fun playAudio(context: Context, filename: File) {
        try {
            bgMusic = MediaPlayer()
            bgMusic.setDataSource(filename.absolutePath)

            bgMusic.prepare()
            bgMusic.start()
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(context, "Error playing audio", Toast.LENGTH_SHORT).show()
        }
    }


}
