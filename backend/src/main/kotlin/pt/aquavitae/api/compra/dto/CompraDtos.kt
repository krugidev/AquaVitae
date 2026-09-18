package pt.aquavitae.api.compra.dto

import pt.aquavitae.api.compra.BebidaLinkCompra
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
