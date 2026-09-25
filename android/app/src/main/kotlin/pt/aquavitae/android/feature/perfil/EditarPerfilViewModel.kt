package pt.aquavitae.android.feature.perfil

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pt.aquavitae.android.data.model.Avatar
import pt.aquavitae.android.data.model.Nacionalidade
import pt.aquavitae.android.data.model.UtilizadorUpdateRequest
import pt.aquavitae.android.data.network.toUserMessage
import pt.aquavitae.android.data.repository.LookupRepository
import pt.aquavitae.android.data.repository.UserRepository
import pt.aquavitae.android.ui.components.iniciaisDe
import javax.inject.Inject

private const val MAX_BIO = 1000

/** Estado do ecrã "Editar perfil" (`android/design/perfil/02-editar-perfil.png`). */
sealed interface EditarPerfilUiState {
    data object Loading : EditarPerfilUiState
    data class Error(val message: String) : EditarPerfilUiState
    data class Ready(
        val avatar: Avatar? = null,
        val avatarIniciais: String = "?",
        val firstName: String = "",
        val lastName: String = "",
        val username: String = "",
        val nationalityId: Long? = null,
        val bioDesc: String = "",
        val email: String? = null,
        val nacionalidades: List<Nacionalidade> = emptyList(),
        val mostrarEscolherAvatar: Boolean = false,
        val aGuardar: Boolean = false,
        val erro: String? = null,
    ) : EditarPerfilUiState
}

/**
 * Editar o perfil: nome, apelido, username, nacionalidade, descrição/bio e avatar (popup à parte,
 * `EscolherAvatarSheet`, só confirma localmente — quem grava tudo é o "Guardar" daqui). `PUT /api/users/me` já
 * aceitava `username` no backend (validado, 3–30, único sem distinguir maiúsculas) — só faltava no modelo Android.
 * A nacionalidade atual só vem como nome em `GET /users/me` (não o id): resolve-se por nome na lista de
 * nacionalidades, que é uma lista curada sem nomes repetidos.
 */
@HiltViewModel
class EditarPerfilViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val lookupRepository: LookupRepository,
) : ViewModel() {

    private val _state = MutableStateFlow<EditarPerfilUiState>(EditarPerfilUiState.Loading)
    val state: StateFlow<EditarPerfilUiState> = _state.asStateFlow()

    init {
        carregar()
    }

    fun carregar() {
        _state.value = EditarPerfilUiState.Loading
        viewModelScope.launch {
            userRepository.getMe()
                .onSuccess { utilizador ->
                    val nacionalidades = lookupRepository.nacionalidades().getOrDefault(emptyList())
                    val nationalityId = nacionalidades.firstOrNull { it.nome == utilizador.nationality }?.id
                    _state.value = EditarPerfilUiState.Ready(
                        avatar = utilizador.avatar,
                        avatarIniciais = iniciaisDe(utilizador.firstName, utilizador.lastName, utilizador.username),
                        firstName = utilizador.firstName.orEmpty(),
                        lastName = utilizador.lastName.orEmpty(),
                        username = utilizador.username.orEmpty(),
                        nationalityId = nationalityId,
                        bioDesc = utilizador.bioDesc.orEmpty(),
                        email = utilizador.email,
                        nacionalidades = nacionalidades,
                    )
                }
                .onFailure { _state.value = EditarPerfilUiState.Error(it.toUserMessage()) }
        }
    }

    fun onFirstName(v: String) = atualizarPronto { copy(firstName = v) }
    fun onLastName(v: String) = atualizarPronto { copy(lastName = v) }
    fun onUsername(v: String) = atualizarPronto { copy(username = v.filter { !it.isWhitespace() }) }
    fun onNationality(id: Long) = atualizarPronto { copy(nationalityId = id) }
    fun onBio(v: String) = atualizarPronto { copy(bioDesc = v.take(MAX_BIO)) }

    fun abrirEscolherAvatar() = atualizarPronto { copy(mostrarEscolherAvatar = true) }
    fun fecharEscolherAvatar() = atualizarPronto { copy(mostrarEscolherAvatar = false) }
    fun onAvatarEscolhido(avatar: Avatar) = atualizarPronto {
        copy(avatar = avatar, mostrarEscolherAvatar = false)
    }

    fun guardar(onGuardado: () -> Unit) {
        val atual = _state.value as? EditarPerfilUiState.Ready ?: return
        atualizarPronto { copy(aGuardar = true, erro = null) }
        viewModelScope.launch {
            val request = UtilizadorUpdateRequest(
                firstName = atual.firstName.trim(),
                lastName = atual.lastName.trim(),
                username = atual.username.trim(),
                nationalityId = atual.nationalityId,
                bioDesc = atual.bioDesc.trim(),
                avatarId = atual.avatar?.id,
            )
            userRepository.updatePerfil(request)
                .onSuccess {
                    atualizarPronto { copy(aGuardar = false) }
                    onGuardado()
                }
                .onFailure { atualizarPronto { copy(aGuardar = false, erro = it.toUserMessage()) } }
        }
    }

    private fun atualizarPronto(transform: EditarPerfilUiState.Ready.() -> EditarPerfilUiState.Ready) {
        _state.update { (it as? EditarPerfilUiState.Ready)?.transform() ?: it }
    }
}
