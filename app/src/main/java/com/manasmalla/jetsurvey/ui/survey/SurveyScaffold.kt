package com.manasmalla.jetsurvey.ui.survey

import android.content.Context
import android.content.ContextWrapper
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.compose.AppTheme
import com.manasmalla.jetsurvey.data.Options
import com.manasmalla.jetsurvey.data.SurveyQuestion
import com.manasmalla.jetsurvey.ui.survey.util.SurveyBottomBar
import com.manasmalla.jetsurvey.ui.survey.util.SurveyDbHelper
import com.manasmalla.jetsurvey.ui.oldtheme.slightlyDeemphasizedAlpha
import com.manasmalla.jetsurvey.ui.oldtheme.stronglyDeemphasizedAlpha

private tailrec fun Context.findActivity(): AppCompatActivity =
    when (this) {
        is AppCompatActivity -> this
        is ContextWrapper -> this.baseContext.findActivity()
        else -> throw IllegalArgumentException("Could not find activity!")
    }

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun SurveyScaffold(
    modifier: Modifier = Modifier,
    onNavigateUp: () -> Unit = {},
    onNavigateToResults: () -> Unit = {}
) {
    // 1. Get application context
    val context = LocalContext.current.applicationContext

    // 2. Pass a custom factory to viewModel()
    val surveyViewModel: SurveyViewModel = viewModel(
        factory = viewModelFactory {
            initializer<SurveyViewModel> {
                val dbHelper = SurveyDbHelper(context)
                SurveyViewModel(dbHelper)
            }
        }
    )

    val progress: Int = surveyViewModel.progress
    val questions: List<SurveyQuestion> = surveyViewModel.questionsList

    // ------------ Decoration ------------
    Scaffold(topBar = {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    // UPDATED: Changed total to 2
                    "$progress of 2",
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 48.dp)
                        .wrapContentWidth(Alignment.CenterHorizontally),
                    style = MaterialTheme.typography.labelLarge
                )
                IconButton(onClick = onNavigateUp) {
                    Icon(Icons.Rounded.Close, contentDescription = null)
                }
            }
            LinearProgressIndicator(
                // UPDATED: Changed from 0.2f to 0.5f so step 2 reaches 100% (1.0f)
                progress = 0.5f.times(progress),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }, bottomBar = {
        SurveyBottomBar(
            progress = progress,
            isNextEnabled = surveyViewModel.isNextEnabled,
            onPreviousPressed = surveyViewModel::previousQuestion,
            onNextPressed =
                // UPDATED: Changed condition check from 5 to 2
                if (surveyViewModel.progress != 2)
                    surveyViewModel::nextQuestion
                else onNavigateToResults
        )
    }, modifier = modifier.statusBarsPadding()) { paddingValues ->
        // ------------ Decoration ------------

        AnimatedContent(targetState = questions, label = "QuestionsPage") { surveyQuestions ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
            ) {

                surveyQuestions.forEachIndexed { index, surveyQuestion ->

                    Text(
                        text = surveyQuestion.question,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 40.dp, vertical = 20.dp)
                            .clip(MaterialTheme.shapes.small)
                            .background(color = MaterialTheme.colorScheme.inverseOnSurface)
                            .padding(20.dp),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = slightlyDeemphasizedAlpha),
                    )

                    if (surveyQuestion.description != "") {
                        Text(
                            text = surveyQuestion.description,
                            modifier = Modifier.padding(start = 40.dp, bottom = 20.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                                .copy(alpha = stronglyDeemphasizedAlpha),
                        )
                    }

                    val currentQuestionId = "question_${progress}_$index"

                    when (val options: Options = surveyQuestion.options) {
                        is Options.DateChoice -> {
                            val fragmentManager =
                                LocalContext.current.findActivity().supportFragmentManager
                            DateQuestion(
                                date = surveyViewModel.formattedDate, showDatePicker = {
                                    surveyViewModel.showDatePicker(fragmentManager = fragmentManager)
                                }
                            )
                        }

                        is Options.ImageChoice -> {
                            FileQuestion(
                                modifier = Modifier.padding(32.dp),
                                imageUri = surveyViewModel.selfie,
                                onImageSelected = surveyViewModel::onImageSelected
                            )
                        }

                        is Options.MultipleChoice -> {
                            MultipleOptionsSection(
                                options = options,
                                selectedOptions = surveyViewModel.getSelectedOptionsForQuestion(currentQuestionId).toMutableList(),
                                onOptionSelected = { option ->
                                    surveyViewModel.updateMultipleOptionsAnswer(
                                        option,
                                        currentQuestionId
                                    )
                                }
                            )
                        }

                        is Options.SingleChoice -> {
                            SingleOptionsSection(
                                selected = surveyViewModel.composeCharacter,
                                options = options,
                                onOptionSelected = surveyViewModel::updateComposeCharacter
                            )
                        }

                        is Options.SliderChoice -> {
                            SliderChoice(
                                options = options,
                                modifier = Modifier.padding(32.dp),
                                value = surveyViewModel.selfieFeeling ?: 0.5f,
                                onValueChange = surveyViewModel::onSelfieFeelingChange
                            )
                        }

                        is Options.CheckboxChoice -> {
                            CheckboxChoice(
                                selectedOptions = surveyViewModel.getSelectedOptionsForQuestion(currentQuestionId),
                                onOptionToggle = { option ->
                                    surveyViewModel.updateMultipleOptionsAnswer(
                                        option,
                                        questionId = currentQuestionId
                                    )
                                },
                                options = options
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SurveyScaffoldPreview() {
    AppTheme {
        SurveyScaffold()
    }
}