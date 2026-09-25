package pt.aquavitae.api.lookup.dto

import pt.aquavitae.api.lookup.AvatarCategoria
import pt.aquavitae.api.lookup.Casta
import pt.aquavitae.api.lookup.CastaTipo
import pt.aquavitae.api.lookup.UtilizadorAvatar

data class LookupItemDto(
    val id: Long,
    val nome: String?,
)

data class CastaDto(
    val id: Long,
    val nome: String?,
    val tipoId: Long?,
    val tipoNome: String?,
) {
    companion object {
        fun from(casta: Casta) = CastaDto(
            id = casta.id,
            nome = casta.name,
            tipoId = casta.tipo?.id,
            tipoNome = casta.tipo?.value,
        )
    }
}

data class AvatarDto(
    val id: Long,
    val nome: String?,
    val path: String?,
    val categoriaId: Long?,
    val categoriaNome: String?,
) {
    companion object {
        fun from(avatar: UtilizadorAvatar) = AvatarDto(
            id = avatar.id,
            nome = avatar.nome,
            path = avatar.pathImage,
            categoriaId = avatar.categoria?.id,
            categoriaNome = avatar.categoria?.value,
        )
    }
}

fun AvatarCategoria.toLookupItemDto() = LookupItemDto(id = id, nome = value)
fun CastaTipo.toLookupItemDto() = LookupItemDto(id = id, nome = value)
