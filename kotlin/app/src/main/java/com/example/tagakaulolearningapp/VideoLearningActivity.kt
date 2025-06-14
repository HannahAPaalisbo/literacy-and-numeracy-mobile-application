@file:Suppress("DEPRECATION")

package com.example.tagakaulolearningapp

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import android.widget.MediaController
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore.Video
import android.view.LayoutInflater
import android.widget.*
import androidx.core.view.contains
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.upstream.FileDataSource
import com.google.android.exoplayer2.database.ExoDatabaseProvider
import com.google.android.exoplayer2.upstream.*
import com.google.android.exoplayer2.upstream.cache.CacheWriter
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import com.google.android.exoplayer2.util.Util
import okhttp3.*
import org.apache.poi.openxml4j.util.ZipSecureFile.ThresholdInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class VideoLearningActivity : AppCompatActivity() {
    private lateinit var db: SQLiteDatabase
    private lateinit var topicId: String
    private lateinit var lessonId: String
    private lateinit var topicName: String
    private lateinit var topic: String
    private lateinit var videoPath: String
    private lateinit var imageUrl: String
    private lateinit var videoView: PlayerView
    private lateinit var httpDataSourceFactory: HttpDataSource.Factory
    private lateinit var defaultDataSourceFactory: DefaultDataSourceFactory
    private lateinit var cacheDataSourceFactory: DataSource.Factory
    private lateinit var simpleExoPlayer: SimpleExoPlayer
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var hasNetwork: ConnectivityClass
    private lateinit var customCacheDir: File
    lateinit var cache: SimpleCache

    private var mPlayer: SimpleExoPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        actionBar?.hide()
        DeviceNavigationClass.hideNavigationBar(this)
        setContentView(R.layout.activity_video_learning)

        cache = CacheManager.getSimpleCache(this)

        sharedPreferences = getSharedPreferences("MyPreferences", MODE_PRIVATE)
        topicId = intent.getStringExtra("topic").toString()
        lessonId = intent.getStringExtra("subjectId").toString()
        topicName = intent.getStringExtra("topicName").toString()

        videoView = findViewById(R.id.videoView)

        val tableName = "tbl_topic"

        db = openOrCreateDatabase("tagakauloDB", Context.MODE_PRIVATE, null)
        val checkQuery2 = "SELECT * FROM $tableName WHERE topicId = '$topicId'"
        val topicCursor: Cursor = db.rawQuery(checkQuery2, null)

        if (topicCursor.moveToFirst()) {
            val columnIndexTopicName = topicCursor.getColumnIndex("topic")
            val columnIndexVideoPath = topicCursor.getColumnIndex("videoPath")

            if (columnIndexTopicName != -1 && columnIndexVideoPath != -1) {
                do {
                    topic = topicCursor.getString(columnIndexTopicName)
                    videoPath = topicCursor.getString(columnIndexVideoPath)

                } while (topicCursor.moveToNext())
            }

        } else {

        }

        topicCursor.close()
        db.close()
        hasNetwork = ConnectivityClass()

        val filename = videoPath.substringAfterLast("/")

        customCacheDir = File(this.cacheDir, "Videos")

        if (!customCacheDir.exists()) {
            customCacheDir.mkdirs()
        }
        val videoCacheFile = File(customCacheDir, filename)

        if (videoCacheFile.exists()) {
            println("$videoCacheFile already exists")
            playVideoFromCache(videoCacheFile)
        } else {
            if (hasNetwork.isInternetConnected(this@VideoLearningActivity)) {
                println("$videoCacheFile starting to download")
                downloadVideoAndPlay(videoPath, videoCacheFile)
            } else {
                Toast.makeText(this@VideoLearningActivity, "Unable to download video. No internet", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun downloadVideoAndPlay(videoPath: String, videoCacheFile: File) {
        if (videoCacheFile.exists()) {
            Toast.makeText(this, "video loading...", Toast.LENGTH_SHORT).show()
            playVideoFromCache(videoCacheFile)
        }
        else {
            val client = OkHttpClient()
            val request = Request.Builder().url(videoPath).build()
            client.newCall(request).enqueue(object : Callback {
                override fun onResponse(call: Call, response: Response) {
                    val responseBody = response.body
                    responseBody?.byteStream()?.use { input ->
                        videoCacheFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }

                    runOnUiThread {
                        Toast.makeText(this@VideoLearningActivity, "video loading...", Toast.LENGTH_SHORT).show()
                        playVideoFromCache(videoCacheFile)
                    }
                }

                override fun onFailure(call: Call, e: IOException) {
                    runOnUiThread {
                        Toast.makeText(this@VideoLearningActivity, "Unable to download video", Toast.LENGTH_SHORT).show()
                    }
                }
            })

        }
    }

    private fun playVideoFromCache(videoFile: File) {
        initPlayer()
    }

    private fun initPlayer() {
        httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setAllowCrossProtocolRedirects(true)

        defaultDataSourceFactory = DefaultDataSourceFactory(
            applicationContext, httpDataSourceFactory
        )
        val cacheDataSourceFactory = CacheDataSource.Factory()
            .setCache(cache)
            .setUpstreamDataSourceFactory(httpDataSourceFactory)
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)

        simpleExoPlayer = SimpleExoPlayer.Builder(this)
            .setMediaSourceFactory(DefaultMediaSourceFactory(cacheDataSourceFactory))
            .build()

        val filename = videoPath.substringAfterLast("/")
        val videoFile = File(customCacheDir, filename)
        val videoUri = Uri.fromFile(videoFile)

        val mediaItem = MediaItem.fromUri(videoUri)
        val mediaSource = ProgressiveMediaSource.Factory(FileDataSource.Factory())
            .createMediaSource(mediaItem)

        videoView.player = simpleExoPlayer
        simpleExoPlayer!!.playWhenReady = false
        simpleExoPlayer!!.seekTo(0, 0)
        simpleExoPlayer!!.repeatMode = Player.REPEAT_MODE_OFF
        simpleExoPlayer!!.setMediaSource(mediaSource, true)
        simpleExoPlayer!!.prepare()
    }

    private fun updateCentralDBLearnerProgress() {
        val learnerId = sharedPreferences.getString("learnerId","").toString()
        val fileAccess = "getLearnerProgress.php?learnerId=$learnerId&topicId=$topicId&dateStartTaken=${getDate()}"
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
                    Toast.makeText(this@VideoLearningActivity, "No connection", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }
    private fun getDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd")
        val currentDate = Date()
        return sdf.format(currentDate)
    }
    override fun onBackPressed() {
        simpleExoPlayer.stop()
        super.onBackPressed()
    }
}
