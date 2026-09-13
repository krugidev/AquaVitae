package pt.aquavitae.android.feature.auth

/** Estado da UI de autenticação (login e registo partilham o mesmo ViewModel/estado). */
sealed interface AuthUiState {
    data object Idle : AuthUiState
    data object Loading : AuthUiState
    data class Success(val username: String) : AuthUiState
    data class Error(val message: String) : AuthUiState
}
