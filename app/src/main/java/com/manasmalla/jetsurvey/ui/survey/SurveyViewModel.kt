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

    // This is question number
    var progress by mutableStateOf(1)
        private set
    val question get() = questions[progress - 1]

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

    // Map holding selected options FOR EACH question ID
    val multipleChoiceAnswers = mutableStateMapOf<String, List<String>>()

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

    private fun checkIfNextEnabled(): Boolean {
        val currentQuestionId = "question_$progress"
        return when (question.options) {
            Options.DateChoice -> date != null
            Options.ImageChoice -> selfie != null
            is Options.MultipleChoice -> !multipleChoiceAnswers[currentQuestionId].isNullOrEmpty()
            is Options.SingleChoice -> composeCharacter.isNotEmpty()
            is Options.SliderChoice -> selfieFeeling != null
            is Options.CheckboxChoice -> !multipleChoiceAnswers[currentQuestionId].isNullOrEmpty()
        }
    }

    fun nextQuestion() {
        if (progress < questions.size) {
            progress += 1
            val currentQuestionId = "question_$progress"

            // Reset options for the new question page so checkboxes render unchecked
            multipleChoiceAnswers[currentQuestionId] = emptyList()

            // Reset other input fields
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
            loadSavedOptionsForQuestion("question_$progress")
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

    // 1-parameter overload for simple UI calls
    fun updateMultipleOptionsAnswer(option: String) {
        updateMultipleOptionsAnswer(optionString = option, questionId = "question_$progress")
    }

    // Main 2-parameter function to handle single vs multiple choices based on question type
    fun updateMultipleOptionsAnswer(optionString: String, questionId: String) {
        val questionIndex = questionId.removePrefix("question_").toIntOrNull()?.minus(1) ?: (progress - 1)
        val targetQuestion = questions.getOrNull(questionIndex) ?: question

        val currentList = multipleChoiceAnswers[questionId].orEmpty().toMutableList()

        when (targetQuestion.options) {
            is Options.CheckboxChoice -> {
                // Single-selection behavior: Only one checkbox can be checked at a time
                if (currentList.contains(optionString)) {
                    currentList.clear() // Allows deselecting if clicked again
                } else {
                    currentList.clear()
                    currentList.add(optionString)
                }
            }
            is Options.MultipleChoice -> {
                // Multi-selection behavior: Multiple checkboxes can be checked
                if (currentList.contains(optionString)) {
                    currentList.remove(optionString)
                } else {
                    currentList.add(optionString)
                }
            }
            else -> {
                // Fallback toggle behavior
                if (currentList.contains(optionString)) {
                    currentList.remove(optionString)
                } else {
                    currentList.add(optionString)
                }
            }
        }

        // Reassigning the list forces Compose state to update
        multipleChoiceAnswers[questionId] = currentList

        // Immediately update button visibility state
        isNextEnabled = checkIfNextEnabled()

        // Save to SQLite on background thread
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
}