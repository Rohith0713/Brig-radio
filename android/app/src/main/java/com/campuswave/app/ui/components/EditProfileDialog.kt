package com.campuswave.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.campuswave.app.ui.theme.*

@Composable
fun EditProfileDialog(
    currentName: String,
    currentPin: String?,
    currentDepartment: String?,
    currentYear: String?,
    currentBranch: String?,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String, String) -> Unit
) {
    var name by remember { mutableStateOf(currentName) }
    var pin by remember { mutableStateOf(currentPin ?: "") }
    var department by remember { mutableStateOf(currentDepartment ?: "") }
    var year by remember { mutableStateOf(currentYear ?: "") }
    var branch by remember { mutableStateOf(currentBranch ?: "") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = campusSurface(),
        title = { Text("Edit Personal Details", color = campusOnBackground(), fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Display Name") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = campusOnBackground(),
                        unfocusedTextColor = campusOnBackground(),
                        focusedBorderColor = PrimaryBlue,
                        unfocusedBorderColor = if (LocalIsDarkTheme.current) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.1f),
                        focusedLabelColor = PrimaryBlue,
                        unfocusedLabelColor = if (LocalIsDarkTheme.current) DarkGrey else LightGrey
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = pin,
                    onValueChange = { pin = it },
                    label = { Text("College PIN") },
                    singleLine = true,
                    placeholder = { Text("e.g. 21ABC123") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = campusOnBackground(),
                        unfocusedTextColor = campusOnBackground(),
                        focusedBorderColor = PrimaryBlue,
                        unfocusedBorderColor = if (LocalIsDarkTheme.current) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.1f),
                        focusedLabelColor = PrimaryBlue,
                        unfocusedLabelColor = if (LocalIsDarkTheme.current) DarkGrey else LightGrey
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = department,
                    onValueChange = { department = it },
                    label = { Text("Department") },
                    singleLine = true,
                    placeholder = { Text("e.g. Computer Science") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = campusOnBackground(),
                        unfocusedTextColor = campusOnBackground(),
                        focusedBorderColor = PrimaryBlue,
                        unfocusedBorderColor = if (LocalIsDarkTheme.current) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.1f),
                        focusedLabelColor = PrimaryBlue,
                        unfocusedLabelColor = if (LocalIsDarkTheme.current) DarkGrey else LightGrey
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                
                 OutlinedTextField(
                    value = year,
                    onValueChange = { year = it },
                    label = { Text("Year") },
                    singleLine = true,
                    placeholder = { Text("e.g. 3rd Year") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = campusOnBackground(),
                        unfocusedTextColor = campusOnBackground(),
                        focusedBorderColor = PrimaryBlue,
                        unfocusedBorderColor = if (LocalIsDarkTheme.current) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.1f),
                        focusedLabelColor = PrimaryBlue,
                        unfocusedLabelColor = if (LocalIsDarkTheme.current) DarkGrey else LightGrey
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                
                 OutlinedTextField(
                    value = branch,
                    onValueChange = { branch = it },
                    label = { Text("Branch") },
                    singleLine = true,
                    placeholder = { Text("e.g. B.Tech") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = campusOnBackground(),
                        unfocusedTextColor = campusOnBackground(),
                        focusedBorderColor = PrimaryBlue,
                        unfocusedBorderColor = if (LocalIsDarkTheme.current) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.1f),
                        focusedLabelColor = PrimaryBlue,
                        unfocusedLabelColor = if (LocalIsDarkTheme.current) DarkGrey else LightGrey
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(name, pin, department, year, branch) },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Update", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = if (LocalIsDarkTheme.current) DarkGrey else LightGrey)
            }
        }
    )
}
