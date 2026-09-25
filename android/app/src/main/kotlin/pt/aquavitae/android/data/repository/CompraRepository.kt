package pt.aquavitae.android.data.repository

import pt.aquavitae.android.data.model.CliquePendente
import pt.aquavitae.android.data.model.OfertaCompra
import pt.aquavitae.android.data.model.RespostaCliqueRequest
import pt.aquavitae.android.data.network.AquaVitaeApi
import javax.inject.Inject

/**
 * Comprar: as ofertas de uma bebida, o registo do clique num link de afiliado e o inquérito "Compraste?" ao voltar do
 * browser. **Nunca se abre um link de afiliado sem o utilizador tocar** — o registo do clique existe para a comissão e
 * para este inquérito, não para pré-carregar nada.
 */
class CompraRepository @Inject constructor(
    private val api: AquaVitaeApi,
) {
    suspend fun getOfertas(bebidaId: Long): Result<List<OfertaCompra>> = runCatching { api.getLinksCompra(bebidaId) }

    /** Fire-and-forget do lado de quem chama: falhar (409 se o link ficou indisponível, sem rede) nunca impede de abrir a loja. */
    suspend fun registarClique(bebidaId: Long, linkId: Long): Result<Unit> = runCatching { api.registarCliqueCompra(bebidaId, linkId) }

    suspend fun getCliquesPendentes(): Result<List<CliquePendente>> = runCatching { api.getCliquesPendentes() }

    suspend fun responder(cliqueId: Long, resposta: String): Result<Unit> =
        runCatching { api.responderCliqueCompra(cliqueId, RespostaCliqueRequest(resposta)) }
}
