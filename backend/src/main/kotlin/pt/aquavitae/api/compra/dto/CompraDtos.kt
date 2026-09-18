package pt.aquavitae.api.compra.dto

import pt.aquavitae.api.compra.BebidaLinkCompra
import java.math.BigDecimal

data class LinkCompraDto(
    val id: Long,
    val retalhistaNome: String?,
    val retalhistaLogo: String?,
    val url: String?,
    val preco: BigDecimal?,
) {
    companion object {
        fun from(link: BebidaLinkCompra) = LinkCompraDto(
            id = link.id,
            retalhistaNome = link.retalhista?.nome,
            retalhistaLogo = link.retalhista?.pathLogo,
            url = link.url,
            preco = link.precoAtual,
        )
    }
}
