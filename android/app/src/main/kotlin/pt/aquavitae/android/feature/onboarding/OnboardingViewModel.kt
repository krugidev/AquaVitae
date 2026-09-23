package pt.aquavitae.android.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pt.aquavitae.android.data.model.Avatar
import pt.aquavitae.android.data.model.Casta
import pt.aquavitae.android.data.model.LookupItem
import pt.aquavitae.android.data.model.LookupState
import pt.aquavitae.android.data.model.Nacionalidade
import pt.aquavitae.android.data.network.toUserMessage
import pt.aquavitae.android.data.repository.LookupRepository
import pt.aquavitae.android.data.repository.PreferenciaRepository
import pt.aquavitae.android.data.repository.UserRepository
import javax.inject.Inject

/** Tudo o que os 7 ecrãs do onboarding mostram e o que o utilizador já escolheu. */
data class OnboardingUiState(
    val step: OnboardingStep = OnboardingStep.Nome,

    // O que o utilizador escreveu ou escolheu (nulo/vazio = ainda nada, ou ignorou).
    val firstName: String = "",
    val lastName: String = "",
    val nationalityId: Long? = null,
    val bioDesc: String = "",
    /** A pílula de categoria de avatar escolhida; `null` = a primeira. */
    val avatarCategoriaId: Long? = null,
    val avatarId: Long? = null,
    val categoriaIds: Set<Long> = emptySet(),
    val docura: Int? = null,
    val acidez: Int? = null,
    val castaIds: Set<Long> = emptySet(),
    /** Quantas castas a lista mostra (de 10 em 10). */
    val castasVisiveis: Int = CASTAS_POR_PAGINA,

    // As opções, que vêm da API.
    val nacionalidades: LookupState<List<Nacionalidade>> = LookupState.Loading,
    val avatarCategorias: LookupState<List<LookupItem>> = LookupState.Loading,
    val avatares: LookupState<List<Avatar>> = LookupState.Loading,
    val categorias: LookupState<List<LookupItem>> = LookupState.Loading,
    val castas: LookupState<List<Casta>> = LookupState.Loading,

    // O fim do fluxo: guardar o perfil e as preferências.
    val saving: Boolean = false,
    val error: String? = null,
    val done: Boolean = false,
) {
    /** O id da categoria "Vinho" (só se as categorias já chegaram). */
    val vinhoId: Long?
        get() = (categorias as? LookupState.Ready)?.data
            ?.firstOrNull { it.nome.equals(CATEGORIA_VINHO, ignoreCase = true) }?.id

    val escolheuVinho: Boolean get() = vinhoId?.let { it in categoriaIds } == true
}

/**
 * O onboarding, ligado à API real: carrega as opções (nacionalidades, avatares, categorias, castas), guarda o que o
 * utilizador vai escolhendo e, no último ecrã, envia tudo de uma vez — `PUT /api/users/me` (nome, nacionalidade,
 * descrição, avatar) e `PUT /api/users/me/preferencias`. Só se envia o que foi respondido: ignorar não entra nas
 * preferências, e se ignorou tudo não se faz pedido nenhum.
 */
