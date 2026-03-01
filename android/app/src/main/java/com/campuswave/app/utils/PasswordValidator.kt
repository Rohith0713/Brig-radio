package com.campuswave.app.utils

/**
 * Password strength levels
 */
enum class PasswordStrength {
    WEAK,
    MEDIUM,
    STRONG
}

/**
 * Result of password validation with individual rule checks
 */
data class PasswordValidationResult(
    val hasMinLength: Boolean = false,
    val hasUppercase: Boolean = false,
    val hasLowercase: Boolean = false,
    val hasNumber: Boolean = false,
    val hasSpecialChar: Boolean = false,
    val hasNoSpaces: Boolean = true,
    val notMatchingUserInfo: Boolean = true
) {
    /**
     * Check if all required rules are satisfied
     */
    val isValid: Boolean
        get() = hasMinLength && hasUppercase && hasLowercase && 
                hasNumber && hasSpecialChar && hasNoSpaces && notMatchingUserInfo
    
    /**
     * Calculate password strength based on rules satisfied
     */
    val strength: PasswordStrength
        get() {
            val score = listOf(
                hasMinLength,
                hasUppercase,
                hasLowercase,
                hasNumber,
                hasSpecialChar,
                hasNoSpaces
            ).count { it }
            
            return when {
                score <= 3 -> PasswordStrength.WEAK
                score <= 5 -> PasswordStrength.MEDIUM
                else -> PasswordStrength.STRONG
            }
        }
    
    /**
     * Get list of error messages for failed rules
     */
    val errorMessages: List<String>
        get() = buildList {
            if (!hasMinLength) add("Password must be at least 8 characters long.")
            if (!hasUppercase) add("Include at least one uppercase letter (A-Z).")
            if (!hasLowercase) add("Include at least one lowercase letter (a-z).")
            if (!hasNumber) add("Include at least one number (0-9).")
            if (!hasSpecialChar) add("Include at least one special character (@ # $ % ! & *).")
            if (!hasNoSpaces) add("Password cannot contain spaces.")
            if (!notMatchingUserInfo) add("Password cannot match your name, email, or phone.")
        }
    
    /**
     * Get the first error message, if any
     */
    val firstError: String?
        get() = errorMessages.firstOrNull()
}

/**
 * Password validation utility
 */
object PasswordValidator {
    
    private const val MIN_LENGTH = 8
    private val SPECIAL_CHARS = setOf('@', '#', '$', '%', '!', '&', '*', '^', '(', ')', '-', '_', '+', '=', '[', ']', '{', '}', '|', '\\', '/', '?', '<', '>', ',', '.', '~', '`')
    
    /**
     * Validate a password and return detailed results
     * 
     * @param password The password to validate
     * @param name Optional user's name to check against
     * @param email Optional user's email to check against
     * @param phone Optional user's phone number to check against
     */
    fun validatePassword(
        password: String,
        name: String? = null,
        email: String? = null,
        phone: String? = null
    ): PasswordValidationResult {
        return PasswordValidationResult(
            hasMinLength = password.length >= MIN_LENGTH,
            hasUppercase = password.any { it.isUpperCase() },
            hasLowercase = password.any { it.isLowerCase() },
            hasNumber = password.any { it.isDigit() },
            hasSpecialChar = password.any { it in SPECIAL_CHARS },
            hasNoSpaces = !password.contains(' '),
            notMatchingUserInfo = true // Validation rule disabled as per request
        )
    }
    
    /**
     * Quick check if password is valid (all rules satisfied)
     */
    fun isValid(
        password: String,
        name: String? = null,
        email: String? = null,
        phone: String? = null
    ): Boolean {
        return validatePassword(password, name, email, phone).isValid
    }
    
    /**
     * Get password strength
     */
    fun getStrength(password: String): PasswordStrength {
        return validatePassword(password).strength
    }
    
    /**
     * Check if password contains user identifying info
     */
    private fun containsUserInfo(
        password: String,
        name: String?,
        email: String?,
        phone: String?
    ): Boolean {
        val passwordLower = password.lowercase()
        
        // Check name (if at least 3 chars to avoid false positives)
        if (!name.isNullOrBlank()) {
            val nameParts = name.lowercase().split(" ").filter { it.length >= 3 }
            if (nameParts.any { passwordLower.contains(it) }) {
                return true
            }
        }
        
        // Check email prefix (before @)
        if (!email.isNullOrBlank()) {
            val emailPrefix = email.substringBefore('@').lowercase()
            if (emailPrefix.length >= 3 && passwordLower.contains(emailPrefix)) {
                return true
            }
        }
        
        // Check phone number (if at least 6 digits to avoid false positives)
        if (!phone.isNullOrBlank()) {
            val phoneDigits = phone.filter { it.isDigit() }
            if (phoneDigits.length >= 6 && password.contains(phoneDigits)) {
                return true
            }
        }
        
        return false
    }
}
