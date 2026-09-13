package pt.aquavitae.api.bebida

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import pt.aquavitae.api.bebida.dto.BebidaDetailDto
import pt.aquavitae.api.bebida.dto.BebidaSummaryDto
import pt.aquavitae.api.bebida.dto.CastaPercentagemDto
import pt.aquavitae.api.bebida.dto.VinhoDetalheDto
import pt.aquavitae.api.common.ResourceNotFoundException
import pt.aquavitae.api.vinho.VinhoCastaRepository
import pt.aquavitae.api.vinho.VinhoRepository

private const val CATEGORIA_VINHO = "Vinho"

@Service
class BebidaService(
    private val bebidaRepository: BebidaRepository,
    private val vinhoRepository: VinhoRepository,
    private val vinhoCastaRepository: VinhoCastaRepository,
) {

    fun search(search: String?, categoriaId: Long?, pageable: Pageable): Page<BebidaSummaryDto> =
        bebidaRepository.search(search?.trim()?.ifBlank { null }, categoriaId, pageable)
            .map { BebidaSummaryDto.from(it) }

    fun getDetail(id: Long): BebidaDetailDto {
        val bebida = bebidaRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Bebida $id não encontrada") }

        val vinhoDetalhe = if (bebida.categoria?.value == CATEGORIA_VINHO) {
            buildVinhoDetalhe(id)
        } else {
            null
        }

        return BebidaDetailDto.from(bebida, vinhoDetalhe)
    }

    // As restantes categorias (whisky, gin, licor, vodka, aguardente) seguem
    // exatamente o mesmo padrão: entidade subtype com bebida_id partilhado
    // (ver pt.aquavitae.api.vinho.Vinho) + um DTO de detalhe próprio,
    // adicionado aqui como outro `if` / `when` branch.
    private fun buildVinhoDetalhe(bebidaId: Long): VinhoDetalheDto? {
        val vinho = vinhoRepository.findById(bebidaId).orElse(null) ?: return null
        val castas = vinhoCastaRepository.findByVinho_BebidaId(bebidaId).map {
            CastaPercentagemDto(casta = it.casta?.name, percentagem = it.percentagem)
        }
        return VinhoDetalheDto(
            corpo = vinho.corpo?.value,
            nivelAcidez = vinho.nivelAcidez,
            nivelDocura = vinho.nivelDocura,
            tanino = vinho.tanino?.value,
            tipo = vinho.tipo?.value,
            castas = castas,
        )
    }
}
