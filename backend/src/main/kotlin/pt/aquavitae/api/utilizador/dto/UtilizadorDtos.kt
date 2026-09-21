package pt.aquavitae.api.utilizador.dto

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

data class UtilizadorUpdateRequest(
    val firstName: String? = null,
    val lastName: String? = null,
    val username: String? = null,
    val nationalityId: Long? = null,
    val bioDesc: String? = null,
    val avatarId: Long? = null,
)
