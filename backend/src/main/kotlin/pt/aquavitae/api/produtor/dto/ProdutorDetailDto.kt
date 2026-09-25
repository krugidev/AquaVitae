package pt.aquavitae.api.produtor.dto

import pt.aquavitae.api.lookup.dto.LookupItemDto
import pt.aquavitae.api.produtor.Produtor
import java.math.BigDecimal

data class ProdutorDetailDto(
    val id: Long,
    val nome: String?,
    val paisNome: String?,
    val regiaoId: Long?,
    val regiao: String?,
    val historia: String?,
    val anoFundacao: Int?,
    val website: String?,
    val imagePath: String?,
    // Morada em texto livre; a app mostra-a e "Abrir no mapa" pesquisa por ela quando não há coordenadas.
    val morada: String?,
    val latitude: BigDecimal?,
    val longitude: BigDecimal?,
    val permiteVisitas: Boolean,
    // Nº de bebidas do produtor no catálogo (o "Produtos" da página do produtor).
    val totalProdutos: Int,
    // Rating geral: a média de todas as reviews das bebidas do produtor (ver ProdutorRating); null se ainda não há reviews.
    val ratingMedio: Double?,
    val totalReviews: Int,
    // As categorias em que o produtor tem bebidas (o catálogo do produtor só mostra essas).
    val categorias: List<LookupItemDto>,
) {
    companion object {
        // `produtor.pais` e `produtor.regiao` têm de vir carregados (LAZY): ver ProdutorRepository.findByIdWithPais.
        fun from(
            produtor: Produtor,
            totalProdutos: Int,
            ratingMedio: Double?,
            totalReviews: Int,
            categorias: List<LookupItemDto>,
        ) = ProdutorDetailDto(
            id = produtor.id,
            nome = produtor.nome,
            paisNome = produtor.pais?.value,
            regiaoId = produtor.regiao?.id,
            regiao = produtor.regiao?.nome,
            historia = produtor.historia,
            anoFundacao = produtor.anoFundacao,
            website = produtor.website,
            imagePath = produtor.pathImagem,
            morada = produtor.morada,
            latitude = produtor.latitude,
            longitude = produtor.longitude,
            permiteVisitas = produtor.permiteVisitas,
            totalProdutos = totalProdutos,
            ratingMedio = ratingMedio,
            totalReviews = totalReviews,
            categorias = categorias,
        )
    }
}
