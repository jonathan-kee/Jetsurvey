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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.compose.AppTheme
import com.manasmalla.jetsurvey.data.Options
import com.manasmalla.jetsurvey.data.questions

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
        // Will loop through the list and create 3 text box
        options.checkboxOptions.forEach { option ->
            // "Strongly\ndisagree"
            // "Neutral"
            // "Strongly\nagree"
            print(option)
            val checked = selectedOptions.contains(option)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clickable { onOptionToggle(option) }
                    .padding(vertical = 8.dp)
            ) {
                Checkbox(
                    checked = checked,
                    onCheckedChange = null, // Click handling delegated to outer Row
                    modifier = Modifier.scale(1.3f) // Scales up the checkbox visually (e.g. 1.3x - 1.5x)
                )
                Text(
                    text = option,
                    style = MaterialTheme.typography.bodyMedium, // Bumped up from labelSmall for scale balance
                    modifier = Modifier.padding(start = 8.dp) // Extra spacing for scaled bounds
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
            // This is give the user the ability to switch options
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