package com.manasmalla.jetsurvey.ui.survey.util

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.manasmalla.jetsurvey.data.SurveySummaryItem

class SurveyDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "survey.db"
        private const val DATABASE_VERSION = 1
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Plain SQL to create the table
        val createTableQuery = """
            CREATE TABLE IF NOT EXISTS survey_responses (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                question_id TEXT UNIQUE NOT NULL,
                selected_options TEXT NOT NULL
            );
        """.trimIndent()

        db.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS survey_responses")
        onCreate(db)
    }

    // Plain SQL execute query
    fun saveResponse(questionId: String, selectedOptions: List<String>) {
        val db = writableDatabase
        val optionsJoined = selectedOptions.joinToString(",")

        // Plain SQL Upsert query using parameterized bindings (?)
        val sql = """
            INSERT INTO survey_responses (question_id, selected_options) 
            VALUES (?, ?) 
            ON CONFLICT(question_id) DO UPDATE SET selected_options = excluded.selected_options;
        """.trimIndent()

        db.execSQL(sql, arrayOf(questionId, optionsJoined))
    }

    fun getAllSavedResponses(): List<SurveySummaryItem> {
        val db = readableDatabase
        // Plain SQL SELECT query to fetch all rows
        val sql = "SELECT question_id, selected_options FROM survey_responses ORDER BY question_id ASC"
        val cursor = db.rawQuery(sql, null)

        val summaryList = mutableListOf<SurveySummaryItem>()

        cursor.use {
            val questionIdIndex = it.getColumnIndexOrThrow("question_id")
            val optionsIndex = it.getColumnIndexOrThrow("selected_options")

            // Loop through all records in the database
            while (it.moveToNext()) {
                val qId = it.getString(questionIdIndex)
                val rawOptions = it.getString(optionsIndex)
                val optionsList = if (rawOptions.isNotBlank()) rawOptions.split(",") else emptyList()

                summaryList.add(SurveySummaryItem(qId, optionsList))
            }
        }

        return summaryList
    }
}