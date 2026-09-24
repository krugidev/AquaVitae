package pt.aquavitae.api.bebida.dto

import pt.aquavitae.api.bebida.Bebida
import java.math.BigDecimal

// Filtros do popup "Filtros do catálogo" dos mockups. categoriaIds/castaIds/regiaoIds vêm
// null quando a lista pedida vier vazia (ver BebidaController) — "sem filtro",
// não "não bate com nada", que é o que aconteceria com um IN () vazio em JPQL.
// `regiaoIds` são os ids devolvidos por /lookup/regioes?paisId= (as pílulas do popup).
data class BebidaFiltro(
    val search: String? = null,
    val categoriaIds: List<Long>? = null,
    val produtorId: Long? = null,
    val paisId: Long? = null,
    val regiaoIds: List<Long>? = null,
    val ratingMin: BigDecimal? = null,
    val acidezMin: Int? = null,
    val acidezMax: Int? = null,
    val docuraMin: Int? = null,
    val docuraMax: Int? = null,
    val corpoId: Long? = null,
    val taninoId: Long? = null,
    val tipoId: Long? = null,
    val castaIds: List<Long>? = null,
    val precoMin: BigDecimal? = null,
    val precoMax: BigDecimal? = null,
)

data class BebidaSummaryDto(
    val id: Long,
    val nome: String?,
    val categoriaNome: String?,
    val produtorNome: String?,
    // LAZY (produtor.regiao) sob o Open-Session-In-View do pedido HTTP — o mesmo padrão já usado para
    // produtorNome (bebida.produtor), sem JOIN FETCH dedicado (ver o comentário do BebidaRepository.search
    // sobre não navegar produtor.regiao no WHERE). Cartão do catálogo: "Produtor • Região".
    val produtorRegiao: String? = null,
    val ratingMedio: BigDecimal,
    val totalReviews: Int,
    val imagePath: String?,
    // Enriquecimento (preenchido pelo BebidaSummaryAssembler — ver esse ficheiro
    // para o porquê de isto não estar no `from` estático abaixo).
    val precoDesde: BigDecimal? = null,
    val retalhistaNome: String? = null,
    val corpo: String? = null,
    val nivelAcidez: Int? = null,
    val nivelDocura: Int? = null,
    // Só vinho, tal como corpo/nivelAcidez/nivelDocura (ver comentário em BebidaService.buildVinhoDetalhe).
    val tipo: String? = null,
    val tanino: String? = null,
    // null = utilizador não autenticado; true/false = autenticado, com/sem a marcação.
    val isFavorito: Boolean? = null,
    val isWishlist: Boolean? = null,
    val isProvada: Boolean? = null,
    val notaPropria: BigDecimal? = null,
) {
    companion object {
        fun from(bebida: Bebida) = BebidaSummaryDto(
            id = bebida.id,
            nome = bebida.nome,
            categoriaNome = bebida.categoria?.value,
            produtorNome = bebida.produtor?.nome,
            produtorRegiao = bebida.produtor?.regiao?.nome,
            ratingMedio = bebida.ratingMedio,
            totalReviews = bebida.totalReviews,
            imagePath = bebida.pathImage,
        )
    }
}

// Wrapper usado pelas listas de favoritos/wishlist/provadas: a data (de
// adição/marcação) e o estado de review pertencem à relação utilizador<->bebida,
// não à bebida em si, por isso não vivem dentro de BebidaSummaryDto.
data class BebidaRelacaoDto(
    val bebida: BebidaSummaryDto,
    val data: java.time.Instant?,
    val hasReview: Boolean? = null,
)

data class CastaPercentagemDto(
    val casta: String?,
    val percentagem: BigDecimal?,
)

data class VinhoDetalheDto(
    val corpo: String?,
    val nivelAcidez: Int?,
    val nivelDocura: Int?,
    val tanino: String?,
    val tipo: String?,
    val castas: List<CastaPercentagemDto>,
)

data class ProdutorResumoDto(
    val id: Long,
    val nome: String?,
    val regiaoId: Long?,
    val regiao: String?,
    val anoFundacao: Int?,
    val permiteVisitas: Boolean,
)

data class BebidaDetailDto(
    val id: Long,
    val nome: String?,
    val categoriaNome: String?,
    val produtorId: Long?,
    val produtorNome: String?,
    val paisOrigemNome: String?,
    val anoProducao: Int?,
    val teorAlcoolico: BigDecimal?,
    val volumeMl: BigDecimal?,
    val imagePath: String?,
    val ratingMedio: BigDecimal,
    val totalReviews: Int,
    val vinhoDetalhe: VinhoDetalheDto?,
    val produtorResumo: ProdutorResumoDto? = null,
    val linkCompra: pt.aquavitae.api.compra.dto.LinkCompraDto? = null,
    val isFavorito: Boolean? = null,
    val isWishlist: Boolean? = null,
    val isProvada: Boolean? = null,
    val notaPropria: BigDecimal? = null,
) {
    companion object {
        fun from(bebida: Bebida, vinhoDetalhe: VinhoDetalheDto?) = BebidaDetailDto(
            id = bebida.id,
            nome = bebida.nome,
            categoriaNome = bebida.categoria?.value,
            produtorId = bebida.produtor?.id,
            produtorNome = bebida.produtor?.nome,
            paisOrigemNome = bebida.paisOrigem?.value,
            anoProducao = bebida.anoProducao,
            teorAlcoolico = bebida.teorAlcoolico,
            volumeMl = bebida.volumeMl,
            imagePath = bebida.pathImage,
            ratingMedio = bebida.ratingMedio,
            totalReviews = bebida.totalReviews,
            vinhoDetalhe = vinhoDetalhe,
            produtorResumo = bebida.produtor?.let {
                ProdutorResumoDto(
                    id = it.id,
                    nome = it.nome,
                    regiaoId = it.regiao?.id,
                    regiao = it.regiao?.nome,
                    anoFundacao = it.anoFundacao,
                    permiteVisitas = it.permiteVisitas,
                )
            },
        )
    }
}
