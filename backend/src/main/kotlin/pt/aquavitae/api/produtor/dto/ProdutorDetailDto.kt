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
    // Nº de bebidas do produtor no catálogo (o "Produtos" da página do produtor).
    val totalProdutos: Int,
) {
    companion object {
        // `produtor.pais` tem de vir carregado (LAZY): ver ProdutorRepository.findByIdWithPais.
        fun from(produtor: Produtor, totalProdutos: Int) = ProdutorDetailDto(
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
            totalProdutos = totalProdutos,
        )
    }
}
