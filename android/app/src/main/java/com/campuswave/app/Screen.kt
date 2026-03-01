package com.campuswave.app

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Welcome : Screen("welcome")
    object Login : Screen("login") { // Removed {role} parameter requirement in route string for simplicity, though we might keep it optional or just "login"
        // fun createRoute(role: String) = "login/$role" -> We will simplify this
    }
    object Register : Screen("register/{role}") {
        fun createRoute(role: String) = "register/$role"
    }
    object OtpVerification : Screen("otp_verification/{email}/{purpose}") {
        fun createRoute(email: String, purpose: String = "registration") = "otp_verification/$email/$purpose"
    }
    
    object ForgotPassword : Screen("forgot_password")
    object ResetPassword : Screen("reset_password/{email}/{otp}") {
        fun createRoute(email: String, otp: String) = "reset_password/$email/$otp"
    }
    
    object NewPassword : Screen("new_password/{token}") {
        fun createRoute(token: String) = "new_password/$token"
    }
    
    // Admin Screens
    object AdminDashboard : Screen("admin_dashboard")
    object CreateRadio : Screen("create_radio")
    object RadioSuggestions : Screen("radio_suggestions")
    object EditRadio : Screen("edit_radio/{radioId}") {
        fun createRoute(radioId: Int) = "edit_radio/$radioId"
    }
    object LiveHosting : Screen("live_hosting/{radioId}") {
        fun createRoute(radioId: Int) = "live_hosting/$radioId"
    }
    object InviteAdmin : Screen("invite_admin")
    object PastRadios : Screen("past_radios")
    object ManageMarquee : Screen("manage_marquee")
    object AdminAnalytics : Screen("admin_analytics")
    object Banners : Screen("banners")
    object LivePodcastControl : Screen("live_podcast_control")
    
    // Student Screens
    object StudentDashboard : Screen("student_dashboard")
    object SubmitSuggestion : Screen("submit_suggestion")
    object UserDetails : Screen("user_details")
    object Notifications : Screen("notifications")
    object HelpDesk : Screen("help_desk")
    object AboutCollege : Screen("about_college")
    object ReportIssue : Screen("report_issue")
    object Placements : Screen("placements")
    object UploadPlacement : Screen("upload_placement")
    
    // New Feature Screens
    object Search : Screen("search")
    object Favorites : Screen("favorites")
    object Calendar : Screen("calendar")
    
    // College Updates
    object CollegeUpdates : Screen("college_updates?updateId={updateId}") {
        fun createRoute(updateId: Int? = null) = "college_updates" + (updateId?.let { "?updateId=$it" } ?: "")
    }
    object CreateCollegeUpdate : Screen("create_college_update")
    object AdminPosts : Screen("admin_posts")
    
    // Podcasts
    object PodcastList : Screen("podcast_list")
    object PodcastComingSoon : Screen("podcast_coming_soon")
    
    // Common Screens
    object RadioDetails : Screen("radio_details/{radioId}?autoStart={autoStart}") {
        fun createRoute(radioId: Int, autoStart: Boolean = false) = "radio_details/$radioId?autoStart=$autoStart"
    }
    
    // Issues
    object StudentIssues : Screen("student_issues")
    object StudentIssueDetail : Screen("student_issue_detail/{issueId}") {
        fun createRoute(issueId: Int) = "student_issue_detail/$issueId"
    }
    object AdminIssues : Screen("admin_issues")
    object AdminIssueDetail : Screen("admin_issue_detail/{issueId}") {
        fun createRoute(issueId: Int) = "admin_issue_detail/$issueId"
    }
}
