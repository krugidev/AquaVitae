package pt.aquavitae.api.admin

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import pt.aquavitae.api.bebida.Bebida
import pt.aquavitae.api.bebida.PesquisaTexto
import pt.aquavitae.api.produtor.Produtor

// A pesquisa das listas do painel é a do catálogo (PesquisaTexto: sem acentos nem maiúsculas, curingas escapados com '!').
private const val SEM_ACENTOS = "'${PesquisaTexto.COM_ACENTO}', '${PesquisaTexto.SEM_ACENTO}'"
private const val BEBIDA_NOME = "CAST(FUNCTION('translate', UPPER(b.nome), $SEM_ACENTOS) AS String)"
private const val PRODUTOR_NOME_DA_BEBIDA = "CAST(FUNCTION('translate', UPPER(p.nome), $SEM_ACENTOS) AS String)"
private const val CASTA_NOME = "CAST(FUNCTION('translate', UPPER(vc.casta.name), $SEM_ACENTOS) AS String)"
private const val PRODUTOR_NOME = "CAST(FUNCTION('translate', UPPER(p.nome), $SEM_ACENTOS) AS String)"

// O FROM + WHERE da lista de bebidas, partilhado com a query da contagem (a mesma lista, senão a paginação mente). LEFT JOINs: uma
// bebida sem produtor/país/categoria tem de continuar na lista — é precisamente o que se quer apanhar.
// Os códigos de `qualidade` são os de QualidadeBebida; o `IS NULL` de um texto em branco: no Oracle '' é NULL e o TRIM de "   "
// também, por isso `TRIM(x) IS NULL` apanha vazio e só espaços (`= ''` nunca é verdadeiro).
private const val BEBIDAS_DESDE = """
    FROM Bebida b LEFT JOIN b.categoria c LEFT JOIN b.produtor p LEFT JOIN b.paisOrigem pa
    WHERE (:searchPattern IS NULL
            OR $BEBIDA_NOME LIKE :searchPattern ESCAPE '!'
            OR b.ean LIKE :searchPattern ESCAPE '!'
            OR $PRODUTOR_NOME_DA_BEBIDA LIKE :searchPattern ESCAPE '!'
            OR EXISTS (SELECT 1 FROM VinhoCasta vc WHERE vc.vinho.bebidaId = b.id AND $CASTA_NOME LIKE :searchPattern ESCAPE '!'))
      AND (:categoriaId IS NULL OR c.id = :categoriaId)
      AND (:qualidade IS NULL
            OR (:qualidade = 'sem-imagem' AND TRIM(b.pathImage) IS NULL)
            OR (:qualidade = 'sem-produtor' AND p.id IS NULL)
            OR (:qualidade = 'sem-ean' AND b.ean IS NULL)
            OR (:qualidade = 'sem-link' AND NOT EXISTS (SELECT 1 FROM BebidaLinkCompra l WHERE l.bebida = b AND l.isAtivo = true))
            OR (:qualidade = 'dados-gerais' AND (b.nome IS NULL OR c.id IS NULL OR pa.id IS NULL OR b.teorAlcoolico IS NULL OR b.volumeMl IS NULL))
            OR (:qualidade = 'atributos-vinho' AND c.value = 'Vinho'
                  AND (NOT EXISTS (SELECT 1 FROM Vinho v WHERE v.bebidaId = b.id AND v.tipo IS NOT NULL)
                       OR NOT EXISTS (SELECT 1 FROM VinhoCasta vc2 WHERE vc2.vinho.bebidaId = b.id))))
"""

interface AdminBebidaRepository : JpaRepository<Bebida, Long> {

    @Query(
        value = """
            SELECT new pt.aquavitae.api.admin.BebidaAdminLinha(
                b.id, b.nome, c.value, p.nome, pa.value, b.ean, b.pathImage, b.anoProducao, b.teorAlcoolico, b.volumeMl,
                (SELECT COUNT(l2) FROM BebidaLinkCompra l2 WHERE l2.bebida = b AND l2.isAtivo = true))
            $BEBIDAS_DESDE
            """,
        countQuery = "SELECT COUNT(b) $BEBIDAS_DESDE",
    )
    fun listar(
        @Param("searchPattern") searchPattern: String?,
        @Param("categoriaId") categoriaId: Long?,
        @Param("qualidade") qualidade: String?,
        pageable: Pageable,
    ): Page<BebidaAdminLinha>

    // Links de compra utilizáveis (`true`) ou desativados pela verificação diária (`false`), para o painel inicial.
    @Query("SELECT COUNT(l) FROM BebidaLinkCompra l WHERE l.isAtivo = :ativo")
    fun contarLinks(@Param("ativo") ativo: Boolean): Long
}

private const val PRODUTORES_DESDE = """
    FROM Produtor p LEFT JOIN p.pais pa LEFT JOIN p.regiao r
    WHERE (:searchPattern IS NULL OR $PRODUTOR_NOME LIKE :searchPattern ESCAPE '!')
      AND (:qualidade IS NULL
            OR (:qualidade = 'sem-imagem' AND TRIM(p.pathImagem) IS NULL)
            OR (:qualidade = 'sem-historia' AND p.historia IS NULL)
            OR (:qualidade = 'sem-morada' AND TRIM(p.morada) IS NULL)
            OR (:qualidade = 'sem-coordenadas' AND (p.latitude IS NULL OR p.longitude IS NULL))
            OR (:qualidade = 'sem-regiao' AND r.id IS NULL)
            OR (:qualidade = 'sem-bebidas' AND NOT EXISTS (SELECT 1 FROM Bebida b WHERE b.produtor.id = p.id)))
"""

interface AdminProdutorRepository : JpaRepository<Produtor, Long> {

    @Query(
        value = """
            SELECT new pt.aquavitae.api.admin.ProdutorAdminLinha(
                p.id, p.nome, pa.value, r.nome, p.anoFundacao, p.website, p.pathImagem, p.morada, p.latitude, p.longitude,
                CASE WHEN p.historia IS NULL THEN 0 ELSE 1 END,
                (SELECT COUNT(b2) FROM Bebida b2 WHERE b2.produtor.id = p.id))
            $PRODUTORES_DESDE
            """,
        countQuery = "SELECT COUNT(p) $PRODUTORES_DESDE",
    )
    fun listar(
        @Param("searchPattern") searchPattern: String?,
        @Param("qualidade") qualidade: String?,
        pageable: Pageable,
    ): Page<ProdutorAdminLinha>

    // Os produtores com este nome, sem acentos nem maiúsculas (`nomeNormalizado` já vem de PesquisaTexto.normalizar): o "já existe" do
    // formulário. `ignorarId` deixa o produtor que se está a editar fora da comparação (uma criação passa -1).
    @Query("SELECT p.id FROM Produtor p WHERE $PRODUTOR_NOME = :nomeNormalizado AND p.id <> :ignorarId ORDER BY p.id")
    fun idsComNome(@Param("nomeNormalizado") nomeNormalizado: String, @Param("ignorarId") ignorarId: Long): List<Long>
}
