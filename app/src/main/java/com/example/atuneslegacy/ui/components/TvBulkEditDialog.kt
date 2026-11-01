package com.example.atuneslegacy.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.atuneslegacy.ui.theme.LocalThemeColors

@Composable
fun TvBulkEditDialog(
    selectedCount: Int,
    onDismiss: () -> Unit,
    onSave: (seasonStr: String, titlePrefix: String) -> Unit
) {
    val themeColors = LocalThemeColors.current
    var seasonInput by remember { mutableStateOf("") }
    var titlePrefixInput by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Bulk Edit TV Episodes ($selectedCount selected)",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = themeColors.textPrimary
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Leave fields blank to keep existing values.",
                    fontSize = 12.sp,
                    color = themeColors.textSecondary
                )
                OutlinedTextField(
                    value = seasonInput,
                    onValueChange = { seasonInput = it },
                    label = { Text("New Season Number (optional)") },
                    singleLine = true,
                    textStyle = TextStyle(color = themeColors.inputText),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = themeColors.surface,
                        unfocusedContainerColor = themeColors.surface
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = titlePrefixInput,
                    onValueChange = { titlePrefixInput = it },
                    label = { Text("Episode Title Prefix (optional)") },
                    singleLine = true,
                    textStyle = TextStyle(color = themeColors.inputText),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = themeColors.surface,
                        unfocusedContainerColor = themeColors.surface
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(seasonInput, titlePrefixInput)
                },
                colors = ButtonDefaults.buttonColors(containerColor = themeColors.primaryAccent)
            ) {
                Text("Apply to Selected")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(8.dp),
        containerColor = themeColors.surface
    )
}
