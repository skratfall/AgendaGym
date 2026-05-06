package com.gym.agenda.di.viewmodel



import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gym.agenda.data.model.NotificationEvent
import com.gym.agenda.data.model.User
import com.gym.agenda.data.repository.AuthRepository
import com.gym.agenda.state.AuthUiState
import com.gym.agenda.state.UiState
import com.gym.agenda.utils.NotificationMessages
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _notification = MutableStateFlow<NotificationEvent?>(null)
    val notification: StateFlow<NotificationEvent?> = _notification.asStateFlow()

    val currentUser = authRepository.authState

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            // El estado ya se actualiza automáticamente desde el repository
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            authRepository.login(email, password)
                .onSuccess { user ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        currentUser = user
                    )
                    _notification.value = NotificationEvent.Success(NotificationMessages.LOGIN_SUCCESS)
                }
                .onFailure { error ->
                    val errorMsg = when {
                        error.message?.contains("not found", ignoreCase = true) == true -> NotificationMessages.LOGIN_ERROR
                        error.message?.contains("invalid", ignoreCase = true) == true -> NotificationMessages.LOGIN_ERROR
                        else -> error.message ?: "Error al iniciar sesión"
                    }
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = errorMsg
                    )
                    _notification.value = NotificationEvent.Error(errorMsg)
                }
        }
    }

    fun register(email: String, password: String, name: String, phone: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            authRepository.register(email, password, name, phone)
                .onSuccess { user ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        currentUser = user
                    )
                    _notification.value = NotificationEvent.Success(NotificationMessages.REGISTER_SUCCESS)
                }
                .onFailure { error ->
                    val errorMsg = when {
                        error.message?.contains("already", ignoreCase = true) == true -> NotificationMessages.REGISTER_ERROR
                        error.message?.contains("exists", ignoreCase = true) == true -> NotificationMessages.REGISTER_ERROR
                        else -> error.message ?: "Error al registrar"
                    }
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = errorMsg
                    )
                    _notification.value = NotificationEvent.Error(errorMsg)
                }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _uiState.value = AuthUiState()
            _notification.value = NotificationEvent.Success(NotificationMessages.LOGOUT_SUCCESS)
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun dismissNotification() {
        _notification.value = null
    }
}