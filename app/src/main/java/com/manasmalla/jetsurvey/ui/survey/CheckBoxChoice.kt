package com.manasmalla.jetsurvey.ui.survey

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.compose.AppTheme
import com.manasmalla.jetsurvey.data.Options
import com.manasmalla.jetsurvey.data.questions
import com.manasmalla.jetsurvey.ui.theme.JetsurveyTheme

@Composable
fun CheckboxChoice(
    selectedOptions: List<String> = emptyList(), // Immutable state passed in
    onOptionToggle: (String) -> Unit = {},        // Emits selected option string
    options: Options.CheckboxChoice,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        options.checkboxOptions.forEach { option ->
            val checked = selectedOptions.contains(option)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clickable { onOptionToggle(option) }
                    .padding(vertical = 4.dp)
            ) {
                Checkbox(
                    checked = checked,
                    onCheckedChange = null // Click handling delegated to outer Row
                )
                Text(
                    text = option,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CheckboxChoicePreview() {
    AppTheme {
        val selectedOptions = remember { mutableStateListOf<String>() }

        CheckboxChoice(
            selectedOptions = selectedOptions,
            onOptionToggle = { option ->
                if (selectedOptions.contains(option)) {
                    selectedOptions.remove(option)
                } else {
                    selectedOptions.add(option)
                }
            },
            options = questions[0].options as Options.CheckboxChoice
        )
    }
}