package com.campuswave.app.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
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
fun RegisterScreen(
    role: String,
    onRegisterClick: (name: String, collegeId: String, password: String, phoneNumber: String, department: String?, reason: String?) -> Unit,
    onBackClick: () -> Unit,
    onLoginClick: () -> Unit = {},
    isLoading: Boolean = false,
    errorMessage: String? = null
) {
    var fullName by remember { mutableStateOf("") }
    var collegeId by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var department by remember { mutableStateOf("") }
    var reasonForAccess by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    
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

    // Password validation logic
    val passwordValidation = remember(password, fullName, phoneNumber) {
        PasswordValidator.validatePassword(
            password = password,
            name = fullName,
            email = phoneNumber 
        )
    }
    val isPasswordValid = passwordValidation.strength != PasswordStrength.WEAK && 
                          passwordValidation.hasNoSpaces
    // NOTE: notMatchingUserInfo is now forced to true in PasswordValidator.kt per previous request

    val passwordsMatch = password == confirmPassword && password.isNotEmpty()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.Start
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // CAMPUSWAVE Tag (Top Left as per image)
            Surface(
                color = if (isDark) Color(0xFF4F46E5).copy(alpha = 0.2f) else Color(0xFFE8EAFF),
                shape = RoundedCornerShape(100.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, if (isDark) Color(0xFF4F46E5).copy(alpha = 0.3f) else Color(0xFF4F46E5).copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.MusicNote, 
                        contentDescription = null,
                        tint = if (isDark) Color(0xFF818CF8) else PrimaryBlue,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "CAMPUSWAVE",
                        color = if (isDark) Color(0xFF818CF8) else PrimaryBlue,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Title
            Text(
                text = if (role.lowercase() == "admin") "Admin Registration" else buildAnnotatedString {
                    append("Join the ")
                    withStyle(style = SpanStyle(color = neonPurple)) {
                        append("Frequency")
                    }
                }.toString(), // Using toString() for simplicity if not using AnnotatedString directly in if
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = primaryTextColor
            )

            // Re-evaluating title to use buildAnnotatedString correctly
            if (role.lowercase() != "admin") {
                // Keep the original styled title for students
            } else {
                 // Text above stands as Admin Registration
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = if (role.lowercase() == "admin") "Submit your request to the Main Admin for approval." 
                       else "Create your student account to start broadcasting and tuning in.",
                style = MaterialTheme.typography.bodyLarge,
                color = secondaryTextColor
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Form Fields
            Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                
                // Full Name
                FormField(
                    label = "Full Name",
                    value = fullName,
                    onValueChange = { fullName = it },
                    placeholder = "Alex Rivera",
                    icon = Icons.Default.Person,
                    inputBackground = inputBackground,
                    inputBorder = inputBorder,
                    neonPurple = neonPurple,
                    primaryTextColor = primaryTextColor
                )

                // Email Address (Mapped to phoneNumber in logic)
                FormField(
                    label = "Email Address",
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    placeholder = "student@university.edu",
                    icon = Icons.Default.Email,
                    keyboardType = KeyboardType.Email,
                    inputBackground = inputBackground,
                    inputBorder = inputBorder,
                    neonPurple = neonPurple,
                    primaryTextColor = primaryTextColor
                )

                // College ID / Admin ID
                FormField(
                    label = if (role.lowercase() == "admin") "Admin ID (6-digit)" else "College ID",
                    value = collegeId,
                    onValueChange = { collegeId = it },
                    placeholder = if (role.lowercase() == "admin") "#654321" else "ID #12345678",
                    icon = if (role.lowercase() == "admin") Icons.Default.Badge else Icons.Default.Badge,
                    inputBackground = inputBackground,
                    inputBorder = inputBorder,
                    neonPurple = neonPurple,
                    primaryTextColor = primaryTextColor
                )

                if (role.lowercase() == "admin") {
                    // Department
                    FormField(
                        label = "Department",
                        value = department,
                        onValueChange = { department = it },
                        placeholder = "e.g. Computer Science",
                        icon = Icons.Default.School,
                        inputBackground = inputBackground,
                        inputBorder = inputBorder,
                        neonPurple = neonPurple,
                        primaryTextColor = primaryTextColor
                    )

                    // Reason for Access
                    FormField(
                        label = "Reason for Admin Access",
                        value = reasonForAccess,
                        onValueChange = { reasonForAccess = it },
                        placeholder = "e.g. To manage radio events",
                        icon = Icons.Default.Description,
                        inputBackground = inputBackground,
                        inputBorder = inputBorder,
                        neonPurple = neonPurple,
                        primaryTextColor = primaryTextColor
                    )
                }

                // Password
                val focusManager = androidx.compose.ui.platform.LocalFocusManager.current
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Password",
                        color = primaryTextColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        placeholder = { Text("••••••••", color = Color(0xFF64748B)) },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF64748B))
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
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }

                // Confirm Password
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Confirm Password",
                        color = primaryTextColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        placeholder = { Text("••••••••", color = Color(0xFF64748B)) },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF64748B))
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
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Error Message
            if (errorMessage != null) {
                Text(
                    text = errorMessage,
                    color = Color.Red,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            // Create Account Button
            Button(
                onClick = {
                    if (passwordsMatch) {
                        onRegisterClick(
                            fullName, 
                            collegeId, 
                            password, 
                            phoneNumber,
                            if (role.lowercase() == "admin") department else null,
                            if (role.lowercase() == "admin") reasonForAccess else null
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = fullName.isNotEmpty() && collegeId.isNotEmpty() && phoneNumber.isNotEmpty() &&
                          passwordsMatch && isPasswordValid && !isLoading &&
                          (role.lowercase() != "admin" || (department.isNotEmpty() && reasonForAccess.isNotEmpty())),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            if (fullName.isNotEmpty() && collegeId.isNotEmpty() && phoneNumber.isNotEmpty() &&
                                passwordsMatch && isPasswordValid && (role.lowercase() != "admin" || (department.isNotEmpty() && reasonForAccess.isNotEmpty()))) primaryGradient 
                            else Brush.linearGradient(listOf(Color.Gray, Color.Gray))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (role.lowercase() == "admin") "Submit Request" else "Create Account",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Footer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Already have an account? ",
                    color = secondaryTextColor,
                    style = MaterialTheme.typography.bodyMedium
                )
                TextButton(
                    onClick = onLoginClick,
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = "Log in",
                        color = if (isDark) Color.White else PrimaryBlue,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun FormField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text,
    inputBackground: Color,
    inputBorder: Color,
    neonPurple: Color,
    primaryTextColor: Color = Color(0xFF1E293B)
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = label,
            color = primaryTextColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = Color(0xFF64748B)) },
            leadingIcon = {
                Icon(icon, contentDescription = null, tint = Color(0xFF64748B))
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
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
    }
}
