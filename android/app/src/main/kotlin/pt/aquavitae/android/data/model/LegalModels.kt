package pt.aquavitae.android.data.model

import com.squareup.moshi.JsonClass

/**
 * `GET /api/legal/termos`: o texto dos termos e condições, já dividido em blocos. O texto vive na API (um ficheiro que se
 * edita à mão): mudar os termos não pede uma versão nova da app.
 */
@JsonClass(generateAdapter = true)
data class TermosTexto(
    val titulo: String,
    val blocos: List<TermosBloco>,
)

/** `tipo` é `"titulo"` (o título de uma secção) ou `"paragrafo"`. */
@JsonClass(generateAdapter = true)
data class TermosBloco(
    val tipo: String,
    val texto: String,
) {
    val isTitulo: Boolean get() = tipo == "titulo"
}
