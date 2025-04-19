package com.exa.android.reflekt.loopit.data.remote.authentication.repo

import com.exa.android.reflekt.loopit.util.Response
import com.exa.android.reflekt.loopit.util.model.profileUser
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<Unit>
    suspend fun signUp(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        role: String,
        isStudent: Boolean,
        collegeName: String,
        year: String,
        location: String,
        companyName: String,
        ctc: String,
        experience: String,

        ): Result<Unit>
    fun getCurrentUser(): FirebaseUser?
    suspend fun sendEmailVerification()
    fun logout()
    suspend fun sendPasswordResetEmail(email: String): Result<Unit>
}

class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<Unit> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            if (!result.user?.isEmailVerified!!) {
                auth.signOut()
                Result.failure(Exception("Please verify your email before logging in."))
            } else {
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signUp(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        role: String,
        isStudent: Boolean,
        collegeName: String,
        year: String,
        location: String,
        companyName: String,
        ctc: String,
        experience: String
    ): Result<Unit> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            result.user?.sendEmailVerification()?.await()

            val user = profileUser(
                uid = result.user?.uid ?: "",
                email = email,
                firstName = firstName,
                lastName = lastName,
                role = role,
                isStudent = isStudent,
                collegeName = collegeName,
                year = year,
                location = location,
                companyName = companyName,
                ctc = ctc,
                experience = experience,
                createdAt = Timestamp.now(),
            )

            firestore.collection("profile").document(result.user?.uid ?: "").set(user).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getCurrentUser(): FirebaseUser? = auth.currentUser

    override suspend fun sendEmailVerification() {
        auth.currentUser?.sendEmailVerification()?.await()
    }

    override fun logout() {
        auth.signOut()
    }
    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}