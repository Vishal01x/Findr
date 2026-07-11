package com.exa.android.reflekt.loopit.data.feature.authentication.vm

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exa.android.reflekt.loopit.data.feature.authentication.repo.AuthRepository
import com.exa.android.reflekt.loopit.data.feature.main.chat.data.repository.ChatRepository
import com.exa.android.reflekt.loopit.data.feature.main.chat.data.repository.UserRepository
import com.exa.android.reflekt.loopit.data.feature.main.map.data.worker.PreferenceHelper
import com.exa.android.reflekt.loopit.core.utils.CurChatManager.activeChatId
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.firestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject


@HiltViewModel
class AuthVM @Inject constructor(
    val repository: AuthRepository,
    val auth: FirebaseAuth,
    private val preferenceHelper: PreferenceHelper,
    private val userRepository: UserRepository,
    private val dataStore: DataStore<Preferences>,
    private val firestoreService: ChatRepository
) : ViewModel() {

    // Login State
    val loginState = mutableStateOf(LoginState())

    // SignUp State
    val signUpState = mutableStateOf(SignUpState())

    // Verification State
    val verificationState = mutableStateOf(VerificationState())

    // Google Profile Setup State
    val googleProfileSetupState = mutableStateOf(GoogleProfileSetupState())

    // Role Suggestions
    val roleSuggestions = mutableStateListOf<String>()

    val forgotPasswordState = mutableStateOf(ForgotPasswordState())

    init {
        checkCurrentUser()
    }

    val currentUser: FirebaseUser?
        get() = auth.currentUser


    private fun checkCurrentUser() {
        viewModelScope.launch {
            val currentUser = repository.getCurrentUser()
            if (currentUser != null && currentUser.isEmailVerified) {
                preferenceHelper.saveUserId(currentUser.uid)

                // Firebase Auth already restores the session locally (no network call), so the
                // only slow part is Firestore's isProfileComplete() lookup. Use the cached
                // SharedPreferences flag from the last check to decide instantly, then refresh
                // it from Firestore in the background instead of blocking the login screen.
                val cachedComplete = preferenceHelper.getProfileComplete(currentUser.uid)
                if (cachedComplete != null) {
                    loginState.value = loginState.value.copy(
                        isCheckingAuth = false,
                        loginSuccess = cachedComplete,
                        needsProfileSetup = !cachedComplete,
                        email = currentUser.email ?: ""
                    )
                    // Silent background refresh - only reacts if the cached value was stale.
                    launch {
                        val freshComplete = repository.isProfileComplete(currentUser.uid)
                        preferenceHelper.saveProfileComplete(currentUser.uid, freshComplete)
                        if (freshComplete != cachedComplete) {
                            loginState.value = loginState.value.copy(
                                loginSuccess = freshComplete,
                                needsProfileSetup = !freshComplete
                            )
                        }
                    }
                } else {
                    // First time checking this user on this device - no cache yet, must wait.
                    val isComplete = repository.isProfileComplete(currentUser.uid)
                    preferenceHelper.saveProfileComplete(currentUser.uid, isComplete)
                    loginState.value = loginState.value.copy(
                        isCheckingAuth = false,
                        loginSuccess = isComplete,
                        needsProfileSetup = !isComplete,
                        email = currentUser.email ?: ""
                    )
                }
            } else {
                loginState.value = loginState.value.copy(isCheckingAuth = false)
            }
        }
    }

    // UI Events
    fun onLoginEvent(event: LoginEvent) {
        when (event) {
            is LoginEvent.EmailChanged -> loginState.value =
                loginState.value.copy(email = event.email)

            is LoginEvent.PasswordChanged -> loginState.value =
                loginState.value.copy(password = event.password)

            is LoginEvent.TogglePasswordVisibility ->
                loginState.value =
                    loginState.value.copy(passwordVisible = !loginState.value.passwordVisible)

            is LoginEvent.Submit -> login()
            is LoginEvent.GoogleSignIn -> signInWithGoogle(event.idToken)
            LoginEvent.ClearError -> {
                loginState.value = loginState.value.copy(errorMessage = null)
            }
        }
    }

    fun onSignUpEvent(event: SignUpEvent) {
        when (event) {
            is SignUpEvent.EmailChanged -> signUpState.value =
                signUpState.value.copy(email = event.email, emailError = null)

            is SignUpEvent.EmailValidationFailed -> signUpState.value =
                signUpState.value.copy(emailError = "Please enter a valid email address")

            is SignUpEvent.ClearMessages -> signUpState.value =
                signUpState.value.copy(errorMessage = null, infoMessage = null)

            is SignUpEvent.PasswordChanged -> signUpState.value =
                signUpState.value.copy(password = event.password)

            is SignUpEvent.FullNameChanged -> signUpState.value =
                signUpState.value.copy(fullName = event.fullName)

            is SignUpEvent.RoleChanged -> {
                signUpState.value = signUpState.value.copy(roleQuery = event.query)
                if (event.query.isNotEmpty()) {
                    fetchRoleSuggestions(event.query)
                } else {
                    roleSuggestions.clear()
                }
            }

            is SignUpEvent.ToggleStudentStatus ->
                signUpState.value = signUpState.value.copy(isStudent = !signUpState.value.isStudent)

            is SignUpEvent.CollegeNameChanged ->
                signUpState.value = signUpState.value.copy(collegeName = event.collegeName)

            is SignUpEvent.BranchChanged ->
                signUpState.value = signUpState.value.copy(branch = event.branch)

            is SignUpEvent.CourseChanged ->
                signUpState.value = signUpState.value.copy(course = event.course)

            is SignUpEvent.YearChanged -> signUpState.value =
                signUpState.value.copy(year = event.year)

            is SignUpEvent.LocationChanged ->
                signUpState.value = signUpState.value.copy(location = event.location)

            is SignUpEvent.CompanyNameChanged ->
                signUpState.value = signUpState.value.copy(companyName = event.companyName)

            is SignUpEvent.CtcChanged -> signUpState.value = signUpState.value.copy(ctc = event.ctc)
            is SignUpEvent.ExperienceChanged ->
                signUpState.value = signUpState.value.copy(experience = event.experience)

            is SignUpEvent.Continue -> {
                signUpState.value = signUpState.value.copy(showNext = true)
            }

            is SignUpEvent.Submit -> signUp()
            is SignUpEvent.SelectRole -> {
                val current = signUpState.value.selectedRoles.toMutableList()
                if (current.contains(event.role)) {
                    current.remove(event.role)
                } else {
                    current.add(event.role)
                }
                signUpState.value = signUpState.value.copy(
                    selectedRoles = current,
                    roleQuery = "" // Clear search after selection
                )
                roleSuggestions.clear()
            }

            is SignUpEvent.TogglePasswordVisibility ->  // Added new event
                signUpState.value = signUpState.value.copy(
                    passwordVisible = !signUpState.value.passwordVisible
                )

            is SignUpEvent.AccountTypeSelected -> {
                if (event.type == "Personal") {
                    // Save professional data before switching
                    signUpState.value = signUpState.value.copy(
                        professionalAccountData = ProfessionalAccountData(
                            isStudent = signUpState.value.isStudent,
                            collegeName = signUpState.value.collegeName,
                            year = signUpState.value.year,
                            location = signUpState.value.location,
                            companyName = signUpState.value.companyName,
                            experience = signUpState.value.experience,
                            ctc = signUpState.value.ctc
                        ),
                        selectedAccountType = event.type,
                        // Restore personal data
                        email = signUpState.value.personalAccountData.email,
                        password = signUpState.value.personalAccountData.password,
                        fullName = signUpState.value.personalAccountData.fullName,
                        selectedRoles = signUpState.value.personalAccountData.selectedRoles,
                        roleQuery = signUpState.value.personalAccountData.roleQuery
                    )
                } else {
                    signUpState.value = signUpState.value.copy(
                        personalAccountData = PersonalAccountData(
                            email = signUpState.value.email,
                            password = signUpState.value.password,
                            fullName = signUpState.value.fullName,
                            selectedRoles = signUpState.value.selectedRoles,
                            roleQuery = signUpState.value.roleQuery
                        ),
                        selectedAccountType = event.type,
                        // Restore professional data
                        isStudent = signUpState.value.professionalAccountData.isStudent,
                        collegeName = signUpState.value.professionalAccountData.collegeName,
                        year = signUpState.value.professionalAccountData.year,
                        location = signUpState.value.professionalAccountData.location,
                        companyName = signUpState.value.professionalAccountData.companyName,
                        experience = signUpState.value.professionalAccountData.experience,
                        ctc = signUpState.value.professionalAccountData.ctc
                    )
                }
            }
        }
        validateForm()
    }

    private fun validateForm() {
        val current = signUpState.value
        val isValid = when (current.selectedAccountType) {
            "Personal" -> current.email.isNotBlank() && current.password.isNotBlank()
            "Professional" -> current.email.isNotBlank() &&
                    current.password.isNotBlank() &&
                    current.selectedRoles.isNotEmpty() // Check for selected roles
            else -> false
        }
        signUpState.value = signUpState.value.copy(isFormValid = isValid)
    }

    private fun login() {
        viewModelScope.launch {
            loginState.value = loginState.value.copy(isLoading = true)
            val result = repository.login(
                loginState.value.email,
                loginState.value.password
            )
            loginState.value = loginState.value.copy(isLoading = false)

            when {
                result.isSuccess -> {
                    firestoreService.registerFCMToken()
                    val userId = repository.getCurrentUser()?.uid ?: ""
                    preferenceHelper.saveUserId(userId)
                    preferenceHelper.saveProfileComplete(userId, true)
                    loginState.value = loginState.value.copy(
                        loginSuccess = true,
                        errorMessage = null
                    )
                }

                result.isFailure -> {
                    loginState.value = loginState.value.copy(
                        errorMessage = result.exceptionOrNull()?.message
                    )
                }
            }
        }
    }

    private fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            loginState.value = loginState.value.copy(isGoogleLoading = true)
            val result = repository.signInWithGoogle(idToken)

            when {
                result.isSuccess -> {
                    val user = result.getOrNull()!!
                    repository.createOrUpdateGoogleUserProfile(user)
                    preferenceHelper.saveUserId(user.uid)
                    val isComplete = repository.isProfileComplete(user.uid)
                    preferenceHelper.saveProfileComplete(user.uid, isComplete)
                    loginState.value = loginState.value.copy(
                        isGoogleLoading = false,
                        loginSuccess = isComplete,
                        needsProfileSetup = !isComplete,
                        errorMessage = null
                    )
                }
                result.isFailure -> {
                    loginState.value = loginState.value.copy(
                        isGoogleLoading = false,
                        errorMessage = result.exceptionOrNull()?.message ?: "Google sign-in failed"
                    )
                }
            }
        }
    }

    fun saveGoogleUserProfile() {
        val uid = auth.currentUser?.uid ?: return
        val s = googleProfileSetupState.value
        viewModelScope.launch {
            googleProfileSetupState.value = s.copy(isLoading = true)
            val result = repository.saveGoogleUserProfile(
                uid = uid,
                isStudent = s.isStudent,
                collegeName = s.collegeName,
                branch = s.branch,
                course = s.course,
                year = s.year,
                location = s.location,
                companyName = s.companyName,
                ctc = s.ctc,
                experience = s.experience,
                selectedRoles = s.selectedRoles
            )
            when {
                result.isSuccess -> {
                    googleProfileSetupState.value = s.copy(isLoading = false)
                    preferenceHelper.saveProfileComplete(uid, true)
                    loginState.value = loginState.value.copy(
                        loginSuccess = true,
                        needsProfileSetup = false
                    )
                }
                result.isFailure -> {
                    googleProfileSetupState.value = s.copy(
                        isLoading = false,
                        errorMessage = result.exceptionOrNull()?.message ?: "Failed to save profile"
                    )
                }
            }
        }
    }

    fun onGoogleProfileSetupEvent(event: GoogleProfileSetupEvent) {
        val s = googleProfileSetupState.value
        googleProfileSetupState.value = when (event) {
            is GoogleProfileSetupEvent.ToggleStudentStatus -> s.copy(isStudent = !s.isStudent)
            is GoogleProfileSetupEvent.CollegeNameChanged -> s.copy(collegeName = event.value)
            is GoogleProfileSetupEvent.BranchChanged -> s.copy(branch = event.value)
            is GoogleProfileSetupEvent.CourseChanged -> s.copy(course = event.value)
            is GoogleProfileSetupEvent.YearChanged -> s.copy(year = event.value)
            is GoogleProfileSetupEvent.LocationChanged -> s.copy(location = event.value)
            is GoogleProfileSetupEvent.CompanyNameChanged -> s.copy(companyName = event.value)
            is GoogleProfileSetupEvent.CtcChanged -> s.copy(ctc = event.value)
            is GoogleProfileSetupEvent.ExperienceChanged -> s.copy(experience = event.value)
            is GoogleProfileSetupEvent.RoleChanged -> {
                s.copy(roleQuery = event.query).also {
                    if (event.query.isNotEmpty()) fetchRoleSuggestions(event.query)
                    else roleSuggestions.clear()
                }
            }
            is GoogleProfileSetupEvent.SelectRole -> {
                val current = s.selectedRoles.toMutableList()
                if (current.contains(event.role)) current.remove(event.role) else current.add(event.role)
                s.copy(selectedRoles = current, roleQuery = "").also { roleSuggestions.clear() }
            }
            is GoogleProfileSetupEvent.Submit -> s.also { saveGoogleUserProfile() }
        }
    }

    private fun signUp() {
        viewModelScope.launch {
            signUpState.value = signUpState.value.copy(isLoading = true)
            // Split full name into first and last names
            val fullName = signUpState.value.fullName.trim()
//            val firstName = fullName.substringBeforeLast(" ").take(50) // Limit length and take first part
//            val lastName = fullName.substringAfterLast(" ", "").take(50) // Take last part, empty if no space
            val result = repository.signUp(
                email = signUpState.value.email,
                password = signUpState.value.password,
                name = fullName,
                role = signUpState.value.selectedRoles.joinToString(", "),
                isStudent = signUpState.value.isStudent,
                collegeName = signUpState.value.collegeName,
                branch = signUpState.value.branch,
                course = signUpState.value.course,
                year = signUpState.value.year,
                location = signUpState.value.location,
                companyName = signUpState.value.companyName,
                ctc = signUpState.value.ctc,
                experience = signUpState.value.experience,
            )
//            userRepository.updateUserNameAndImage(signUpState.value.fullName, "")
            when {
                result.isSuccess -> {
//                    Log.d("FireStore Service", "Signup done - ${signUpState.value.fullName}")
//                    withContext(Dispatchers.IO) {
//                        userRepository.updateUserNameAndImage(signUpState.value.fullName, "")
//                        firestoreService.registerFCMToken()
//                    }
                    val resumedExistingAccount = result.getOrDefault(false)
                    if (resumedExistingAccount) {
                        signUpState.value = signUpState.value.copy(
                            infoMessage = "This email is already registered but unverified. We've sent a new verification link.",
                            errorMessage = null
                        )
                        // Keep isLoading true so the button stays disabled while the snackbar is visible
                        delay(1500)
                    }
                    signUpState.value = signUpState.value.copy(
                        isLoading = false,
                        signUpSuccess = true,
                        errorMessage = null
                    )
                }

                result.isFailure -> {
                    signUpState.value = signUpState.value.copy(
                        isLoading = false,
                        errorMessage = result.exceptionOrNull()?.message
                    )
                }
            }
        }
    }

    private fun fetchRoleSuggestions(query: String) {
        viewModelScope.launch {
            try {
                val snapshot = Firebase.firestore.collection("Role").get().await()
                val roles = snapshot.documents.mapNotNull { it.getString("Name") }
                roleSuggestions.clear()
                roleSuggestions.addAll(roles.filter {
                    it.contains(query, ignoreCase = true)
                })
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun addRole(roleName: String) {
        viewModelScope.launch {
            try {
                val roleData = hashMapOf("Name" to roleName)
                Firebase.firestore.collection("RoleRequest").add(roleData).await()
                // Optionally show a success message or update UI
            } catch (e: Exception) {
                // Handle error (e.g., log or show a toast)
            }
        }
    }


    fun resendVerificationEmail() {
        viewModelScope.launch {
            verificationState.value = verificationState.value.copy(isLoading = true)
            try {
                repository.sendEmailVerification()
                verificationState.value = verificationState.value.copy(
                    message = "Verification email sent! Check your inbox.",
                    isLoading = false
                )
            } catch (e: Exception) {
                verificationState.value = verificationState.value.copy(
                    //errorMessage = e.message,
                    errorMessage = "Verification email resent! Check your inbox.",
                    isLoading = false
                )
            }
        }
    }

    fun checkEmailVerification(navigateToHome: () -> Unit) {
        viewModelScope.launch {
            while (true) {
                repository.getCurrentUser()?.reload()?.await()
                if (repository.getCurrentUser()?.isEmailVerified == true) {
                    navigateToHome()
                    break
                }
                delay(3000)
            }
        }
    }

    fun onForgotPasswordEvent(event: ForgotPasswordEvent) {
        when (event) {
            is ForgotPasswordEvent.EmailChanged ->
                forgotPasswordState.value = forgotPasswordState.value.copy(email = event.email)

            is ForgotPasswordEvent.Submit -> sendPasswordResetEmail()
        }
    }

    private fun sendPasswordResetEmail() {
        viewModelScope.launch {
            forgotPasswordState.value = forgotPasswordState.value.copy(isLoading = true)
            val result = repository.sendPasswordResetEmail(forgotPasswordState.value.email)
            forgotPasswordState.value = forgotPasswordState.value.copy(isLoading = false)

            when {
                result.isSuccess -> {
                    forgotPasswordState.value = forgotPasswordState.value.copy(
                        message = "Password reset email sent. Please check your inbox.",
                        errorMessage = null
                    )
                }

                result.isFailure -> {
                    forgotPasswordState.value = forgotPasswordState.value.copy(
                        errorMessage = result.exceptionOrNull()?.message
                    )
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            val userId = repository.getCurrentUser()?.uid ?: ""

            activeChatId = null
            repository.logout(){
                loginState.value = LoginState() // Reset login state
                // preferenceHelper.clearUserId()
                // dataStore.edit { it.clear() }
            }
        }
    }
}

data class ForgotPasswordState(
    val email: String = "",
    val isLoading: Boolean = false,
    val message: String? = null,
    val errorMessage: String? = null
)

sealed class ForgotPasswordEvent {
    data class EmailChanged(val email: String) : ForgotPasswordEvent()
    object Submit : ForgotPasswordEvent()
}

// State and Event classes
data class LoginState(
    val email: String = "",
    val password: String = "",
    val passwordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val isGoogleLoading: Boolean = false,
    val isCheckingAuth: Boolean = true,
    val loginSuccess: Boolean = false,
    val needsProfileSetup: Boolean = false,
    val errorMessage: String? = null
)

data class GoogleProfileSetupState(
    val isStudent: Boolean = true,
    val collegeName: String = "",
    val branch: String = "",
    val course: String = "",
    val year: String = "",
    val location: String = "",
    val companyName: String = "",
    val ctc: String = "",
    val experience: String = "",
    val roleQuery: String = "",
    val selectedRoles: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

sealed class GoogleProfileSetupEvent {
    object ToggleStudentStatus : GoogleProfileSetupEvent()
    data class CollegeNameChanged(val value: String) : GoogleProfileSetupEvent()
    data class BranchChanged(val value: String) : GoogleProfileSetupEvent()
    data class CourseChanged(val value: String) : GoogleProfileSetupEvent()
    data class YearChanged(val value: String) : GoogleProfileSetupEvent()
    data class LocationChanged(val value: String) : GoogleProfileSetupEvent()
    data class CompanyNameChanged(val value: String) : GoogleProfileSetupEvent()
    data class CtcChanged(val value: String) : GoogleProfileSetupEvent()
    data class ExperienceChanged(val value: String) : GoogleProfileSetupEvent()
    data class RoleChanged(val query: String) : GoogleProfileSetupEvent()
    data class SelectRole(val role: String) : GoogleProfileSetupEvent()
    object Submit : GoogleProfileSetupEvent()
}

sealed class LoginEvent {
    data class EmailChanged(val email: String) : LoginEvent()
    data class PasswordChanged(val password: String) : LoginEvent()
    object TogglePasswordVisibility : LoginEvent()
    object Submit : LoginEvent()
    object ClearError : LoginEvent()
    data class GoogleSignIn(val idToken: String) : LoginEvent()
}

data class SignUpState(
    val email: String = "",
    val emailError: String? = null,
    val password: String = "",
    val fullName: String = "",
    val isStudent: Boolean = true,
    val collegeName: String = "",
    val branch: String = "",
    val course: String = "",
    val year: String = "",
    val location: String = "",
    val companyName: String = "",
    val ctc: String = "",
    val experience: String = "",
    val showNext: Boolean = false,
    val isLoading: Boolean = false,
    val signUpSuccess: Boolean = false,
    val errorMessage: String? = null,
    val infoMessage: String? = null,
    val roleSuggestions: List<String> = emptyList(),
    val passwordVisible: Boolean = false,
    val selectedAccountType: String = "Personal",
    val isFormValid: Boolean = false,
    val roleQuery: String = "",
    val selectedRoles: List<String> = emptyList(),
    val personalAccountData: PersonalAccountData = PersonalAccountData(),
    val professionalAccountData: ProfessionalAccountData = ProfessionalAccountData()
)

sealed class SignUpEvent {
    data class EmailChanged(val email: String) : SignUpEvent()
    object EmailValidationFailed : SignUpEvent()
    object ClearMessages : SignUpEvent()
    data class PasswordChanged(val password: String) : SignUpEvent()
    data class FullNameChanged(val fullName: String) : SignUpEvent()  // Replaced firstName/lastName
    data class RoleChanged(val query: String) : SignUpEvent()
    object ToggleStudentStatus : SignUpEvent()
    data class CollegeNameChanged(val collegeName: String) : SignUpEvent()
    data class BranchChanged(val branch: String) : SignUpEvent()  // NEW
    data class CourseChanged(val course: String) : SignUpEvent()  // NEW
    data class YearChanged(val year: String) : SignUpEvent()
    data class LocationChanged(val location: String) : SignUpEvent()
    data class CompanyNameChanged(val companyName: String) : SignUpEvent()
    data class CtcChanged(val ctc: String) : SignUpEvent()
    data class ExperienceChanged(val experience: String) : SignUpEvent()
    object Continue : SignUpEvent()
    object Submit : SignUpEvent()
    object TogglePasswordVisibility : SignUpEvent()  // Added for password visibility
    data class SelectRole(val role: String) : SignUpEvent()  // Keep this
    data class AccountTypeSelected(val type: String) : SignUpEvent()
}

data class VerificationState(
    val isLoading: Boolean = false,
    val message: String? = null,
    val errorMessage: String? = null
)

data class PersonalAccountData(
    val email: String = "",
    val password: String = "",
    val fullName: String = "",
    val selectedRoles: List<String> = emptyList(),
    val roleQuery: String = ""
)

data class ProfessionalAccountData(
    val isStudent: Boolean = true,
    val collegeName: String = "",
    val branch: String = "",
    val course: String = "",
    val year: String = "",
    val location: String = "",
    val companyName: String = "",
    val experience: String = "",
    val ctc: String = ""
)