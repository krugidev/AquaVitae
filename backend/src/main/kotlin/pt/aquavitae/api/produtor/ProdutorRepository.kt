package pt.aquavitae.api.produtor

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.Optional

// Linha do ranking do "produtor da semana" (ver ProdutorDestaque). `ratingMedio` é a média do rating das
// bebidas do produtor que já têm reviews; null se nenhuma tem.
interface CandidatoDestaqueRow {
    val id: Long
    val nome: String?
    val ratingMedio: Double?
}

interface ProdutorRepository : JpaRepository<Produtor, Long> {

    // `pais` é LAZY: vai já carregado, o DTO não deve depender do Open-Session-In-View (ver CLAUDE.md).
    @Query("SELECT p FROM Produtor p LEFT JOIN FETCH p.pais WHERE p.id = :id")
    fun findByIdWithPais(@Param("id") id: Long): Optional<Produtor>

    // Não há tabela de lookup de regiões (produtor_regiao é texto livre) — o filtro
    // "região depende do país" do popup de filtros é resolvido por DISTINCT em vez de
    // introduzir um lookup formal (decisão em backend/API_ENDPOINTS.md).
    @Query("SELECT DISTINCT p.regiao FROM Produtor p WHERE p.pais.id = :paisId AND p.regiao IS NOT NULL ORDER BY p.regiao")
    fun findDistinctRegioesByPaisId(@Param("paisId") paisId: Long): List<String>

    // Só entram produtores com pelo menos uma bebida (JOIN interno). O AVG ignora as bebidas sem reviews
    // (o CASE sem ELSE dá NULL), para 0 reviews não puxar a média de quem já tem avaliações para baixo.
    @Query(
        """
        SELECT p.id AS id, p.nome AS nome, AVG(CASE WHEN b.totalReviews > 0 THEN b.ratingMedio END) AS ratingMedio
        FROM Bebida b JOIN b.produtor p
        GROUP BY p.id, p.nome
        """,
    )
    fun findCandidatosDestaque(): List<CandidatoDestaqueRow>
}
