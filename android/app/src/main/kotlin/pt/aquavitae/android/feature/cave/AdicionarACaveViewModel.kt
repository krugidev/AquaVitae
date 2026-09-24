package pt.aquavitae.android.feature.cave

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pt.aquavitae.android.data.model.CaveResponse
import pt.aquavitae.android.data.network.toUserMessage
import pt.aquavitae.android.data.repository.CaveRepository
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

/** O popup "Adicionar à cave" (`android/design/caves/04-adicionar-a-cave-popup.png`). */
sealed interface AdicionarACaveUiState {
    data object Loading : AdicionarACaveUiState
    data class Error(val message: String) : AdicionarACaveUiState
    data class Ready(
        val caves: List<CaveResponse> = emptyList(),
        val caveSelecionadaId: Long? = null,
        val quantidade: Int = 1,
        val precoPago: String = "",
        val dataAquisicao: LocalDate = LocalDate.now(),
        val janelaInicioAno: String = "",
        val janelaFimAno: String = "",
        val notas: String = "",
        val aGuardar: Boolean = false,
        val erro: String? = null,
        val mostrarNovaCave: Boolean = false,
        val aCriarCave: Boolean = false,
        val erroNovaCave: String? = null,
        val guardado: Boolean = false,
        // Pedido do utilizador (2026-09-23): tocar em "Guardar" com uma cave que já tem esta bebida (✓) pede
        // confirmação antes de duplicar — em vez de bloquear (pode ser mesmo isso que se quer, juntar mais garrafas).
        val mostrarConfirmarDuplicado: Boolean = false,
    ) : AdicionarACaveUiState {
        val caveSelecionadaJaTemBebida: Boolean get() = caves.firstOrNull { it.id == caveSelecionadaId }?.temBebida == true
    }
}

@HiltViewModel
class AdicionarACaveViewModel @Inject constructor(
    private val caveRepository: CaveRepository,
) : ViewModel() {

    private val _state = MutableStateFlow<AdicionarACaveUiState>(AdicionarACaveUiState.Loading)
    val state: StateFlow<AdicionarACaveUiState> = _state.asStateFlow()

    private var bebidaId: Long = 0

    // O preço que já vem escrito em "Preço pago" (ex.: o do link em que o utilizador clicou, no "Compraste?"); em branco por omissão.
    private var precoInicial: String = ""

    /**
     * Chamado uma vez ao abrir (`LaunchedEffect` no ecrã) — ver `BebidaDetalheViewModel` para o mesmo padrão.
     * `precoSugerido` preenche o "Preço pago" (o utilizador pode alterá-lo): só o inquérito "Compraste?" o passa.
     */
    fun carregar(bebidaId: Long, precoSugerido: Double? = null) {
        this.bebidaId = bebidaId
        precoInicial = precoSugerido?.let { String.format(java.util.Locale.US, "%.2f", it).replace('.', ',') }.orEmpty()
        _state.value = AdicionarACaveUiState.Loading
        recarregarCaves()
    }

    private fun recarregarCaves(selecionarId: Long? = null) {
        viewModelScope.launch {
            caveRepository.getCaves(bebidaId)
                .onSuccess { caves ->
                    val atual = _state.value as? AdicionarACaveUiState.Ready
                    val novaSelecao = selecionarId ?: atual?.caveSelecionadaId ?: caves.firstOrNull()?.id
                    _state.value = (atual ?: AdicionarACaveUiState.Ready(precoPago = precoInicial)).copy(
                        caves = caves,
                        caveSelecionadaId = novaSelecao,
                        mostrarNovaCave = false,
                        aCriarCave = false,
                    )
                }
                .onFailure { _state.value = AdicionarACaveUiState.Error(it.toUserMessage()) }
        }
    }

    fun selecionarCave(id: Long) = atualizarPronto { copy(caveSelecionadaId = id) }

    fun onQuantidadeMais() = atualizarPronto { copy(quantidade = (quantidade + 1).coerceAtMost(99)) }

    fun onQuantidadeMenos() = atualizarPronto { copy(quantidade = (quantidade - 1).coerceAtLeast(1)) }

    fun onPrecoPago(valor: String) = atualizarPronto { copy(precoPago = valor.filter { it.isDigit() || it == ',' || it == '.' }) }

    fun onDataAquisicao(data: LocalDate) = atualizarPronto { copy(dataAquisicao = data) }

    fun onJanelaInicioAno(texto: String) = atualizarPronto { copy(janelaInicioAno = texto.filter { it.isDigit() }.take(4)) }

    fun onJanelaFimAno(texto: String) = atualizarPronto { copy(janelaFimAno = texto.filter { it.isDigit() }.take(4)) }

    fun onNotas(texto: String) = atualizarPronto { copy(notas = texto.take(500)) }

    fun abrirNovaCave() = atualizarPronto { copy(mostrarNovaCave = true, erroNovaCave = null) }

    fun fecharNovaCave() = atualizarPronto { copy(mostrarNovaCave = false) }

    fun criarCave(nome: String, descricao: String?) {
        if (nome.isBlank()) return
        atualizarPronto { copy(aCriarCave = true, erroNovaCave = null) }
        viewModelScope.launch {
            caveRepository.createCave(nome.trim(), descricao?.trim()?.ifBlank { null })
                .onSuccess { nova -> recarregarCaves(selecionarId = nova.id) }
                .onFailure { atualizarPronto { copy(aCriarCave = false, erroNovaCave = it.toUserMessage()) } }
        }
    }

    /** Ponto de entrada do botão "Guardar na cave": se a cave escolhida já tem esta bebida, pede confirmação primeiro. */
    fun guardar() {
        val atual = _state.value as? AdicionarACaveUiState.Ready ?: return
        if (atual.caveSelecionadaJaTemBebida) {
            atualizarPronto { copy(mostrarConfirmarDuplicado = true) }
            return
        }
        guardarSemPerguntar()
    }

    fun cancelarDuplicado() = atualizarPronto { copy(mostrarConfirmarDuplicado = false) }

    fun confirmarDuplicado() {
        atualizarPronto { copy(mostrarConfirmarDuplicado = false) }
        guardarSemPerguntar()
    }

    private fun guardarSemPerguntar() {
        val atual = _state.value as? AdicionarACaveUiState.Ready ?: return
        val caveId = atual.caveSelecionadaId ?: return
        atualizarPronto { copy(aGuardar = true, erro = null) }
        viewModelScope.launch {
            val preco = atual.precoPago.replace(',', '.').toDoubleOrNull()
            val janelaInicio = atual.janelaInicioAno.toIntOrNull()?.let { "$it-01-01" }
            val janelaFim = atual.janelaFimAno.toIntOrNull()?.let { "$it-12-31" }
            caveRepository.addBebidaToCave(
                caveId = caveId,
                bebidaId = bebidaId,
                quantidade = atual.quantidade,
                precoPago = preco,
                dataAquisicao = atual.dataAquisicao.format(DateTimeFormatter.ISO_LOCAL_DATE),
                janelaInicio = janelaInicio,
                janelaFim = janelaFim,
                notas = atual.notas.trim().ifBlank { null },
            ).onSuccess { atualizarPronto { copy(aGuardar = false, guardado = true) } }
                .onFailure { atualizarPronto { copy(aGuardar = false, erro = it.toUserMessage()) } }
        }
    }

    private fun atualizarPronto(transform: AdicionarACaveUiState.Ready.() -> AdicionarACaveUiState.Ready) {
        _state.update { (it as? AdicionarACaveUiState.Ready)?.transform() ?: it }
    }
}
