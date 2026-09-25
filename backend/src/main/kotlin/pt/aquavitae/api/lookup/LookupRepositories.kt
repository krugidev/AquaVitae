package pt.aquavitae.api.lookup

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.Optional

interface UtilizadorRoleRepository : JpaRepository<UtilizadorRole, Long> {
    fun findByValue(value: String): Optional<UtilizadorRole>
}

interface UtilizadorNationalityRepository : JpaRepository<UtilizadorNationality, Long>

interface BebidaCategoriaRepository : JpaRepository<BebidaCategoria, Long> {
    fun findByValue(value: String): Optional<BebidaCategoria>
}

interface PaisRepository : JpaRepository<Pais, Long>

interface ProdutorPaisRepository : JpaRepository<ProdutorPais, Long>

interface VinhoCorpoRepository : JpaRepository<VinhoCorpo, Long>

interface VinhoTaninoRepository : JpaRepository<VinhoTanino, Long>

interface VinhoTipoRepository : JpaRepository<VinhoTipo, Long>

interface CastaRepository : JpaRepository<Casta, Long> {
    fun findByTipo_Id(tipoId: Long): List<Casta>
}

interface CastaTipoRepository : JpaRepository<CastaTipo, Long>

interface AvatarCategoriaRepository : JpaRepository<AvatarCategoria, Long>

interface UtilizadorAvatarRepository : JpaRepository<UtilizadorAvatar, Long> {
    fun findByIsActiveTrue(): List<UtilizadorAvatar>
    fun findByCategoria_IdAndIsActiveTrue(categoriaId: Long): List<UtilizadorAvatar>
}

interface RegiaoRepository : JpaRepository<Regiao, Long> {

    // As pílulas de "Origem" do filtro: só as regiões do país que têm pelo menos uma bebida (senão uma pílula dava
    // 0 resultados). Adicionar uma região à tabela `regiao` não a mostra até haver um produto dela.
    @Query(
        """
        SELECT r FROM Regiao r
        WHERE r.pais.id = :paisId
          AND EXISTS (SELECT 1 FROM Bebida b JOIN b.produtor p WHERE p.regiao = r)
        """,
    )
    fun findComBebidasByPaisId(@Param("paisId") paisId: Long): List<Regiao>
}
