package pt.aquavitae.api.compra

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import pt.aquavitae.api.common.ConflictException
import pt.aquavitae.api.common.ResourceNotFoundException
import pt.aquavitae.api.compra.dto.LinkCompraDto
import pt.aquavitae.api.utilizador.Utilizador
import java.math.BigDecimal
import java.time.Instant

@Service
class CompraService(
    private val linkCompraRepository: BebidaLinkCompraRepository,
    private val cliqueCompraRepository: CliqueCompraRepository,
) {

    // Secção "Onde comprar": disponíveis primeiro (mais barato primeiro), indisponíveis no fim.
    fun listByBebida(bebidaId: Long): List<LinkCompraDto> =
        linkCompraRepository.findByBebida_IdAndRetalhista_IsAtivoTrue(bebidaId)
            .sortedWith(
                compareByDescending<BebidaLinkCompra> { it.isAtivo }
                    .thenBy(nullsLast()) { it.precoAtual },
            )
            .map { LinkCompraDto.from(it) }

    // Usado pelo BebidaSummaryAssembler para o "precoDesde" do catálogo/homepage —
    // uma query só para todas as bebidas da página, em vez de uma por bebida.
    // Só conta links disponíveis: uma bebida sem nenhum fica sem preço/botão de compra.
    fun cheapestByBebidaIds(bebidaIds: Collection<Long>): Map<Long, LinkCompraDto> {
        if (bebidaIds.isEmpty()) return emptyMap()
        return linkCompraRepository.findByBebida_IdInAndIsAtivoTrueAndRetalhista_IsAtivoTrue(bebidaIds)
            .filter { it.precoAtual != null }
            .groupBy { it.bebida?.id }
            .mapNotNull { (bebidaId, links) ->
                bebidaId?.let { id -> id to LinkCompraDto.from(links.minBy { it.precoAtual!! }) }
            }
            .toMap()
    }

    @Transactional
    fun registarClique(linkId: Long, utilizador: Utilizador) {
        val link = linkCompraRepository.findById(linkId)
            .orElseThrow { ResourceNotFoundException("Link de compra $linkId não encontrado") }
        if (!link.isAtivo) {
            throw ConflictException("Este link de compra já não está disponível")
        }
        cliqueCompraRepository.save(
            CliqueCompra(utilizador = utilizador, linkCompra = link, dataClique = Instant.now()),
        )
    }

    private fun nullsLast(): Comparator<BigDecimal?> = Comparator.nullsLast(Comparator.naturalOrder())
}
