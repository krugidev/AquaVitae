package pt.aquavitae.api.lookup

import org.springframework.data.jpa.repository.JpaRepository
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
