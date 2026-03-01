package com.campuswave.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.campuswave.app.data.models.User
import com.campuswave.app.ui.theme.*
import com.campuswave.app.utils.UrlUtils

@Composable
fun ProfileSidebar(
    user: User?,
    baseUrl: String = "",
    onProfileClick: () -> Unit,
    onHelpDeskClick: () -> Unit,
    onAboutCollegeClick: () -> Unit,
    onReportSessionClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalDrawerSheet(
        modifier = Modifier.width(300.dp),
        drawerContainerColor = CampusSurface,
        drawerContentColor = CampusDark
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header with profile info
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PrimaryGradient)
                    .padding(24.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Profile Picture
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (user?.profile_picture != null) {
                            SubcomposeAsyncImage(
                                model = UrlUtils.joinUrl(baseUrl, user.profile_picture),
                                contentDescription = "Profile Picture",
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop,
                                loading = {
                                    Box(
                                        modifier = Modifier.fillMaxSize().background(Color.White.copy(alpha = 0.1f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                    }
                                },
                                error = {
                                    Box(
                                        modifier = Modifier.fillMaxSize().background(Color.White.copy(alpha = 0.1f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Person, contentDescription = null, tint = Color.White)
                                    }
                                }
                            )
                        } else {
                            Text(
                                text = user?.name?.take(1)?.uppercase() ?: "?",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = user?.name ?: "User",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = user?.email ?: "",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    
                    if (user?.college_pin != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color.White.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "PIN: ${user.college_pin}",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                fontSize = 12.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Menu Items
            SidebarMenuItem(
                icon = Icons.Default.Person,
                label = "Profile",
                onClick = {
                    onDismiss()
                    onProfileClick()
                }
            )
            
            SidebarMenuItem(
                icon = Icons.AutoMirrored.Filled.HelpOutline,
                label = "Help Desk",
                onClick = {
                    onDismiss()
                    onHelpDeskClick()
                }
            )
            
            SidebarMenuItem(
                icon = Icons.Default.Info,
                label = "About College",
                onClick = {
                    onDismiss()
                    onAboutCollegeClick()
                }
            )
            
            SidebarMenuItem(
                icon = Icons.Default.Flag, // Or Warning
                label = "Report Session",
                onClick = {
                    onDismiss()
                    onReportSessionClick()
                }
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            HorizontalDivider(color = CampusGrey.copy(alpha = 0.2f))
            
            // Logout at bottom
            SidebarMenuItem(
                icon = Icons.AutoMirrored.Filled.ExitToApp,
                label = "Logout",
                tint = ErrorRed,
                onClick = {
                    onDismiss()
                    onLogoutClick()
                }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SidebarMenuItem(
    icon: ImageVector,
    label: String,
    tint: Color = CampusDark,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = label,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = tint
        )
    }
}
