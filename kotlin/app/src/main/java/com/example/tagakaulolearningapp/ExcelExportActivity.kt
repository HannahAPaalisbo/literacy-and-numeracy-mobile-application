package com.example.tagakaulolearningapp

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.text.BoringLayout
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.core.content.ContextCompat
import okhttp3.*
import org.apache.poi.ss.usermodel.*
import org.apache.poi.ss.util.CellRangeAddress
import org.apache.poi.xssf.usermodel.IndexedColorMap
import org.apache.poi.xssf.usermodel.XSSFColor
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ExcelExportActivity : AppCompatActivity() {
    init {
        System.setProperty(
            "org.apache.poi.javax.xml.stream.XMLInputFactory",
            "com.fasterxml.aalto.stax.InputFactoryImpl"
        )
        System.setProperty(
            "org.apache.poi.javax.xml.stream.XMLOutputFactory",
            "com.fasterxml.aalto.stax.OutputFactoryImpl"
        )
        System.setProperty(
            "org.apache.poi.javax.xml.stream.XMLEventFactory",
            "com.fasterxml.aalto.stax.EventFactoryImpl"
        )
    }
    var topicId: String = ""
    var quizType: String = ""
    var teacherId: String = ""
    var currentSubject: String = ""
    var studentList: ArrayList<String> = ArrayList()
    var quarterList: ArrayList<String> = ArrayList()
    var quizList: ArrayList<String> = ArrayList()
    var filename: String = ""
    private lateinit var batchName: String
    lateinit var parentLayout: LinearLayout
    lateinit var txtHeader: TextView
    lateinit var scrollView: ScrollView
    lateinit var txtView: Spinner
    lateinit var txtView3: EditText
    lateinit var spinner: Spinner
    lateinit var spinner1: Spinner
    lateinit var spinner2: Spinner
    lateinit var spinner3: Spinner
    lateinit var button: Button
    private lateinit var userId: String
    private lateinit var db: SQLiteDatabase
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        actionBar?.hide()
        setContentView(R.layout.activity_excel_export)

        val sharedPreferences = getSharedPreferences("MyPreferences", MODE_PRIVATE)
        teacherId = sharedPreferences.getString("userId", "").toString()
        topicId = sharedPreferences.getString("topicId", "").toString()
        quizType = sharedPreferences.getString("quizType", "").toString()
        currentSubject = sharedPreferences.getString("currentSubject", "").toString()
        userId = sharedPreferences.getString("userId", "").toString()
        db = openOrCreateDatabase("tagakauloDB", Context.MODE_PRIVATE, null)

        //db.execSQL("DROP TABLE IF EXISTS 'tbl_grading';")
        val tableName = "tbl_grading"
        val createTable = "CREATE TABLE IF NOT EXISTS $tableName (batch TEXT, studentName TEXT, quarter TEXT, record TEXT, subject TEXT, score REAL, addedBy TEXT);"
        db.execSQL(createTable)

        parentLayout = findViewById(R.id.parent_layout)
        txtHeader = findViewById(R.id.header)
        scrollView = findViewById(R.id.scrollView2)
        txtView = findViewById(R.id.textView)
        spinner = findViewById(R.id.spinner)
        spinner1 = findViewById(R.id.spinner1)
        spinner2 = findViewById(R.id.spinner2)
        spinner3 = findViewById(R.id.spinner3)
        txtView3 = findViewById<EditText>(R.id.textView3)
        button = findViewById<Button>(R.id.button)
        val button2 = findViewById<Button>(R.id.button2)

        getSubjects()

        if (button.text.toString().uppercase() == "ADD STUDENT") {
            txtHeader.text = "Export Datas"
        }
        else {

        }

        val filesNames = arrayListOf(getString(R.string.spinner1_prompt), "Quiz", "Recitation")
        val quarterList = arrayListOf(getString(R.string.spinner2_prompt), "First Quarter", "Second Quarter", "Third Quarter", "Fourth Quarter")

        val adapter3 = ArrayAdapter(this, android.R.layout.simple_spinner_item, quizList)
        adapter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        val prompt1 = getString(R.string.spinner_prompt)
        adapter3.insert(prompt1, 0)
        spinner3.adapter = adapter3

        spinner3.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parentView: AdapterView<*>?,
                selectedItemView: View?,
                position: Int,
                id: Long
            ) {
                var batchSelectedItem = parentView?.getItemAtPosition(position).toString()

                if (batchSelectedItem == getString(R.string.spinner_prompt)) {
                    for (i in 0 until parentLayout.childCount) {
                        val childView = parentLayout.getChildAt(i)
                        parentLayout.removeView(childView)
                    }
                    showAllData()
                }
                else {
                    showRecord(batchSelectedItem, "", "")
                }

                val studentList = getQuizNameList(batchSelectedItem)
                val textView = ArrayAdapter(this@ExcelExportActivity, android.R.layout.simple_spinner_item, studentList)
                textView.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                txtView.adapter = textView
            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {
                for (i in 0 until parentLayout.childCount) {
                    val childView = parentLayout.getChildAt(i)
                    parentLayout.removeView(childView)
                }
                showAllData()
            }
        }

        val adapter2 = ArrayAdapter(this, android.R.layout.simple_spinner_item, quarterList)
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner2.adapter = adapter2

        val adapter1 = ArrayAdapter(this, android.R.layout.simple_spinner_item, filesNames)
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner1.adapter = adapter1

        val studentList = getLearnerList()
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, studentList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        val prompt = getString(R.string.student_prompt)
        adapter.insert(prompt, 0)
        spinner.adapter = adapter

        if (quizType.isNotEmpty()) { spinner1.setSelection(1) }
        getPreference()

        button.setOnClickListener {
            batchName = spinner3.selectedItem.toString()
            var selectedRecord = spinner1.selectedItem.toString()
            var selectedSubject = txtView.selectedItem.toString()
            var studentName = spinner.selectedItem.toString()
            var selectedQuarter = spinner2.selectedItem.toString()
            var score = txtView3.text

            if (button.text.toString().uppercase() == "ADD STUDENT") {
                if (selectedQuarter == getString(R.string.spinner2_prompt) || selectedRecord == getString(R.string.spinner1_prompt) || selectedSubject == prompt || score.isNullOrEmpty() || studentName == getString(R.string.student_prompt)) {
                    if (selectedQuarter == getString(R.string.spinner2_prompt) && selectedRecord == getString(R.string.spinner1_prompt) && selectedSubject == prompt && score.isNullOrEmpty() && studentName == getString(R.string.student_prompt)) {
                        Toast.makeText(this, "Kindly fill the empty inputs", Toast.LENGTH_LONG).show()
                    }
                    else if (studentName == getString(R.string.student_prompt)) {
                        Toast.makeText(this, "Student Name is empty", Toast.LENGTH_LONG).show()
                    }
                    else if (score.isNullOrEmpty()) {
                        Toast.makeText(this, "Score is empty", Toast.LENGTH_LONG).show()
                    }
                    else if (selectedQuarter == getString(R.string.spinner2_prompt)) {
                        Toast.makeText(this, "Kindly select what quarter", Toast.LENGTH_LONG).show()
                    }
                    else if (selectedRecord == getString(R.string.spinner1_prompt)) {
                        Toast.makeText(this, "Kindly select where to record", Toast.LENGTH_LONG).show()
                    }
                    else if (selectedSubject == prompt) {
                        Toast.makeText(this, "Kindly select a subject", Toast.LENGTH_LONG).show()
                    }
                }
                else {
                    val name = studentName.split(Regex(",\\s*"))
                    val lastname = name[0]
                    val firstname = name[1].lowercase().replaceFirstChar { it.uppercaseChar() }

                    var cleanedName = lastname.uppercase() + ", " + firstname
                    val record = checkStudent(batchName, cleanedName, selectedQuarter, selectedRecord, selectedSubject)
                    if (record) {
                        showRecord(batchName, cleanedName, selectedRecord)
                        Toast.makeText(this, "The record already exists", Toast.LENGTH_SHORT).show()
                    }
                    else {
                        Toast.makeText(this, "Successfully Added", Toast.LENGTH_LONG).show()
                        addStudents(batchName, selectedQuarter, cleanedName, selectedRecord, selectedSubject, score.toString().toDouble())
                        val learnerId = getLearnerId(lastname.lowercase().replaceFirstChar { it.uppercaseChar() }, firstname)
                        updateCentralDBLearnerProgress(learnerId, batchName, score.toString())
                        showRecord(batchName, cleanedName, selectedRecord)
                        txtView.setSelection(0)
                        getPreference()
                        txtView3.setText("")
                    }
                }
            }
            else {
                if (batchName == getString(R.string.spinner_prompt) || selectedQuarter == getString(R.string.spinner2_prompt) || selectedRecord == getString(R.string.spinner1_prompt) || selectedSubject == prompt || score.isNullOrEmpty() || studentName == getString(R.string.student_prompt)) {
                    if (batchName == getString(R.string.spinner_prompt) || selectedQuarter == getString(R.string.spinner2_prompt) && selectedRecord == getString(R.string.spinner1_prompt) && selectedSubject == prompt && score.isNullOrEmpty() && studentName == getString(R.string.student_prompt)) {
                        Toast.makeText(this, "Kindly fill the empty inputs", Toast.LENGTH_LONG).show()
                    }
                    else if (batchName == getString(R.string.spinner_prompt)) {
                        Toast.makeText(this, "Kindly select what batch", Toast.LENGTH_LONG).show()
                    }
                    else if (studentName == getString(R.string.student_prompt)) {
                        Toast.makeText(this, "Student Name is empty", Toast.LENGTH_LONG).show()
                    }
                    else if (score.isNullOrEmpty()) {
                        Toast.makeText(this, "Score is empty", Toast.LENGTH_LONG).show()
                    }
                    else if (selectedQuarter == getString(R.string.spinner2_prompt)) {
                        Toast.makeText(this, "Kindly select what quarter", Toast.LENGTH_LONG).show()
                    }
                    else if (selectedRecord == getString(R.string.spinner1_prompt)) {
                        Toast.makeText(this, "Kindly select where to record", Toast.LENGTH_LONG).show()
                    }
                    else if (selectedSubject == prompt) {
                        Toast.makeText(this, "Kindly select a subject", Toast.LENGTH_LONG).show()
                    }
                }
                else {
                    val name = studentName.split(Regex(",\\s*"))
                    val lastname = name[0]
                    val firstname = name[1].lowercase().replaceFirstChar { it.uppercaseChar() }

                    var cleanedName = lastname.uppercase() + ", " + firstname
                    updateStudents(batchName, selectedQuarter, cleanedName.toString(), selectedRecord, selectedSubject, score.toString().toDouble())

                    txtView.setSelection(0)
                    spinner.setSelection(0)
                    txtView3.setText("")
                    button.text = "ADD STUDENT"
                    parentLayout.removeAllViews()
                    showRecord(batchName, cleanedName.toString(), selectedRecord)
                    Toast.makeText(this, "Successfully Updated", Toast.LENGTH_SHORT).show()

                }

            }
        }
        button2.setOnClickListener {
            batchName = spinner3.selectedItem.toString()
            getStudents(batchName)
            if (studentList.isEmpty()) {
                Toast.makeText(this, "No grade records found", Toast.LENGTH_LONG).show()
            } else {
                quizList.removeAt(0)
                quarterList.removeAt(0)
                val workbook = createWorkbook(studentList, quizList, quarterList)
                createExcel(workbook)
                Toast.makeText(this, "Check Documents for the file", Toast.LENGTH_LONG).show()
                studentList.clear()
                this.quarterList.clear()
            }

        }
    }

    private fun createWorkbook(studentList: List<String>, quizList: List<String>, quarterList: List<String>): Workbook {
        val workbook = XSSFWorkbook()

        val usedQuarterNames = HashSet<String>() // To track used quarter names

        val quarterSheet = arrayListOf<String>("First Quarter", "Second Quarter", "Third Quarter", "Fourth Quarter")
        for (quarter in quarterSheet) {
            val currentSheet = workbook.getSheet(quarter)
            var sampleSheet = ""
            if (currentSheet == null) { sampleSheet = "null" }
            else { sampleSheet = currentSheet.sheetName }
            if (sampleSheet == quarter) {
                val cellStyle = getHeaderStyle(workbook)

                addData(workbook, workbook.getSheet(quarter))
                createSheetHeader(cellStyle, workbook.getSheet(quarter))
            }
            else {
                val createSheet = workbook.createSheet(quarter)
                val cellStyle = getHeaderStyle(workbook)

                addData(workbook, createSheet)
                createSheetHeader(cellStyle, createSheet)
            }
        }

        return workbook
    }

    private fun createSheetHeader(cellStyle: CellStyle, sheet: Sheet) {
        val row = sheet.createRow(0)

        val HEADER_LIST = arrayOf("Student Name") + quizList.toTypedArray() + arrayOf("Points", "Average", "Grade")

        for ((index, value) in HEADER_LIST.withIndex()) {
            val columnWidth = (15 * 500)

            sheet.setColumnWidth(index, columnWidth)

            val cell = row.createCell(index)

            cell.setCellValue(value)

            cell.setCellStyle(cellStyle)
        }
    }

    private fun getHeaderStyle(workbook: Workbook): CellStyle {
        val cellStyle = workbook.createCellStyle()

        val font = workbook.createFont()
        font.setBold(true)

        cellStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.index)
        cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND)

        cellStyle.setAlignment(HorizontalAlignment.CENTER)
        cellStyle.setFont(font)

        return cellStyle
    }

    private fun addData(workbook: Workbook, sheet: Sheet) {

        val rowIndex = 0
        var row = sheet.createRow(rowIndex)

        createCell(row, 0, "Student Name") //Column 1
        var quizIndex = 1
        for (i in quizList.indices) {
            val subject = quizList[i]
            createCell(row, quizIndex, subject)
            quizIndex += 1
        }
        createCell(row, quizList.size + 1, "Points")
        createCell(row, quizList.size + 2, "Average")
        createCell(row, quizList.size + 3, "Grade")
        val columnLetter1 = getColumnLetter((quizList.size + 3))
        sheet.setAutoFilter(CellRangeAddress.valueOf("A:$columnLetter1"))

        var rowIndex1 = 1
        var sheetName = sheet.sheetName

        for (i in 0 until quarterList.size) {
            var quarter = quarterList[i]
            if (sheetName == quarter) {
                var row2: Row = sheet.createRow(rowIndex1)
                var studentName = studentList[i]
                createCell(row2, 0, studentName)
                var quizIndex3 = 1
                for (j in quizList.indices) {
                    var subject = quizList[j]
                    val score = getScore(studentName, subject, quarter)
                    createCell(row2, quizIndex3, score.toString()) // Start from column 2
                    quizIndex3 += 1
                }
                var points = getStudentScore(i)
                var average = getAverage(i)
                var grade = getGrade(average)
                createCell(row2, quizList.size + 1, points.toString())
                createCell(row2, quizList.size + 2, String.format("%.2f", average) + "%") // Add "Average" header
                val gradeCell = row2.createCell((quizList.size + 3))
                gradeCell.setCellValue(grade)
                val cellStyle : CellStyle = workbook.createCellStyle()

                if (grade == "Passed" || grade == "Outstanding") {
                    cellStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.index)
                    cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND)
                    gradeCell.setCellStyle(cellStyle)
                } else if (grade == "Failed") {
                    cellStyle.setFillForegroundColor(IndexedColors.CORAL.index)
                    cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND)
                    gradeCell.setCellStyle(cellStyle)
                }

                rowIndex1 += 1
            }
        }
    }
    private fun getColumnLetter(columnIndex: Int): String {
        var newColumnIndex = 0
        if (columnIndex < 1) {
            newColumnIndex = 1
        } else { newColumnIndex = columnIndex + 1 }

        val newColumn = StringBuilder()
        var n = newColumnIndex

        while (n > 0) {
            val remainder = (n - 1) % 26
            newColumn.insert(0, ('A' + remainder).toChar())
            n = (n - remainder ) / 26
        }

        return newColumn.toString()
    }
    private fun createCell(row: Row, columnIndex: Int, value: String?) {
        val cell = row.createCell(columnIndex)
        cell?.setCellValue(value)
    }

    private fun createExcel(workbook: Workbook) {
        val filename = teacherId + "_Grading_" + batchName + ".xlsx"
        val appDirectory = File(Environment.getExternalStorageDirectory(), "Documents")

        if (!appDirectory.exists()) {
            appDirectory.mkdirs()
        }

        val excelFile = File(appDirectory, filename)

        try {
            val fileOut = FileOutputStream(excelFile)
            workbook.write(fileOut)
            fileOut.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
    private fun checkStudent(batch: String, studentName: String, quarter: String, record: String, quiz: String): Boolean {
        val tableName = "tbl_grading"
        val checkQuery = "SELECT * FROM $tableName WHERE studentName = ? AND quarter = ? AND record = ? AND subject = ? AND addedBy = ? OR addedBy = ?"
        val checkArray = arrayOf(studentName, quarter, record, quiz, userId, "SYS")
        val studentCursor = db.rawQuery(checkQuery, checkArray)

        if (studentCursor.count > 0) {
            return true
            studentCursor.close()
        } else {
            return false
            studentCursor.close()
        }
    }
    private fun addStudents(batch: String, quarter: String, studentName: String, record: String, quiz: String, score: Double) {
        val tableName = "tbl_grading"
        val insertQuery = "INSERT INTO $tableName (studentName, quarter, record, subject, score, addedBy) VALUES (?,?,?,?,?,?)"
        val teacherArray = arrayOf(studentName, quarter, record, quiz, score, userId)
        db.execSQL(insertQuery, teacherArray)
        println("Added $studentName with a score of $score under the subject $quiz ($record - $quarter)")
    }

    private fun updateStudents(batch: String, quarter: String, studentName: String, record: String, quiz: String, score: Double) {
        val tableName = "tbl_grading"
        val updateQuery = "UPDATE $tableName SET studentName = ?, quarter = ?, record = ?, subject = ?, score = ? WHERE quarter = ? AND studentName = ? AND record = ? AND subject = ? AND addedBy = ? OR addedBy = ?"
        val teacherArray = arrayOf(studentName, quarter, record, quiz, score, quarter, studentName, record, quiz, userId, "SYS")
        db.execSQL(updateQuery, teacherArray)
        println("Updated $studentName with a score of $score under the subject $quiz ($record - $quarter)")
    }

    private fun showRecord(batch: String, studentName: String, record: String){
        parentLayout.removeAllViews()

        txtHeader.visibility = View.VISIBLE
        scrollView.visibility = View.VISIBLE
        txtHeader.text = batch

        if (studentName == "" || record == "") {
            val tableName = "tbl_grading"
            val query = "SELECT * FROM $tableName WHERE addedBy = ? OR addedBy = ? ORDER BY studentName, record ASC"
            val recordArray = arrayOf(userId, "SYS")
            val recordCursor = db.rawQuery(query, recordArray)

            if (batch != "") {
                txtHeader.text = "All Records"
                if (recordCursor.moveToFirst()) {
                    val batchIndex = recordCursor.getColumnIndex("batch")
                    val studentNameIndex = recordCursor.getColumnIndex("studentName")
                    val recordIndex = recordCursor.getColumnIndex("record")
                    val subjectIndex = recordCursor.getColumnIndex("subject")
                    val quarterIndex = recordCursor.getColumnIndex("quarter")
                    val scoreIndex = recordCursor.getColumnIndex("score")
                    if(studentNameIndex != -1) {
                        do {
                            val quarter = recordCursor.getString(quarterIndex)
                            var record = recordCursor.getString(recordIndex)
                            var studentName = recordCursor.getString(studentNameIndex)

                            val displayName = studentName.split(", ")
                            val newName = displayName[0].uppercase() + ", " + displayName[1].firstOrNull()?.uppercase() ?: ""

                            val inflater = LayoutInflater.from(this)
                            val subjectLayout = inflater.inflate(R.layout.layout_excel_data, parentLayout, false) as RelativeLayout
                            parentLayout.removeView(subjectLayout)
                            val childLayout = subjectLayout.findViewById<RelativeLayout>(R.id.childLayout)
                            val txtQuarter = subjectLayout.findViewById<TextView>(R.id.quarter)
                            val txtRecord = subjectLayout.findViewById<TextView>(R.id.record)
                            val txtSubject = subjectLayout.findViewById<TextView>(R.id.subject)
                            val txtScore = subjectLayout.findViewById<TextView>(R.id.score)
                            val btnUpdate = subjectLayout.findViewById<Button>(R.id.update)

                            if (record.lowercase() == "quiz") {
                                val record = recordCursor.getString(recordIndex)
                                val subject = recordCursor.getString(subjectIndex)
                                val score = recordCursor.getString(scoreIndex)
                                val quarter = recordCursor.getString(quarterIndex)

                                var quarterInitial = 0
                                when (quarter) {
                                    "First Quarter" -> { txtQuarter.text = "1" }
                                    "Second Quarter" -> { txtQuarter.text = "2" }
                                    "Third Quarter" -> { txtQuarter.text = "3" }
                                    "Fourth Quarter" -> { txtQuarter.text = "4" }
                                    else -> { txtQuarter.text = "0" }
                                }
                                val quizBg = ContextCompat.getDrawable(this, R.drawable.def_container_quiz_bg)
                                childLayout.background = quizBg
                                btnUpdate.text = btnUpdate.text.toString()
                                txtHeader.text = batch
                                txtSubject.text = subject
                                txtRecord.text = newName
                                txtScore.text = score
                                btnUpdate.setOnClickListener {
                                    if (record == "Quiz") { spinner1.setSelection(1) } else { spinner1.setSelection(2) }
                                    val index2 = quizList.indexOf(subject)
                                    val quarterList = arrayListOf(getString(R.string.spinner2_prompt), "First Quarter", "Second Quarter", "Third Quarter", "Fourth Quarter")
                                    val index3 = quarterList.indexOf(quarter)
                                    spinner.setSelection(index2)
                                    spinner2.setSelection(index3)

                                    button.text = btnUpdate.text.toString()
                                    btnUpdate.text = btnUpdate.text.toString()
                                }
                            }
                            else {
                                val record = recordCursor.getString(recordIndex)
                                val subject = recordCursor.getString(subjectIndex)
                                val score = recordCursor.getString(scoreIndex)
                                val quarter = recordCursor.getString(quarterIndex)

                                var quarterInitial = 0
                                when (quarter) {
                                    "First Quarter" -> { txtQuarter.text = "1" }
                                    "Second Quarter" -> { txtQuarter.text = "2" }
                                    "Third Quarter" -> { txtQuarter.text = "3" }
                                    "Fourth Quarter" -> { txtQuarter.text = "4" }
                                    else -> { txtQuarter.text = "0" }
                                }

                                val quizBg = ContextCompat.getDrawable(this, R.drawable.def_container_recite_bg)
                                childLayout.background = quizBg

                                btnUpdate.text = btnUpdate.text.toString()
                                txtHeader.text = batch
                                txtSubject.text = subject
                                txtRecord.text = newName
                                txtScore.text = score
                                btnUpdate.setOnClickListener {
                                    if (record == "Quiz") { spinner1.setSelection(1) } else { spinner1.setSelection(2) }
                                    val index2 = quizList.indexOf(subject)
                                    val quarterList = arrayListOf(getString(R.string.spinner2_prompt), "First Quarter", "Second Quarter", "Third Quarter", "Fourth Quarter")
                                    val index3 = quarterList.indexOf(quarter)
                                    spinner.setSelection(index2)
                                    spinner2.setSelection(index3)

                                    button.text = btnUpdate.text.toString()
                                    btnUpdate.text = btnUpdate.text.toString()
                                }
                            }
                            val uniqueId = View.generateViewId()
                            subjectLayout.id = uniqueId
                            parentLayout.addView(subjectLayout)
                        } while (recordCursor.moveToNext())
                    }
                }
            }
        }
        else {
            val tableName = "tbl_grading"
            val query = "SELECT * FROM $tableName WHERE studentName = ? AND record = ? AND addedBy = ? OR addedBy = ?"
            val recordArray = arrayOf(studentName, record, userId, "SYS")
            val recordCursor = db.rawQuery(query, recordArray)

            if (recordCursor.moveToFirst()) {
                val batchIndex = recordCursor.getColumnIndex("batch")
                val studentNameIndex = recordCursor.getColumnIndex("studentName")
                val recordIndex = recordCursor.getColumnIndex("record")
                val subjectIndex = recordCursor.getColumnIndex("subject")
                val quarterIndex = recordCursor.getColumnIndex("quarter")
                val scoreIndex = recordCursor.getColumnIndex("score")
                if(batchIndex != -1) {
                    do {
                        val quarter = recordCursor.getString(quarterIndex)
                        var record = recordCursor.getString(recordIndex)
                        var studentName = recordCursor.getString(studentNameIndex)

                        val displayName = studentName.split(", ")
                        val newName =
                            displayName[0].uppercase() + ", " + displayName[1].firstOrNull()
                                ?.uppercase() ?: ""

                        val inflater = LayoutInflater.from(this)
                        val subjectLayout = inflater.inflate(
                            R.layout.layout_excel_data,
                            parentLayout,
                            false
                        ) as RelativeLayout
                        parentLayout.removeView(subjectLayout)
                        val childLayout =
                            subjectLayout.findViewById<RelativeLayout>(R.id.childLayout)
                        val txtQuarter = subjectLayout.findViewById<TextView>(R.id.quarter)
                        val txtRecord = subjectLayout.findViewById<TextView>(R.id.record)
                        val txtSubject = subjectLayout.findViewById<TextView>(R.id.subject)
                        val txtScore = subjectLayout.findViewById<TextView>(R.id.score)
                        val btnUpdate = subjectLayout.findViewById<Button>(R.id.update)

                        if (record.lowercase() == "quiz") {
                            val record = recordCursor.getString(recordIndex)
                            val subject = recordCursor.getString(subjectIndex)
                            val score = recordCursor.getString(scoreIndex)
                            val quarter = recordCursor.getString(quarterIndex)

                            var quarterInitial = 0
                            when (quarter) {
                                "First Quarter" -> {
                                    txtQuarter.text = "1"
                                }
                                "Second Quarter" -> {
                                    txtQuarter.text = "2"
                                }
                                "Third Quarter" -> {
                                    txtQuarter.text = "3"
                                }
                                "Fourth Quarter" -> {
                                    txtQuarter.text = "4"
                                }
                                else -> {
                                    txtQuarter.text = "0"
                                }
                            }
                            val quizBg =
                                ContextCompat.getDrawable(this, R.drawable.def_container_quiz_bg)
                            childLayout.background = quizBg
                            btnUpdate.text = btnUpdate.text.toString()
                            txtHeader.text = batch
                            txtSubject.text = subject
                            txtRecord.text = newName
                            txtScore.text = score
                            btnUpdate.setOnClickListener {
                                if (record == "Quiz") {
                                    spinner1.setSelection(1)
                                } else {
                                    spinner1.setSelection(2)
                                }
                                val index2 = quizList.indexOf(subject)
                                val quarterList = arrayListOf(
                                    getString(R.string.spinner2_prompt),
                                    "First Quarter",
                                    "Second Quarter",
                                    "Third Quarter",
                                    "Fourth Quarter"
                                )
                                val index3 = quarterList.indexOf(quarter)
                                spinner.setSelection(index2)
                                spinner2.setSelection(index3)

                                button.text = btnUpdate.text.toString()
                                btnUpdate.text = btnUpdate.text.toString()
                            }
                        } else {
                            val record = recordCursor.getString(recordIndex)
                            val subject = recordCursor.getString(subjectIndex)
                            val score = recordCursor.getString(scoreIndex)
                            val quarter = recordCursor.getString(quarterIndex)

                            var quarterInitial = 0
                            when (quarter) {
                                "First Quarter" -> {
                                    txtQuarter.text = "1"
                                }
                                "Second Quarter" -> {
                                    txtQuarter.text = "2"
                                }
                                "Third Quarter" -> {
                                    txtQuarter.text = "3"
                                }
                                "Fourth Quarter" -> {
                                    txtQuarter.text = "4"
                                }
                                else -> {
                                    txtQuarter.text = "0"
                                }
                            }

                            val quizBg =
                                ContextCompat.getDrawable(this, R.drawable.def_container_recite_bg)
                            childLayout.background = quizBg

                            btnUpdate.text = btnUpdate.text.toString()
                            txtHeader.text = batch
                            txtSubject.text = subject
                            txtRecord.text = newName
                            txtScore.text = score
                            btnUpdate.setOnClickListener {
                                if (record == "Quiz") {
                                    spinner1.setSelection(1)
                                } else {
                                    spinner1.setSelection(2)
                                }
                                val index2 = quizList.indexOf(subject)
                                val quarterList = arrayListOf(
                                    getString(R.string.spinner2_prompt),
                                    "First Quarter",
                                    "Second Quarter",
                                    "Third Quarter",
                                    "Fourth Quarter"
                                )
                                val index3 = quarterList.indexOf(quarter)
                                spinner.setSelection(index2)
                                spinner2.setSelection(index3)

                                button.text = btnUpdate.text.toString()
                                btnUpdate.text = btnUpdate.text.toString()
                            }
                        }
                        val uniqueId = View.generateViewId()
                        subjectLayout.id = uniqueId
                        parentLayout.addView(subjectLayout)
                    } while (recordCursor.moveToNext())
                }
            }
        }

    }
    private fun showAllData() {
        parentLayout.removeAllViews()

        txtHeader.visibility = View.VISIBLE
        scrollView.visibility = View.VISIBLE
        val tableName = "tbl_grading"
        val query = "SELECT * FROM $tableName WHERE addedBy = ? OR addedBy = ? ORDER BY studentName, record ASC"
        val recordCursor = db.rawQuery(query, arrayOf(userId, "SYS"))

        if (recordCursor.moveToFirst()) {
            val studentNameIndex = recordCursor.getColumnIndex("studentName")
            val recordIndex = recordCursor.getColumnIndex("record")
            val subjectIndex = recordCursor.getColumnIndex("subject")
            val quarterIndex = recordCursor.getColumnIndex("quarter")
            val scoreIndex = recordCursor.getColumnIndex("score")
            if (studentNameIndex != -1) {
                do {
                    val quarter = recordCursor.getString(quarterIndex)
                    var record = recordCursor.getString(recordIndex)
                    var studentName = recordCursor.getString(studentNameIndex)

                    val displayName = studentName.split(", ")
                    val newName = displayName[0].uppercase() + ", " + displayName[1].firstOrNull()?.uppercase() ?: ""

                    val inflater = LayoutInflater.from(this)
                    val subjectLayout = inflater.inflate(R.layout.layout_excel_data, parentLayout, false) as RelativeLayout
                    parentLayout.removeView(subjectLayout)
                    val childLayout = subjectLayout.findViewById<RelativeLayout>(R.id.childLayout)
                    val txtQuarter = subjectLayout.findViewById<TextView>(R.id.quarter)
                    val txtRecord = subjectLayout.findViewById<TextView>(R.id.record)
                    val txtSubject = subjectLayout.findViewById<TextView>(R.id.subject)
                    val txtScore = subjectLayout.findViewById<TextView>(R.id.score)
                    val btnUpdate = subjectLayout.findViewById<Button>(R.id.update)

                    txtHeader.text = "All Records"

                    if (record.lowercase() == "quiz") {
                        val record = recordCursor.getString(recordIndex)
                        val subject = recordCursor.getString(subjectIndex)
                        val score = recordCursor.getString(scoreIndex)
                        val quarter = recordCursor.getString(quarterIndex)

                        var quarterInitial = 0
                        when (quarter) {
                            "First Quarter" -> { txtQuarter.text = "1" }
                            "Second Quarter" -> { txtQuarter.text = "2" }
                            "Third Quarter" -> { txtQuarter.text = "3" }
                            "Fourth Quarter" -> { txtQuarter.text = "4" }
                            else -> { txtQuarter.text = "0" }
                        }
                        val quizBg = ContextCompat.getDrawable(this, R.drawable.def_container_quiz_bg)
                        childLayout.background = quizBg
                        btnUpdate.text = btnUpdate.text.toString()
                        txtSubject.text = subject
                        txtRecord.text = newName
                        txtScore.text = score
                        btnUpdate.setOnClickListener {
                            if (record == "Quiz") { spinner1.setSelection(1) } else { spinner1.setSelection(2) }
                            val index2 = quizList.indexOf(subject)
                            val quarterList = arrayListOf(getString(R.string.spinner2_prompt), "First Quarter", "Second Quarter", "Third Quarter", "Fourth Quarter")
                            val index3 = quarterList.indexOf(quarter)
                            spinner.setSelection(index2)
                            spinner2.setSelection(index3)

                            button.text = btnUpdate.text.toString()
                            btnUpdate.text = btnUpdate.text.toString()
                        }
                    }
                    else {
                        val record = recordCursor.getString(recordIndex)
                        val subject = recordCursor.getString(subjectIndex)
                        val score = recordCursor.getString(scoreIndex)
                        val quarter = recordCursor.getString(quarterIndex)

                        var quarterInitial = 0
                        when (quarter) {
                            "First Quarter" -> { txtQuarter.text = "1" }
                            "Second Quarter" -> { txtQuarter.text = "2" }
                            "Third Quarter" -> { txtQuarter.text = "3" }
                            "Fourth Quarter" -> { txtQuarter.text = "4" }
                            else -> { txtQuarter.text = "0" }
                        }

                        val quizBg = ContextCompat.getDrawable(this, R.drawable.def_container_recite_bg)
                        childLayout.background = quizBg

                        btnUpdate.text = btnUpdate.text.toString()
                        txtSubject.text = subject
                        txtRecord.text = newName
                        txtScore.text = score
                        btnUpdate.setOnClickListener {
                            if (record == "Quiz") { spinner1.setSelection(1) } else { spinner1.setSelection(2) }
                            val index2 = quizList.indexOf(subject)
                            val quarterList = arrayListOf(getString(R.string.spinner2_prompt), "First Quarter", "Second Quarter", "Third Quarter", "Fourth Quarter")
                            val index3 = quarterList.indexOf(quarter)
                            spinner.setSelection(index2)
                            spinner2.setSelection(index3)

                            button.text = btnUpdate.text.toString()
                            btnUpdate.text = btnUpdate.text.toString()
                        }
                    }

                    val uniqueId = View.generateViewId()
                    subjectLayout.id = uniqueId
                    parentLayout.addView(subjectLayout)
                } while (recordCursor.moveToNext())
            }
        }
        else {
            txtHeader.text = "No Records Found"
        }
    }
    private fun getLearnerList(): ArrayList<String> {
        var studentList: ArrayList<String> = ArrayList()

        studentList.add(getString(R.string.student_prompt))

        val tableName = "tbl_learner"
        val checkQuery = "SELECT * FROM $tableName WHERE addedBy = '$userId' ORDER BY lastName ASC"
        val batchCursor: Cursor = db.rawQuery(checkQuery, null)
        if (batchCursor.count > 0) {
            if (batchCursor.moveToFirst()){
                val firstNameColumnIndex = batchCursor.getColumnIndex("firstName")
                val lastNameColumnIndex = batchCursor.getColumnIndex("lastName")

                if (firstNameColumnIndex != -1) {
                    do {
                        val firstName = batchCursor.getString(firstNameColumnIndex)
                        val lastName = batchCursor.getString(lastNameColumnIndex)
                        var studentName = lastName.uppercase() + ", " + firstName.lowercase().replaceFirstChar { it.uppercaseChar() }
                        studentList.add(studentName)
                    } while (batchCursor.moveToNext())
                }
            }
        }
        return studentList
    }
    private fun getQuizNameList(lessonId: String): ArrayList<String> {
        var batchList: ArrayList<String> = ArrayList()

        batchList.add(getString(R.string.spinner3_prompt))

        val tableName = "tbl_quiz"
        val checkQuery = "SELECT DISTINCT(quizName) AS quizName FROM $tableName WHERE addedBy = '$userId' AND lessonName = '$lessonId' AND quizName <> '' ORDER BY quizName"
        val batchCursor: Cursor = db.rawQuery(checkQuery, null)
        if (batchCursor.count > 0) {
            if (batchCursor.moveToFirst()){
                val batchColumnIndex = batchCursor.getColumnIndex("quizName")
                if (batchColumnIndex != -1) {
                    do {
                        val batch = batchCursor.getString(batchColumnIndex)
                        batchList.add(batch)
                    } while (batchCursor.moveToNext())
                }
            }
        }
        return batchList
    }
    private fun getSelectedBatch(): String {
        val currentBatch = spinner3.selectedItem.toString()
        return currentBatch
    }
    private fun getStudents(batchName: String) {
        val studentQuery = "SELECT DISTINCT studentName, quarter FROM tbl_grading WHERE addedBy = '$userId' OR addedBy = 'SYS' AND batch = '$batchName'"
        val studentCursor = db.rawQuery(studentQuery, null)

        if (studentCursor.moveToFirst()) {
            val studentIndex = studentCursor.getColumnIndex("studentName")
            val quarterIndex = studentCursor.getColumnIndex("quarter")
            if (studentIndex != -1) {
                do {
                    val studentName = studentCursor.getString(studentIndex)
                    val quarter = studentCursor.getString(quarterIndex)
                    studentList.add(studentName)
                    quarterList.add(quarter)
                    println("$studentName has already been added to the student list")
                } while (studentCursor.moveToNext())
            } else {

            }

        }
        studentCursor.close()
    }
    private fun getScore(studentName: String, subject: String, quarter: String): Double {
        var getQuery = "SELECT * FROM tbl_grading WHERE studentName = '$studentName' AND quarter = '$quarter' AND subject = '$subject' AND addedBy = '$userId' OR addedBy = 'SYS';"
        val quizCursor: Cursor = db.rawQuery(getQuery, null)
        if (quizCursor.moveToFirst()) {
            val columnIndex = quizCursor.getColumnIndex("score")

            if (columnIndex != -1) {
                var initialQuiz = 0.0
                do {
                    initialQuiz += quizCursor.getString(columnIndex).toDouble()
                } while (quizCursor.moveToNext())

                return initialQuiz
            } else {
                return 0.0
            }
        } else {
            return 0.0
        }
        quizCursor.close()
    }
    private fun getStudentScore(index: Int): Double {
        val studentName = studentList[index]
        val quarter = quarterList[index]
        var getQuery = "SELECT SUM(score) AS score FROM tbl_grading WHERE batch = '$batchName' AND studentName = '$studentName' AND quarter = '$quarter' AND addedBy = '$userId';"
        val quizCursor: Cursor = db.rawQuery(getQuery, null)
        if (quizCursor.moveToFirst()) {
            val columnIndex = quizCursor.getColumnIndex("score")

            if (columnIndex != -1) {
                var initialQuiz = 0.0
                do {
                    initialQuiz += quizCursor.getDouble(columnIndex)
                } while (quizCursor.moveToNext())

                return initialQuiz
            } else {
                return 0.0
            }
        } else {
            return 0.0
        }
        quizCursor.close()
    }
    private fun getAverage(index: Int): Double {
        val totalQuiz = getTotalScore()
        val studentName = studentList[index]
        val quarter = quarterList[index]
        val getQuery = "SELECT SUM(score) as score FROM tbl_grading WHERE studentName = '$studentName' AND addedBy = '$userId' OR addedBy = 'SYS' AND quarter = '$quarter';"

        var returnValue = 0.0
        val quizCursor: Cursor = db.rawQuery(getQuery, null)
        if (quizCursor.moveToFirst()) {
            val scoreIndex = quizCursor.getColumnIndex("score")
            val score = quizCursor.getString(scoreIndex)
            returnValue = (score.toDouble()/totalQuiz) * 100.0
            return returnValue
        }
        else {
            return returnValue
        }
    }
    private fun getGrade(average: Double): String{
        if (average < 75.0) {
            return "Failed"
        }
        else if (average >= 75.0 && average <= 89.0) {
            return "Passed"
        }
        else if (average >= 90.0 && average >= 100.0) {
            return "Outstanding"
        }
        else {
            return "N/A"
        }
    }
    private fun getSubjects() {
        val tableName = "tbl_subject"
        val query = "SELECT * FROM $tableName WHERE subject != 'Games' AND addedBy = '$userId'"
        val subjectCursor: Cursor = db.rawQuery(query, null)
        if (subjectCursor.moveToFirst()) {
            val studentIndex = subjectCursor.getColumnIndex("subject")
            if (studentIndex != -1) {
                do {
                    val subjectName = subjectCursor.getString(studentIndex)
                    quizList.add(subjectName)
                    println("$subjectName has already been added to the quizList list")
                } while (subjectCursor.moveToNext())
            } else {

            }
        }
    }
    private fun getPreference() {
        if (currentSubject != null) {
            val index = quizList.indexOf(currentSubject)
            spinner.setSelection(index)
        }
    }
    private fun getTotalScore(): Double {
        val tableName = "tbl_quiz"
        val query = "SELECT SUM(score) AS score FROM $tableName"
        val quizArray = arrayOf(topicId)
        val quizCursor: Cursor = db.rawQuery(query, null)
        var returnValue = 0.0
        if (quizCursor.moveToFirst()) {
            val scoreIndex = quizCursor.getColumnIndex("score")
            val score = quizCursor.getString(scoreIndex)
            returnValue = score.toDouble()
        }
        return returnValue
    }
    private fun updateCentralDBLearnerProgress(learnerId: String, quizName: String, score: String) {
        val sharedPreferences = getSharedPreferences("MyPreferences", MODE_PRIVATE)
        val tableName = "tbl_quiz"
        var quizId = ""
        val checkQuery = "SELECT * FROM $tableName WHERE addedBy = '$userId' AND lessonName = '$quizName'"
        val batchCursor: Cursor = db.rawQuery(checkQuery, null)
        if (batchCursor.count > 0) {
            if (batchCursor.moveToFirst()){
                val batchColumnIndex = batchCursor.getColumnIndex("quizId")
                if (batchColumnIndex != -1) {
                    quizId = batchCursor.getString(batchColumnIndex)
                }
            }
        }

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
                    Toast.makeText(this@ExcelExportActivity, "No connection", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun getLearnerId(lastname: String, firstname: String): String {
        val tableName = "tbl_learner"
        val studentQuery = "SELECT * FROM $tableName WHERE firstName = ? AND lastName = ?"
        val studentCursor = db.rawQuery(studentQuery, arrayOf(firstname, lastname))

        if (studentCursor.moveToFirst()) {
            val learnerIdIndex = studentCursor.getColumnIndex("learnerId")
            if (learnerIdIndex != -1) {
                val learnerId = studentCursor.getString(learnerIdIndex)
                studentCursor.close()
                return learnerId
            }
        }

        studentCursor.close()
        return ""
    }

    private fun getDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd")
        val currentDate = Date()
        return sdf.format(currentDate)
    }
}