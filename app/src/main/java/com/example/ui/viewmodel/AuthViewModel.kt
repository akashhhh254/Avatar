package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AuthMethod {
    PHONE, EMAIL
}

data class AuthUiState(
    val authMethod: AuthMethod = AuthMethod.PHONE,
    val phoneNumber: String = "",
    val email: String = "",
    val otpCode: String = "",
    val isOtpSent: Boolean = false,
    val isTwoFactorStep: Boolean = false,
    val twoFactorPinInput: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val isLoggedIn: Boolean = false
)

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val authRepository = AuthRepository(application)

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    val isTwoFactorEnabled: StateFlow<Boolean> = authRepository.isTwoFactorEnabled
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

    val savedPin: StateFlow<String?> = authRepository.savedPin
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    init {
        viewModelScope.launch {
            authRepository.isLoggedIn.collect { loggedIn ->
                _uiState.value = _uiState.value.copy(isLoggedIn = loggedIn)
            }
        }
    }

    fun setAuthMethod(method: AuthMethod) {
        _uiState.value = _uiState.value.copy(authMethod = method, errorMessage = null)
    }

    fun onPhoneNumberChanged(phone: String) {
        _uiState.value = _uiState.value.copy(phoneNumber = phone, errorMessage = null)
    }

    fun onEmailChanged(email: String) {
        _uiState.value = _uiState.value.copy(email = email, errorMessage = null)
    }

    fun onOtpCodeChanged(otp: String) {
        _uiState.value = _uiState.value.copy(otpCode = otp, errorMessage = null)
    }

    fun on2FactorPinChanged(pin: String) {
        _uiState.value = _uiState.value.copy(twoFactorPinInput = pin, errorMessage = null)
    }

    fun requestOtp() {
        val state = _uiState.value
        if (state.authMethod == AuthMethod.PHONE) {
            if (state.phoneNumber.trim().length < 8) {
                _uiState.value = state.copy(errorMessage = "Please enter a valid phone number")
                return
            }
        } else {
            if (!state.email.contains("@") || !state.email.contains(".")) {
                _uiState.value = state.copy(errorMessage = "Please enter a valid email address")
                return
            }
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            kotlinx.coroutines.delay(1000) // Simulate secure SMS/Email Gateway dispatch
            val target = if (state.authMethod == AuthMethod.PHONE) state.phoneNumber else state.email
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                isOtpSent = true,
                otpCode = "123456", // Auto fill demo code for seamless testing
                successMessage = "Verification OTP code sent to $target"
            )
        }
    }

    fun verifyOtp() {
        val state = _uiState.value
        val otp = state.otpCode
        if (otp.length != 6) {
            _uiState.value = state.copy(errorMessage = "OTP must be 6 digits")
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true)
            val isEmail = state.authMethod == AuthMethod.EMAIL
            val identifier = if (isEmail) state.email else state.phoneNumber

            val is2FAActive = isTwoFactorEnabled.value
            val currentSavedPin = savedPin.value

            if (is2FAActive && !currentSavedPin.isNullOrBlank()) {
                // Requires 2FA PIN entry step
                _uiState.value = state.copy(
                    isLoading = false,
                    isTwoFactorStep = true,
                    successMessage = "OTP Verified! Enter your 2-Step Verification PIN to complete sign-in."
                )
            } else {
                val success = authRepository.verifyOtpAndLogin(identifier, isEmail, otp)
                if (success) {
                    _uiState.value = state.copy(isLoading = false, isLoggedIn = true)
                } else {
                    _uiState.value = state.copy(isLoading = false, errorMessage = "Invalid verification code")
                }
            }
        }
    }

    fun verify2FactorPin() {
        val state = _uiState.value
        val pinInput = state.twoFactorPinInput
        val expectedPin = savedPin.value

        if (pinInput != expectedPin) {
            _uiState.value = state.copy(errorMessage = "Incorrect 2-Step Verification PIN")
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true)
            val isEmail = state.authMethod == AuthMethod.EMAIL
            val identifier = if (isEmail) state.email else state.phoneNumber
            authRepository.verifyOtpAndLogin(identifier, isEmail, state.otpCode)
            _uiState.value = state.copy(isLoading = false, isLoggedIn = true)
        }
    }

    fun toggleTwoFactorAuth(enable: Boolean, pin: String) {
        viewModelScope.launch {
            authRepository.setTwoFactorAuth(enable, pin)
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _uiState.value = AuthUiState()
        }
    }
}