@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val lookupRepository: LookupRepository,
    private val userRepository: UserRepository,
    private val preferenciaRepository: PreferenciaRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(OnboardingUiState())
    val state: StateFlow<OnboardingUiState> = _state.asStateFlow()

    init {
        carregarLookups()
    }

    /** Vai buscar as opções que ainda não chegaram (no arranque e no "TENTAR DE NOVO"). */
    fun carregarLookups() {
        val atual = _state.value
        if (atual.nacionalidades !is LookupState.Ready) {
            load({ lookupRepository.nacionalidades() }) { copy(nacionalidades = it) }
        }
        if (atual.avatarCategorias !is LookupState.Ready) {
            load({ lookupRepository.avatarCategorias() }) { copy(avatarCategorias = it) }
        }
        if (atual.avatares !is LookupState.Ready) {
            load({ lookupRepository.avatares() }) { copy(avatares = it) }
        }
        if (atual.categorias !is LookupState.Ready) {
            load({ lookupRepository.categoriasBebida() }) { copy(categorias = it) }
        }
        if (atual.castas !is LookupState.Ready) {
            load({ lookupRepository.castas() }) { copy(castas = it) }
        }
    }

    // Vai buscar uma lista e põe o estado dela (a carregar, erro ou pronta) no campo que `guardar` indica.
    private fun <T> load(
        fetch: suspend () -> Result<T>,
        guardar: OnboardingUiState.(LookupState<T>) -> OnboardingUiState,
    ) {
        _state.update { it.guardar(LookupState.Loading) }
        viewModelScope.launch {
            val estado = fetch().fold(
                onSuccess = { LookupState.Ready(it) },
                onFailure = { LookupState.Error(it.toUserMessage()) },
            )
            _state.update { it.guardar(estado) }
        }
    }

    // --- O que o utilizador escreve e escolhe ---

    fun onFirstName(value: String) = _state.update { it.copy(firstName = value.take(NOME_MAX)) }

    fun onLastName(value: String) = _state.update { it.copy(lastName = value.take(APELIDO_MAX)) }

    fun onNationality(id: Long) = _state.update { it.copy(nationalityId = id) }

    fun onBio(value: String) = _state.update { it.copy(bioDesc = value.take(DESCRICAO_MAX)) }

    fun onAvatarCategoria(id: Long) = _state.update { it.copy(avatarCategoriaId = id) }

    /** Tocar no avatar escolhido outra vez tira-o (não há "ignorar" neste ecrã). */
    fun onAvatar(id: Long) = _state.update { it.copy(avatarId = if (it.avatarId == id) null else id) }

    fun toggleCategoria(id: Long) = _state.update { it.copy(categoriaIds = it.categoriaIds.alternar(id)) }

    fun setDocura(nivel: Int) = _state.update { it.copy(docura = nivel) }

    fun setAcidez(nivel: Int) = _state.update { it.copy(acidez = nivel) }

    fun toggleCasta(id: Long) = _state.update { it.copy(castaIds = it.castaIds.alternar(id)) }

    /** A lista de castas chegou ao fim das que mostra: mostra mais 10 (as castas já estão todas na app). */
    fun carregarMaisCastas() = _state.update {
        val total = (it.castas as? LookupState.Ready)?.data?.size ?: return@update it
        if (it.castasVisiveis >= total) it else it.copy(castasVisiveis = it.castasVisiveis + CASTAS_POR_PAGINA)
    }

    // --- Navegação entre os ecrãs ---

    /** "PRÓXIMO" e a seta para a frente. No último ecrã guarda tudo e conclui. */
    fun next() {
        val atual = _state.value
        if (atual.saving) return
        val seguinte = nextStep(atual.step, atual.escolheuVinho)
        if (seguinte != null) _state.update { it.copy(step = seguinte, error = null) } else finish()
    }

    /** "IGNORAR": apaga a resposta deste ecrã (não entra nas preferências) e segue. */
    fun skip() {
        _state.update {
            when (it.step) {
                OnboardingStep.Docura -> it.copy(docura = null)
                OnboardingStep.Acidez -> it.copy(acidez = null)
                OnboardingStep.Castas -> it.copy(castaIds = emptySet())
                else -> it
            }
        }
        next()
    }

    /** A seta para trás e o gesto de voltar. Devolve `false` no 1.º ecrã (não há para onde voltar). */
    fun back(): Boolean {
        val atual = _state.value
        if (atual.saving) return true
        val anterior = previousStep(atual.step) ?: return false
        _state.update { it.copy(step = anterior, error = null) }
        return true
    }

    // Envia o perfil e as preferências (só o que foi respondido). Se falhar, o utilizador fica no último ecrã com a mensagem
    // e pode tentar de novo: os dois pedidos podem repetir-se sem efeitos secundários.
    private fun finish() {
        val s = _state.value
        val perfil = buildPerfil(s.firstName, s.lastName, s.nationalityId, s.bioDesc, s.avatarId)
        val preferencias = buildPreferencias(s.categoriaIds, s.docura, s.acidez, s.castaIds, s.vinhoId)
        _state.update { it.copy(saving = true, error = null) }
        viewModelScope.launch {
            val falha = perfil?.let { userRepository.updatePerfil(it).exceptionOrNull() }
                ?: preferencias?.let { preferenciaRepository.updatePreferencias(it).exceptionOrNull() }
            _state.update {
                if (falha == null) it.copy(saving = false, done = true) else it.copy(saving = false, error = falha.toUserMessage())
            }
        }
    }

    private fun Set<Long>.alternar(id: Long): Set<Long> = if (id in this) this - id else this + id
}
