package com.exa.android.reflekt.loopit.data.feature.authentication.repo

import android.util.Log
import com.exa.android.reflekt.loopit.data.feature.main.chat.data.repository.ChatRepository
import com.exa.android.reflekt.loopit.data.feature.main.profile.domain.model.CollegeInfo
import com.exa.android.reflekt.loopit.data.feature.main.profile.domain.model.ExperienceInfo
import com.exa.android.reflekt.loopit.data.feature.main.profile.domain.model.ProfileData
import com.exa.android.reflekt.loopit.data.feature.main.profile.domain.model.ProfileHeaderData
import com.exa.android.reflekt.loopit.data.feature.main.profile.domain.model.SocialLinks
import com.exa.android.reflekt.loopit.core.env.AppCollections
import com.exa.android.reflekt.loopit.core.env.EnvironmentManager
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<Unit>
    suspend fun signInWithGoogle(idToken: String): Result<FirebaseUser>
    /** Returns Result.success(true) if an existing unverified account was resumed instead of creating a new one. */
    suspend fun signUp(
        email: String,
        password: String,
        name: String,
        role: String,
        isStudent: Boolean,
        collegeName: String,
        branch: String,
        course: String,
        year: String,
        location: String,
        companyName: String,
        ctc: String,
        experience: String,
    ): Result<Boolean>
    fun getCurrentUser(): FirebaseUser?
    suspend fun sendEmailVerification()
    fun logout(onSuccess: () -> Unit)
    suspend fun sendPasswordResetEmail(email: String): Result<Unit>
    suspend fun createOrUpdateGoogleUserProfile(user: FirebaseUser): Result<Unit>
    suspend fun isProfileComplete(uid: String): Boolean
    suspend fun saveGoogleUserProfile(
        uid: String,
        isStudent: Boolean,
        collegeName: String,
        branch: String,
        course: String,
        year: String,
        location: String,
        companyName: String,
        ctc: String,
        experience: String,
        selectedRoles: List<String>
    ): Result<Unit>
}

