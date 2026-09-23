package pt.aquavitae.android.data.model

import com.squareup.moshi.JsonClass

/** Um valor de lookup simples (`GET /api/lookup/categorias-bebida`, `/avatar-categorias`, ...). */
@JsonClass(generateAdapter = true)
data class LookupItem(
    val id: Long,
    val nome: String?,
)

/** `GET /api/lookup/nacionalidades`. `codigoPais` é o código ISO do país (PT, ES, ...), de onde se desenha a bandeira. */
@JsonClass(generateAdapter = true)
data class Nacionalidade(
    val id: Long,
    val nome: String?,
    val codigoPais: String?,
)

/** `GET /api/lookup/avatares`. `path` é relativo à API (ver `resolveImageUrl`); os avatares são SVG. */
@JsonClass(generateAdapter = true)
data class Avatar(
    val id: Long,
    val nome: String?,
    val path: String?,
    val categoriaId: Long?,
    val categoriaNome: String?,
)

/** `GET /api/lookup/castas` (277; as de destaque vêm primeiro, o resto por ordem alfabética). */
@JsonClass(generateAdapter = true)
data class Casta(
    val id: Long,
    val nome: String?,
    val tipoId: Long?,
    val tipoNome: String?,
)

/** Estado de uma lista que se vai buscar à API (as opções de um ecrã): a carregar, com erro (dá para tentar de novo) ou pronta. */
sealed interface LookupState<out T> {
    data object Loading : LookupState<Nothing>
    data class Error(val message: String) : LookupState<Nothing>
    data class Ready<T>(val data: T) : LookupState<T>
}
