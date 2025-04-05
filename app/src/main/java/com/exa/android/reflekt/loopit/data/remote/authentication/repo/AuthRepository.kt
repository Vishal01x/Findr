package com.exa.android.reflekt.loopit.data.remote.authentication.repo

import com.exa.android.reflekt.loopit.util.Response
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class AuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) {
    fun isUserLoggedIn(): Boolean = firebaseAuth.currentUser != null

    fun registerUser(email: String, password: String): Flow<Response<Boolean>> = flow {
        emit(Response.Loading)
        try {
            val result = suspendCancellableCoroutine { continuation ->
                firebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // Resume the coroutine with a successful result
                            continuation.resume(true)
                        } else {
                            // Resume the coroutine with an exception for error handling, from here it will direct to catch block
                            continuation.resumeWithException(
                                task.exception ?: Exception("Unknown Error")
                            )
                        }
                    }
            }
            emit(Response.Success(result))
        } catch (e: Exception) {
            // Emit an error response when an exception is caught
            emit(Response.Error(e.localizedMessage ?: "Error in registration of User"))
        }
    }

    fun loginUser(email : String, password : String) : Flow<Response<Boolean>> = flow {
        emit(Response.Loading)
        try {
            val result = suspendCancellableCoroutine { continuation ->
                firebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // Resume the coroutine with a successful result
                            continuation.resume(true)
                        } else {
                            // Resume the coroutine with an exception for error handling, from here it will direct to catch block
                            continuation.resumeWithException(
                                task.exception ?: Exception("Unknown Error")
                            )
                        }
                    }
            }
            emit(Response.Success(result))
        } catch (e: Exception) {
            // Emit an error response when an exception is caught
            emit(Response.Error(e.localizedMessage ?: "User Login Failed"))
        }
    }

    fun resetPassword(email : String) : Flow<Response<Boolean>> = flow{
        emit(Response.Loading)
        try {
            val result = suspendCancellableCoroutine { continuation ->
                firebaseAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // Resume the coroutine with a successful result
                            continuation.resume(true)
                        } else {
                            // Resume the coroutine with an exception for error handling, from here it will direct to catch block
                            continuation.resumeWithException(
                                task.exception ?: Exception("Unknown Error")
                            )
                        }
                    }
            }
            emit(Response.Success(result))
        } catch (e: Exception) {
            // Emit an error response when an exception is caught
            emit(Response.Error(e.localizedMessage ?: "Reset Password Failed"))
        }
    }
/*
    fun registerFCMToken() {
        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token ->
                Log.d("FCM", "Token: $token")
                updateToken(token) // Store in Firestore
            }
            .addOnFailureListener {
                Log.e("FCM", "Failed to get token", it)
            }
    }

    private fun updateToken(token: String) {
        val userId = auth.currentUser?.uid ?: return
        val userRef = firestore.collection("users").document(userId)

        userRef.update("fcmToken", token)
            .addOnSuccessListener { Log.d("FCM", "Token updated in Firestore") }
            .addOnFailureListener { Log.e("FCM", "Failed to update token", it) }
    }*/

}