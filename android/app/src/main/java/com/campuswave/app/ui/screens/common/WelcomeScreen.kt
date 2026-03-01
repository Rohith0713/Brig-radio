package com.campuswave.app.ui.screens.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.NightlightRound
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.campuswave.app.R
import com.campuswave.app.data.theme.ThemeMode
import com.campuswave.app.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun WelcomeScreen(
    onLoginClick: () -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    val themeManager = LocalThemeManager.current
    val scope = rememberCoroutineScope()
    
    val backgroundColor = if (isDark) DarkBackground else Color(0xFFF5F5F7) // Light gray/whiteish
    val textColor = if (isDark) Color.White else Color(0xFF1E1E1E)
    val purpleColor = Color(0xFF8B5CF6)
    
    val primaryGradient = Brush.horizontalGradient(
        colors = listOf(Color(0xFF8B5CF6), Color(0xFF7C3AED))
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Spacer(modifier = Modifier.weight(1f))
            
            // App Logo Container
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Color.White) // Or match theme
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                 Image(
                    painter = painterResource(id = R.drawable.ic_college_logo),
                    contentDescription = "App Logo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Text Content
            Text(
                text = "Tune in to your",
                style = MaterialTheme.typography.headlineMedium, // ~28sp
                fontWeight = FontWeight.Bold,
                color = textColor,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = "campus vibe",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = purpleColor,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Login Card/Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDark) Color(0xFF1F222E) else Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp) // Soft shadow
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Join the campus community",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isDark) Color.Gray else Color.DarkGray,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    Button(
                        onClick = onLoginClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = PaddingValues()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(primaryGradient),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Login",
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Footer
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Text(
                    text = "Need Help? • Privacy Policy",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            
             Text(
                text = "v1.0.0",
                style = MaterialTheme.typography.labelSmall,
                color = Color.LightGray,
                 modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        
        // Dark Mode Toggle
        FloatingActionButton(
            onClick = { 
                scope.launch {
                    val newMode = if (isDark) ThemeMode.LIGHT else ThemeMode.DARK
                    themeManager?.saveThemeMode(newMode)
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = if (isDark) Color.White else Color.Black,
            contentColor = if (isDark) Color.Black else Color.White
        ) {
            Icon(
                imageVector = if (isDark) Icons.Filled.WbSunny else Icons.Filled.NightlightRound,
                contentDescription = "Toggle Theme"
            )
        }
    }
}
