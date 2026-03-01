package com.campuswave.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.campuswave.app.utils.PasswordStrength
import com.campuswave.app.utils.PasswordValidationResult

/**
 * Premium animated password strength indicator with rule checklist
 */
@Composable
fun PasswordStrengthIndicator(
    validationResult: PasswordValidationResult,
    modifier: Modifier = Modifier,
    showRules: Boolean = true
) {
    val strength = validationResult.strength
    
    // Animated progress (0.0 to 1.0)
    val progress by animateFloatAsState(
        targetValue = when (strength) {
            PasswordStrength.WEAK -> 0.33f
            PasswordStrength.MEDIUM -> 0.66f
            PasswordStrength.STRONG -> 1.0f
        },
        animationSpec = tween(durationMillis = 300),
        label = "progress_animation"
    )
    
    // Colors for each strength level
    val weakColor = Color(0xFFEF4444)    // Red
    val mediumColor = Color(0xFFF59E0B)  // Orange/Yellow
    val strongColor = Color(0xFF22C55E)  // Green
    
    val indicatorColor by animateColorAsState(
        targetValue = when (strength) {
            PasswordStrength.WEAK -> weakColor
            PasswordStrength.MEDIUM -> mediumColor
            PasswordStrength.STRONG -> strongColor
        },
        animationSpec = tween(durationMillis = 300),
        label = "color_animation"
    )
    
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Strength Label with Emoji
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Password Strength",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF718096),
                fontWeight = FontWeight.Medium
            )
            
            Text(
                text = when (strength) {
                    PasswordStrength.WEAK -> "Weak"
                    PasswordStrength.MEDIUM -> "Medium"
                    PasswordStrength.STRONG -> "Strong"
                },
                style = MaterialTheme.typography.bodySmall,
                color = indicatorColor,
                fontWeight = FontWeight.Bold
            )
        }
        
        // Animated Progress Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFFE2E8F0))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = when (strength) {
                                PasswordStrength.WEAK -> listOf(weakColor, weakColor)
                                PasswordStrength.MEDIUM -> listOf(weakColor, mediumColor)
                                PasswordStrength.STRONG -> listOf(weakColor, mediumColor, strongColor)
                            }
                        )
                    )
            )
        }
        
        // Rules Checklist
        if (showRules) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                PasswordRuleItem(
                    text = "At least 8 characters",
                    isSatisfied = validationResult.hasMinLength
                )
                PasswordRuleItem(
                    text = "One uppercase letter (A-Z)",
                    isSatisfied = validationResult.hasUppercase
                )
                PasswordRuleItem(
                    text = "One lowercase letter (a-z)",
                    isSatisfied = validationResult.hasLowercase
                )
                PasswordRuleItem(
                    text = "One number (0-9)",
                    isSatisfied = validationResult.hasNumber
                )
                PasswordRuleItem(
                    text = "One special character (@#\$%!&*)",
                    isSatisfied = validationResult.hasSpecialChar
                )
                if (!validationResult.hasNoSpaces) {
                    PasswordRuleItem(
                        text = "No spaces allowed",
                        isSatisfied = false
                    )
                }
                if (!validationResult.notMatchingUserInfo) {
                    PasswordRuleItem(
                        text = "Cannot match your personal info",
                        isSatisfied = false
                    )
                }
            }
        }
    }
}

@Composable
private fun PasswordRuleItem(
    text: String,
    isSatisfied: Boolean
) {
    val iconColor by animateColorAsState(
        targetValue = if (isSatisfied) Color(0xFF22C55E) else Color(0xFFEF4444),
        animationSpec = tween(durationMillis = 200),
        label = "rule_color"
    )
    
    val textColor by animateColorAsState(
        targetValue = if (isSatisfied) Color(0xFF4A5568) else Color(0xFF9CA3AF),
        animationSpec = tween(durationMillis = 200),
        label = "text_color"
    )
    
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = if (isSatisfied) Icons.Default.Check else Icons.Default.Close,
            contentDescription = if (isSatisfied) "Satisfied" else "Not satisfied",
            tint = iconColor,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = textColor,
            fontSize = 12.sp
        )
    }
}

/**
 * Compact version of the strength indicator (just the bar and label)
 */
@Composable
fun PasswordStrengthBar(
    strength: PasswordStrength,
    modifier: Modifier = Modifier
) {
    val progress by animateFloatAsState(
        targetValue = when (strength) {
            PasswordStrength.WEAK -> 0.33f
            PasswordStrength.MEDIUM -> 0.66f
            PasswordStrength.STRONG -> 1.0f
        },
        animationSpec = tween(durationMillis = 300),
        label = "progress"
    )
    
    val color by animateColorAsState(
        targetValue = when (strength) {
            PasswordStrength.WEAK -> Color(0xFFEF4444)
            PasswordStrength.MEDIUM -> Color(0xFFF59E0B)
            PasswordStrength.STRONG -> Color(0xFF22C55E)
        },
        animationSpec = tween(durationMillis = 300),
        label = "color"
    )
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(4.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(Color(0xFFE2E8F0))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress)
                .fillMaxHeight()
                .clip(RoundedCornerShape(2.dp))
                .background(color)
        )
    }
}
