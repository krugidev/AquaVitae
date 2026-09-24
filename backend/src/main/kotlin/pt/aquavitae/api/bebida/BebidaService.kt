package pt.aquavitae.api.bebida

import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import pt.aquavitae.api.bebida.dto.BebidaDetailDto
import pt.aquavitae.api.bebida.dto.BebidaFiltro
import pt.aquavitae.api.bebida.dto.BebidaSummaryDto
import pt.aquavitae.api.bebida.dto.CastaPercentagemDto
import pt.aquavitae.api.bebida.dto.VinhoDetalheDto
import pt.aquavitae.api.common.ResourceNotFoundException
import pt.aquavitae.api.compra.CompraService
import pt.aquavitae.api.favorito.FavoritoRepository
import pt.aquavitae.api.preferencia.UtilizadorCategoriaPreferidaRepository
import pt.aquavitae.api.preferencia.UtilizadorPreferenciaRepository
import pt.aquavitae.api.provada.BebidaProvadaRepository
import pt.aquavitae.api.review.ReviewRepository
import pt.aquavitae.api.utilizador.Utilizador
import pt.aquavitae.api.vinho.VinhoCastaRepository
import pt.aquavitae.api.vinho.VinhoRepository
import pt.aquavitae.api.wishlist.WishlistRepository

private const val CATEGORIA_VINHO = "Vinho"

@Service
class BebidaService(
    private val bebidaRepository: BebidaRepository,
    private val vinhoRepository: VinhoRepository,
    private val vinhoCastaRepository: VinhoCastaRepository,
    private val bebidaSummaryAssembler: BebidaSummaryAssembler,
    private val compraService: CompraService,
    private val favoritoRepository: FavoritoRepository,
    private val wishlistRepository: WishlistRepository,
    private val bebidaProvadaRepository: BebidaProvadaRepository,
    private val reviewRepository: ReviewRepository,
    private val preferenciaRepository: UtilizadorPreferenciaRepository,
    private val categoriaPreferidaRepository: UtilizadorCategoriaPreferidaRepository,
) {

    fun search(filtro: BebidaFiltro, pedido: Pageable, utilizador: Utilizador?): Page<BebidaSummaryDto> {
        val pageable = pedido.comOrdenacaoPadraoDeBebidas()
        val page = bebidaRepository.search(
            search = filtro.search?.trim()?.ifBlank { null },
            categoriaIds = filtro.categoriaIds?.ifEmpty { null },
            produtorId = filtro.produtorId,
            paisId = filtro.paisId,
            regiaoIds = filtro.regiaoIds?.ifEmpty { null },
            ratingMin = filtro.ratingMin,
            acidezMin = filtro.acidezMin,
            acidezMax = filtro.acidezMax,
            docuraMin = filtro.docuraMin,
            docuraMax = filtro.docuraMax,
            corpoId = filtro.corpoId,
            taninoId = filtro.taninoId,
            tipoId = filtro.tipoId,
            castaIds = filtro.castaIds?.ifEmpty { null },
            precoMin = filtro.precoMin,
            precoMax = filtro.precoMax,
            pageable = pageable,
        )
        return PageImpl(bebidaSummaryAssembler.assemble(page.content, utilizador), pageable, page.totalElements)
    }

    // "Escolhidos para ti": sem preferências definidas (onboarding ignorado),
    // cai para as bebidas com melhor rating em geral — nunca devolve vazio à toa.
    fun sugeridas(utilizador: Utilizador, pedido: Pageable): Page<BebidaSummaryDto> {
        val pageable = pedido.comOrdenacaoPadraoDeBebidas()
        val preferencia = preferenciaRepository.findByUtilizador_Id(utilizador.id).orElse(null)
        val categoriaIds = categoriaPreferidaRepository.findByUtilizador_Id(utilizador.id).mapNotNull { it.categoria?.id }

        val page = bebidaRepository.search(
            search = null,
            categoriaIds = categoriaIds.ifEmpty { null },
            produtorId = null,
            paisId = null,
            regiaoIds = null,
            ratingMin = null,
            acidezMin = preferencia?.acidezMin,
            acidezMax = preferencia?.acidezMax,
            docuraMin = preferencia?.docuraMin,
            docuraMax = preferencia?.docuraMax,
            corpoId = null,
            taninoId = null,
            tipoId = null,
            castaIds = null,
            precoMin = null,
            precoMax = null,
            pageable = pageable,
        )
        return PageImpl(bebidaSummaryAssembler.assemble(page.content, utilizador), pageable, page.totalElements)
    }

    // Transação de leitura: o detalhe navega bebida -> produtor -> região (LAZY), sem depender do Open-Session-In-View.
    @Transactional(readOnly = true)
    fun getDetail(id: Long, utilizador: Utilizador?): BebidaDetailDto {
        val bebida = bebidaRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Bebida $id não encontrada") }

        val vinhoDetalhe = if (bebida.categoria?.value == CATEGORIA_VINHO) {
            buildVinhoDetalhe(id)
        } else {
            null
        }

        val linkCompra = compraService.cheapestByBebidaIds(listOf(id))[id]

        return BebidaDetailDto.from(bebida, vinhoDetalhe).copy(
            linkCompra = linkCompra,
            isFavorito = utilizador?.let { favoritoRepository.findByUtilizador_IdAndBebida_Id(it.id, id) != null },
            isWishlist = utilizador?.let { wishlistRepository.findByUtilizador_IdAndBebida_Id(it.id, id) != null },
            isProvada = utilizador?.let { bebidaProvadaRepository.findByUtilizador_IdAndBebida_Id(it.id, id) != null },
            notaPropria = utilizador?.let {
                reviewRepository.findByBebida_IdAndUtilizador_Id(id, it.id).map { r -> r.rating }.orElse(null)
            },
        )
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
