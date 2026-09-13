package pt.aquavitae.api.favorito

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
class FavoritoController(
    private val favoritoRepository: FavoritoRepository,
    private val bebidaRepository: BebidaRepository,
) {

    @GetMapping("/api/users/me/favoritos")
    fun list(@AuthenticationPrincipal utilizador: Utilizador): List<BebidaSummaryDto> =
        favoritoRepository.findByUtilizador_Id(utilizador.id)
            .mapNotNull { it.bebida }
            .map { BebidaSummaryDto.from(it) }

    @PostMapping("/api/bebidas/{bebidaId}/favorito")
    fun add(@PathVariable bebidaId: Long, @AuthenticationPrincipal utilizador: Utilizador): ResponseEntity<Void> {
        if (favoritoRepository.findByUtilizador_IdAndBebida_Id(utilizador.id, bebidaId) == null) {
            val bebida = bebidaRepository.findById(bebidaId)
                .orElseThrow { ResourceNotFoundException("Bebida $bebidaId não encontrada") }
            favoritoRepository.save(
                Favorito(utilizador = utilizador, bebida = bebida, dataCriacao = Instant.now()),
            )
        }
        return ResponseEntity.noContent().build()
    }

    @DeleteMapping("/api/bebidas/{bebidaId}/favorito")
    fun remove(@PathVariable bebidaId: Long, @AuthenticationPrincipal utilizador: Utilizador): ResponseEntity<Void> {
        favoritoRepository.findByUtilizador_IdAndBebida_Id(utilizador.id, bebidaId)?.let {
            favoritoRepository.delete(it)
        }
        return ResponseEntity.noContent().build()
    }
}
