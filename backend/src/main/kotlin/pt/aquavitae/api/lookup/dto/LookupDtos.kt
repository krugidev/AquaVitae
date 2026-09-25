package pt.aquavitae.api.lookup.dto

import pt.aquavitae.api.lookup.AvatarCategoria
import pt.aquavitae.api.lookup.Casta
import pt.aquavitae.api.lookup.CastaTipo
import pt.aquavitae.api.lookup.UtilizadorAvatar
import pt.aquavitae.api.lookup.UtilizadorNationality

data class LookupItemDto(
    val id: Long,
    val nome: String?,
)

// `codigoPais`: código ISO do país (PT, ES, ...) para a app desenhar a bandeira; null se a nacionalidade não tem código.
data class NacionalidadeDto(
    val id: Long,
    val nome: String?,
    val codigoPais: String?,
) {
    companion object {
        fun from(nacionalidade: UtilizadorNationality) = NacionalidadeDto(
            id = nacionalidade.id,
            nome = nacionalidade.value,
            codigoPais = nacionalidade.codigoPais,
        )
    }
}

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
