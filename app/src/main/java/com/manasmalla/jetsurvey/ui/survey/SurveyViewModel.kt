package com.manasmalla.jetsurvey.ui.survey

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import com.manasmalla.jetsurvey.data.Options
import com.manasmalla.jetsurvey.data.SurveyQuestion
import com.manasmalla.jetsurvey.data.SurveySummaryItem
import com.manasmalla.jetsurvey.data.questions
import com.manasmalla.jetsurvey.ui.survey.util.SurveyDbHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class SurveyViewModel(private val dbHelper: SurveyDbHelper) : ViewModel() {

    // This represents the current page number (1, 2, 3...)
    var progress by mutableStateOf(1)
        private set

    // Kept for backward compatibility if single-question references are needed
    val question get() = questions.getOrNull((progress - 1) * 4) ?: questions.first()

    var isNextEnabled by mutableStateOf(false)
        private set

    // Input states for other question types
    var composeCharacter by mutableStateOf("")
    var selfieFeeling by mutableStateOf<Float?>(null)
    var selfie: Uri? by mutableStateOf(null)
    private var date: Long? by mutableStateOf(null)

    val formattedDate: String
        get() = SimpleDateFormat(
            "EEE, MMM dd",
            Locale.getDefault()
        ).format(Date(date ?: Calendar.getInstance().time.time))

    // Map holding selected options FOR EACH unique question ID
    val multipleChoiceAnswers = mutableStateMapOf<String, List<String>>()

    // Exposes a chunk of up to 4 questions for the current page
    val questionsList: List<SurveyQuestion>
        get() {
            val startIndex = (progress - 1) * 4
            if (startIndex >= questions.size) return emptyList()
            return questions.subList(startIndex, minOf(startIndex + 4, questions.size))
        }

    fun showDatePicker(fragmentManager: FragmentManager) {
        val picker = MaterialDatePicker.Builder.datePicker()
            .setCalendarConstraints(
                CalendarConstraints.Builder().setValidator(DateValidatorPointBackward.now()).build()
            )
            .setSelection(date)
            .build()
        picker.show(fragmentManager, picker.toString())
        picker.addOnPositiveButtonClickListener {
            date = it
            isNextEnabled = checkIfNextEnabled()
        }
    }

    // FIXED: Checks all questions currently visible on this page
    private fun checkIfNextEnabled(): Boolean {
        val currentQuestions = questionsList
        if (currentQuestions.isEmpty()) return false

        // Ensure every question on the current page has a valid answer
        return currentQuestions.indices.all { index ->
            val currentQuestionId = "question_${progress}_$index"
            val q = currentQuestions[index]

            when (q.options) {
                Options.DateChoice -> date != null
                Options.ImageChoice -> selfie != null
                is Options.MultipleChoice -> !multipleChoiceAnswers[currentQuestionId].isNullOrEmpty()
                is Options.SingleChoice -> composeCharacter.isNotEmpty()
                is Options.SliderChoice -> selfieFeeling != null
                is Options.CheckboxChoice -> !multipleChoiceAnswers[currentQuestionId].isNullOrEmpty()
            }
        }
    }

    // FIXED: Moves forward by page instead of single question index
    fun nextQuestion() {
        val maxPages = (questions.size + 3) / 4 // Total pages calculation
        if (progress < maxPages) {
            progress += 1

            // Reset other input fields for the new page
            composeCharacter = ""
            selfieFeeling = null
            selfie = null
            date = null

            isNextEnabled = checkIfNextEnabled()
        }
    }

    fun previousQuestion() {
        if (progress > 1) {
            progress--
            // Load saved answers for the previous page slots if stored
            questionsList.indices.forEach { index ->
                loadSavedOptionsForQuestion("question_${progress}_$index")
            }
        }
    }

    fun updateComposeCharacter(character: String) {
        composeCharacter = character
        isNextEnabled = checkIfNextEnabled()
    }

    fun onSelfieFeelingChange(newFeeling: Float) {
        selfieFeeling = newFeeling
        isNextEnabled = checkIfNextEnabled()
    }

    fun onImageSelected(uri: Uri) {
        selfie = uri
        isNextEnabled = checkIfNextEnabled()
    }

    fun getSelectedOptionsForQuestion(questionId: String): List<String> {
        return multipleChoiceAnswers[questionId] ?: emptyList()
    }

    fun updateMultipleOptionsAnswer(option: String) {
        updateMultipleOptionsAnswer(optionString = option, questionId = "question_${progress}_0")
    }

    fun updateMultipleOptionsAnswer(optionString: String, questionId: String) {
        val currentList = multipleChoiceAnswers[questionId].orEmpty().toMutableList()

        // Extract question index to determine option behavior rules if needed
        val questionIndex = questionId.substringAfterLast("_").toIntOrNull() ?: 0
        val targetQuestion = questionsList.getOrNull(questionIndex) ?: question

        when (targetQuestion.options) {
            is Options.CheckboxChoice -> {
                // Single-selection behavior per specific checkbox group
                if (currentList.contains(optionString)) {
                    currentList.clear()
                } else {
                    currentList.clear()
                    currentList.add(optionString)
                }
            }
            is Options.MultipleChoice -> {
                if (currentList.contains(optionString)) {
                    currentList.remove(optionString)
                } else {
                    currentList.add(optionString)
                }
            }
            else -> {
                if (currentList.contains(optionString)) {
                    currentList.remove(optionString)
                } else {
                    currentList.add(optionString)
                }
            }
        }

        multipleChoiceAnswers[questionId] = currentList

        // Re-evaluate if all questions on the page are now completed
        isNextEnabled = checkIfNextEnabled()

        viewModelScope.launch(Dispatchers.IO) {
            dbHelper.saveResponse(questionId, currentList)
        }
    }

    fun loadSavedOptionsForQuestion(questionId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val savedList = dbHelper.getSavedResponse(questionId)
            withContext(Dispatchers.Main) {
                multipleChoiceAnswers[questionId] = savedList
                isNextEnabled = checkIfNextEnabled()
            }
        }
    }

    // Summary Screen Support
    var summaryResults by mutableStateOf<List<SurveySummaryItem>>(emptyList())
        private set

    fun loadSummaryResults() {
        viewModelScope.launch(Dispatchers.IO) {
            val results = dbHelper.getAllSavedResponses()
            withContext(Dispatchers.Main) {
                summaryResults = results
            }
        }
    }

    // Inside SurveyViewModel.kt
    fun syncAndFinishSurvey(webAppUrl: String, onFinished: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            // syncToGoogleSheets returns a Boolean (true if HTTP_OK)
            val isSyncedSuccessfully = dbHelper.syncToGoogleSheets(webAppUrl)

            if (isSyncedSuccessfully) {
                // Truncate the SQLite table because data was safely sent
                dbHelper.clearTable()
            }

            withContext(Dispatchers.Main) {
                onFinished()
            }
        }
    }
}