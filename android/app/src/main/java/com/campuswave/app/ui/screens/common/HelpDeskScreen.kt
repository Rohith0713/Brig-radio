package com.campuswave.app.ui.screens.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.campuswave.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpDeskScreen(
    onBackClick: () -> Unit
) {
    Scaffold(
        containerColor = CampusBackground,
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PrimaryGradient)
            ) {
                TopAppBar(
                    title = {
                        Text(
                            text = "Help & Support",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = Color.White
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Welcome Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = PrimaryBlue)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.SupportAgent,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "How can we help you?",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Find answers to common questions or contact our support team.",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.9f),
                        textAlign = TextAlign.Center
                    )
                }
            }
            
            // FAQ Section
            Text(
                text = "Frequently Asked Questions",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = CampusDark
            )
            
            FaqItem(
                question = "Login issues",
                answer = "If you're having trouble logging in, please ensure your official college email is correct. If the issue persists, try resetting your password or contact support."
            )
            
            FaqItem(
                question = "Audio not playing",
                answer = "Check your internet connection and volume settings. If the audio stream is paused, try tapping the 'Resume' button. Restarting the app may also help."
            )
            
            FaqItem(
                question = "App errors",
                answer = "If the app crashes or behaves unexpectedly, please clear the app cache from settings or try reinstalling the application to ensure you have the latest version."
            )
            
            FaqItem(
                question = "Account problems",
                answer = "For issues related to your profile, name, or college PIN, please use the 'Profile' section to edit your details or reach out to the admin for assistance."
            )
            
            // Contact Section
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Need More Help?",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = CampusDark
            )
            
            ContactCard(
                icon = Icons.Default.Email,
                title = "Email Support",
                subtitle = "support@campuswave.com",
                description = "We typically respond within 24 hours"
            )
            
            ContactCard(
                icon = Icons.Default.School,
                title = "Campus Office",
                subtitle = "Student Services Building",
                description = "Mon-Fri, 9:00 AM - 5:00 PM"
            )
            
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Composable
private fun FaqItem(
    question: String,
    answer: String
) {
    var expanded by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CampusSurface),
        onClick = { expanded = !expanded }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = question,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = CampusDark,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = CampusGrey
                )
            }
            
            if (expanded) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = answer,
                    fontSize = 14.sp,
                    color = CampusGrey,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@Composable
private fun ContactCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    description: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CampusSurface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = PrimaryBlue.copy(alpha = 0.1f),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = PrimaryBlue,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            Column {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = CampusDark
                )
                Text(
                    text = subtitle,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = PrimaryBlue
                )
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = CampusGrey
                )
            }
        }
    }
}
