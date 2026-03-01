package com.campuswave.app.data.models

import com.google.gson.annotations.SerializedName

/**
 * Issue status enum matching backend states
 */
enum class IssueStatus {
    @SerializedName("open") OPEN,
    @SerializedName("in_discussion") IN_DISCUSSION,
    @SerializedName("resolved") RESOLVED
}

/**
 * Issue data class representing a student-reported issue
 */
data class Issue(
    val id: Int,
    val title: String,
    val description: String,
    @SerializedName("student_id") val studentId: Int,
    @SerializedName("student_name") val studentName: String?,
    @SerializedName("student_email") val studentEmail: String?,
    @SerializedName("student_roll_number") val studentRollNumber: String?,
    val status: IssueStatus,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("resolved_at") val resolvedAt: String?,
    @SerializedName("message_count") val messageCount: Int = 0,
    val messages: List<IssueMessage>? = null
)

/**
 * Issue message data class for chat messages
 */
data class IssueMessage(
    val id: Int,
    @SerializedName("issue_id") val issueId: Int,
    @SerializedName("sender_id") val senderId: Int,
    @SerializedName("sender_name") val senderName: String?,
    @SerializedName("sender_role") val senderRole: String, // "admin" or "student"
    val message: String,
    @SerializedName("created_at") val createdAt: String?
)

/**
 * Request to create a new issue
 */
data class CreateIssueRequest(
    val title: String,
    val description: String
)

/**
 * Request to send a chat message
 */
data class SendIssueMessageRequest(
    val message: String
)

/**
 * Response for issue creation/modification
 */
data class IssueResponse(
    val message: String?,
    val issue: Issue?
)

/**
 * Response for sending a message
 */
data class IssueMessageResponse(
    val message: String?,
    @SerializedName("issue_message") val issueMessage: IssueMessage?,
    @SerializedName("issue_status") val issueStatus: String?
)

/**
 * Statistics for issue dashboard
 */
data class IssueStats(
    @SerializedName("open_issues") val openIssues: Int,
    @SerializedName("in_discussion") val inDiscussion: Int,
    @SerializedName("resolved_issues") val resolvedIssues: Int,
    @SerializedName("total_active") val totalActive: Int
)
