package com.manasmalla.jetsurvey.ui.survey.util

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SurveyBottomBar(progress: Int, onPreviousPressed: ()->Unit = {}, isNextEnabled: Boolean = true, onNextPressed: ()->Unit = {}){
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 7.dp,
    ) {
        Row(
            modifier =
            Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .navigationBarsPadding(), horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Larger button padding (e.g., 16.dp top/bottom instead of the default ~8.dp)
            val buttonPadding = PaddingValues(vertical = 16.dp, horizontal = 24.dp)

            if (progress > 1) {
                OutlinedButton(
                    contentPadding = buttonPadding,
                    onClick = onPreviousPressed, modifier =
                    Modifier
                        .weight(1f)
                ) {
                    Text(text = "Previous")
                }
            }

            Button(
                contentPadding = buttonPadding,
                onClick = onNextPressed, modifier =
                Modifier
                    .weight(1f), enabled = isNextEnabled
            ) {
                Text(text = if (progress < 5) "Next" else "Done")
            }

        }
    }
}