package pt.aquavitae.android.data.model

import com.squareup.moshi.JsonClass

/**
 * `GET /api/bebidas`, `/sugeridas`, `/produtores/{id}/bebidas` e o `bebida` dentro de [BebidaRelacao] (favoritos, wishlist,
 * provadas). Os campos de marcação (`isFavorito`/`isWishlist`/`isProvada`/`notaPropria`) só vêm preenchidos com sessão
 * (`null` = não autenticado).
 */
@JsonClass(generateAdapter = true)
data class BebidaSummary(
    val id: Long,
    val nome: String?,
    val categoriaNome: String?,
    val produtorNome: String?,
    val produtorRegiao: String? = null,
    val ratingMedio: Double,
    val totalReviews: Int,
    val imagePath: String?,
    val precoDesde: Double? = null,
    val retalhistaNome: String? = null,
    val corpo: String? = null,
    val nivelAcidez: Int? = null,
    val nivelDocura: Int? = null,
    // Só vinho, tal como corpo/nivelAcidez/nivelDocura.
    val tipo: String? = null,
    val tanino: String? = null,
    val isFavorito: Boolean? = null,
    val isWishlist: Boolean? = null,
    val isProvada: Boolean? = null,
    val notaPropria: Double? = null,
)

@JsonClass(generateAdapter = true)
data class BebidaDetail(
    val id: Long,
    val nome: String?,
    val categoriaNome: String?,
    val produtorId: Long?,
    val produtorNome: String?,
    val paisOrigemNome: String?,
    val anoProducao: Int?,
    val teorAlcoolico: Double?,
    val volumeMl: Double?,
    val imagePath: String?,
    val ratingMedio: Double,
    val totalReviews: Int,
    // Só vem preenchido quando categoriaNome == "Vinho".
    val vinhoDetalhe: VinhoDetalhe?,
    val produtorResumo: ProdutorResumo? = null,
    val linkCompra: LinkCompra? = null,
    val isFavorito: Boolean? = null,
    val isWishlist: Boolean? = null,
    val isProvada: Boolean? = null,
    val notaPropria: Double? = null,
)

@JsonClass(generateAdapter = true)
data class VinhoDetalhe(
    val corpo: String?,
    val nivelAcidez: Int?,
    val nivelDocura: Int?,
    val tanino: String?,
    val tipo: String?,
    val castas: List<CastaPercentagem>,
)

@JsonClass(generateAdapter = true)
data class CastaPercentagem(
    val casta: String?,
    val percentagem: Double?,
)

/** Resumo do produtor dentro do detalhe de uma bebida (nome, região; ver [pt.aquavitae.android.data.model.ProdutorDetail] para a página inteira). */
@JsonClass(generateAdapter = true)
data class ProdutorResumo(
    val id: Long,
    val nome: String?,
    val regiaoId: Long?,
    val regiao: String?,
    val anoFundacao: Int?,
    val permiteVisitas: Boolean,
)

/** A oferta mais barata (ver `backend/API_ENDPOINTS.md`, "Compra / afiliados"); `null` se a bebida não tem nenhuma disponível. */
@JsonClass(generateAdapter = true)
data class LinkCompra(
    val id: Long,
    val retalhistaNome: String?,
    val preco: Double?,
    val url: String?,
)

/**
 * O wrapper de `GET /users/me/favoritos`, `/wishlist`, `/provadas`: a data de adição/marcação e o estado de review
 * pertencem à relação utilizador↔bebida, não à bebida em si.
 */
@JsonClass(generateAdapter = true)
data class BebidaRelacao(
    val bebida: BebidaSummary,
    val data: String?,
    val hasReview: Boolean? = null,
)
