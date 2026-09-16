package pt.aquavitae.api.bebida.dto

import pt.aquavitae.api.bebida.Bebida
import java.math.BigDecimal

data class BebidaSummaryDto(
    val id: Long,
    val nome: String?,
    val categoriaNome: String?,
    val produtorNome: String?,
    val ratingMedio: BigDecimal,
    val totalReviews: Int,
    val imagePath: String?,
) {
    companion object {
        fun from(bebida: Bebida) = BebidaSummaryDto(
            id = bebida.id,
            nome = bebida.nome,
            categoriaNome = bebida.categoria?.value,
            produtorNome = bebida.produtor?.nome,
            ratingMedio = bebida.ratingMedio,
            totalReviews = bebida.totalReviews,
            imagePath = bebida.pathImage,
        )
    }
}

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
        )
    }
}
