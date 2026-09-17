package pt.aquavitae.api.review

import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface ReviewRepository : JpaRepository<Review, Long> {
    fun findByBebida_IdOrderByDataCriacaoDesc(bebidaId: Long): List<Review>
    fun findByBebida_IdAndUtilizador_Id(bebidaId: Long, utilizadorId: Long): Optional<Review>
    fun countByUtilizador_Id(utilizadorId: Long): Long
}
