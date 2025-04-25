package com.exa.android.reflekt.loopit.data.remote.authentication.vm

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exa.android.reflekt.loopit.data.remote.authentication.repo.AuthRepository
import com.exa.android.reflekt.loopit.data.remote.main.Repository.FirestoreService
import com.exa.android.reflekt.loopit.data.remote.main.Repository.UserRepository
import com.exa.android.reflekt.loopit.data.remote.main.ViewModel.LocationViewModel
import com.exa.android.reflekt.loopit.data.remote.main.worker.PreferenceHelper
import com.exa.android.reflekt.loopit.util.Response
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.firestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject


@HiltViewModel
class AuthVM @Inject constructor(
    val repository: AuthRepository,
    val auth: FirebaseAuth,
    private val preferenceHelper: PreferenceHelper,
    private val userRepository: UserRepository,
    private val firestoreService: FirestoreService
) : ViewModel() {

    // Login State
    val loginState = mutableStateOf(LoginState())

    // SignUp State
    val signUpState = mutableStateOf(SignUpState())

    // Verification State
    val verificationState = mutableStateOf(VerificationState())

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
                loginState.value = loginState.value.copy(
                    loginSuccess = true,
                    email = currentUser.email ?: ""
                )
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
        }
    }

    fun onSignUpEvent(event: SignUpEvent) {
        when (event) {
            is SignUpEvent.EmailChanged -> signUpState.value =
                signUpState.value.copy(email = event.email)

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
                year = signUpState.value.year,
                location = signUpState.value.location,
                companyName = signUpState.value.companyName,
                ctc = signUpState.value.ctc,
                experience = signUpState.value.experience,
            )
            signUpState.value = signUpState.value.copy(isLoading = false)
//            userRepository.updateUserNameAndImage(signUpState.value.fullName, "")
            when {
                result.isSuccess -> {
//                    Log.d("FireStore Service", "Signup done - ${signUpState.value.fullName}")
//                    withContext(Dispatchers.IO) {
//                        userRepository.updateUserNameAndImage(signUpState.value.fullName, "")
//                        firestoreService.registerFCMToken()
//                    }
                    signUpState.value = signUpState.value.copy(
                        signUpSuccess = true,
                        errorMessage = null
                    )
                }

                result.isFailure -> {
                    signUpState.value = signUpState.value.copy(
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
            preferenceHelper.clearUserId()
            repository.logout()
            loginState.value = LoginState() // Reset login state
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
    val loginSuccess: Boolean = false,
    val errorMessage: String? = null
)

sealed class LoginEvent {
    data class EmailChanged(val email: String) : LoginEvent()
    data class PasswordChanged(val password: String) : LoginEvent()
    object TogglePasswordVisibility : LoginEvent()
    object Submit : LoginEvent()
}

data class SignUpState(
    val email: String = "",
    val password: String = "",
    val fullName: String = "",
    val isStudent: Boolean = true,
    val collegeName: String = "",
    val year: String = "",
    val location: String = "",
    val companyName: String = "",
    val ctc: String = "",
    val experience: String = "",
    val showNext: Boolean = false,
    val isLoading: Boolean = false,
    val signUpSuccess: Boolean = false,
    val errorMessage: String? = null,
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
    data class PasswordChanged(val password: String) : SignUpEvent()
    data class FullNameChanged(val fullName: String) : SignUpEvent()  // Replaced firstName/lastName
    data class RoleChanged(val query: String) : SignUpEvent()
    object ToggleStudentStatus : SignUpEvent()
    data class CollegeNameChanged(val collegeName: String) : SignUpEvent()
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
    val year: String = "",
    val location: String = "",
    val companyName: String = "",
    val experience: String = "",
    val ctc: String = ""
)