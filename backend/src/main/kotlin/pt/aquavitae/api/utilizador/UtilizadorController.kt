package pt.aquavitae.api.utilizador

import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

data class UtilizadorMeDto(
    val id: Long,
    val username: String?,
    val email: String?,
    val firstName: String?,
    val lastName: String?,
)

@RestController
class UtilizadorController {

    @GetMapping("/api/users/me")
    fun me(@AuthenticationPrincipal utilizador: Utilizador): UtilizadorMeDto = UtilizadorMeDto(
        id = utilizador.id,
        username = utilizador.username,
        email = utilizador.email,
        firstName = utilizador.firstName,
        lastName = utilizador.lastName,
    )
}
