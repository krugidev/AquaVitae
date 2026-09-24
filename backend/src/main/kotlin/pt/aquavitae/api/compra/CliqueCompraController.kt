package pt.aquavitae.api.compra

import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import pt.aquavitae.api.compra.dto.CliquePendenteDto
import pt.aquavitae.api.compra.dto.RespostaCliqueRequest
import pt.aquavitae.api.utilizador.Utilizador

/** O inquérito "Compraste?" depois de um clique num link de compra (a app pergunta ao voltar do browser). */
@RestController
@RequestMapping("/api/users/me/cliques-compra")
class CliqueCompraController(
    private val compraService: CompraService,
) {

    // Os cliques ainda por perguntar (mais de 2 minutos e menos de 72 horas), o mais recente de cada bebida.
    @GetMapping("/pendentes")
    fun pendentes(@AuthenticationPrincipal utilizador: Utilizador): List<CliquePendenteDto> = compraService.pendentes(utilizador)

    @PostMapping("/{id}/resposta")
    fun responder(
        @PathVariable id: Long,
        @RequestBody request: RespostaCliqueRequest,
        @AuthenticationPrincipal utilizador: Utilizador,
    ): ResponseEntity<Void> {
        compraService.responder(id, request.resposta, utilizador)
        return ResponseEntity.noContent().build()
    }
}
