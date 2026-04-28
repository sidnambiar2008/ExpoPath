package org.communityday.navigation.events.data

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.firestore

class AuthRepository {
    private val auth = Firebase.auth

    // Flow to track the current user's state across the app
    val currentUser = auth.authStateChanged

    suspend fun login(email: String, password: String): Result<Unit> {
        return try {
            auth.signInWithEmailAndPassword(email, password)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun logout() {
        auth.signOut()
    }
    suspend fun signUp(email: String, password: String): Result<Unit> {
        return try {
            // 1. Create the user in Firebase Auth
            val authResult = auth.createUserWithEmailAndPassword(email, password)
            val uid = authResult.user?.uid ?: throw Exception("UID not found")

            // 2. Create the blank profile in Firestore
            // This ensures the /users/UID document exists for your rules to check
            Firebase.firestore
                .collection("users")
                .document(uid)
                .set(mapOf("isAdmin" to false)) // Default to false for safety

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            Firebase.auth.sendPasswordResetEmail(email)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}