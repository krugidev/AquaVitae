package pt.aquavitae.api.bebida

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import pt.aquavitae.api.lookup.BebidaCategoria
import java.math.BigDecimal

interface BebidaRepository : JpaRepository<Bebida, Long> {

    // LEFT JOIN "ad hoc" (JPA 2.1+) para Vinho: só a categoria Vinho tem subtype
    // implementado por agora (ver comentário em BebidaService.buildVinhoDetalhe),
    // por isso os filtros de acidez/doçura/corpo/tanino/tipo/castas só encontram
    // bebidas fora dessa categoria se os parâmetros ficarem todos null.
    // precoMin/Max é "existe um link ativo dentro do intervalo" — aproximação
    // aceitável para o MVP (poucos links por bebida); não garante que É o link
    // mais barato que cai no intervalo se houver vários retalhistas.
    // `searchPattern` (ver PesquisaTexto: sem acentos, maiúsculas, curingas escapados) casa com o nome da bebida, o nome do produtor ou o nome de uma das castas (barra de pesquisa
    // do mockup) e, como tudo o resto, combina em AND com os filtros aplicados.
    // Produtor e região vão em EXISTS com `b.produtor.id` (a FK, sem join): navegar `b.produtor.regiao` no
    // WHERE criaria um INNER JOIN implícito que tirava da pesquisa toda a bebida sem produtor, mesmo sem filtro.
    // `regiaoIds` são os ids devolvidos por /lookup/regioes?paisId= (as pílulas do popup de filtros).
    // `produtorId` limita a um produtor (o catálogo do produtor, fatia 6): a FK da própria bebida, sem join.
    @Query(
        """
        SELECT b FROM Bebida b LEFT JOIN Vinho v ON v.bebidaId = b.id
        WHERE (:searchPattern IS NULL
                OR CAST(FUNCTION('translate', UPPER(b.nome), '${PesquisaTexto.COM_ACENTO}', '${PesquisaTexto.SEM_ACENTO}') AS String) LIKE :searchPattern ESCAPE '!'
                OR EXISTS (SELECT 1 FROM Produtor p WHERE p.id = b.produtor.id
                             AND CAST(FUNCTION('translate', UPPER(p.nome), '${PesquisaTexto.COM_ACENTO}', '${PesquisaTexto.SEM_ACENTO}') AS String) LIKE :searchPattern ESCAPE '!')
                OR EXISTS (SELECT 1 FROM VinhoCasta vc WHERE vc.vinho = v
                             AND CAST(FUNCTION('translate', UPPER(vc.casta.name), '${PesquisaTexto.COM_ACENTO}', '${PesquisaTexto.SEM_ACENTO}') AS String) LIKE :searchPattern ESCAPE '!'))
          AND (:categoriaIds IS NULL OR b.categoria.id IN :categoriaIds)
          AND (:produtorId IS NULL OR b.produtor.id = :produtorId)
          AND (:paisId IS NULL OR b.paisOrigem.id = :paisId)
          AND (:regiaoIds IS NULL OR EXISTS (SELECT 1 FROM Produtor p WHERE p.id = b.produtor.id AND p.regiao.id IN :regiaoIds))
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
        @Param("searchPattern") searchPattern: String?,
        @Param("categoriaIds") categoriaIds: List<Long>?,
        @Param("produtorId") produtorId: Long?,
        @Param("paisId") paisId: Long?,
        @Param("regiaoIds") regiaoIds: List<Long>?,
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

    // Página do produtor: "Produtos" com filtro opcional por categoria. Ambas as colunas são FKs da própria
    // bebida (sem joins).
    @Query(
        """
        SELECT b FROM Bebida b
        WHERE b.produtor.id = :produtorId
          AND (:categoriaId IS NULL OR b.categoria.id = :categoriaId)
        """,
    )
    fun findByProdutor(
        @Param("produtorId") produtorId: Long,
        @Param("categoriaId") categoriaId: Long?,
        pageable: Pageable,
    ): Page<Bebida>

    fun countByProdutor_Id(produtorId: Long): Long

    // Rating geral do produtor (ver ProdutorRating): SUM(rating × reviews) e SUM(reviews) de todas as suas bebidas.
    // Sem bebidas, as duas somas vêm null.
    @Query(
        """
        SELECT SUM(b.ratingMedio * b.totalReviews) AS somaPonderada, SUM(b.totalReviews) AS totalReviews
        FROM Bebida b
        WHERE b.produtor.id = :produtorId
        """,
    )
    fun ratingDoProdutor(@Param("produtorId") produtorId: Long): RatingDoProdutorRow

    // As categorias em que o produtor tem bebidas (os separadores do "catálogo do produtor"), pela ordem do lookup.
    @Query(
        """
        SELECT DISTINCT c FROM Bebida b JOIN b.categoria c
        WHERE b.produtor.id = :produtorId
        ORDER BY c.id
        """,
    )
    fun categoriasDoProdutor(@Param("produtorId") produtorId: Long): List<BebidaCategoria>
}

interface RatingDoProdutorRow {
    val somaPonderada: BigDecimal?
    val totalReviews: Long?
}
