package pt.aquavitae.api.compra

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import pt.aquavitae.api.common.ResourceNotFoundException
import pt.aquavitae.api.compra.dto.LinkCompraDto
import pt.aquavitae.api.utilizador.Utilizador
import java.time.Instant

@Service
class CompraService(
    private val linkCompraRepository: BebidaLinkCompraRepository,
    private val cliqueCompraRepository: CliqueCompraRepository,
) {

    fun listByBebida(bebidaId: Long): List<LinkCompraDto> =
        linkCompraRepository.findByBebida_IdAndRetalhista_IsAtivoTrueOrderByPrecoAtualAsc(bebidaId)
            .map { LinkCompraDto.from(it) }

    // Usado pelo BebidaSummaryAssembler para o "precoDesde" do catálogo/homepage —
    // uma query só para todas as bebidas da página, em vez de uma por bebida.
    fun cheapestByBebidaIds(bebidaIds: Collection<Long>): Map<Long, LinkCompraDto> {
        if (bebidaIds.isEmpty()) return emptyMap()
        return linkCompraRepository.findByBebida_IdInAndRetalhista_IsAtivoTrue(bebidaIds)
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
        cliqueCompraRepository.save(
            CliqueCompra(utilizador = utilizador, linkCompra = link, dataClique = Instant.now()),
        )
    }
}
