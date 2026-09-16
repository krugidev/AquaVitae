package pt.aquavitae.api.utilizador

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.Optional

interface UtilizadorRepository : JpaRepository<Utilizador, Long> {
    fun findByEmail(email: String): Optional<Utilizador>
    fun existsByEmail(email: String): Boolean
    fun existsByUsername(username: String): Boolean

    // Usado pelo JwtAuthenticationFilter: esse filtro corre fora de uma sessão
    // Hibernate aberta, por isso `role` (LAZY) tem de vir já carregado aqui —
    // caso contrário dá LazyInitializationException ("no session") e o pedido
    // falha com 401, mascarando o erro real.
    @Query("SELECT u FROM Utilizador u LEFT JOIN FETCH u.role WHERE u.id = :id")
    fun findByIdWithRole(id: Long): Optional<Utilizador>
}
