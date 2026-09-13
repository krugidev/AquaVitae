package pt.aquavitae.api.bebida

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface BebidaRepository : JpaRepository<Bebida, Long> {

    @Query(
        """
        SELECT b FROM Bebida b
        WHERE (:search IS NULL OR UPPER(b.nome) LIKE UPPER(CONCAT('%', :search, '%')))
          AND (:categoriaId IS NULL OR b.categoria.id = :categoriaId)
        ORDER BY b.nome
        """,
    )
    fun search(
        @Param("search") search: String?,
        @Param("categoriaId") categoriaId: Long?,
        pageable: Pageable,
    ): Page<Bebida>
}
