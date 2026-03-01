package com.campuswave.app.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.campuswave.app.data.models.User
import com.campuswave.app.ui.theme.CampusBackground
import com.campuswave.app.ui.theme.CampusSurface
import com.campuswave.app.ui.theme.CampusGrey
import com.campuswave.app.ui.theme.CampusDark
import com.campuswave.app.ui.theme.SuccessGreen
import com.campuswave.app.ui.theme.ErrorRed
import com.campuswave.app.ui.theme.PrimaryGradient
import com.campuswave.app.ui.viewmodels.AdminViewModel
import com.campuswave.app.utils.ApiResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminRequestsScreen(
    viewModel: AdminViewModel,
    onBackClick: () -> Unit
) {
    val requestsState by viewModel.adminRequests.collectAsState()
    val approvalState by viewModel.approvalState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchAdminRequests()
    }

    Scaffold(
        containerColor = CampusBackground,
        topBar = {
            TopAppBar(
                title = { Text("Admin Requests", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                modifier = Modifier.background(PrimaryGradient)
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            when (val result = requestsState) {
                is ApiResult.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is ApiResult.Error -> {
                    Text(
                        text = result.message,
                        color = ErrorRed,
                        modifier = Modifier.align(Alignment.Center).padding(16.dp)
                    )
                }
                is ApiResult.Success -> {
                    val requests = result.data
                    if (requests.isEmpty()) {
                        Text(
                            text = "No pending admin requests.",
                            color = CampusGrey,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(requests) { request ->
                                AdminRequestCard(
                                    user = request,
                                    onApprove = { viewModel.approveAdmin(request.id, true) },
                                    onReject = { viewModel.approveAdmin(request.id, false) }
                                )
                            }
                        }
                    }
                }
            }

            // Show approval feedback
            approvalState?.let { result ->
                if (result is ApiResult.Error) {
                    Snackbar(
                        modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
                        containerColor = ErrorRed
                    ) {
                        Text(result.message)
                    }
                }
            }
        }
    }
}

@Composable
fun AdminRequestCard(
    user: User,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CampusSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Person, contentDescription = null, tint = CampusGrey, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = user.name, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = CampusDark)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Email, contentDescription = null, tint = CampusGrey, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = user.email, fontSize = 14.sp, color = CampusGrey)
            }

            user.department?.let { dept ->
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Badge, contentDescription = null, tint = CampusGrey, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Dept: $dept", fontSize = 14.sp, color = CampusGrey)
                }
            }

            user.reason_for_access?.let { reason ->
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = "Reason for access:", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = CampusDark)
                Text(text = reason, fontSize = 14.sp, color = CampusGrey, modifier = Modifier.padding(top = 4.dp))
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onReject,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reject")
                }

                Button(
                    onClick = onApprove,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Approve")
                }
            }
        }
    }
}
