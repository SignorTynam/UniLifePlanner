package com.example.unilifeplanner.data.repository

import com.example.unilifeplanner.data.datastore.UserProfileDataStore
import com.example.unilifeplanner.domain.model.UserProfile
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.Flow

class UserProfileRepository(
    private val userProfileDataStore: UserProfileDataStore,
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    fun getProfile(): Flow<UserProfile> {
        return userProfileDataStore.getUserProfile(getCurrentUserEmail())
    }

    suspend fun saveProfile(profile: UserProfile) {
        userProfileDataStore.saveUserProfile(
            profile.copy(email = getCurrentUserEmail())
        )
    }

    fun getCurrentUserEmail(): String {
        return firebaseAuth.currentUser?.email.orEmpty()
    }

    fun logout() {
        firebaseAuth.signOut()
    }
}
