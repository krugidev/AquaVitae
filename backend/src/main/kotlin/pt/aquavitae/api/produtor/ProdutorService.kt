package pt.aquavitae.api.produtor

import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import pt.aquavitae.api.bebida.BebidaRepository
import pt.aquavitae.api.bebida.BebidaSummaryAssembler
import pt.aquavitae.api.bebida.comOrdenacaoPadraoDeBebidas
import pt.aquavitae.api.bebida.dto.BebidaSummaryDto
import pt.aquavitae.api.common.ResourceNotFoundException
import pt.aquavitae.api.lookup.dto.LookupItemDto
import pt.aquavitae.api.produtor.dto.ProdutorDetailDto
import pt.aquavitae.api.utilizador.Utilizador
import java.time.Clock
import java.time.LocalDate

@Service
class ProdutorService(
    private val produtorRepository: ProdutorRepository,
    private val bebidaRepository: BebidaRepository,
    private val bebidaSummaryAssembler: BebidaSummaryAssembler,
    private val clock: Clock,
) {

    @Transactional(readOnly = true)
    fun getDetail(id: Long): ProdutorDetailDto {
        val produtor = produtorRepository.findByIdWithPais(id)
            .orElseThrow { ResourceNotFoundException("Produtor $id não encontrado") }
        val rating = bebidaRepository.ratingDoProdutor(id)
        val totalReviews = rating.totalReviews ?: 0L
        return ProdutorDetailDto.from(
            produtor,
            totalProdutos = bebidaRepository.countByProdutor_Id(id).toInt(),
            ratingMedio = ProdutorRating.media(rating.somaPonderada, totalReviews),
            totalReviews = totalReviews.toInt(),
            categorias = bebidaRepository.categoriasDoProdutor(id).map { LookupItemDto(it.id, it.value) },
        )
    }

    // Separador "Produtos" da página do produtor, com o filtro por categoria.
    @Transactional(readOnly = true)
    fun listBebidas(produtorId: Long, categoriaId: Long?, pedido: Pageable, utilizador: Utilizador?): Page<BebidaSummaryDto> {
        if (!produtorRepository.existsById(produtorId)) {
            throw ResourceNotFoundException("Produtor $produtorId não encontrado")
        }
        val pageable = pedido.comOrdenacaoPadraoDeBebidas()
        val page = bebidaRepository.findByProdutor(produtorId, categoriaId, pageable)
        return PageImpl(bebidaSummaryAssembler.assemble(page.content, utilizador), pageable, page.totalElements)
    }

    @Transactional(readOnly = true)
    fun destaque(): ProdutorDetailDto {
        val candidatos = produtorRepository.findCandidatosDestaque().map {
            ProdutorDestaque.Candidato(id = it.id, nome = it.nome, ratingMedio = it.ratingMedio ?: 0.0)
        }
        val escolhido = ProdutorDestaque.escolher(candidatos, LocalDate.now(clock))
            ?: throw ResourceNotFoundException("Ainda não há produtores com bebidas para destacar")
        return getDetail(escolhido.id)
    }
}
