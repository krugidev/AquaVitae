package pt.aquavitae.api.bebida

import org.springframework.stereotype.Service
import pt.aquavitae.api.bebida.dto.BebidaSummaryDto
import pt.aquavitae.api.compra.CompraService
import pt.aquavitae.api.favorito.FavoritoRepository
import pt.aquavitae.api.provada.BebidaProvadaRepository
import pt.aquavitae.api.review.ReviewRepository
import pt.aquavitae.api.utilizador.Utilizador
import pt.aquavitae.api.vinho.VinhoRepository
import pt.aquavitae.api.wishlist.WishlistRepository

// Enriquece BebidaSummaryDto.from(bebida) com dados que exigem outras tabelas
// (preço mais barato, detalhe do subtype vinho, marcações do utilizador) —
// centralizado aqui porque catálogo, sugeridas, favoritos, wishlist e
// provadas devolvem todos o mesmo formato de item e precisam do mesmo
// enriquecimento. Faz sempre queries em lote (nunca uma por bebida).
@Service
class BebidaSummaryAssembler(
    private val vinhoRepository: VinhoRepository,
    private val compraService: CompraService,
    private val favoritoRepository: FavoritoRepository,
    private val wishlistRepository: WishlistRepository,
    private val bebidaProvadaRepository: BebidaProvadaRepository,
    private val reviewRepository: ReviewRepository,
) {

    fun assemble(bebidas: List<Bebida>, utilizador: Utilizador?): List<BebidaSummaryDto> {
        if (bebidas.isEmpty()) return emptyList()
        val ids = bebidas.map { it.id }

        val vinhoPorBebida = vinhoRepository.findAllById(ids).associateBy { it.bebidaId }
        val precoPorBebida = compraService.cheapestByBebidaIds(ids)

        val favoritoIds = utilizador?.let {
            favoritoRepository.findByUtilizador_IdAndBebida_IdIn(it.id, ids).mapNotNull { f -> f.bebida?.id }.toSet()
        }
        val wishlistIds = utilizador?.let {
            wishlistRepository.findByUtilizador_IdAndBebida_IdIn(it.id, ids).mapNotNull { w -> w.bebida?.id }.toSet()
        }
        val provadaIds = utilizador?.let {
            bebidaProvadaRepository.findByUtilizador_IdAndBebida_IdIn(it.id, ids).mapNotNull { p -> p.bebida?.id }.toSet()
        }
        val notaPorBebida = utilizador?.let {
            reviewRepository.findByBebida_IdInAndUtilizador_Id(ids, it.id).associate { r -> r.bebida?.id to r.rating }
        }

        return bebidas.map { bebida ->
            val vinho = vinhoPorBebida[bebida.id]
            val link = precoPorBebida[bebida.id]
            BebidaSummaryDto.from(bebida).copy(
                precoDesde = link?.preco,
                retalhistaNome = link?.retalhistaNome,
                corpo = vinho?.corpo?.value,
                nivelAcidez = vinho?.nivelAcidez,
                nivelDocura = vinho?.nivelDocura,
                isFavorito = favoritoIds?.let { bebida.id in it },
                isWishlist = wishlistIds?.let { bebida.id in it },
                isProvada = provadaIds?.let { bebida.id in it },
                notaPropria = notaPorBebida?.get(bebida.id),
            )
        }
    }
}
