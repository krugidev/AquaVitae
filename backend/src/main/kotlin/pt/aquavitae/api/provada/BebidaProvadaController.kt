package pt.aquavitae.api.provada

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
class BebidaProvadaController(
    private val bebidaProvadaRepository: BebidaProvadaRepository,
    private val bebidaRepository: BebidaRepository,
) {

    @GetMapping("/api/users/me/provadas")
    fun list(@AuthenticationPrincipal utilizador: Utilizador): List<BebidaSummaryDto> =
        bebidaProvadaRepository.findByUtilizador_Id(utilizador.id)
            .mapNotNull { it.bebida }
            .map { BebidaSummaryDto.from(it) }

    @PostMapping("/api/bebidas/{bebidaId}/provada")
    fun add(@PathVariable bebidaId: Long, @AuthenticationPrincipal utilizador: Utilizador): ResponseEntity<Void> {
        if (bebidaProvadaRepository.findByUtilizador_IdAndBebida_Id(utilizador.id, bebidaId) == null) {
            val bebida = bebidaRepository.findById(bebidaId)
                .orElseThrow { ResourceNotFoundException("Bebida $bebidaId não encontrada") }
            bebidaProvadaRepository.save(
                BebidaProvada(utilizador = utilizador, bebida = bebida, data = Instant.now()),
            )
        }
        return ResponseEntity.noContent().build()
    }

    @DeleteMapping("/api/bebidas/{bebidaId}/provada")
    fun remove(@PathVariable bebidaId: Long, @AuthenticationPrincipal utilizador: Utilizador): ResponseEntity<Void> {
        bebidaProvadaRepository.findByUtilizador_IdAndBebida_Id(utilizador.id, bebidaId)?.let {
            bebidaProvadaRepository.delete(it)
        }
        return ResponseEntity.noContent().build()
    }
}
