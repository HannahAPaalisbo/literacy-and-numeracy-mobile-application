package com.example.tagakaulolearningapp

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

class UserOption : ConnectivityClass() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        actionBar?.hide()
        setContentView(R.layout.activity_user_option)

        DeviceNavigationClass.hideNavigationBar(this)

        val sharedPreferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
        sharedPreferences.edit().clear().apply()

        val defaultUser = findViewById<Button>(R.id.btnLearner)
        val teacherUser = findViewById<Button>(R.id.btnTeacher)
        val refreshLayout = findViewById<SwipeRefreshLayout>(R.id.swiperefresh)

        refreshLayout.setOnRefreshListener {
            if (checkAndShowToast(this@UserOption, "userSelectionPage") == false) {
                Toast.makeText(this@UserOption, "Failed to refresh contents", Toast.LENGTH_SHORT).show()
            }
            refreshLayout.isRefreshing = false
        }

        defaultUser.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)

            val editor = sharedPreferences.edit()
            editor.putString("userType", "learner")
            editor.apply()

            startActivity(intent)
            finish()
        }

        teacherUser.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)

            val editor = sharedPreferences.edit()
            editor.putString("userType", "teacher")
            editor.apply()

            startActivity(intent)
            finish()
        }
    }
}