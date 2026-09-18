package pt.aquavitae.api.bebida

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import pt.aquavitae.api.bebida.dto.BebidaDetailDto
import pt.aquavitae.api.bebida.dto.BebidaFiltro
import pt.aquavitae.api.bebida.dto.BebidaSummaryDto
import pt.aquavitae.api.utilizador.Utilizador
import java.math.BigDecimal

@RestController
@RequestMapping("/api/bebidas")
class BebidaController(
    private val bebidaService: BebidaService,
) {

    @GetMapping
    fun search(
        @RequestParam(required = false) search: String?,
        @RequestParam(required = false) categoriaIds: List<Long>?,
        @RequestParam(required = false) paisId: Long?,
        @RequestParam(required = false) ratingMin: BigDecimal?,
        @RequestParam(required = false) acidezMin: Int?,
        @RequestParam(required = false) acidezMax: Int?,
        @RequestParam(required = false) docuraMin: Int?,
        @RequestParam(required = false) docuraMax: Int?,
        @RequestParam(required = false) corpoId: Long?,
        @RequestParam(required = false) taninoId: Long?,
        @RequestParam(required = false) tipoId: Long?,
        @RequestParam(required = false) castaIds: List<Long>?,
        @RequestParam(required = false) precoMin: BigDecimal?,
        @RequestParam(required = false) precoMax: BigDecimal?,
        pageable: Pageable,
        // Endpoint é público (GET /api/bebidas/** tem permitAll no SecurityConfig), mas
        // se vier um Bearer token válido o JwtAuthenticationFilter já autentica na mesma
        // — por isso isto pode ser não-nulo mesmo sem a rota exigir auth.
        @AuthenticationPrincipal(errorOnInvalidType = false) utilizador: Utilizador?,
    ): Page<BebidaSummaryDto> = bebidaService.search(
        BebidaFiltro(
            search = search,
            categoriaIds = categoriaIds,
            paisId = paisId,
            ratingMin = ratingMin,
            acidezMin = acidezMin,
            acidezMax = acidezMax,
            docuraMin = docuraMin,
            docuraMax = docuraMax,
            corpoId = corpoId,
            taninoId = taninoId,
            tipoId = tipoId,
            castaIds = castaIds,
            precoMin = precoMin,
            precoMax = precoMax,
        ),
        pageable,
        utilizador,
    )

    @GetMapping("/sugeridas")
    fun sugeridas(pageable: Pageable, @AuthenticationPrincipal utilizador: Utilizador): Page<BebidaSummaryDto> =
        bebidaService.sugeridas(utilizador, pageable)

    @GetMapping("/{id}")
    fun getById(
        @PathVariable id: Long,
        @AuthenticationPrincipal(errorOnInvalidType = false) utilizador: Utilizador?,
    ): BebidaDetailDto = bebidaService.getDetail(id, utilizador)
}
