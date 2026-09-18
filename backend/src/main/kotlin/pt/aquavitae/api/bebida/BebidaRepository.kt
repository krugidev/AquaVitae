package pt.aquavitae.api.bebida

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.math.BigDecimal

interface BebidaRepository : JpaRepository<Bebida, Long> {

    // LEFT JOIN "ad hoc" (JPA 2.1+) para Vinho: só a categoria Vinho tem subtype
    // implementado por agora (ver comentário em BebidaService.buildVinhoDetalhe),
    // por isso os filtros de acidez/doçura/corpo/tanino/tipo/castas só encontram
    // bebidas fora dessa categoria se os parâmetros ficarem todos null.
    // precoMin/Max é "existe um link ativo dentro do intervalo" — aproximação
    // aceitável para o MVP (poucos links por bebida); não garante que É o link
    // mais barato que cai no intervalo se houver vários retalhistas.
    @Query(
        """
        SELECT b FROM Bebida b LEFT JOIN Vinho v ON v.bebidaId = b.id
        WHERE (:search IS NULL OR UPPER(b.nome) LIKE UPPER(CONCAT('%', :search, '%')))
          AND (:categoriaIds IS NULL OR b.categoria.id IN :categoriaIds)
          AND (:paisId IS NULL OR b.paisOrigem.id = :paisId)
          AND (:ratingMin IS NULL OR b.ratingMedio >= :ratingMin)
          AND (:acidezMin IS NULL OR v.nivelAcidez >= :acidezMin)
          AND (:acidezMax IS NULL OR v.nivelAcidez <= :acidezMax)
          AND (:docuraMin IS NULL OR v.nivelDocura >= :docuraMin)
          AND (:docuraMax IS NULL OR v.nivelDocura <= :docuraMax)
          AND (:corpoId IS NULL OR v.corpo.id = :corpoId)
          AND (:taninoId IS NULL OR v.tanino.id = :taninoId)
          AND (:tipoId IS NULL OR v.tipo.id = :tipoId)
          AND (:castaIds IS NULL OR EXISTS (SELECT 1 FROM VinhoCasta vc WHERE vc.vinho = v AND vc.casta.id IN :castaIds))
          AND (:precoMin IS NULL OR EXISTS (
                SELECT 1 FROM BebidaLinkCompra l WHERE l.bebida = b AND l.isAtivo = true
                  AND l.retalhista.isAtivo = true AND l.precoAtual >= :precoMin))
          AND (:precoMax IS NULL OR EXISTS (
                SELECT 1 FROM BebidaLinkCompra l WHERE l.bebida = b AND l.isAtivo = true
                  AND l.retalhista.isAtivo = true AND l.precoAtual <= :precoMax))
        """,
    )
    fun search(
        @Param("search") search: String?,
        @Param("categoriaIds") categoriaIds: List<Long>?,
        @Param("paisId") paisId: Long?,
        @Param("ratingMin") ratingMin: BigDecimal?,
        @Param("acidezMin") acidezMin: Int?,
        @Param("acidezMax") acidezMax: Int?,
        @Param("docuraMin") docuraMin: Int?,
        @Param("docuraMax") docuraMax: Int?,
        @Param("corpoId") corpoId: Long?,
        @Param("taninoId") taninoId: Long?,
        @Param("tipoId") tipoId: Long?,
        @Param("castaIds") castaIds: List<Long>?,
        @Param("precoMin") precoMin: BigDecimal?,
        @Param("precoMax") precoMax: BigDecimal?,
        pageable: Pageable,
    ): Page<Bebida>
}
