package pt.aquavitae.api.produtor

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import pt.aquavitae.api.bebida.dto.BebidaSummaryDto
import pt.aquavitae.api.produtor.dto.ProdutorDetailDto
import pt.aquavitae.api.utilizador.Utilizador

@RestController
@RequestMapping("/api/produtores")
class ProdutorController(
    private val produtorService: ProdutorService,
) {

    // O caminho literal /destaque tem prioridade sobre /{id} (o Spring escolhe o padrão mais específico).
    @GetMapping("/destaque")
    fun destaque(): ProdutorDetailDto = produtorService.destaque()

    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long): ProdutorDetailDto = produtorService.getDetail(id)

    // Público (GET /api/produtores/** tem permitAll), mas com Bearer válido as marcações do utilizador
    // (favorito/wishlist/provada) vêm preenchidas, como no catálogo.
    @GetMapping("/{id}/bebidas")
    fun bebidas(
        @PathVariable id: Long,
        @RequestParam(required = false) categoriaId: Long?,
        pageable: Pageable,
        @AuthenticationPrincipal(errorOnInvalidType = false) utilizador: Utilizador?,
    ): Page<BebidaSummaryDto> = produtorService.listBebidas(id, categoriaId, pageable, utilizador)
}
