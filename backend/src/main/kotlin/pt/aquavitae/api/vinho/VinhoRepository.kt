package pt.aquavitae.api.vinho

import org.springframework.data.jpa.repository.JpaRepository

interface VinhoRepository : JpaRepository<Vinho, Long>

interface VinhoCastaRepository : JpaRepository<VinhoCasta, Long> {
    fun findByVinho_BebidaId(bebidaId: Long): List<VinhoCasta>
}
