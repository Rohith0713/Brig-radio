package com.campuswave.app.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MusicNote
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
import com.campuswave.app.ui.theme.*
import com.campuswave.app.ui.theme.LocalIsDarkTheme

@Composable
fun LoginScreen(
    role: String,
    onLoginClick: (email: String, password: String) -> Unit,
    onBackClick: () -> Unit,
    onRegisterClick: () -> Unit = {},
    onForgotPasswordClick: (String) -> Unit = {},
    isLoading: Boolean = false,
    errorMessage: String? = null
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var hasAttemptedLogin by remember { mutableStateOf(false) }
    
    val isDark = LocalIsDarkTheme.current
    val backgroundColor = if (isDark) DarkBackground else LightBackground
    val inputBackground = if (isDark) Color(0xFF1F222E) else Color.White
    val inputBorder = if (isDark) Color(0xFF2E3244) else Color(0xFFE2E8F0)
    val neonPurple = Color(0xFF8B5CF6)
    val primaryTextColor = if (isDark) Color.White else Color(0xFF1E293B)
    val secondaryTextColor = if (isDark) Color.White.copy(alpha = 0.5f) else Color(0xFF64748B)
    
    val primaryGradient = Brush.horizontalGradient(
        colors = listOf(Color(0xFF4F46E5), Color(0xFF7C3AED))
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))
            
            // App Icon (College Logo)
            Image(
                painter = androidx.compose.ui.res.painterResource(id = com.campuswave.app.R.drawable.ic_college_logo),
                contentDescription = null,
                modifier = Modifier
                    .size(120.dp)
                    .padding(bottom = 16.dp),
                contentScale = androidx.compose.ui.layout.ContentScale.Fit
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Welcome Text
            Text(
                text = "BRIG RADIO",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = primaryTextColor
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Welcome back to the frequency.",
                style = MaterialTheme.typography.bodyLarge,
                color = secondaryTextColor
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Email Input
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = { Text("Email Address", color = Color(0xFF64748B)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = null,
                        tint = Color(0xFF64748B)
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = inputBackground,
                    unfocusedContainerColor = inputBackground,
                    focusedTextColor = primaryTextColor,
                    unfocusedTextColor = primaryTextColor,
                    focusedBorderColor = neonPurple,
                    unfocusedBorderColor = inputBorder
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Password Input
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = { Text("Password", color = Color(0xFF64748B)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = Color(0xFF64748B)
                    )
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
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = inputBackground,
                    unfocusedContainerColor = inputBackground,
                    focusedTextColor = primaryTextColor,
                    unfocusedTextColor = primaryTextColor,
                    focusedBorderColor = neonPurple,
                    unfocusedBorderColor = inputBorder
                )
            )
            
            // Forgot Password Link
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = { onForgotPasswordClick(email) }) {
                    Row {
                        Text("Forgot ", color = secondaryTextColor, fontSize = 14.sp)
                        Text("Password?", color = neonPurple, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            
            if (!hasAttemptedLogin) Spacer(modifier = Modifier.height(24.dp).let { if(errorMessage!=null) Modifier.height(8.dp) else it })
            
            // Error Message
            if (errorMessage != null) {
                Text(
                    text = errorMessage,
                    color = Color.Red,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
            
            // Login Button (Gradient)
            Button(
                onClick = { 
                    hasAttemptedLogin = true
                    onLoginClick(email, password) 
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = email.isNotEmpty() && password.isNotEmpty() && !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            if (email.isNotEmpty() && password.isNotEmpty()) primaryGradient 
                            else Brush.linearGradient(listOf(Color.Gray, Color.Gray))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text(
                            text = "Log In",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Register Link
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "New here? ",
                    color = secondaryTextColor,
                    fontSize = 15.sp
                )
                TextButton(
                    onClick = onRegisterClick,
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        "Create Account",
                        color = if (isDark) Color.White else PrimaryBlue,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// Add these helper gradient classes if appropriate, or just inline them. 
// For now inlined above.
