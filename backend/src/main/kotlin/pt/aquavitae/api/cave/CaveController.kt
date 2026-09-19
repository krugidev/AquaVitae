package pt.aquavitae.api.cave

import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import pt.aquavitae.api.cave.dto.CaveBebidaRequest
import pt.aquavitae.api.cave.dto.CaveBebidaResponse
import pt.aquavitae.api.cave.dto.CaveBebidaUpdateRequest
import pt.aquavitae.api.cave.dto.CaveConsumirRequest
import pt.aquavitae.api.cave.dto.CaveConsumoResponse
import pt.aquavitae.api.cave.dto.CaveDetailResponse
import pt.aquavitae.api.cave.dto.CaveRequest
import pt.aquavitae.api.cave.dto.CaveResponse
import pt.aquavitae.api.utilizador.Utilizador

@RestController
class CaveController(
    private val caveService: CaveService,
) {

    @GetMapping("/api/users/me/caves")
    fun list(@AuthenticationPrincipal utilizador: Utilizador): List<CaveResponse> =
        caveService.listByUtilizador(utilizador)

    @PostMapping("/api/users/me/caves")
    fun create(
        @AuthenticationPrincipal utilizador: Utilizador,
        @Valid @RequestBody request: CaveRequest,
    ): ResponseEntity<CaveResponse> =
        ResponseEntity.status(HttpStatus.CREATED).body(caveService.create(utilizador, request))

    // ?sort=preco|dataConsumo (por omissão, dataConsumo).
    @GetMapping("/api/caves/{id}")
    fun getById(
        @PathVariable id: Long,
        @RequestParam(required = false) sort: String?,
        @AuthenticationPrincipal utilizador: Utilizador,
    ): CaveDetailResponse = caveService.getDetail(id, utilizador, sort)

    @PostMapping("/api/caves/{id}/bebidas")
    fun addBebida(
        @PathVariable id: Long,
        @AuthenticationPrincipal utilizador: Utilizador,
        @Valid @RequestBody request: CaveBebidaRequest,
    ): ResponseEntity<CaveBebidaResponse> =
        ResponseEntity.status(HttpStatus.CREATED).body(caveService.addBebida(id, utilizador, request))

    @PatchMapping("/api/caves/{id}/bebidas/{caveBebidaId}")
    fun updateBebida(
        @PathVariable id: Long,
        @PathVariable caveBebidaId: Long,
        @AuthenticationPrincipal utilizador: Utilizador,
        @Valid @RequestBody request: CaveBebidaUpdateRequest,
    ): CaveBebidaResponse = caveService.updateBebida(id, caveBebidaId, utilizador, request)

    // "Marcar como consumida": consome UMA garrafa (decrementa a quantidade e guarda a consumida numa linha
    // própria, com a data). Corpo opcional: { dataConsumo?, notas? }.
    @PostMapping("/api/caves/{id}/bebidas/{caveBebidaId}/consumir")
    fun consumir(
        @PathVariable id: Long,
        @PathVariable caveBebidaId: Long,
        @AuthenticationPrincipal utilizador: Utilizador,
        @RequestBody(required = false) request: CaveConsumirRequest?,
    ): CaveConsumoResponse = caveService.consumir(id, caveBebidaId, utilizador, request)

    @DeleteMapping("/api/caves/{id}/bebidas/{caveBebidaId}")
    fun removeBebida(
        @PathVariable id: Long,
        @PathVariable caveBebidaId: Long,
        @AuthenticationPrincipal utilizador: Utilizador,
    ): ResponseEntity<Void> {
        caveService.removeBebida(id, caveBebidaId, utilizador)
        return ResponseEntity.noContent().build()
    }
}
