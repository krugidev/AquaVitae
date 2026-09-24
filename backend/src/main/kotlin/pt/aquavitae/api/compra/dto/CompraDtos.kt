package pt.aquavitae.api.compra.dto

import pt.aquavitae.api.compra.BebidaLinkCompra
import pt.aquavitae.api.compra.RespostaClique
import java.math.BigDecimal
import java.time.Instant

data class LinkCompraDto(
    val id: Long,
    val retalhistaNome: String?,
    val retalhistaLogo: String?,
    val url: String?,
    // Se disponivel = false, "preco" é o último preço conhecido (a app mostra-o esbatido, sem botão).
    val preco: BigDecimal?,
    val precoAtualizadoEm: Instant?,
    val disponivel: Boolean,
    // "Sem stock" / "Página indisponível" — só preenchido quando disponivel = false.
    val motivoIndisponivel: String?,
    val indisponivelDesde: Instant?,
) {
    companion object {
        fun from(link: BebidaLinkCompra) = LinkCompraDto(
            id = link.id,
            retalhistaNome = link.retalhista?.nome,
            retalhistaLogo = link.retalhista?.pathLogo,
            url = link.url,
            preco = link.precoAtual,
            precoAtualizadoEm = link.dataAtualizacao,
            disponivel = link.isAtivo,
            motivoIndisponivel = if (link.isAtivo) null else link.motivo,
            indisponivelDesde = if (link.isAtivo) null else link.dataIndisponivel,
        )
    }
}

/** Um clique num link de compra por perguntar ("Compraste a bebida X?"). */
data class CliquePendenteDto(
    val id: Long,
    val bebidaId: Long,
    val bebidaNome: String?,
    val bebidaImagePath: String?,
    val retalhistaNome: String?,
    // O preço do link agora (a app usa-o para preencher o "preço pago" ao adicionar à cave).
    val preco: BigDecimal?,
    val dataClique: Instant,
)

data class RespostaCliqueRequest(val resposta: RespostaClique)
