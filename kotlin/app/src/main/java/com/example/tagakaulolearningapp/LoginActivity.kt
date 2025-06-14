package com.example.tagakaulolearningapp

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.opengl.Visibility
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.text.InputType
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import com.bumptech.glide.Glide

class LoginActivity : AppCompatActivity() {
    private lateinit var userOption: String
    private lateinit var db: SQLiteDatabase
    private lateinit var teacherId: String
    private lateinit var learnerId: String
    private lateinit var firstName: String
    private lateinit var lastName: String
    private lateinit var password: String
    private lateinit var sharedPreferences: SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        actionBar?.hide()
        setContentView(R.layout.activity_login)

        DeviceNavigationClass.hideNavigationBar(this)

        db = openOrCreateDatabase("tagakauloDB", Context.MODE_PRIVATE, null)

        sharedPreferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
        val userType = sharedPreferences.getString("userType", "")

        val etUser = findViewById<EditText>(R.id.etUser)
        val etLastName = findViewById<EditText>(R.id.etLastname)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnShowPass = findViewById<ImageView>(R.id.btnPass)

        if (userType == "learner") {
            btnShowPass.visibility = View.GONE
            etUser.setHint("Enter your first name")
            btnLogin.setOnClickListener {
                var userId = etUser.text.toString().trim()
                var lastName = etLastName.text.toString()
                if (userId != "" && lastName != "") {
                    var checkUser = checkLearner(userId.toString().trim().lowercase().replaceFirstChar { it.uppercaseChar() }, lastName.toString().trim().lowercase().replaceFirstChar { it.uppercaseChar() })

                    if (checkUser) {
                        Toast.makeText(this@LoginActivity, "Mag siling da kita!", Toast.LENGTH_SHORT).show()
                        Handler().postDelayed({
                            val intent = Intent(this, HomeActivity::class.java)

                            startActivity(intent)
                            finish()
                        }, 500)
                    }
                    else {
                        Toast.makeText(this, "No user found", Toast.LENGTH_SHORT).show()
                    }
                }
                else {
                    Toast.makeText(this, "Please provide your first name and last name", Toast.LENGTH_SHORT).show()
                }
            }
        }
        else {
            etUser.setHint("Enter your ID number")
            etLastName.setHint("Enter your password")

            btnShowPass.visibility = View.VISIBLE

            etLastName.transformationMethod = PasswordTransformationMethod.getInstance()

            btnShowPass.setOnClickListener {
                if (etLastName.transformationMethod == HideReturnsTransformationMethod.getInstance()) {
                    etLastName.transformationMethod = PasswordTransformationMethod.getInstance()
                    Glide.with(this)
                        .load(R.drawable.ic_hide_pass)
                        .into(btnShowPass)
                } else {
                    etLastName.transformationMethod = HideReturnsTransformationMethod.getInstance()
                    Glide.with(this)
                        .load(R.drawable.ic_show_pass)
                        .into(btnShowPass)
                }
            }

            btnLogin.setOnClickListener {
                var userId = etUser.text.toString().trim()
                var lastName = etLastName.text.toString()
                if (userId != "" && lastName != "") {
                    val checkUser = checkTeacher(userId, lastName)

                    if (checkUser) {

                        Toast.makeText(this@LoginActivity, "Mag siling da kita!", Toast.LENGTH_SHORT).show()
                        Handler().postDelayed({
                            val intent = Intent(this, HomeActivity::class.java)

                            startActivity(intent)
                            finish()
                        }, 500)
                    }
                    else {
                        Toast.makeText(this, "No user found", Toast.LENGTH_SHORT).show()
                    }
                }
                else {
                    Toast.makeText(this, "Please provide your credentials", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    private fun checkTeacher(userId: String, password: String): Boolean {
        val tableName = "tbl_teacher"

        db = openOrCreateDatabase("tagakauloDB", Context.MODE_PRIVATE, null)
        val checkQuery = "SELECT * FROM $tableName WHERE teacherId = ? AND password = ?"
        val topicArray = arrayOf(userId, password)
        val teacherCursor: Cursor = db.rawQuery(checkQuery, topicArray)
        if (teacherCursor.moveToFirst()) {
            val columnIndexTeacherId = teacherCursor.getColumnIndex("teacherId")
            val columnIndexFirstName = teacherCursor.getColumnIndex("firstName")
            val columnIndexLastName = teacherCursor.getColumnIndex("lastName")
            if (columnIndexFirstName != -1 && columnIndexLastName != -1) {
                do {
                    teacherId = teacherCursor.getString(columnIndexTeacherId)
                    firstName = teacherCursor.getString(columnIndexFirstName)
                    lastName = teacherCursor.getString(columnIndexLastName)

                    val editor = sharedPreferences.edit()
                    editor.putString("userId", teacherId)
                    editor.putString("firstName", firstName)
                    editor.putString("lastName", lastName)
                    editor.apply()
                } while (teacherCursor.moveToNext())
            }
            return true
        } else {
            return false
        }
    }
    private fun checkLearner(learnerFN: String, learnerLN: String): Boolean {
        val tableName = "tbl_learner"

        db = openOrCreateDatabase("tagakauloDB", Context.MODE_PRIVATE, null)
        var checkQuery = "SELECT * FROM $tableName WHERE firstName = ? AND lastName = ?"
        val topicArray = arrayOf(learnerFN, learnerLN)
        val learnerCursor: Cursor = db.rawQuery(checkQuery, topicArray)
        if (learnerCursor.moveToFirst()) {
            val columnIndexLearnerId = learnerCursor.getColumnIndex("learnerId")
            val columnIndexFirstName = learnerCursor.getColumnIndex("firstName")
            val columnIndexLastName = learnerCursor.getColumnIndex("lastName")
            val columnIndexAdviser = learnerCursor.getColumnIndex("addedBy")
            if (columnIndexFirstName != -1 && columnIndexLastName != -1) {
                    learnerId = learnerCursor.getString(columnIndexLearnerId)
                    firstName = learnerCursor.getString(columnIndexFirstName)
                    lastName = learnerCursor.getString(columnIndexLastName)
                    val batch = learnerCursor.getString(columnIndexAdviser)

                    val editor = sharedPreferences.edit()
                    editor.putString("learnerId", learnerId)
                    editor.putString("firstName", learnerFN)
                    editor.putString("lastName", learnerLN)
                    editor.putString("adviser", batch)
                    editor.apply()
            }
            learnerCursor.close()
            return true
        }
        else {
            return false
        }
    }

    override fun onBackPressed() {
        val intent = Intent(this, UserOption::class.java)
        startActivity(intent)
        finish()
    }
}