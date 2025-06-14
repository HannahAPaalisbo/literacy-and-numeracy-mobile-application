package com.example.tagakaulolearningapp

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase

class TopicProgressClass(context: Context, userType: String, user: String, subjectId: String, topicId: String, status: String) {
    private lateinit var db: SQLiteDatabase
    private lateinit var subject: String
    private lateinit var progress: String
    private val tableName1 = "tbl_learnerProgress"
    private val tableName2 = "tbl_teacherProgress"
    private val user = user
    private val userType = userType
    private val subjectId = subjectId
    private val topicId = topicId
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
        val checkQuery1 = "SELECT * FROM $tableName1 WHERE studentName = ? AND subjectId = ? AND topicId = ?"
        val progressArray = arrayOf(user, subjectId, topicId)
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
        val insertQuery = "UPDATE $tableName1 SET status = ? WHERE studentName = ? AND topicId = ? AND subjectId = ?"
        val progressArray = arrayOf(progress, user, topicId, subjectId)
        db.execSQL(insertQuery, progressArray)
    }
    private fun createLearnerProgress() {
        val insertQuery = "INSERT INTO $tableName1 (studentName, subjectId, topicId, status) VALUES (?,?,?,?)"
        val progressArray = arrayOf(user, subjectId, topicId, status)
        db.execSQL(insertQuery, progressArray)
    }

    private fun checkTeacherProgress() {
        val checkQuery1 = "SELECT * FROM $tableName2 WHERE teacherId = ? AND subjectId = ? AND topicId = ?"
        val progressArray = arrayOf(user, subjectId, topicId)
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
        val insertQuery = "UPDATE $tableName2 SET status = ? WHERE teacherId = ? AND topicId = ? AND subjectId = ?"
        val progressArray = arrayOf(progress, user, topicId, subjectId)
        db.execSQL(insertQuery, progressArray)
    }
    private fun createTeacherProgress() {
        val insertQuery = "INSERT INTO $tableName2 (teacherId, subjectId, topicId, status) VALUES (?,?,?,?)"
        val progressArray = arrayOf(user, subjectId, topicId, status)
        db.execSQL(insertQuery, progressArray)
    }
}