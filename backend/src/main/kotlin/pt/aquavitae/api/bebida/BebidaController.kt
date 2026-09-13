package pt.aquavitae.api.bebida

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import pt.aquavitae.api.bebida.dto.BebidaDetailDto
import pt.aquavitae.api.bebida.dto.BebidaSummaryDto

@RestController
@RequestMapping("/api/bebidas")
class BebidaController(
    private val bebidaService: BebidaService,
) {

    @GetMapping
    fun search(
        @RequestParam(required = false) search: String?,
        @RequestParam(required = false) categoriaId: Long?,
        pageable: Pageable,
    ): Page<BebidaSummaryDto> = bebidaService.search(search, categoriaId, pageable)

    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long): BebidaDetailDto = bebidaService.getDetail(id)
}
