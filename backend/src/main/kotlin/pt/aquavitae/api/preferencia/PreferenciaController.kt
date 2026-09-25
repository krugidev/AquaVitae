package pt.aquavitae.api.preferencia

import jakarta.validation.Valid
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import pt.aquavitae.api.preferencia.dto.PreferenciaRequest
import pt.aquavitae.api.preferencia.dto.PreferenciaResponse
import pt.aquavitae.api.utilizador.Utilizador

@RestController
class PreferenciaController(
    private val preferenciaService: PreferenciaService,
) {

    @GetMapping("/api/users/me/preferencias")
    fun get(@AuthenticationPrincipal utilizador: Utilizador): PreferenciaResponse =
        preferenciaService.get(utilizador.id)

    @PutMapping("/api/users/me/preferencias")
    fun upsert(
        @AuthenticationPrincipal utilizador: Utilizador,
        @Valid @RequestBody request: PreferenciaRequest,
    ): PreferenciaResponse = preferenciaService.upsert(utilizador, request)
}
