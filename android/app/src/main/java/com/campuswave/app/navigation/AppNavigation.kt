package com.campuswave.app.navigation

import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.campuswave.app.Screen
import com.campuswave.app.data.auth.AuthManager
import com.campuswave.app.services.AudioServiceManager
import com.campuswave.app.ui.screens.SplashScreen
import com.campuswave.app.ui.screens.admin.*
import com.campuswave.app.ui.screens.auth.*
import android.util.Patterns
import android.widget.Toast
import com.campuswave.app.data.network.ApiConfig
import com.campuswave.app.data.network.RetrofitClient
import com.campuswave.app.ui.screens.admin.AdminRadiosTab
import com.campuswave.app.ui.screens.common.*
import com.campuswave.app.ui.screens.student.*
import com.campuswave.app.ui.theme.BRIG_RADIOThemeWithManager
import com.campuswave.app.ui.viewmodels.*
import com.campuswave.app.utils.ApiResult
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Composable
fun AppNavigation(
    navController: NavHostController,
    authManager: AuthManager,
    adminViewModel: AdminViewModel,
    radioViewModel: RadioViewModel,
    suggestionsViewModel: SuggestionsViewModel,
    podcastViewModel: PodcastViewModel,
    placementViewModel: PlacementViewModel,
    authViewModel: AuthViewModel,
    deepLinkDestination: String? = null,
    onDeepLinkConsumed: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Handle deep links when they arrive
    LaunchedEffect(deepLinkDestination) {
        if (deepLinkDestination != null) {
            navController.navigate(deepLinkDestination) {
                launchSingleTop = true
            }
            onDeepLinkConsumed()
        }
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        modifier = Modifier.background(MaterialTheme.colorScheme.background),
        enterTransition = { fadeIn(animationSpec = tween(300)) },
        exitTransition = { fadeOut(animationSpec = tween(300)) },
        popEnterTransition = { fadeIn(animationSpec = tween(300)) },
        popExitTransition = { fadeOut(animationSpec = tween(300)) }
    ) {
        // ==================== Splash Screen ====================
        composable(
            route = Screen.Splash.route,
            exitTransition = { fadeOut(animationSpec = tween(600, easing = EaseInOut)) },
            popExitTransition = { fadeOut(animationSpec = tween(600, easing = EaseInOut)) }
        ) {
            SplashScreen(
                onSplashComplete = {
                    coroutineScope.launch {
                        val token = authManager.getToken()
                        val role = authManager.getUserRole()
                        
                        if (token != null && role != null) {
                            if (role.uppercase() == "ADMIN") {
                                navController.navigate(Screen.AdminDashboard.route) {
                                    popUpTo(Screen.Splash.route) { inclusive = true }
                                }
                            } else {
                                navController.navigate(Screen.StudentDashboard.route) {
                                    popUpTo(Screen.Splash.route) { inclusive = true }
                                }
                            }
                        } else {
                            navController.navigate(Screen.Welcome.route) {
                                popUpTo(Screen.Splash.route) { inclusive = true }
                            }
                        }
                    }
                }
            )
        }
        
        // ==================== Auth Screens ====================
        composable(Screen.Welcome.route) {
            var isCheckingSession by remember { mutableStateOf(true) }
            LaunchedEffect(Unit) {
                try {
                    val token = authManager.getToken()
                    val role = authManager.getUserRole()
                    if (token != null && role != null) {
                        if (role.uppercase() == "ADMIN") {
                            navController.navigate(Screen.AdminDashboard.route) {
                                popUpTo(Screen.Welcome.route) { inclusive = true }
                            }
                        } else {
                            navController.navigate(Screen.StudentDashboard.route) {
                                popUpTo(Screen.Welcome.route) { inclusive = true }
                            }
                        }
                    }
                } catch (e: Exception) { }
                isCheckingSession = false
            }
            if (isCheckingSession) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            } else {
                WelcomeScreen(onLoginClick = { navController.navigate(Screen.Login.route) })
            }
        }
        
        composable(Screen.Login.route) {
            val loginState by authViewModel.loginState.collectAsState()
            val verificationNeeded by authViewModel.verificationNeeded.collectAsState()
            val forgotState by authViewModel.forgotPasswordState.collectAsState()
            var emailForOtp by remember { mutableStateOf<String?>(null) }
            
            LaunchedEffect(loginState, verificationNeeded, forgotState) {
                if (forgotState is ApiResult.Success) {
                     emailForOtp?.let { email ->
                         navController.navigate(Screen.OtpVerification.createRoute(email, "reset"))
                         authViewModel.resetForgotPasswordState()
                     }
                } else if (verificationNeeded != null) {
                    navController.navigate(Screen.OtpVerification.createRoute(verificationNeeded!!, "registration"))
                    authViewModel.resetVerificationNeeded()
                } else if (loginState is ApiResult.Success) {
                    val userRole = (loginState as ApiResult.Success).data
                    if (userRole == "ADMIN") {
                        navController.navigate(Screen.AdminDashboard.route) { popUpTo(Screen.Welcome.route) { inclusive = true } }
                    } else {
                        navController.navigate(Screen.StudentDashboard.route) { popUpTo(Screen.Welcome.route) { inclusive = true } }
                    }
                    authViewModel.resetLoginState()
                }
            }
            
            LoginScreen(
                role = "Student",
                onLoginClick = { email, password -> authViewModel.login(email, password) },
                onBackClick = { navController.popBackStack() },
                onRegisterClick = { navController.navigate(Screen.Register.createRoute("Student")) },
                onForgotPasswordClick = { email ->
                     if (email.isNotBlank() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                         emailForOtp = email
                         authViewModel.forgotPassword(email)
                     } else {
                         navController.navigate(Screen.ForgotPassword.route)
                     }
                },
                isLoading = loginState is ApiResult.Loading,
                errorMessage = (loginState as? ApiResult.Error)?.message
            )
        }

        composable(Screen.ForgotPassword.route) {
            val forgotState by authViewModel.forgotPasswordState.collectAsState()
            var emailForOtp by remember { mutableStateOf<String?>(null) }
            LaunchedEffect(forgotState) {
                if (forgotState is ApiResult.Success) {
                    emailForOtp?.let { email ->
                        navController.navigate(Screen.OtpVerification.createRoute(email, "reset"))
                        authViewModel.resetForgotPasswordState()
                    }
                }
            }
            ForgotPasswordScreen(
                onSendOtpClick = { email -> emailForOtp = email; authViewModel.forgotPassword(email) },
                onBackClick = { navController.popBackStack() },
                isLoading = forgotState is ApiResult.Loading,
                errorMessage = (forgotState as? ApiResult.Error)?.message
            )
        }

        composable(route = Screen.ResetPassword.route, arguments = listOf(navArgument("email") { type = NavType.StringType }, navArgument("otp") { type = NavType.StringType })) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            val otp = backStackEntry.arguments?.getString("otp") ?: ""
            val resetState by authViewModel.resetPasswordState.collectAsState()
            LaunchedEffect(resetState) {
                if (resetState is ApiResult.Success) {
                    navController.navigate(Screen.Login.route) { popUpTo(0) { inclusive = true } }
                    authViewModel.resetResetPasswordState()
                }
            }
            ResetPasswordScreen(
                email = email, otp = otp,
                onResetClick = { password -> authViewModel.resetPassword(email, otp, password) },
                isLoading = resetState is ApiResult.Loading,
                errorMessage = (resetState as? ApiResult.Error)?.message
            )
        }

        composable(route = Screen.Register.route, arguments = listOf(navArgument("role") { type = NavType.StringType })) { backStackEntry ->
            val role = backStackEntry.arguments?.getString("role") ?: "Student"
            val registerState by authViewModel.registerState.collectAsState()
            val verificationNeeded by authViewModel.verificationNeeded.collectAsState()
            LaunchedEffect(registerState, verificationNeeded) {
                if (verificationNeeded != null) {
                    navController.navigate(Screen.OtpVerification.createRoute(verificationNeeded!!, "registration"))
                    authViewModel.resetVerificationNeeded()
                } else if (registerState is ApiResult.Success) {
                    val userRole = (registerState as ApiResult.Success).data
                    if (userRole != "VERIFY") {
                        if (userRole == "ADMIN") navController.navigate(Screen.AdminDashboard.route) { popUpTo(Screen.Splash.route) { inclusive = true } }
                        else navController.navigate(Screen.StudentDashboard.route) { popUpTo(Screen.Splash.route) { inclusive = true } }
                        authViewModel.resetRegisterState()
                    }
                }
            }
            RegisterScreen(
                role = role,
                onRegisterClick = { n, c, p, g, d, r -> authViewModel.register(n, g, p, role.uppercase(), null, d, r) },
                onBackClick = { navController.popBackStack() },
                onLoginClick = { navController.popBackStack() },
                isLoading = registerState is ApiResult.Loading,
                errorMessage = (registerState as? ApiResult.Error)?.message
            )
        }

        composable(route = Screen.OtpVerification.route, arguments = listOf(navArgument("email") { type = NavType.StringType }, navArgument("purpose") { type = NavType.StringType })) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            val purpose = backStackEntry.arguments?.getString("purpose") ?: "registration"
            val otpState by authViewModel.otpState.collectAsState()
            val verifyResetState by authViewModel.verifyResetState.collectAsState()
            var verifiedOtpForReset by remember { mutableStateOf<String?>(null) }
            LaunchedEffect(otpState, verifyResetState) {
                if (purpose == "profile_reset" && verifyResetState is ApiResult.Success) {
                    val resetToken = (verifyResetState as ApiResult.Success).data
                    navController.navigate(Screen.NewPassword.createRoute(resetToken)) { popUpTo(Screen.OtpVerification.route) { inclusive = true } }
                    authViewModel.clearResetFlowStates()
                } else if (otpState is ApiResult.Success) {
                    if (purpose == "reset") {
                        verifiedOtpForReset?.let { otp -> navController.navigate(Screen.ResetPassword.createRoute(email, otp)) { popUpTo(Screen.OtpVerification.route) { inclusive = true } } }
                    } else if (purpose != "profile_reset") { 
                        val userRole = (otpState as ApiResult.Success).data
                        if (userRole == "ADMIN") navController.navigate(Screen.AdminDashboard.route) { popUpTo(Screen.Splash.route) { inclusive = true } }
                        else navController.navigate(Screen.StudentDashboard.route) { popUpTo(Screen.Splash.route) { inclusive = true } }
                        authViewModel.resetOtpState()
                    }
                }
            }
            OtpVerificationScreen(
                email = email,
                onVerifyClick = { otp -> if (purpose == "profile_reset") authViewModel.verifyProfileResetOtp(otp) else if (purpose == "reset") { verifiedOtpForReset = otp; authViewModel.verifyResetOtp(email, otp) } else authViewModel.verifyOtp(email, otp) },
                onResendClick = { if (purpose == "profile_reset") authViewModel.startPasswordReset() else authViewModel.resendOtp(email) },
                isLoading = (otpState is ApiResult.Loading) || (verifyResetState is ApiResult.Loading),
                errorMessage = (otpState as? ApiResult.Error)?.message ?: (verifyResetState as? ApiResult.Error)?.message
            )
        }

        composable(route = Screen.NewPassword.route, arguments = listOf(navArgument("token") { type = NavType.StringType })) { backStackEntry ->
            val token = backStackEntry.arguments?.getString("token") ?: ""
            val completeState by authViewModel.completeResetState.collectAsState()
            LaunchedEffect(completeState) { if (completeState is ApiResult.Success) { android.widget.Toast.makeText(context, "Password updated", android.widget.Toast.LENGTH_SHORT).show(); navController.popBackStack(); authViewModel.clearResetFlowStates() } }
            NewPasswordScreen(onUpdateClick = { authViewModel.completeProfilePasswordReset(token, it) }, isLoading = completeState is ApiResult.Loading, errorMessage = (completeState as? ApiResult.Error)?.message)
        }

        composable(Screen.UserDetails.route) {
            val resetRequestState by authViewModel.resetRequestState.collectAsState()
            var userEmail by remember { mutableStateOf("") }
            LaunchedEffect(Unit) { userEmail = authManager.userEmail.first() ?: "" }
            LaunchedEffect(resetRequestState) { if (resetRequestState is ApiResult.Success) { navController.navigate(Screen.OtpVerification.createRoute(userEmail, "profile_reset")); authViewModel.clearResetFlowStates() } }
            UserDetailsScreen(
                userName = authManager.userName.collectAsState(initial = "Loading...").value ?: "Unknown", userEmail = userEmail, userId = userEmail,
                userRole = authManager.getUserRole() ?: "STUDENT", onBackClick = { navController.popBackStack() },
                onLogoutClick = { AudioServiceManager.stop(context); coroutineScope.launch { authViewModel.logoutSync(); navController.navigate(Screen.Login.route) { popUpTo(0) { inclusive = true } } } },
                onResetPasswordClick = { authViewModel.startPasswordReset() }
            )
        }

        // ==================== Admin Screens ====================
        composable(route = Screen.AdminDashboard.route) {
            val dashboardStats by radioViewModel.dashboardStats.collectAsState()
            val liveRadios by radioViewModel.liveRadios.collectAsState()
            val upcomingRadios by radioViewModel.upcomingRadios.collectAsState()
            val activeMarquee by radioViewModel.activeMarquee.collectAsState()
            val synchronizedTime by radioViewModel.synchronizedTime.collectAsState()
            var currentUser by remember { mutableStateOf<com.campuswave.app.data.models.User?>(null) }
            LaunchedEffect(Unit) {
                radioViewModel.fetchDashboardStats(); radioViewModel.fetchLiveRadios(); radioViewModel.fetchUpcomingRadios(); radioViewModel.fetchActiveMarquee()
                try {
                    val token = authManager.getToken()
                    if (token != null) currentUser = com.campuswave.app.data.network.RetrofitClient.apiService.getCurrentUser("Bearer $token")
                } catch (e: Exception) {
                    currentUser = com.campuswave.app.data.models.User(0, authManager.userEmail.first() ?: "", authManager.userName.first() ?: "Admin", "ADMIN")
                }
            }
            AdminDashboardScreen(
                stats = (dashboardStats as? ApiResult.Success)?.data, liveRadios = (liveRadios as? ApiResult.Success)?.data ?: emptyList(),
                upcomingRadios = (upcomingRadios as? ApiResult.Success)?.data ?: emptyList(), isLoadingStats = dashboardStats is ApiResult.Loading,
                activeMarquee = (activeMarquee as? ApiResult.Success)?.data, currentUser = currentUser,
                onCreateRadioClick = { navController.navigate(Screen.CreateRadio.route) }, onRadioSuggestionsClick = { navController.navigate(Screen.RadioSuggestions.route) },
                onAnalyticsClick = { navController.navigate(Screen.AdminAnalytics.route) }, onUploadPlacementClick = { navController.navigate(Screen.UploadPlacement.route) },
                onUserDetailsClick = { navController.navigate(Screen.UserDetails.route) },
                onLogoutClick = { AudioServiceManager.stop(context); coroutineScope.launch { authViewModel.logoutSync(); navController.navigate(Screen.Login.route) { popUpTo(0) { inclusive = true } } } },
                onRadioClick = { navController.navigate(Screen.RadioDetails.createRoute(it)) }, onLikeClick = { radioViewModel.toggleFavorite(it) },
                onHostRadioClick = { navController.navigate(Screen.LiveHosting.createRoute(it)) }, onPastRadioClick = { navController.navigate(Screen.PastRadios.route) },
                onInviteAdminClick = { navController.navigate(Screen.InviteAdmin.route) }, onAdminRequestsClick = { navController.navigate("admin_requests") },
                onManageMarqueeClick = { navController.navigate(Screen.ManageMarquee.route) }, onManageBannersClick = { navController.navigate(Screen.Banners.route) },
                onCollegeUpdatesClick = { navController.navigate(Screen.CreateCollegeUpdate.route) }, onAdminPostsClick = { navController.navigate(Screen.AdminPosts.route) },
                onLivePodcastClick = { navController.navigate(Screen.PodcastList.route) }, onIssueReportsClick = { navController.navigate(Screen.AdminIssues.route) },
                onRefresh = { radioViewModel.fetchDashboardStats(); radioViewModel.fetchLiveRadios(); radioViewModel.fetchUpcomingRadios(); radioViewModel.fetchActiveMarquee() },
                synchronizedTimeMillis = synchronizedTime
            )
        }
        composable(Screen.ManageMarquee.route) { ManageMarqueeScreen(viewModel = adminViewModel, onBackClick = { navController.popBackStack() }) }
        composable("admin_requests") { AdminRequestsScreen(viewModel = adminViewModel, onBackClick = { navController.popBackStack() }) }
        composable(Screen.PastRadios.route) {
            val missedRadios by radioViewModel.missedRadios.collectAsState()
            LaunchedEffect(Unit) { radioViewModel.fetchMissedRadios() }
            Scaffold(topBar = { TopAppBar(title = { Text("Past Radio Sessions") }, navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") } }) }) { padding ->
                Box(modifier = Modifier.padding(padding)) { AdminRadiosTab(radios = (missedRadios as? ApiResult.Success)?.data ?: emptyList(), isLive = false, onRadioClick = { navController.navigate(Screen.RadioDetails.createRoute(it)) }, onLikeClick = { radioViewModel.toggleFavorite(it) }) }
            }
        }
        composable(Screen.InviteAdmin.route) { InviteAdminScreen(onBackClick = { navController.popBackStack() }) }
        composable(Screen.CreateRadio.route) { CreateRadioScreen(radioViewModel = radioViewModel, onBackClick = { navController.popBackStack() }, onRadioCreated = { navController.popBackStack() }) }
        composable(Screen.RadioSuggestions.route) {
            val pendingSuggestions by suggestionsViewModel.pendingRadioSuggestions.collectAsState()
            LaunchedEffect(Unit) { suggestionsViewModel.fetchPendingRadioSuggestions() }
            RadioSuggestionsScreen(suggestions = (pendingSuggestions as? ApiResult.Success)?.data ?: emptyList(), isLoading = pendingSuggestions is ApiResult.Loading, onApprove = { suggestionsViewModel.approveRadioSuggestion(it) }, onReject = { suggestionsViewModel.rejectRadioSuggestion(it) }, onRefresh = { suggestionsViewModel.fetchPendingRadioSuggestions() }, onBackClick = { navController.popBackStack() })
        }
        composable(Screen.AdminAnalytics.route) {
            val analyticsState by adminViewModel.analyticsState.collectAsState()
            LaunchedEffect(Unit) { adminViewModel.fetchAnalytics() }
            AdminAnalyticsScreen(analyticsState = analyticsState, onBackClick = { navController.popBackStack() }, onRefresh = { adminViewModel.fetchAnalytics() })
        }
        composable(Screen.UploadPlacement.route) {
            val placementsState by placementViewModel.placements.collectAsState()
            val postersState by placementViewModel.posters.collectAsState()
            val createState by placementViewModel.createState.collectAsState()
            val uploadPosterState by placementViewModel.uploadPosterState.collectAsState()
            UploadPlacementScreen(
                placements = (placementsState as? ApiResult.Success)?.data ?: emptyList(), posters = (postersState as? ApiResult.Success)?.data ?: emptyList(),
                onBackClick = { navController.popBackStack() }, onPlacementPosted = { placementViewModel.addPlacement(it) }, onDeletePlacement = { placementViewModel.deletePlacement(it) },
                onPosterUpload = { t, c, d, p -> placementViewModel.uploadPoster(t, c, d, p) }, onDeletePoster = { placementViewModel.deletePlacementPoster(it) },
                onTogglePosterVisibility = { id, vis -> placementViewModel.togglePosterVisibility(id, vis) },
                isLoading = placementsState is ApiResult.Loading || createState is ApiResult.Loading || uploadPosterState is ApiResult.Loading,
                errorMessage = (placementsState as? ApiResult.Error)?.message ?: (createState as? ApiResult.Error)?.message ?: (uploadPosterState as? ApiResult.Error)?.message
            )
            LaunchedEffect(createState) { if (createState is ApiResult.Success) { android.widget.Toast.makeText(context, "Placement successfully created", android.widget.Toast.LENGTH_SHORT).show(); placementViewModel.resetCreateState() } }
            LaunchedEffect(uploadPosterState) { if (uploadPosterState is ApiResult.Success) { android.widget.Toast.makeText(context, "Poster successfully uploaded", android.widget.Toast.LENGTH_SHORT).show(); placementViewModel.resetUploadPosterState() } }
        }

        // ==================== Podcast Screens ====================
        composable("create_podcast") {
            val isLoading by podcastViewModel.isLoading.collectAsState()
            val successMessage by podcastViewModel.successMessage.collectAsState()
            val errorMessage by podcastViewModel.errorMessage.collectAsState()
            CreatePodcastScreen(onBackClick = { navController.popBackStack() }, onCreatePodcast = { t, d, s -> podcastViewModel.createPodcast(t, d, s) }, isLoading = isLoading, successMessage = successMessage, errorMessage = errorMessage, onMessageShown = { podcastViewModel.clearMessages() })
        }
        composable(route = "live_podcast_control/{podcastId}", arguments = listOf(navArgument("podcastId") { type = NavType.IntType })) { backStackEntry ->
            val podcastId = backStackEntry.arguments?.getInt("podcastId") ?: 0
            val activePodcast by podcastViewModel.activePodcast.collectAsState()
            val viewerCount by podcastViewModel.viewerCount.collectAsState()
            val handRaises by podcastViewModel.handRaises.collectAsState()
            val isLoading by podcastViewModel.isLoading.collectAsState()
            val errorMessage by podcastViewModel.errorMessage.collectAsState()
            val isSpeaking by podcastViewModel.isSpeaking.collectAsState()
            LaunchedEffect(podcastId) { podcastViewModel.fetchPodcastDetails(podcastId); podcastViewModel.startPolling(podcastId) }
            DisposableEffect(Unit) { onDispose { podcastViewModel.stopPolling(); podcastViewModel.clearActivePodcast() } }
            if (activePodcast != null) {
                LivePodcastControlScreen(
                    podcast = activePodcast!!, viewerCount = viewerCount, handRaises = handRaises, onBackClick = { navController.popBackStack() },
                    onToggleMute = { podcastViewModel.toggleMute(podcastId) }, onEndPodcast = { podcastViewModel.endPodcast(podcastId); navController.popBackStack() },
                    onAcceptHandRaise = { podcastViewModel.acceptHandRaise(podcastId, it) }, onIgnoreHandRaise = { podcastViewModel.ignoreHandRaise(podcastId, it) },
                    isSpeaking = isSpeaking, isLoading = isLoading, errorMessage = errorMessage
                )
            }
        }
        composable(route = "live_podcast_viewer/{podcastId}", arguments = listOf(navArgument("podcastId") { type = NavType.IntType })) { backStackEntry ->
            val podcastId = backStackEntry.arguments?.getInt("podcastId") ?: 0
            val activePodcast by podcastViewModel.activePodcast.collectAsState()
            val handRaiseStatus by podcastViewModel.myHandRaiseStatus.collectAsState()
            val isLoading by podcastViewModel.isLoading.collectAsState()
            val isSpeaking by podcastViewModel.isSpeaking.collectAsState()
            LaunchedEffect(podcastId) { podcastViewModel.joinPodcast(podcastId); podcastViewModel.startStudentPolling(podcastId) }
            DisposableEffect(Unit) { onDispose { podcastViewModel.leavePodcast(podcastId); podcastViewModel.stopPolling(); podcastViewModel.clearActivePodcast() } }
            if (activePodcast != null) {
                LivePodcastViewerScreen(
                    podcast = activePodcast!!, handRaiseStatus = handRaiseStatus, onLeave = { podcastViewModel.leavePodcast(podcastId); navController.popBackStack() },
                    onRaiseHand = { podcastViewModel.raiseHand(podcastId) }, onCancelHandRaise = { podcastViewModel.cancelHandRaise(podcastId) },
                    isAdminSpeaking = isSpeaking, isLoading = isLoading
                )
            }
        }
        composable(Screen.PodcastList.route) {
            val livePodcast by podcastViewModel.livePodcast.collectAsState()
            val scheduledPodcasts by podcastViewModel.scheduledPodcasts.collectAsState()
            val isLoading by podcastViewModel.isLoading.collectAsState()
            val userRole by authManager.userRole.collectAsState(initial = "STUDENT")
            LaunchedEffect(Unit) { podcastViewModel.fetchLivePodcast(); podcastViewModel.fetchActivePodcasts() }
            PodcastListScreen(
                livePodcast = livePodcast, scheduledPodcasts = scheduledPodcasts, isLoading = isLoading, userRole = userRole ?: "STUDENT",
                onBackClick = { navController.popBackStack() },
                onPodcastClick = { id -> if ((userRole ?: "STUDENT") == "ADMIN") navController.navigate("live_podcast_control/$id") else navController.navigate("live_podcast_viewer/$id") },
                onGoLiveClick = { id -> podcastViewModel.goLive(id); navController.navigate("live_podcast_control/$id") },
                onCreatePodcastClick = { navController.navigate("create_podcast") }, onRefresh = { podcastViewModel.fetchLivePodcast(); podcastViewModel.fetchActivePodcasts() }
            )
        }

        // ==================== Student Screens ====================
        composable(route = Screen.StudentDashboard.route, enterTransition = { fadeIn(animationSpec = tween(400, easing = EaseInOut)) }) {
            val liveRadios by radioViewModel.liveRadios.collectAsState()
            val upcomingRadios by radioViewModel.upcomingRadios.collectAsState()
            val missedRadios by radioViewModel.missedRadios.collectAsState()
            val banners by radioViewModel.banners.collectAsState()
            val livePodcast by podcastViewModel.livePodcast.collectAsState()
            val synchronizedTime by radioViewModel.synchronizedTime.collectAsState()
            val activeMarquee by radioViewModel.activeMarquee.collectAsState()
            var currentUser by remember { mutableStateOf<com.campuswave.app.data.models.User?>(null) }
            LaunchedEffect(Unit) {
                radioViewModel.fetchLiveRadios(); radioViewModel.fetchUpcomingRadios(); radioViewModel.fetchMissedRadios(); radioViewModel.fetchActiveMarquee(); radioViewModel.fetchBanners(); podcastViewModel.fetchLivePodcast()
                try {
                    val token = authManager.getToken()
                    if (token != null) currentUser = com.campuswave.app.data.network.RetrofitClient.apiService.getCurrentUser("Bearer $token")
                } catch (e: Exception) {
                    currentUser = com.campuswave.app.data.models.User(0, authManager.userEmail.first() ?: "", authManager.userName.first() ?: "User", "STUDENT")
                }
            }
            StudentDashboardScreen(
                liveRadios = (liveRadios as? ApiResult.Success)?.data ?: emptyList(), upcomingRadios = (upcomingRadios as? ApiResult.Success)?.data ?: emptyList(),
                pastRadios = (missedRadios as? ApiResult.Success)?.data ?: emptyList(), isLoadingLive = liveRadios is ApiResult.Loading,
                isLoadingUpcoming = upcomingRadios is ApiResult.Loading, isLoadingPast = missedRadios is ApiResult.Loading,
                activeMarquee = (activeMarquee as? ApiResult.Success)?.data, banners = (banners as? ApiResult.Success)?.data ?: emptyList(),
                currentUser = currentUser, baseUrl = com.campuswave.app.data.network.ApiConfig.BASE_URL.removeSuffix("/api/"),
                livePodcast = livePodcast, onNotificationsClick = { navController.navigate(Screen.Notifications.route) },
                onSuggestRadioClick = { navController.navigate(Screen.SubmitSuggestion.route) }, onUserDetailsClick = { navController.navigate(Screen.UserDetails.route) },
                onLogoutClick = { AudioServiceManager.stop(context); coroutineScope.launch { authViewModel.logoutSync(); navController.navigate(Screen.Login.route) { popUpTo(0) { inclusive = true } } } },
                onRadioClick = { navController.navigate(Screen.RadioDetails.createRoute(it)) }, onLikeClick = { radioViewModel.toggleFavorite(it) },
                onReminderToggle = { radioViewModel.toggleSubscription(it) }, onCollegeUpdatesClick = { navController.navigate(Screen.CollegeUpdates.route) },
                onPodcastsClick = { navController.navigate(Screen.PodcastList.route) }, onJoinPodcastClick = { navController.navigate("live_podcast_viewer/$it") },
                onPlacementsClick = { navController.navigate(Screen.Placements.route) }, onIssuesClick = { navController.navigate(Screen.StudentIssues.route) },
                onRefresh = { radioViewModel.fetchLiveRadios(); radioViewModel.fetchDashboardStats(); radioViewModel.fetchActiveMarquee(); podcastViewModel.fetchLivePodcast() },
                synchronizedTimeMillis = synchronizedTime
            )
        }
        composable(Screen.Placements.route) {
            val placementsState by placementViewModel.placements.collectAsState()
            val postersState by placementViewModel.posters.collectAsState()
            PlacementsScreen(placements = (placementsState as? ApiResult.Success)?.data ?: emptyList(), posters = (postersState as? ApiResult.Success)?.data ?: emptyList(), onBackClick = { navController.popBackStack() }, onNotificationClick = { navController.navigate(Screen.Notifications.route) }, onProfileClick = { navController.navigate(Screen.UserDetails.route) }, onBookmarkClick = { placementViewModel.toggleBookmark(it) })
        }
        composable(Screen.SubmitSuggestion.route) {
            val createState by suggestionsViewModel.createRadioSuggestionState.collectAsState()
            LaunchedEffect(createState) { if (createState is ApiResult.Success) { suggestionsViewModel.resetCreateRadioSuggestionState(); navController.popBackStack() } }
            SubmitSuggestionScreen(isLoading = createState is ApiResult.Loading, errorMessage = (createState as? ApiResult.Error)?.message, onSubmit = { t, d, c -> suggestionsViewModel.createRadioSuggestion(t, d, c) }, onBackClick = { navController.popBackStack() }, onSubmitted = { navController.popBackStack() })
        }
        composable(route = Screen.Banners.route) {
            val banners by radioViewModel.banners.collectAsState()
            LaunchedEffect(Unit) { radioViewModel.fetchBanners() }
            AdminBannerScreen(banners = (banners as? ApiResult.Success)?.data ?: emptyList(), onBackClick = { navController.popBackStack() }, onUploadClick = { radioViewModel.uploadBanner(it) }, onDeleteClick = { radioViewModel.deleteBanner(it) })
        }
        composable(Screen.Notifications.route) { NotificationsScreen(radioViewModel = radioViewModel, onBackClick = { navController.popBackStack() }) }

        // ==================== College Updates ====================
        composable(route = Screen.CollegeUpdates.route, arguments = listOf(navArgument("updateId") { type = NavType.StringType; nullable = true; defaultValue = null })) { backStackEntry ->
            val updateId = backStackEntry.arguments?.getString("updateId")?.toIntOrNull()
            CollegeUpdatesScreen(navController = navController, targetUpdateId = updateId)
        }
        composable(Screen.CreateCollegeUpdate.route) { CreateCollegeUpdateScreen(navController = navController) }
        composable(Screen.AdminPosts.route) { AdminPostsScreen(navController = navController) }
        composable(Screen.HelpDesk.route) { HelpDeskScreen(onBackClick = { navController.popBackStack() }) }
        composable(Screen.AboutCollege.route) { AboutCollegeScreen(onBackClick = { navController.popBackStack() }) }

        // ==================== Common Screens ====================
        composable(route = Screen.RadioDetails.route, arguments = listOf(navArgument("radioId") { type = NavType.IntType }, navArgument("autoStart") { type = NavType.BoolType; defaultValue = false })) { backStackEntry ->
            val radioId = backStackEntry.arguments?.getInt("radioId") ?: 0
            val autoStart = backStackEntry.arguments?.getBoolean("autoStart") ?: false
            val radioState by radioViewModel.radioDetails.collectAsState()
            val synchronizedTime by radioViewModel.synchronizedTime.collectAsState()
            var userRole by remember { mutableStateOf<String?>(null) }
            LaunchedEffect(radioId) { userRole = AuthManager(context).getUserRole() }
            DisposableEffect(radioId) { radioViewModel.startRadioPolling(radioId); onDispose { radioViewModel.stopRadioPolling() } }
            when (val state = radioState) {
                is ApiResult.Success -> RadioDetailsScreen(radio = state.data, userRole = userRole ?: "student", autoStart = autoStart, onBackClick = { navController.popBackStack() }, onEditClick = { navController.navigate(Screen.EditRadio.createRoute(it.id)) }, onDeleteClick = { radioViewModel.deleteRadio(it.id); navController.popBackStack() }, onHostClick = { navController.navigate(Screen.LiveHosting.createRoute(radioId)) }, onToggleReminder = { radioViewModel.toggleSubscription(it.id) }, synchronizedTimeMillis = synchronizedTime)
                else -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            }
        }
        composable(route = Screen.EditRadio.route, arguments = listOf(navArgument("radioId") { type = NavType.IntType })) { backStackEntry ->
            val radioId = backStackEntry.arguments?.getInt("radioId") ?: 0
            val radioState by radioViewModel.radioDetails.collectAsState()
            LaunchedEffect(radioId) { radioViewModel.fetchRadioDetails(radioId) }
            if (radioState is ApiResult.Success) EditRadioScreen(radio = (radioState as ApiResult.Success).data, radioViewModel = radioViewModel, onBackClick = { navController.popBackStack() }, onUpdateClick = { navController.popBackStack() })
        }
        composable(route = Screen.LiveHosting.route, arguments = listOf(navArgument("radioId") { type = NavType.IntType })) { backStackEntry ->
            val radioId = backStackEntry.arguments?.getInt("radioId") ?: 0
            val radioState by radioViewModel.radioDetails.collectAsState()
            val hostingState by radioViewModel.hostingState.collectAsState()
            DisposableEffect(radioId) { radioViewModel.startRadioPolling(radioId); onDispose { radioViewModel.stopRadioPolling() } }
            if (radioState is ApiResult.Success) LiveHostingScreen(radio = (radioState as ApiResult.Success).data, onBackClick = { navController.popBackStack() }, onStartHosting = { radioViewModel.startHosting(radioId, it) }, onPauseHosting = { radioViewModel.pauseHosting(radioId) }, onResumeHosting = { radioViewModel.resumeHosting(radioId) }, onEndHosting = { radioViewModel.endHosting(radioId) }, isLoading = hostingState is ApiResult.Loading, errorMessage = (hostingState as? ApiResult.Error)?.message)
        }

        // ==================== Issues ====================
        composable(Screen.StudentIssues.route) { StudentIssuesScreen(onBackClick = { navController.popBackStack() }, onIssueClick = { navController.navigate(Screen.StudentIssueDetail.createRoute(it)) }) }
        composable(route = Screen.StudentIssueDetail.route, arguments = listOf(navArgument("issueId") { type = NavType.IntType })) { backStackEntry ->
            val issueId = backStackEntry.arguments?.getInt("issueId") ?: 0
            StudentIssueDetailScreen(issueId = issueId, onBackClick = { navController.popBackStack() })
        }
        composable(Screen.AdminIssues.route) { AdminIssuesScreen(onBackClick = { navController.popBackStack() }, onIssueClick = { navController.navigate(Screen.AdminIssueDetail.createRoute(it)) }) }
        composable(route = Screen.AdminIssueDetail.route, arguments = listOf(navArgument("issueId") { type = NavType.IntType })) { backStackEntry ->
            val issueId = backStackEntry.arguments?.getInt("issueId") ?: 0
            AdminIssueDetailScreen(issueId = issueId, onBackClick = { navController.popBackStack() })
        }
        composable(Screen.PodcastComingSoon.route) { PodcastComingSoonScreen(onBackClick = { navController.popBackStack() }) }
    }
}
