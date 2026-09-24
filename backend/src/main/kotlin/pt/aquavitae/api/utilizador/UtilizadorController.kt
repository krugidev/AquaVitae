package pt.aquavitae.api.utilizador

import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import pt.aquavitae.api.utilizador.dto.ApagarContaRequest
import pt.aquavitae.api.utilizador.dto.UtilizadorMeDto
import pt.aquavitae.api.utilizador.dto.UtilizadorUpdateRequest

@RestController
class UtilizadorController(
    private val utilizadorService: UtilizadorService,
    private val contaService: ContaService,
) {

    @GetMapping("/api/users/me")
    fun me(@AuthenticationPrincipal utilizador: Utilizador): UtilizadorMeDto =
        utilizadorService.getMe(utilizador.id)

    @PutMapping("/api/users/me")
    fun update(
        @AuthenticationPrincipal utilizador: Utilizador,
        @Valid @RequestBody request: UtilizadorUpdateRequest,
    ): UtilizadorMeDto = utilizadorService.updateProfile(utilizador.id, request)

    // "Apagar conta" (Conta e segurança): pede a password da conta e apaga tudo (ver ContaService). 204; password errada 403.
    @PostMapping("/api/users/me/apagar")
    fun apagarConta(
        @AuthenticationPrincipal utilizador: Utilizador,
        @Valid @RequestBody request: ApagarContaRequest,
    ): ResponseEntity<Void> {
        contaService.apagarConta(utilizador.id, request.password)
        return ResponseEntity.noContent().build()
    }

    // O popup dos termos e condições (no login, para quem nunca aceitou ou aceitou uma versão anterior).
    @PostMapping("/api/users/me/termos/aceitar")
    fun aceitarTermos(@AuthenticationPrincipal utilizador: Utilizador): UtilizadorMeDto =
        utilizadorService.aceitarTermos(utilizador.id)
}
