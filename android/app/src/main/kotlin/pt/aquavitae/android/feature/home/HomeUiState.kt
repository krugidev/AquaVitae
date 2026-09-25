package pt.aquavitae.android.feature.home

import pt.aquavitae.android.data.model.BebidaSummary
import pt.aquavitae.android.data.model.CaveBebidaResponse
import pt.aquavitae.android.data.model.CaveResponse
import pt.aquavitae.android.data.model.LookupItem
import pt.aquavitae.android.data.model.ProdutorDetail
import pt.aquavitae.android.data.model.UtilizadorMe

/**
 * Tudo o que a homepage mostra. Ao contrário dos outros ecrãs, não há um único "erro" que bloqueia o ecrã inteiro: se
 * `utilizador` não carregar (a saudação, o avatar, as estatísticas), mostra-se o erro com "TENTAR DE NOVO"; as restantes
 * secções (sugestões, caves, produtor em destaque) falham em silêncio para não vazio — cada uma fica só sem conteúdo, com
 * o resto do ecrã na mesma a funcionar.
 */
sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Error(val message: String) : HomeUiState

    data class Ready(
        val utilizador: UtilizadorMe,
        val categorias: List<LookupItem> = emptyList(),
        val categoriaSelecionadaId: Long? = null,
        val sugestoesLoading: Boolean = false,
        val sugestoes: List<BebidaSummary> = emptyList(),
        val caves: List<CaveResponse> = emptyList(),
        val caveSelecionadaId: Long? = null,
        val garrafasCaveSelecionada: List<CaveBebidaResponse> = emptyList(),
        val produtorDestaque: ProdutorDetail? = null,
    ) : HomeUiState {
        val caveSelecionada: CaveResponse? get() = caves.firstOrNull { it.id == caveSelecionadaId }
    }
}
