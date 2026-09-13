package pt.aquavitae.api.wishlist

import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import pt.aquavitae.api.bebida.BebidaRepository
import pt.aquavitae.api.bebida.dto.BebidaSummaryDto
import pt.aquavitae.api.common.ResourceNotFoundException
import pt.aquavitae.api.utilizador.Utilizador
import java.time.Instant

@RestController
class WishlistController(
    private val wishlistRepository: WishlistRepository,
    private val bebidaRepository: BebidaRepository,
) {

    @GetMapping("/api/users/me/wishlist")
    fun list(@AuthenticationPrincipal utilizador: Utilizador): List<BebidaSummaryDto> =
        wishlistRepository.findByUtilizador_Id(utilizador.id)
            .mapNotNull { it.bebida }
            .map { BebidaSummaryDto.from(it) }

    @PostMapping("/api/bebidas/{bebidaId}/wishlist")
    fun add(@PathVariable bebidaId: Long, @AuthenticationPrincipal utilizador: Utilizador): ResponseEntity<Void> {
        if (wishlistRepository.findByUtilizador_IdAndBebida_Id(utilizador.id, bebidaId) == null) {
            val bebida = bebidaRepository.findById(bebidaId)
                .orElseThrow { ResourceNotFoundException("Bebida $bebidaId não encontrada") }
            wishlistRepository.save(
                Wishlist(utilizador = utilizador, bebida = bebida, dataCriacao = Instant.now()),
            )
        }
        return ResponseEntity.noContent().build()
    }

    @DeleteMapping("/api/bebidas/{bebidaId}/wishlist")
    fun remove(@PathVariable bebidaId: Long, @AuthenticationPrincipal utilizador: Utilizador): ResponseEntity<Void> {
        wishlistRepository.findByUtilizador_IdAndBebida_Id(utilizador.id, bebidaId)?.let {
            wishlistRepository.delete(it)
        }
        return ResponseEntity.noContent().build()
    }
}
