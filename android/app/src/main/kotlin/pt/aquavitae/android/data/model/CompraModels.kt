package pt.aquavitae.android.data.model

import com.squareup.moshi.JsonClass

/**
 * Uma oferta de um retalhista para uma bebida (`GET /api/bebidas/{id}/links-compra`): a secção "Onde comprar". Vêm as
 * disponíveis primeiro (mais barata primeiro) e as indisponíveis no fim, com o último preço conhecido e o motivo
 * ("Sem stock", "Página indisponível"). `url` é o link de afiliado final — só se abre por toque do utilizador.
 */
@JsonClass(generateAdapter = true)
data class OfertaCompra(
    val id: Long,
    val retalhistaNome: String?,
    val retalhistaLogo: String?,
    val url: String?,
    val preco: Double?,
    val precoAtualizadoEm: String?,
    val disponivel: Boolean,
    val motivoIndisponivel: String?,
    val indisponivelDesde: String?,
)

/** Um clique num link de compra por perguntar: "Compraste X?" (`GET /api/users/me/cliques-compra/pendentes`). */
@JsonClass(generateAdapter = true)
data class CliquePendente(
    val id: Long,
    val bebidaId: Long,
    val bebidaNome: String?,
    val bebidaImagePath: String?,
    val retalhistaNome: String?,
    /** O preço do link agora — sugere-se como "preço pago" ao adicionar à cave. */
    val preco: Double?,
    val dataClique: String,
)

@JsonClass(generateAdapter = true)
data class RespostaCliqueRequest(val resposta: String) {
    companion object {
        const val COMPREI = "COMPREI"
        const val NAO_COMPREI = "NAO_COMPREI"
    }
}
