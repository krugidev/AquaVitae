package pt.aquavitae.android.feature.favoritos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pt.aquavitae.android.data.model.Avatar
import pt.aquavitae.android.data.model.BebidaRelacao
import pt.aquavitae.android.data.network.toUserMessage
import pt.aquavitae.android.data.repository.FavoritoRepository
import pt.aquavitae.android.data.repository.UserRepository
import pt.aquavitae.android.ui.components.iniciaisDe
import javax.inject.Inject

/** Estado do ecrã "Os meus favoritos" (`android/design/favoritos-wishlist/02-favoritos.png`). */
sealed interface FavoritosUiState {
    data object Loading : FavoritosUiState
    data class Error(val message: String) : FavoritosUiState
    data class Ready(
        val favoritos: List<BebidaRelacao> = emptyList(),
        val totalProvadas: Int = 0,
        val avatar: Avatar? = null,
        val iniciais: String = "?",
        val categoriaFiltro: String? = null,
        // "Ver as N garrafas favoritas" (pedido do utilizador): só 3 por omissão, evita um ecrã muito comprido.
        val expandido: Boolean = false,
        val aRemoverId: Long? = null,
    ) : FavoritosUiState {
        /** Categorias presentes nos favoritos, por ordem alfabética — só as que o utilizador realmente tem. */
        val categorias: List<String> get() = favoritos.mapNotNull { it.bebida.categoriaNome }.distinct().sorted()

        val filtrados: List<BebidaRelacao>
            get() = if (categoriaFiltro == null) favoritos else favoritos.filter { it.bebida.categoriaNome == categoriaFiltro }

        val visiveis: List<BebidaRelacao> get() = if (expandido) filtrados else filtrados.take(3)

        val temMais: Boolean get() = !expandido && filtrados.size > 3

        val comNotaTua: Int get() = favoritos.count { it.bebida.notaPropria != null }
    }
}

/**
 * Favoritos do utilizador: filtro por categoria (só as presentes na lista), "PROVADA EM"/"Ver a tua review"/"Avaliar"
 * conforme o estado de review de cada bebida, e "Para a cave" (busca o detalhe e abre o `AdicionarACaveSheet`).
 */
@HiltViewModel
class FavoritosViewModel @Inject constructor(
    private val favoritoRepository: FavoritoRepository,
    private val userRepository: UserRepository,
) : ViewModel() {

    private val _state = MutableStateFlow<FavoritosUiState>(FavoritosUiState.Loading)
    val state: StateFlow<FavoritosUiState> = _state.asStateFlow()

    fun carregar() {
        _state.value = FavoritosUiState.Loading
        viewModelScope.launch {
            favoritoRepository.getFavoritos()
                .onSuccess { favoritos ->
                    // "24 provadas" é uma estatística da conta, não só dos favoritos — vem de GET /users/me. Se este
                    // pedido falhar, os favoritos mostram-se na mesma (só sem essa linha), não vale a pena bloquear.
                    val utilizador = userRepository.getMe().getOrNull()
                    _state.value = FavoritosUiState.Ready(
                        favoritos = favoritos,
                        totalProvadas = utilizador?.totalProvadas ?: 0,
                        avatar = utilizador?.avatar,
                        iniciais = iniciaisDe(utilizador?.firstName, utilizador?.lastName, utilizador?.username),
                    )
                }
                .onFailure { _state.value = FavoritosUiState.Error(it.toUserMessage()) }
        }
    }

    fun selecionarCategoria(nome: String?) = atualizarPronto { copy(categoriaFiltro = nome) }

    fun expandir() = atualizarPronto { copy(expandido = true) }

    /** Tira o coração da lista de imediato (otimista); se o pedido falhar, volta a carregar tudo para repor. */
    fun removerFavorito(bebidaId: Long) {
        atualizarPronto { copy(aRemoverId = bebidaId, favoritos = favoritos.filterNot { it.bebida.id == bebidaId }) }
        viewModelScope.launch {
            favoritoRepository.removeFavorito(bebidaId)
                .onSuccess { atualizarPronto { copy(aRemoverId = null) } }
                .onFailure { carregar() }
        }
    }

    private fun atualizarPronto(transform: FavoritosUiState.Ready.() -> FavoritosUiState.Ready) {
        _state.update { (it as? FavoritosUiState.Ready)?.transform() ?: it }
    }
}
