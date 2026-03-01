package com.campuswave.app.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.campuswave.app.utils.PasswordValidator
import com.campuswave.app.utils.PasswordStrength
import com.campuswave.app.ui.components.PasswordStrengthIndicator
import com.campuswave.app.ui.theme.*
import com.campuswave.app.ui.theme.LocalIsDarkTheme

@Composable
fun ResetPasswordScreen(
    email: String,
    otp: String,
    onResetClick: (password: String) -> Unit,
    isLoading: Boolean = false,
    errorMessage: String? = null
) {
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    
    val passwordsMatch = password == confirmPassword && password.isNotEmpty()
    
    val isDark = LocalIsDarkTheme.current
    val backgroundColor = if (isDark) DarkBackground else LightBackground
    val inputBackground = if (isDark) Color(0xFF1F222E) else Color.White
    val inputBorder = if (isDark) Color(0xFF2E3244) else Color(0xFFE2E8F0)
    val neonPurple = Color(0xFF8B5CF6)
    val primaryTextColor = if (isDark) Color.White else Color(0xFF1E293B)
    val secondaryTextColor = if (isDark) Color.White.copy(alpha = 0.7f) else Color(0xFF64748B)

    val primaryGradient = Brush.horizontalGradient(
        colors = listOf(Color(0xFF4F46E5), Color(0xFF7C3AED))
    )

    // Password validation logic
    val passwordValidation = remember(password) {
        PasswordValidator.validatePassword(password = password)
    }
    val isPasswordValid = passwordSelection(passwordValidation)
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            // Title Section
            Spacer(modifier = Modifier.height(60.dp))
            
            Text(
                text = "Reset",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = primaryTextColor
            )
            
            Text(
                text = "Password",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = neonPurple
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "Create a new secure password for your account to regain access.",
                style = MaterialTheme.typography.bodyLarge,
                color = secondaryTextColor
            )
            
            Spacer(modifier = Modifier.height(40.dp))
            
            // New Password Field
            Text(
                text = "New Password",
                color = primaryTextColor,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = { Text("Enter new password", color = Color(0xFF64748B)) },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = Color(0xFF64748B))
                },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = null,
                            tint = Color(0xFF64748B)
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = inputBackground,
                    unfocusedContainerColor = inputBackground,
                    focusedBorderColor = neonPurple,
                    unfocusedBorderColor = inputBorder,
                    focusedTextColor = primaryTextColor,
                    unfocusedTextColor = primaryTextColor
                )
            )
            
            if (password.isNotEmpty()) {
                PasswordStrengthIndicator(
                    validationResult = passwordValidation,
                    showRules = true,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Confirm Password Field
            Text(
                text = "Confirm Password",
                color = primaryTextColor,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                placeholder = { Text("Repeat your password", color = Color(0xFF64748B)) },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = Color(0xFF64748B))
                },
                trailingIcon = {
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Icon(
                            imageVector = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = null,
                            tint = Color(0xFF64748B)
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                shape = RoundedCornerShape(12.dp),
                isError = confirmPassword.isNotEmpty() && !passwordsMatch,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = inputBackground,
                    unfocusedContainerColor = inputBackground,
                    focusedBorderColor = neonPurple,
                    unfocusedBorderColor = inputBorder,
                    focusedTextColor = primaryTextColor,
                    unfocusedTextColor = primaryTextColor
                )
            )
            
            if (confirmPassword.isNotEmpty() && !passwordsMatch) {
                Text(
                    text = "Passwords do not match",
                    color = Color.Red,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Error Message
            if (errorMessage != null) {
                Text(
                    text = errorMessage,
                    color = Color.Red,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
            
            // Reset Button
            Button(
                onClick = { onResetClick(password) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = passwordsMatch && isPasswordValid && !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(if (passwordsMatch && isPasswordValid) primaryGradient else Brush.linearGradient(listOf(Color.Gray, Color.Gray))),
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text(
                            text = "Update Password",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// Helper for password validity
private fun passwordSelection(validation: com.campuswave.app.utils.PasswordValidationResult): Boolean {
    return validation.strength != PasswordStrength.WEAK && validation.hasNoSpaces
}
