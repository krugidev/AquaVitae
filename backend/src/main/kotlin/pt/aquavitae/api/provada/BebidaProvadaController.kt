package pt.aquavitae.api.provada

import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import pt.aquavitae.api.bebida.BebidaRepository
import pt.aquavitae.api.bebida.BebidaSummaryAssembler
import pt.aquavitae.api.bebida.dto.BebidaRelacaoDto
import pt.aquavitae.api.common.ResourceNotFoundException
import pt.aquavitae.api.utilizador.Utilizador
import java.time.Instant
import java.time.ZoneOffset

@RestController
class BebidaProvadaController(
    private val bebidaProvadaRepository: BebidaProvadaRepository,
    private val bebidaRepository: BebidaRepository,
    private val bebidaSummaryAssembler: BebidaSummaryAssembler,
) {

    @GetMapping("/api/users/me/provadas")
    fun list(
        @AuthenticationPrincipal utilizador: Utilizador,
        @RequestParam(required = false) categoriaId: Long?,
        @RequestParam(required = false) ano: Int?,
    ): List<BebidaRelacaoDto> {
        var provadas = bebidaProvadaRepository.findByUtilizador_Id(utilizador.id)
        if (categoriaId != null) {
            provadas = provadas.filter { it.bebida?.categoria?.id == categoriaId }
        }
        if (ano != null) {
            provadas = provadas.filter { it.data?.atZone(ZoneOffset.UTC)?.year == ano }
        }
        val bebidas = provadas.mapNotNull { it.bebida }
        val summaries = bebidaSummaryAssembler.assemble(bebidas, utilizador).associateBy { it.id }
        return provadas.mapNotNull { p ->
            p.bebida?.id?.let { summaries[it] }?.let { BebidaRelacaoDto(it, p.data, hasReview = it.notaPropria != null) }
        }
    }

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
