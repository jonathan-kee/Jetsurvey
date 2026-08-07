package com.manasmalla.jetsurvey.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.manasmalla.jetsurvey.R

fun Modifier.supportWideScreen() = this
    .fillMaxWidth()
    .wrapContentWidth(align = Alignment.CenterHorizontally)
    .widthIn(max = 840.dp)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SurveyResultScreen(
    onDonePressed: () -> Unit,
    onSummaryPressed: () -> Unit,
) {
    val buttonPadding = PaddingValues(vertical = 16.dp, horizontal = 24.dp)
    var isLoading by remember { mutableStateOf(false) }

    Surface(modifier = Modifier.supportWideScreen()) {
        Box(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                content = { innerPadding ->
                    val modifier = Modifier.padding(innerPadding)
                    SurveyResult(
                        title = stringResource(R.string.survey_result_title),
                        subtitle = stringResource(R.string.survey_result_subtitle),
                        description = stringResource(R.string.survey_result_description),
                        modifier = modifier
                    )
                },
                bottomBar = {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedButton(
                            contentPadding = buttonPadding,
                            onClick = onSummaryPressed,
                            enabled = !isLoading,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(text = "Summary")
                        }

                        // Standard Button automatically uses MaterialTheme.colorScheme.primary (your app's green theme)
                        Button(
                            contentPadding = buttonPadding,
                            onClick = {
                                isLoading = true
                                onDonePressed()
                            },
                            enabled = !isLoading,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(text = "End Survey")
                        }
                    }
                }
            )

            // Full-screen overlay with grey background, centered spinner, and bold white text
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.4f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Sending data ...",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SurveyResult(
    title: String,
    subtitle: String,
    description: String,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier.fillMaxSize()) {
        item {
            Spacer(modifier = Modifier.height(44.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.displaySmall,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(20.dp)
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
        }
    }
}