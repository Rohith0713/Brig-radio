package com.campuswave.app.data.auth

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_preferences")

class AuthManager(private val context: Context) {
    
    companion object {
        private val TOKEN_KEY = stringPreferencesKey("auth_token")
        private val USER_ID_KEY = stringPreferencesKey("user_id")
        private val USER_NAME_KEY = stringPreferencesKey("user_name")
        private val USER_EMAIL_KEY = stringPreferencesKey("user_email")
        private val USER_ROLE_KEY = stringPreferencesKey("user_role")
        private val USER_PROFILE_PICTURE_KEY = stringPreferencesKey("user_profile_picture")
        private val USER_COLLEGE_PIN_KEY = stringPreferencesKey("user_college_pin")
        private val USER_DEPARTMENT_KEY = stringPreferencesKey("user_department")
        private val USER_YEAR_KEY = stringPreferencesKey("user_year")
        private val USER_BRANCH_KEY = stringPreferencesKey("user_branch")
    }
    
    val authToken: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[TOKEN_KEY]
    }
    
    val userRole: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_ROLE_KEY]
    }
    
    val userName: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_NAME_KEY]
    }
    
    val userEmail: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_EMAIL_KEY]
    }
    
    val userProfilePicture: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_PROFILE_PICTURE_KEY]
    }
    
    val userCollegePin: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_COLLEGE_PIN_KEY]
    }
    
    val userDepartment: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_DEPARTMENT_KEY]
    }
    
    val userYear: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_YEAR_KEY]
    }
    
    val userBranch: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_BRANCH_KEY]
    }
    
    suspend fun saveAuthData(
        token: String,
        userId: String,
        name: String,
        email: String,
        role: String,
        profilePicture: String? = null,
        collegePin: String? = null,
        department: String? = null,
        year: String? = null,
        branch: String? = null
    ) {
        context.dataStore.edit { preferences ->
            preferences[TOKEN_KEY] = token
            preferences[USER_ID_KEY] = userId
            preferences[USER_NAME_KEY] = name
            preferences[USER_EMAIL_KEY] = email
            preferences[USER_ROLE_KEY] = role
            if (profilePicture != null) preferences[USER_PROFILE_PICTURE_KEY] = profilePicture
            if (collegePin != null) preferences[USER_COLLEGE_PIN_KEY] = collegePin
            if (department != null) preferences[USER_DEPARTMENT_KEY] = department
            if (year != null) preferences[USER_YEAR_KEY] = year
            if (branch != null) preferences[USER_BRANCH_KEY] = branch
        }
    }
    
    suspend fun saveProfilePicture(url: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_PROFILE_PICTURE_KEY] = url
        }
    }
    
    suspend fun getToken(): String? {
        return authToken.first()
    }
    
    suspend fun getUserRole(): String? {
        return userRole.first()
    }
    
    suspend fun isLoggedIn(): Boolean {
        return getToken() != null
    }
    
    suspend fun clearAuthData() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
