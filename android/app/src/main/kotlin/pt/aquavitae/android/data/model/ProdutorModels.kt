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
    /** Morada em texto livre (rua, código postal, localidade); `null` = não disponível. */
    val morada: String? = null,
    val latitude: Double?,
    val longitude: Double?,
    val permiteVisitas: Boolean,
    /** Nº de bebidas do produtor no catálogo. */
    val totalProdutos: Int = 0,
    /** Rating geral: a média de todas as reviews das bebidas do produtor (ponderada pelo nº de reviews); `null` = ainda sem reviews. */
    val ratingMedio: Double? = null,
    val totalReviews: Int = 0,
    /** As categorias em que o produtor tem bebidas (os separadores do catálogo do produtor). */
    val categorias: List<LookupItem> = emptyList(),
)
