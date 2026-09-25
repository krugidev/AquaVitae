package pt.aquavitae.android.data.model

import com.squareup.moshi.JsonClass

/**
 * `GET /api/users/me` (só os campos que a app usa por agora; o Moshi ignora os restantes). `precisaAceitarTermos` é `true`
 * quando o utilizador nunca aceitou os termos ou aceitou antes de os termos em vigor serem publicados.
 */
@JsonClass(generateAdapter = true)
data class UtilizadorMe(
    val id: Long,
    val username: String?,
    val email: String?,
    val firstName: String?,
    val lastName: String?,
    val nationality: String? = null,
    val bioDesc: String? = null,
    val avatar: Avatar? = null,
    val accountCreatedAt: String? = null,
    // Estatísticas do perfil e da homepage.
    val totalProvadas: Int = 0,
    val totalReviews: Int = 0,
    val totalFavoritos: Int = 0,
    val totalWishlist: Int = 0,
    val totalCaves: Int = 0,
    val totalGarrafas: Int = 0,
    val termosAceitesEm: String?,
    val precisaAceitarTermos: Boolean,
) {
    /** "Nome apelido" se o utilizador o preencheu; senão o username (o mesmo critério do backend, `nomeParaMostrar()`). */
    val nomeParaMostrar: String?
        get() = listOfNotNull(firstName, lastName).joinToString(" ").ifBlank { null } ?: username
}

/**
 * `PUT /api/users/me`: só se envia o que o utilizador preencheu (o que vai a `null` não se altera). Tamanhos máximos da API:
 * nome 25, apelido 40, username 3–30, descrição 1000. `username` é único (sem distinguir maiúsculas, ver `CLAUDE.md`) — a API
 * devolve 409 se já estiver em uso (a app não faz verificação ao vivo, sem endpoint próprio para isso; só ao gravar).
 */
@JsonClass(generateAdapter = true)
data class UtilizadorUpdateRequest(
    val firstName: String? = null,
    val lastName: String? = null,
    val username: String? = null,
    val nationalityId: Long? = null,
    val bioDesc: String? = null,
    val avatarId: Long? = null,
)
