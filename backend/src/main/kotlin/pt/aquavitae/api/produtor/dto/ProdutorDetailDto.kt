package pt.aquavitae.api.produtor.dto

import pt.aquavitae.api.produtor.Produtor
import java.math.BigDecimal

data class ProdutorDetailDto(
    val id: Long,
    val nome: String?,
    val paisNome: String?,
    val regiao: String?,
    val historia: String?,
    val anoFundacao: Int?,
    val website: String?,
    val imagePath: String?,
    val latitude: BigDecimal?,
    val longitude: BigDecimal?,
    val permiteVisitas: Boolean,
) {
    companion object {
        fun from(produtor: Produtor) = ProdutorDetailDto(
            id = produtor.id,
            nome = produtor.nome,
            paisNome = produtor.pais?.value,
            regiao = produtor.regiao,
            historia = produtor.historia,
            anoFundacao = produtor.anoFundacao,
            website = produtor.website,
            imagePath = produtor.pathImagem,
            latitude = produtor.latitude,
            longitude = produtor.longitude,
            permiteVisitas = produtor.permiteVisitas,
        )
    }
}
