package com.campuswave.app.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.campuswave.app.ui.theme.*
import com.campuswave.app.ui.theme.LocalIsDarkTheme

@Composable
fun OtpVerificationScreen(
    email: String,
    onVerifyClick: (otp: String) -> Unit,
    onResendClick: () -> Unit,
    isLoading: Boolean = false,
    errorMessage: String? = null
) {
    var otp by remember { mutableStateOf("") }
    
    val isDark = LocalIsDarkTheme.current
    val cardColor = if (isDark) DarkSurface else Color.White
    val primaryTextColor = if (isDark) Color.White else Color(0xFF2D3748)
    val secondaryTextColor = if (isDark) Color.White.copy(alpha = 0.6f) else Color(0xFF718096)
    val inputBorder = if (isDark) Color(0xFF424242) else Color(0xFFE2E8F0)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDark) DarkBackground else LightBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            
            // White Card
            Card(
                modifier = Modifier.fillMaxWidth().padding(top = 40.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = cardColor),
                elevation = CardDefaults.cardElevation(defaultElevation = if (isDark) 0.dp else 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = Color(0xFF5E72E4),
                        modifier = Modifier.size(48.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Title
                    Text(
                        text = "Verification Code",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = primaryTextColor
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Subtitle
                    Text(
                        text = "We have sent a verification code to",
                        style = MaterialTheme.typography.bodyMedium,
                        color = secondaryTextColor,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = email,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = primaryTextColor,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    // OTP Input
                    OutlinedTextField(
                        value = otp,
                        onValueChange = { if (it.length <= 6) otp = it },
                        placeholder = { Text("Enter 6-digit code", color = Color(0xFF9CA3AF)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = primaryTextColor,
                            unfocusedTextColor = primaryTextColor,
                            unfocusedBorderColor = inputBorder,
                            focusedBorderColor = Color(0xFF5E72E4)
                        ),
                        textStyle = LocalTextStyle.current.copy(
                            textAlign = TextAlign.Center,
                            fontSize = 24.sp,
                            letterSpacing = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Error Message
                    if (errorMessage != null) {
                        Text(
                            text = errorMessage,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    
                    // Verify Button
                    Button(
                        onClick = { onVerifyClick(otp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        enabled = otp.length == 6 && !isLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF5E72E4),
                            disabledContainerColor = Color.Gray
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Text(
                                text = "Verify",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Resend Link
                    TextButton(onClick = onResendClick) {
                        Text(
                            text = "Resend Code",
                            color = Color(0xFF5E72E4),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
