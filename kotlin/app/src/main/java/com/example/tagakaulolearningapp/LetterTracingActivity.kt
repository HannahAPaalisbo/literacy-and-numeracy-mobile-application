package com.example.tagakaulolearningapp

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.graphics.Path
import android.graphics.RectF
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import com.airbnb.lottie.LottieAnimationView
import com.example.tagakaulolearningapp.databinding.ActivityLetterTracingBinding
import pk.farimarwat.abckids.AbcdkidsListener
import pk.farimarwat.abckids.TAG

class LetterTracingActivity : AppCompatActivity() {
    lateinit var mContext: Context
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var db: SQLiteDatabase
    private lateinit var binding: ActivityLetterTracingBinding
    private lateinit var topicId: String
    private lateinit var topicName: String
    private lateinit var lessonId: String
    private lateinit var playSoundFx: PlaySoundFx

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        actionBar?.hide()
        DeviceNavigationClass.hideNavigationBar(this)
        binding = ActivityLetterTracingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences("MyPreferences", MODE_PRIVATE)
        db = openOrCreateDatabase("tagakauloDB", Context.MODE_PRIVATE, null)

        playSoundFx = PlaySoundFx(this@LetterTracingActivity)

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
            "A" -> {
                val commonRadius = 80f

                //uppercased 'A'
                path.moveTo(width * 0.71f, height * 0.51f)
                path.lineTo(width * 0.37f, height * 1.6f)

                path.moveTo(width * 0.72f, height * 0.51f)
                path.lineTo(width * 1.13f, height * 1.6f)

                path.moveTo(width * 0.463f, height * 1.3f)
                path.lineTo(width * 1.017f, height * 1.3f)

                //lowercased 'a'
                path.moveTo(width * 1.6f, height * 1.4f - commonRadius)
                path.addCircle(width * 1.6f, height * 1.4f, commonRadius, Path.Direction.CCW)

                path.moveTo(width * 1.8f, height * 1.20f)
                path.lineTo(width * 1.8f, height * 1.6f)

                binding.tlview.setLetter(path)
            }
            "B" -> {
                val startX = 300f
                val startY = 300f
                val endX = 600f
                val endY = 600f

                val commonRadius = 110f

                // Trace the uppercase 'B'
                path.moveTo(width * 0.37f, height * 0.51f)
                path.lineTo(width * 0.37f, height * 1.6f)
                path.moveTo(startX, startY)
                path.addArc((width * 0.37f) - 245f, height * 0.51f, (width * 0.37f) + 245f, height * 1.01f, 270f, 180f)
                path.moveTo(startX, startY)
                path.addArc((width * 0.37f) - 245f, height * 1.01f, (width * 0.37f) + 245f, height * 1.6f, 270f, 180f)

                // Trace the lowercase 'b'
                path.moveTo(width * 1.26f, height * 0.51f)
                path.lineTo(width * 1.26f, height * 1.6f)
                path.moveTo((width * 1.26f) + commonRadius, height * 1.38f)
                path.addArc((width * 1.54f) - commonRadius, (height * 1.38f) - commonRadius, (width * 1.54f) + commonRadius, (height * 1.38f) + commonRadius, 180f, 360f)
                binding.tlview.setLetter(path)
            }
            "D" -> {
                val startX = 300f
                val startY = 300f
                val endX = 600f
                val endY = 600f

                val commonRadius = 110f

                // Trace the uppercase 'D'
                path.moveTo(width * 0.37f, height * 0.51f)
                path.lineTo(width * 0.37f, height * 1.6f)
                path.moveTo(startX, startY)
                path.addArc((width * 0.37f) - 232f, height * 0.51f, (width * 0.37f) + 232f, height * 1.6f, 270f, 180f)

                // Trace the lowercase 'd'
                path.moveTo(width * 1.8f, height * 0.51f)
                path.lineTo(width * 1.8f, height * 1.6f)
                path.moveTo(width * 1.5f, height * 1.4f - commonRadius)
                path.addCircle(width * 1.5f, height * 1.4f, commonRadius, Path.Direction.CCW)
                binding.tlview.setLetter(path)
            }
            "E" -> {
                val commonRadius = 80f

                //uppercased 'E'
                path.moveTo(width * 0.463f, height * 0.51f)
                path.lineTo(width * 0.463f, height * 1.6f)

                path.moveTo(width * 0.463f, height * 0.51f)
                path.lineTo(width * 1.05f, height * 0.51f)

                path.moveTo(width * 0.463f, height * 1.2f)
                path.lineTo(width * 1.05f, height * 1.2f)

                path.moveTo(width * 0.463f, height * 1.6f)
                path.lineTo(width * 1.05f, height * 1.6f)

                //lowercased 'e'
                path.moveTo(width * 1.42f, height * 1.2f)
                path.lineTo(width * 2.22f, height * 1.2f)
                path.addArc((width * 1.8f) - 180f, height * 0.8f, (width * 1.8f) + 180f, height * 1.6f, 0f, -270f)

                binding.tlview.setLetter(path)
            }
            "É" -> {
                val commonRadius = 80f

                //uppercased 'É'
                path.moveTo(width * 0.463f, height * 0.51f)
                path.lineTo(width * 0.463f, height * 1.6f)

                path.moveTo(width * 0.463f, height * 0.51f)
                path.lineTo(width * 1.05f, height * 0.51f)

                path.moveTo(width * 0.463f, height * 1.2f)
                path.lineTo(width * 1.05f, height * 1.2f)

                path.moveTo(width * 0.463f, height * 1.6f)
                path.lineTo(width * 1.05f, height * 1.6f)

                path.moveTo(width * 0.9f, height * 0.21f)
                path.lineTo(width * 0.62f, height * 0.31f)

                //lowercased 'e'
                path.moveTo(width * 1.42f, height * 1.2f)
                path.lineTo(width * 2.22f, height * 1.2f)
                path.addArc((width * 1.8f) - 180f, height * 0.8f, (width * 1.8f) + 180f, height * 1.6f, 0f, -270f)
                path.moveTo(width * 2f, height * 0.51f)
                path.lineTo(width * 1.71f, height * 0.61f)

                binding.tlview.setLetter(path)
            }
            "G" -> {
                val commonRadius = 80f

                //uppercased 'G'
                path.moveTo((width * 0.463f) + commonRadius, height * 0.51f)
                path.addArc((width * 0.663f) - commonRadius, (height * 0.63f) - commonRadius, (width * 0.963f) + commonRadius, (height * 1.6f) + commonRadius, -45f, -270f)

                path.moveTo(width * 0.763f, height * 1.11f)
                path.lineTo(width * 1.05f, height * 1.11f)

                path.moveTo(width * 1.05f, height * 1.11f)
                path.lineTo(width * 1.05f, height * 1.6f)

                //lowercased 'g'
                path.moveTo(width * 1.6f, height * 1.4f - commonRadius)
                path.addCircle(width * 1.6f, height * 1.4f, commonRadius, Path.Direction.CCW)
                path.moveTo(width * 1.8f, height * 1.15f)
                path.lineTo(width * 1.8f, height * 2f)
                path.addArc((width * 1.61f) - commonRadius, (height * 2f) - commonRadius, (width * 1.61f) + commonRadius, (height * 2f) + commonRadius, 0f, 180f)


                binding.tlview.setLetter(path)
            }
            "Ng" -> {
                val commonRadius = 80f

                //uppercased 'G'
                path.moveTo(width * 0.463f, height * 0.51f)
                path.lineTo(width * 0.463f, height * 1.6f)

                path.moveTo(width * 0.463f, height * 0.51f)
                path.lineTo(width * 1.15f, height * 1.6f)

                path.moveTo(width * 1.15f, height * 0.51f)
                path.lineTo(width * 1.15f, height * 1.6f)

                //lowercased 'g'
                path.moveTo(width * 1.6f, height * 1.4f - commonRadius)
                path.addCircle(width * 1.6f, height * 1.4f, commonRadius, Path.Direction.CCW)
                path.moveTo(width * 1.8f, height * 1.15f)
                path.lineTo(width * 1.8f, height * 2f)
                path.addArc((width * 1.61f) - commonRadius, (height * 2f) - commonRadius, (width * 1.61f) + commonRadius, (height * 2f) + commonRadius, 0f, 180f)


                binding.tlview.setLetter(path)
            }
            "H" -> {
                //uppercased 'H'
                path.moveTo(width * 0.463f, height * 0.51f)
                path.lineTo(width * 0.463f, height * 1.6f)

                path.moveTo(width * 1.15f, height * 0.51f)
                path.lineTo(width * 1.15f, height * 1.6f)

                path.moveTo(width * 0.463f, height * 1.2f)
                path.lineTo(width * 1.15f, height * 1.2f)


                //lowercased 'i'

                path.moveTo(width * 1.5f, height * 1.20f)
                path.lineTo(width * 1.5f, height * 1.6f)

                path.moveTo(width * 1.5f, height * 0.91f)
                path.lineTo(width * 1.5f, height * 0.91f)

                binding.tlview.setLetter(path)
            }
            "I" -> {
                //uppercased 'I'
                path.moveTo(width * 0.81f, height * 0.51f)
                path.lineTo(width * 0.81f, height * 1.6f)

                path.moveTo(width * 0.463f, height * 0.51f)
                path.lineTo(width * 1.15f, height * 0.51f)

                path.moveTo(width * 0.463f, height * 1.6f)
                path.lineTo(width * 1.15f, height * 1.6f)


                //lowercased 'i'

                path.moveTo(width * 1.5f, height * 1.20f)
                path.lineTo(width * 1.5f, height * 1.6f)

                path.moveTo(width * 1.5f, height * 0.91f)
                path.lineTo(width * 1.5f, height * 0.91f)

                binding.tlview.setLetter(path)
            }
            "K" -> {
                val commonRadius = 80f

                //uppercased 'K'
                path.moveTo(width * 0.463f, height * 0.51f)
                path.lineTo(width * 0.463f, height * 1.6f)

                path.moveTo(width * 1.15f, height * 0.51f)
                path.lineTo(width * 0.463f, height * 1.3f)

                path.moveTo(width * 0.72f, height * 1.1f)
                path.lineTo(width * 1.15f, height * 1.6f)


                //lowercased 'k'
                path.moveTo(width * 1.56f, height * 0.51f)
                path.lineTo(width * 1.56f, height * 1.6f)

                path.moveTo(width * 1.96f, height * 0.91f)
                path.lineTo(width * 1.56f, height * 1.3f)

                path.moveTo(width * 1.77f, height * 1.20f)
                path.lineTo(width * 1.96f, height * 1.6f)

                binding.tlview.setLetter(path)
            }
            "L" -> {
                val commonRadius = 80f

                //uppercased 'L'
                path.moveTo(width * 0.463f, height * 0.51f)
                path.lineTo(width * 0.463f, height * 1.6f)

                path.moveTo(width * 0.463f, height * 1.6f)
                path.lineTo(width * 0.92f, height * 1.6f)


                //lowercased 'l'
                path.moveTo(width * 1.5f, height * 0.51f)
                path.lineTo(width * 1.5f, height * 1.6f)

                binding.tlview.setLetter(path)
            }
            "M" -> {
                val commonRadius = 80f

                //uppercased 'M'
                path.moveTo(width * 0.263f, height * 0.51f)
                path.lineTo(width * 0.263f, height * 1.6f)

                path.moveTo(width * 0.91f, height * 0.51f)
                path.lineTo(width * 0.91f, height * 1.6f)

                path.moveTo(width * 0.263f, height * 0.51f)
                path.lineTo(width * 0.6f, height * 1.0f)

                path.moveTo(width * 0.91f, height * 0.51f)
                path.lineTo(width * 0.6f, height * 1.0f)


                //lowercased 'm'
                path.moveTo(width * 1.36f, height * 0.95f)
                path.lineTo(width * 1.36f, height * 1.6f)

                path.addArc((width * 1.56f) - commonRadius, (height * 1.20f) - commonRadius, (width * 1.66f) + commonRadius, (height * 1.20f) + commonRadius, 180f, 180f)

                path.moveTo(width * 1.855f, height * 1.20f)
                path.lineTo(width * 1.855f, height * 1.6f)

                path.addArc((width * 2.055f) - commonRadius, (height * 1.20f) - commonRadius, (width * 2.11f) + commonRadius, (height * 1.20f) + commonRadius, 180f, 180f)

                path.moveTo(width * 2.3f, height * 1.20f)
                path.lineTo(width * 2.3f, height * 1.6f)

                binding.tlview.setLetter(path)
            }
            "N" -> {
                val commonRadius = 80f

                //uppercased 'N'
                path.moveTo(width * 0.463f, height * 0.51f)
                path.lineTo(width * 0.463f, height * 1.6f)

                path.moveTo(width * 0.463f, height * 0.51f)
                path.lineTo(width * 1.15f, height * 1.6f)

                path.moveTo(width * 1.15f, height * 0.51f)
                path.lineTo(width * 1.15f, height * 1.6f)

                //lowercased 'n'
                path.moveTo(width * 1.46f, height * 0.95f)
                path.lineTo(width * 1.46f, height * 1.6f)

                path.addArc((width * 1.66f) - commonRadius, (height * 1.20f) - commonRadius, (width * 1.76f) + commonRadius, (height * 1.20f) + commonRadius, 180f, 180f)

                path.moveTo(width * 1.955f, height * 1.20f)
                path.lineTo(width * 1.955f, height * 1.6f)

                binding.tlview.setLetter(path)
            }
            "O" -> {
                val commonRadius = 139.76f
                val lowerCaseRadius = 83.76f

                // Trace the uppercase 'O'
                path.moveTo(300f, 420f)
                path.addCircle(300f, 420f, commonRadius, Path.Direction.CCW)

                // Trace the lowercase 'o'
                path.addCircle(600f, 420f, lowerCaseRadius, Path.Direction.CCW)

                binding.tlview.setLetter(path)
            }
            "P" -> {
                val startX = 300f
                val startY = 300f
                val endX = 600f
                val endY = 600f

                val commonRadius = 110f

                // Trace the uppercase 'P'
                path.moveTo(width * 0.37f, height * 0.51f)
                path.lineTo(width * 0.37f, height * 1.6f)
                path.moveTo(startX, startY)
                path.addArc((width * 0.37f) - 220f, height * 0.51f, (width * 0.37f) + 220f, height * 1.3f, 270f, 180f)

                // Trace the lowercase 'p'
                path.moveTo(width * 1.26f, height * 1.17f)
                path.lineTo(width * 1.26f, height * 2.2f)
                path.moveTo(startX, startY)
                path.addArc((width * 1.54f) - commonRadius, (height * 1.38f) - commonRadius, (width * 1.54f) + commonRadius, (height * 1.38f) + commonRadius, 180f, 360f)
                binding.tlview.setLetter(path)
            }
            "S" -> {
                val startX = 300f
                val startY = 300f
                val endX = 600f
                val endY = 600f

                val commonRadius = 110f

                path.moveTo(width * 1.6f, height * 0.5f)
                path.lineTo(width * 1.4f, height * 0.5f)

                path.addArc((width * 1.4f) - 120f, height * 0.5f, (width * 1.4f) + 120f, height * 1f, 270f, -180f)

                path.addArc((width * 1.4f) - 120f, height * 1f, (width * 1.4f) + 120f, height * 1.5f, -90f, 180f)

                path.moveTo(width * 1.4f, height * 1.5f)
                path.lineTo(width * 1.2f, height * 1.5f)

                binding.tlview.setLetter(path)
            }
            "T" -> {
                //uppercased 'T'
                path.moveTo(width * 0.81f, height * 0.51f)
                path.lineTo(width * 0.81f, height * 1.6f)

                path.moveTo(width * 0.463f, height * 0.51f)
                path.lineTo(width * 1.15f, height * 0.51f)


                //lowercased 't'
                path.moveTo(width * 1.855f, height * 0.51f)
                path.lineTo(width * 1.855f, height * 1.6f)

                path.moveTo(width * 2.15f, height * 1.4f)
                path.lineTo(width * 1.855f, height * 1.6f)

                path.moveTo(width * 1.7f, height * 1.0f)
                path.lineTo(width * 2.15f, height * 1.0f)

                binding.tlview.setLetter(path)
            }
            "U" -> {
                val commonRadius = 139.76f
                //uppercased 'U'
                path.moveTo(width * 0.463f, height * 0.51f)
                path.lineTo(width * 0.463f, height * 1.6f)

                path.addArc((width * 0.463f), (height * 1.6f) - commonRadius, (width * 1.15f), (height * 1.6f) + commonRadius, 180f, -180f)

                path.moveTo(width * 1.15f, height * 1.6f)
                path.lineTo(width * 1.15f, height * 0.51f)



                //lowercased 'u'
                path.moveTo(width * 1.46f, height * 0.95f)
                path.lineTo(width * 1.46f, height * 1.6f)

                path.addArc((width * 1.46f), (height * 1.6f) - commonRadius, (width * 2.02f), (height * 1.6f) + commonRadius, 180f, -180f)

                path.moveTo(width * 2.02f, height * 1.6f)
                path.lineTo(width * 2.02f, height * 0.95f)

                binding.tlview.setLetter(path)
            }
            "W" -> {
                val commonRadius = 139.76f
                //uppercased 'W'
                path.moveTo(width * 0.163f, height * 0.51f)
                path.lineTo(width * 0.363f, height * 1.6f)

                path.moveTo(width * 0.363f, height * 1.61f)
                path.lineTo(width * 0.7f, height * 0.51f)

                path.moveTo(width * 0.7f, height * 0.51f)
                path.lineTo(width * 1.01f, height * 1.6f)

                path.moveTo(width * 1.01f, height * 1.6f)
                path.lineTo(width * 1.31f, height * 0.51f)


                //lowercased 'w'
                path.moveTo(width * 1.46f, height * 0.95f)
                path.lineTo(width * 1.56f, height * 1.6f)

                path.moveTo(width * 1.56f, height * 1.6f)
                path.lineTo(width * 1.66f, height * 1.0f)

                path.moveTo(width * 1.66f, height * 1.0f)
                path.lineTo(width * 1.76f, height * 1.6f)

                path.moveTo(width * 1.76f, height * 1.6f)
                path.lineTo(width * 1.86f, height * 0.95f)

                binding.tlview.setLetter(path)
            }
            "Y" -> {
                //uppercased 'Y'
                path.moveTo(width * 0.463f, height * 0.51f)
                path.lineTo(width * 0.81f, height * 1.0f)

                path.moveTo(width * 1.15f, height * 0.51f)
                path.lineTo(width * 0.81f, height * 1.0f)

                path.moveTo(width * 0.81f, height * 1.0f)
                path.lineTo(width * 0.81f, height * 1.6f)

                //lowercased 'y'

                path.moveTo(width * 1.4f, height * 1.20f)
                path.lineTo(width * 1.7f, height * 1.6f)

                path.moveTo(width * 2.0f, height * 1.20f)
                path.lineTo(width * 1.6f, height * 1.9f)

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
                Log.e(TAG, "Tracing completed")
                playSoundFx.requestSFX(this@LetterTracingActivity, "magaling")
                val parentLayout: RelativeLayout = findViewById(R.id.parentLayout)

                val inflater = LayoutInflater.from(this@LetterTracingActivity)
                val quizLayout = inflater.inflate(R.layout.layout_topic_finished, parentLayout, false) as RelativeLayout

                quizLayout.visibility = View.VISIBLE
                parentLayout.addView(quizLayout)
                Handler().postDelayed({
                    parentLayout.removeView(quizLayout)
                    val intent = Intent(this@LetterTracingActivity, PracticeQuizActivity::class.java)
                    intent.putExtra("subjectId", lessonId)
                    intent.putExtra("topic", topicId)
                    intent.putExtra("topicName", topicName)
                    startActivity(intent)
                }, 1620)
            }

        })
    }
}