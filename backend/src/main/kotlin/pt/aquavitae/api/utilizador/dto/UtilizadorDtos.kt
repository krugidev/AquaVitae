package pt.aquavitae.api.utilizador.dto

import jakarta.validation.constraints.Size
import pt.aquavitae.api.lookup.dto.AvatarDto
import java.time.Instant

data class UtilizadorMeDto(
    val id: Long,
    val username: String?,
    val email: String?,
    val firstName: String?,
    val lastName: String?,
    val nationality: String?,
    val bioDesc: String?,
    val avatar: AvatarDto?,
    val accountCreatedAt: Instant?,
    val totalProvadas: Int,
    val totalReviews: Int,
    val totalFavoritos: Int,
    val totalWishlist: Int,
    val totalCaves: Int,
    val totalGarrafas: Int,
    // Termos e condições: quando aceitou (null = nunca) e se a app deve mostrar o popup — nunca aceitou, ou aceitou antes
    // de os termos em vigor serem publicados. Aceitar: POST /api/users/me/termos/aceitar.
    val termosAceitesEm: Instant?,
    val precisaAceitarTermos: Boolean,
)

// Os tamanhos são os das colunas de `utilizador` (e os do registo): sem eles, um texto a mais dava um erro 500 da BD.
data class UtilizadorUpdateRequest(
    @field:Size(max = 25)
    val firstName: String? = null,

    @field:Size(max = 40)
    val lastName: String? = null,

    @field:Size(min = 3, max = 30)
    val username: String? = null,

    val nationalityId: Long? = null,

    @field:Size(max = 1000)
    val bioDesc: String? = null,

    val avatarId: Long? = null,
)
