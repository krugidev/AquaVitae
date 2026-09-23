package pt.aquavitae.android.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CaveRequest(
    val nome: String,
    val descricao: String? = null,
)

/** Lista "As minhas caves". Os totais só contam garrafas por consumir. `temBebida` só vem a sério com
 * `?bebidaId=` no pedido (popup "Adicionar à cave": destacar onde aquela bebida já está). */
@JsonClass(generateAdapter = true)
data class CaveResponse(
    val id: Long,
    val nome: String?,
    val descricao: String?,
    val totalGarrafas: Int = 0,
    val valorTotal: Double = 0.0,
    val totalProntasAAbrir: Int = 0,
    val temBebida: Boolean = false,
)

/** `prontasAAbrir` inclui as em atraso (cada item traz o `estado`); as já consumidas não aparecem aqui. */
@JsonClass(generateAdapter = true)
data class CaveDetailResponse(
    val id: Long,
    val nome: String?,
    val descricao: String?,
    val totalGarrafas: Int = 0,
    val valorTotal: Double = 0.0,
    val totalProntasAAbrir: Int = 0,
    val prontasAAbrir: List<CaveBebidaResponse> = emptyList(),
    val emGuarda: List<CaveBebidaResponse> = emptyList(),
)

@JsonClass(generateAdapter = true)
data class CaveBebidaRequest(
    val bebidaId: Long,
    val quantidade: Int = 1,
    val precoPago: Double? = null,
    val dataAquisicao: String? = null,
    val janelaInicio: String? = null,
    val janelaFim: String? = null,
    val notas: String? = null,
)

/** Campos omitidos (`null`) ficam como estão — não há forma de limpar uma janela por aqui (ver `CaveConsumirRequest`). */
@JsonClass(generateAdapter = true)
data class CaveBebidaUpdateRequest(
    val quantidade: Int? = null,
    val precoPago: Double? = null,
    val dataAquisicao: String? = null,
    val janelaInicio: String? = null,
    val janelaFim: String? = null,
    val notas: String? = null,
)

/** Corpo opcional de `POST /caves/{id}/bebidas/{caveBebidaId}/consumir`. */
@JsonClass(generateAdapter = true)
data class CaveConsumirRequest(
    val dataConsumo: String? = null,
    val notas: String? = null,
)

@JsonClass(generateAdapter = true)
data class CaveConsumoResponse(
    val restantes: Int,
    val consumida: CaveBebidaResponse,
)

/** `EM_GUARDA` \| `PRONTA` \| `EM_ATRASO` (mostrar o aviso "Em atraso") \| `CONSUMIDA`. */
enum class EstadoCaveBebida { EM_GUARDA, PRONTA, EM_ATRASO, CONSUMIDA }

@JsonClass(generateAdapter = true)
data class CaveBebidaResponse(
    val id: Long,
    val bebidaId: Long?,
    val bebidaNome: String?,
    val categoriaNome: String?,
    val imagePath: String?,
    val quantidade: Int,
    val precoPago: Double?,
    val dataAquisicao: String?,
    val janelaInicio: String?,
    val janelaFim: String?,
    val estado: EstadoCaveBebida,
    val isConsumida: Boolean,
    val dataConsumo: String?,
    val notas: String?,
)

/** "VINHO • BEBER ENTRE 2025-2027" (só os anos, o dia/mês não interessam aqui) — a homepage e o ecrã da cave. */
fun CaveBebidaResponse.descricaoJanela(): String {
    val categoria = categoriaNome?.uppercase(LocalePt)
    val anoInicio = janelaInicio?.take(4)
    val anoFim = janelaFim?.take(4)
    val janela = when {
        anoInicio != null && anoFim != null -> "BEBER ENTRE $anoInicio-$anoFim"
        anoInicio != null -> "BEBER A PARTIR DE $anoInicio"
        anoFim != null -> "BEBER ATÉ $anoFim"
        else -> "EM GUARDA"
    }
    return listOfNotNull(categoria, janela).joinToString(" • ")
}
