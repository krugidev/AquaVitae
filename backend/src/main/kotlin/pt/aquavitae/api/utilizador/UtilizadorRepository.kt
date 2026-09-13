package pt.aquavitae.api.utilizador

import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface UtilizadorRepository : JpaRepository<Utilizador, Long> {
    fun findByEmail(email: String): Optional<Utilizador>
    fun existsByEmail(email: String): Boolean
    fun existsByUsername(username: String): Boolean
}
