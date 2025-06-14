package com.example.tagakaulolearningapp

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase

class QuizProgressClass(context: Context, userType: String, user: String, subjectId: String, quizId: String, status: String) {
    private lateinit var db: SQLiteDatabase
    private lateinit var subject: String
    private lateinit var progress: String
    private val tableName1 = "tbl_learnerQuizProgress"
    private val tableName2 = "tbl_teacherQuizProgress"
    private val user = user
    private val userType = userType
    private val subjectId = subjectId
    private val quizId = quizId
    private val status = status

    init {
        db = context.openOrCreateDatabase("tagakauloDB", Context.MODE_PRIVATE, null)
    }

    private fun checkUser() {
        if (userType.lowercase() == "learner") {
            checkLearnerProgress()
        }
        else if(userType.lowercase() == "teacher") {
            checkTeacherProgress()
        }
        else {

        }
    }

    private fun checkLearnerProgress() {
        val checkQuery1 = "SELECT * FROM $tableName1 WHERE studentName = ? AND subjectId = ? AND quizId = ?"
        val progressArray = arrayOf(user, subjectId, quizId)
        val progressCursor: Cursor = db.rawQuery(checkQuery1, progressArray)

        if (progressCursor.moveToFirst()) {
            val columnIndexSubjectId = progressCursor.getColumnIndex("subject")
            val columnIndexStatus = progressCursor.getColumnIndex("status")

            if (columnIndexSubjectId != -1) {
                do {
                    subject = progressCursor.getString(columnIndexSubjectId)
                    progress = progressCursor.getString(columnIndexStatus)
                } while (progressCursor.moveToNext())
            }

            if (progress > status) {
                //do nothing
            }
            else {
                updateLearnerProgress(progress)
            }

        } else {
            createLearnerProgress()
        }
    }
    private fun updateLearnerProgress(progress: String) {
        val insertQuery = "UPDATE $tableName1 SET status = ? WHERE studentName = ? AND quizId = ? AND subjectId = ?"
        val progressArray = arrayOf(progress, user, quizId, subjectId)
        db.execSQL(insertQuery, progressArray)
    }
    private fun createLearnerProgress() {
        val insertQuery = "INSERT INTO $tableName1 (studentName, subjectId, quizId, status) VALUES (?,?,?,?)"
        val progressArray = arrayOf(user, subjectId, quizId, status)
        db.execSQL(insertQuery, progressArray)
    }

    private fun checkTeacherProgress() {
        val checkQuery1 = "SELECT * FROM $tableName2 WHERE teacherId = ? AND subjectId = ? AND quizId = ?"
        val progressArray = arrayOf(user, subjectId, quizId)
        val progressCursor: Cursor = db.rawQuery(checkQuery1, progressArray)

        if (progressCursor.moveToFirst()) {
            val columnIndexSubjectId = progressCursor.getColumnIndex("subject")
            val columnIndexStatus = progressCursor.getColumnIndex("status")

            if (columnIndexSubjectId != -1) {
                do {
                    subject = progressCursor.getString(columnIndexSubjectId)
                    progress = progressCursor.getString(columnIndexStatus)
                } while (progressCursor.moveToNext())
            }

            if (progress > status) {
                //do nothing
            }
            else {
                updateTeacherProgress(progress)
            }

        } else {
            createTeacherProgress()
        }
    }
    private fun updateTeacherProgress(progress: String) {
        val insertQuery = "UPDATE $tableName2 SET status = ? WHERE teacherId = ? AND quizId = ? AND subjectId = ?"
        val progressArray = arrayOf(progress, user, quizId, subjectId)
        db.execSQL(insertQuery, progressArray)
    }
    private fun createTeacherProgress() {
        val insertQuery = "INSERT INTO $tableName2 (teacherId, subjectId, quizId, status) VALUES (?,?,?,?)"
        val progressArray = arrayOf(user, subjectId, quizId, status)
        db.execSQL(insertQuery, progressArray)
    }
}