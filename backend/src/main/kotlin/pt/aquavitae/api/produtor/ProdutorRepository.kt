package pt.aquavitae.api.produtor

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface ProdutorRepository : JpaRepository<Produtor, Long> {

    // Não há tabela de lookup de regiões (produtor_regiao é texto livre) — o filtro
    // "região depende do país" do popup de filtros é resolvido por DISTINCT em vez de
    // introduzir um lookup formal (decisão em backend/API_ENDPOINTS.md).
    @Query("SELECT DISTINCT p.regiao FROM Produtor p WHERE p.pais.id = :paisId AND p.regiao IS NOT NULL ORDER BY p.regiao")
    fun findDistinctRegioesByPaisId(@Param("paisId") paisId: Long): List<String>
}
