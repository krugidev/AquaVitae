package pt.aquavitae.api.compra

import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import pt.aquavitae.api.compra.dto.LinkCompraDto
import pt.aquavitae.api.utilizador.Utilizador

@RestController
class CompraController(
    private val compraService: CompraService,
) {

    @GetMapping("/api/bebidas/{bebidaId}/links-compra")
    fun list(@PathVariable bebidaId: Long): List<LinkCompraDto> = compraService.listByBebida(bebidaId)

    @PostMapping("/api/bebidas/{bebidaId}/links-compra/{linkId}/clique")
    fun clique(
        @PathVariable bebidaId: Long,
        @PathVariable linkId: Long,
        @AuthenticationPrincipal utilizador: Utilizador,
    ): ResponseEntity<Void> {
        compraService.registarClique(linkId, utilizador)
        return ResponseEntity.noContent().build()
    }
}
