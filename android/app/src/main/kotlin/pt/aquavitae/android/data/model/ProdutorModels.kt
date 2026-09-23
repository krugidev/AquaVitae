package pt.aquavitae.android.data.model

import com.squareup.moshi.JsonClass

/** `GET /api/produtores/{id}` e `/api/produtores/destaque` (o produtor em destaque da semana, na homepage). */
@JsonClass(generateAdapter = true)
data class ProdutorDetail(
    val id: Long,
    val nome: String?,
    val paisNome: String?,
    val regiaoId: Long?,
    val regiao: String?,
    val historia: String?,
    val anoFundacao: Int?,
    val website: String?,
    val imagePath: String?,
    val latitude: Double?,
    val longitude: Double?,
    val permiteVisitas: Boolean,
    /** Nº de bebidas do produtor no catálogo. */
    val totalProdutos: Int = 0,
)
