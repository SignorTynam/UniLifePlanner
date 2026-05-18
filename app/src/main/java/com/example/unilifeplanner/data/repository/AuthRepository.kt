package com.example.unilifeplanner.data.repository

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    fun isUserLoggedIn(): Boolean = firebaseAuth.currentUser != null

    fun getCurrentUserEmail(): String? = firebaseAuth.currentUser?.email

    suspend fun login(
        email: String,
        password: String
    ): Result<Unit> {
        return try {
            firebaseAuth.signInWithEmailAndPassword(email, password).await()
            Result.success(Unit)
        } catch (exception: Exception) {
            Result.failure(Exception(exception.toReadableAuthMessage()))
        }
    }

    suspend fun register(
        email: String,
        password: String
    ): Result<Unit> {
        return try {
            firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            Result.success(Unit)
        } catch (exception: Exception) {
            Result.failure(Exception(exception.toReadableAuthMessage()))
        }
    }

    fun logout() {
        firebaseAuth.signOut()
    }

    private fun Exception.toReadableAuthMessage(): String {
        return when (this) {
            is FirebaseAuthWeakPasswordException -> "La password deve contenere almeno 6 caratteri."
            is FirebaseAuthUserCollisionException -> "Questa email e gia registrata."
            is FirebaseAuthInvalidUserException -> "Utente non trovato."
            is FirebaseAuthInvalidCredentialsException -> "Email o password non valide."
            is FirebaseNetworkException -> "Rete non disponibile. Controlla la connessione."
            else -> message ?: "Errore di autenticazione. Riprova."
        }
    }
}
