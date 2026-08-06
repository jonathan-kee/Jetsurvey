package com.manasmalla.jetsurvey.ui.survey.util

import android.content.ContentValues
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

        val values = ContentValues().apply {
            put("question_id", questionId)
            // Convert list to comma-separated String (e.g., "Option A,Option B")
            put("selected_options", selectedOptions.joinToString(","))
        }

        // CONFLICT_REPLACE overwrites existing rows instead of crashing/failing
        db.insertWithOnConflict(
            "survey_responses",
            null,
            values,
            SQLiteDatabase.CONFLICT_REPLACE
        )
    }

    // -------------------------------------------------------------------
    // 👉 ADDED: Fetch response for a single question (used during navigation)
    // -------------------------------------------------------------------
    fun getSavedResponse(questionId: String): List<String> {
        val db = readableDatabase
        val sql = "SELECT selected_options FROM survey_responses WHERE question_id = ?"
        val cursor = db.rawQuery(sql, arrayOf(questionId))

        var savedOptions = emptyList<String>()

        cursor.use {
            if (it.moveToFirst()) {
                val rawOptions = it.getString(it.getColumnIndexOrThrow("selected_options"))
                if (!rawOptions.isNullOrBlank()) {
                    savedOptions = rawOptions.split(",")
                }
            }
        }

        return savedOptions
    }

    // Used for the Summary Screen
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