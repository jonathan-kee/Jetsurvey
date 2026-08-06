package com.manasmalla.jetsurvey.ui.survey

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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

    var progress by mutableStateOf(1)
        private set
    val question get() = questions[progress - 1]

    var isNextEnabled by mutableStateOf(false)
        private set

    val freeTimeOptions = mutableStateListOf<String>()
    var composeCharacter by mutableStateOf("")
    var selfieFeeling by mutableStateOf<Float?>(null)
    var selfie: Uri? by mutableStateOf(null)
    private var date: Long? by mutableStateOf(null)
    val formattedDate: String
        get() = SimpleDateFormat(
            "EEE, MMM dd",
            Locale.getDefault()
        ).format(Date(date ?: Calendar.getInstance().time.time))

    fun showDatePicker(fragmentManager: FragmentManager) {
        val picker =
            MaterialDatePicker.Builder.datePicker().setCalendarConstraints(
                CalendarConstraints.Builder().setValidator(DateValidatorPointBackward.now()).build()
            ).setSelection(date).build()
        picker.show(fragmentManager, picker.toString())
        picker.addOnPositiveButtonClickListener {
            date = it
            isNextEnabled = checkIfNextEnabled()
        }
    }

    // ... rest of your ViewModel ...
    private fun checkIfNextEnabled(): Boolean {
        return when (question.options) {
            Options.DateChoice -> date != null
            Options.ImageChoice -> selfie != null
            is Options.MultipleChoice -> freeTimeOptions.isNotEmpty()
            is Options.SingleChoice -> composeCharacter.isNotEmpty()
            is Options.SliderChoice -> selfieFeeling != null
            is Options.CheckboxChoice -> freeTimeOptions.isNotEmpty()
        }
    }

    fun updateMultipleOptionsAnswer(option: String) {
        if (freeTimeOptions.contains(option)) {
            freeTimeOptions.remove(option)
        } else {
            freeTimeOptions.add(option)
        }
        isNextEnabled = checkIfNextEnabled()
    }
    // ... rest of your ViewModel ...

    fun nextQuestion() {
        _selectedOptions.clear() // Reset checkboxes before moving to next question
        progress += 1
        freeTimeOptions.clear() // Clears all checked options

        // (Optional) Reset other inputs if moving to a fresh question:
        composeCharacter = ""
        selfieFeeling = null
        selfie = null
        date = null

        isNextEnabled = checkIfNextEnabled() // Re-evaluates for the new question
    }

    fun previousQuestion() {
        _selectedOptions.clear() // Reset checkboxes before moving to previous question
        progress--
    }

    fun updateMultipleOptionsAnswer() {
        isNextEnabled = checkIfNextEnabled()
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

    // ---------- Questions log to database ----------
    // Checkbox code
    // Composable state
    private val _selectedOptions = mutableStateListOf<String>()
    val selectedOptions: List<String> get() = _selectedOptions

    fun onOptionToggle(option: String, questionId: String) {
        // 1. Single selection logic: Clear all existing choices first
        if (_selectedOptions.contains(option)) {
            _selectedOptions.clear()
        } else {
            _selectedOptions.clear()
            _selectedOptions.add(option)
        }

        // 2. Notify Jetsurvey's state so `isNextEnabled` gets calculated
        updateMultipleOptionsAnswer(option)

        // 3. Save to SQLite database on background thread
        viewModelScope.launch(Dispatchers.IO) {
            dbHelper.saveResponse(questionId, _selectedOptions.toList())
        }
    }

    // Holds all query results for the summary screen
    var summaryResults by mutableStateOf<List<SurveySummaryItem>>(emptyList())
        private set

    fun loadSummaryResults() {
        viewModelScope.launch(Dispatchers.IO) {
            val results = dbHelper.getAllSavedResponses()

            // Post result back to Main thread
            withContext(Dispatchers.Main) {
                summaryResults = results
            }
        }
    }
}