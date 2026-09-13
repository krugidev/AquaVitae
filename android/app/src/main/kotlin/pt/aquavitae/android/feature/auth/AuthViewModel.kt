package pt.aquavitae.android.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pt.aquavitae.android.data.repository.AuthRepository
import javax.inject.Inject

/**
 * ViewModel de autenticação (login + registo). Este é o primeiro ecrã do fluxo
 * MVP e serve de exemplo do padrão a seguir nos restantes: estado exposto via
 * [StateFlow] de uma sealed interface ([AuthUiState]), chamadas suspend ao
 * repositório dentro de [viewModelScope], sem lógica de rede na UI.
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = AuthUiState.Error("Preenche email e password.")
            return
        }
        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            authRepository.login(email, password)
                .onSuccess { _uiState.value = AuthUiState.Success(it.username) }
                .onFailure { _uiState.value = AuthUiState.Error(it.message ?: "Não foi possível iniciar sessão.") }
        }
    }

    fun register(
        username: String,
        email: String,
        password: String,
        firstName: String,
        lastName: String,
    ) {
        if (username.isBlank() || email.isBlank() || password.isBlank()) {
            _uiState.value = AuthUiState.Error("Preenche pelo menos username, email e password.")
            return
        }
        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            authRepository.register(username, email, password, firstName, lastName)
                .onSuccess { _uiState.value = AuthUiState.Success(it.username) }
                .onFailure { _uiState.value = AuthUiState.Error(it.message ?: "Não foi possível criar a conta.") }
        }
    }

    /** Repõe o estado para [AuthUiState.Idle], ex.: depois de navegar para longe do ecrã de erro. */
    fun resetState() {
        _uiState.value = AuthUiState.Idle
    }
}