class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val firestoreService: ChatRepository
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<Unit> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            // Staging skips email verification to allow quick test account logins
            val verified = EnvironmentManager.isStaging || result.user?.isEmailVerified == true
            if (!verified) {
                auth.signOut()
                Result.failure(Exception("Please verify your email before logging in."))
            } else {
                firestoreService.registerFCMToken()
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signInWithGoogle(idToken: String): Result<FirebaseUser> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = auth.signInWithCredential(credential).await()
            val user = result.user
                ?: return Result.failure(Exception("Google sign-in failed: user is null"))
            firestoreService.registerFCMToken()
            Result.success(user)
        } catch (e: Exception) {
            Log.e("AuthRepository", "Google sign-in failed: ${e.localizedMessage}", e)
            Result.failure(e)
        }
    }

    override suspend fun createOrUpdateGoogleUserProfile(user: FirebaseUser): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val uid = user.uid
            val userDocRef = firestore.collection(AppCollections.USERS).document(uid)
            val snapshot = userDocRef.get().await()
            
            if (!snapshot.exists()) {
                // Create new profile for Google user
                val profileHeader = ProfileHeaderData(
                    uid = uid,
                    email = user.email ?: "",
                    name = user.displayName ?: "",
                    profileImageUrl = user.photoUrl?.toString() ?: "",
                    createdAt = Timestamp.now(),
                    socialLinks = SocialLinks(email = user.email)
                )
                
                val profile = ProfileData(profileHeader = profileHeader)
                val newUser = mapOf(
                    "profileData" to profile,
                    "name" to (user.displayName ?: ""),
                    "profilePicture" to (user.photoUrl?.toString() ?: "")
                )
                userDocRef.set(newUser, SetOptions.merge()).await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("AuthRepository", "Failed to create Google user profile: ${e.localizedMessage}", e)
            Result.failure(e)
        }
    }

    override suspend fun isProfileComplete(uid: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val snapshot = firestore.collection(AppCollections.USERS).document(uid).get().await()
            // Explicit flag set by saveGoogleUserProfile or signUp
            if (snapshot.getBoolean("profileComplete") == true) return@withContext true
            // Fallback: existing email-registered users have collegeName or companyName in profileHeader
            val header = snapshot.get("profileData.profileHeader") as? Map<*, *>
            val collegeName = header?.get("collegeName") as? String ?: ""
            val companyName = header?.get("companyName") as? String ?: ""
            collegeName.isNotBlank() || companyName.isNotBlank()
        } catch (e: Exception) {
            // On error, assume complete to avoid blocking users
            true
        }
    }

    override suspend fun saveGoogleUserProfile(
        uid: String,
        isStudent: Boolean,
        collegeName: String,
        branch: String,
        course: String,
        year: String,
        location: String,
        companyName: String,
        ctc: String,
        experience: String,
        selectedRoles: List<String>
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val userDocRef = firestore.collection(AppCollections.USERS).document(uid)
            val snapshot = userDocRef.get().await()
            val existingHeader = snapshot.get("profileData.profileHeader") as? Map<*, *>

            val profileHeader = ProfileHeaderData(
                uid = uid,
                email = existingHeader?.get("email") as? String ?: "",
                name = existingHeader?.get("name") as? String ?: "",
                profileImageUrl = existingHeader?.get("profileImageUrl") as? String ?: "",
                role = selectedRoles.joinToString(", "),
                isStudent = isStudent,
                collegeName = collegeName,
                branch = branch,
                course = course,
                year = year,
                location = location,
                companyName = companyName,
                ctc = ctc,
                experience = experience,
                createdAt = existingHeader?.get("createdAt") as? com.google.firebase.Timestamp ?: com.google.firebase.Timestamp.now(),
                socialLinks = SocialLinks(email = existingHeader?.get("email") as? String)
            )

            val collegeInfo = CollegeInfo(
                instituteName = collegeName,
                branch = branch,
                course = course,
                year = year
            )

            val experienceInfo = ExperienceInfo(
                companyName = companyName,
                experience = experience,
                ctc = ctc
            )

            val updates = mapOf(
                "profileData.profileHeader" to profileHeader,
                "profileData.collegeInfo" to collegeInfo,
                "profileData.experienceInfo" to experienceInfo,
                "profileComplete" to true
            )
            userDocRef.update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("AuthRepository", "Failed to save Google user profile: ${e.localizedMessage}", e)
            Result.failure(e)
        }
    }

    /*override suspend fun signUp(
        email: String,
        password: String,
        name : String,
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
            val userDocRef = firestore.collection(AppCollections.USERS).document(result.user?.uid ?: "")
            val profileHeader = ProfileHeaderData(
                uid = result.user?.uid ?: "",
                email = email,
                name = name,
                role = role,
                isStudent = isStudent,
                collegeName = collegeName,
                year = year,
                location = location,
                companyName = companyName,
                ctc = ctc,
                experience = experience,
                createdAt = Timestamp.now(),
                socialLinks = SocialLinks(email = email)
            )

            val collegeInfo = CollegeInfo(
                instituteName = collegeName,
                year = year
            )

            val experienceInfo = ExperienceInfo(
                companyName = companyName,
                experience = experience,
                ctc = ctc
            )

            val profile = ProfileData(
                profileHeader = profileHeader,
                collegeInfo = collegeInfo,
                experienceInfo = experienceInfo
            )

            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(userDocRef)

                // Check if document exists
                if (!snapshot.exists()) {
                    // Create document with profileHeader
                    val newUser = mapOf(
                        "profileData" to profile
                    )
                    val updates = mapOf(
                        "name" to name,
                        "profilePicture" to ""
                    )
                    transaction.set(userDocRef, newUser, SetOptions.merge())

                    //update name and image in additional field you can also call fcm register here
                    transaction.set(userDocRef, updates, SetOptions.merge())
                } else {
                    // Document exists, just update the profileHeader
                    transaction.update(userDocRef, "profileData.profileHeader", profileHeader)
                    transaction.update(userDocRef, "name", name)
                }
            }
            firestoreService.registerFCMToken()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.d("FireStore Service", "User Sign Up failed - ${e.localizedMessage}")
            Result.failure(e)
        }
    }*/


    private fun buildProfileData(
        uid: String,
        email: String,
        name: String,
        role: String,
        isStudent: Boolean,
        collegeName: String,
        branch: String,
        course: String,
        year: String,
        location: String,
        companyName: String,
        ctc: String,
        experience: String,
        createdAt: Timestamp
    ): ProfileData {
        val profileHeader = ProfileHeaderData(
            uid = uid,
            email = email,
            name = name,
            role = role,
            isStudent = isStudent,
            collegeName = collegeName,
            branch = branch,
            course = course,
            year = year,
            location = location,
            companyName = companyName,
            ctc = ctc,
            experience = experience,
            createdAt = createdAt,
            socialLinks = SocialLinks(email = email)
        )
        val collegeInfo = CollegeInfo(
            instituteName = collegeName,
            branch = branch,
            course = course,
            year = year
        )
        val experienceInfo = ExperienceInfo(
            companyName = companyName,
            experience = experience,
            ctc = ctc
        )
        return ProfileData(
            profileHeader = profileHeader,
            collegeInfo = collegeInfo,
            experienceInfo = experienceInfo
        )
    }

    // Reached when signup collides with an existing, unverified account for the same email.
    // Resumes that account instead of failing, so the user isn't stuck unable to re-register.
    private suspend fun resumeUnverifiedSignUp(
        email: String,
        password: String,
        name: String,
        role: String,
        isStudent: Boolean,
        collegeName: String,
        branch: String,
        course: String,
        year: String,
        location: String,
        companyName: String,
        ctc: String,
        experience: String
    ): Result<Boolean> {
        return try {
            val signInResult = auth.signInWithEmailAndPassword(email, password).await()
            val user = signInResult.user
                ?: return Result.failure(Exception("Sign-in failed: user is null"))

            if (user.isEmailVerified) {
                auth.signOut()
                return Result.failure(Exception("This email is already registered and verified. Please log in instead."))
            }

            val uid = user.uid
            val userDocRef = firestore.collection(AppCollections.USERS).document(uid)
            val existingHeader = userDocRef.get().await().get("profileData.profileHeader") as? Map<*, *>
            val createdAt = existingHeader?.get("createdAt") as? Timestamp ?: Timestamp.now()

            val profile = buildProfileData(
                uid, email, name, role, isStudent, collegeName, branch, course,
                year, location, companyName, ctc, experience, createdAt
            )
            userDocRef.set(
                mapOf(
                    "profileData" to profile,
                    "name" to name,
                    "profilePicture" to ""
                ),
                SetOptions.merge()
            ).await()

            user.sendEmailVerification().await()
            Result.success(true)
        } catch (e: Exception) {
            Log.e("AuthRepository", "Resume unverified sign-up failed: ${e.localizedMessage}", e)
            Result.failure(Exception(
                "This email is already registered but not verified. Use the password from your " +
                    "first sign-up attempt, then verify your email. Forgot it? Reset it from the " +
                    "login screen, or change it later in Settings."
            ))
        }
    }

    override suspend fun signUp(
        email: String,
        password: String,
        name: String,
        role: String,
        isStudent: Boolean,
        collegeName: String,
        branch: String,
        course: String,
        year: String,
        location: String,
        companyName: String,
        ctc: String,
        experience: String
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val authResult = try {
                auth.createUserWithEmailAndPassword(email, password).await()
            } catch (e: FirebaseAuthUserCollisionException) {
                return@withContext resumeUnverifiedSignUp(
                    email, password, name, role, isStudent, collegeName, branch, course,
                    year, location, companyName, ctc, experience
                )
            }
            val firebaseUser = authResult.user
                ?: return@withContext Result.failure(Exception("User creation failed: user is null"))

            firebaseUser.sendEmailVerification().await()

            val uid = firebaseUser.uid
            val userDocRef = firestore.collection(AppCollections.USERS).document(uid)
            val profile = buildProfileData(
                uid, email, name, role, isStudent, collegeName, branch, course,
                year, location, companyName, ctc, experience, Timestamp.now()
            )

            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(userDocRef)

                if (!snapshot.exists()) {
                    val newUser = mapOf("profileData" to profile)
                    val updates = mapOf("name" to name, "profilePicture" to "")
                    transaction.set(userDocRef, newUser, SetOptions.merge())
                    transaction.set(userDocRef, updates, SetOptions.merge())
                    //transaction.set( userDocRef,"lastNotifiedAt" to FieldValue.serverTimestamp())
                    firestoreService.registerFCMToken()
                } else {
                    transaction.update(userDocRef, "profileData.profileHeader", profile.profileHeader)
                    transaction.update(userDocRef, "name", name)

                    firestoreService.registerFCMToken()
                }
            }.await() // Important to wait on transaction completion
            Result.success(false)
        } catch (e: Exception) {
            Log.e("FirestoreService", "Sign up failed: ${e.localizedMessage}", e)
            Result.failure(e)
        }
    }


    override fun getCurrentUser(): FirebaseUser? = auth.currentUser

    override suspend fun sendEmailVerification() {
        auth.currentUser?.sendEmailVerification()?.await()
    }

    override fun logout(onSuccess: () -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
               // FirebaseMessaging.getInstance().deleteToken()
                firestoreService.updateToken("")
                auth.signOut()
                onSuccess()
            }catch (e : Exception){

            }

        }
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