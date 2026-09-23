package pt.aquavitae.api.utilizador

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.Optional

interface UtilizadorRepository : JpaRepository<Utilizador, Long> {
    fun findByEmail(email: String): Optional<Utilizador>
    fun existsByEmail(email: String): Boolean
    fun existsByUsername(username: String): Boolean

    // Login por username ou email e unicidade no registo: sem distinguir maiúsculas (o teclado do telemóvel
    // capitaliza a 1.ª letra e "Ana" e "ana" não podem ser duas contas).
    fun findByEmailIgnoreCase(email: String): Optional<Utilizador>
    fun findByUsernameIgnoreCase(username: String): Optional<Utilizador>
    fun existsByEmailIgnoreCase(email: String): Boolean
    fun existsByUsernameIgnoreCase(username: String): Boolean

    // Usado pelo JwtAuthenticationFilter: esse filtro corre fora de uma sessão
    // Hibernate aberta, por isso `role` (LAZY) tem de vir já carregado aqui —
    // caso contrário dá LazyInitializationException ("no session") e o pedido
    // falha com 401, mascarando o erro real.
    @Query("SELECT u FROM Utilizador u LEFT JOIN FETCH u.role WHERE u.id = :id")
    fun findByIdWithRole(id: Long): Optional<Utilizador>

    // Usado pelo perfil (GET/PUT /api/users/me): nationality e avatar são LAZY,
    // e o controller/service não deve confiar no Open-Session-In-View para os
    // carregar — mesmo motivo do findByIdWithRole acima.
    @Query("SELECT u FROM Utilizador u LEFT JOIN FETCH u.nationality LEFT JOIN FETCH u.avatar WHERE u.id = :id")
    fun findByIdWithProfile(id: Long): Optional<Utilizador>
}

/**
 * "Username ou Email" (o campo único do login e da recuperação de password): tenta o email e depois o username, sem
 * distinguir maiúsculas e sem os espaços que o teclado do telemóvel deixa. `null` se não houver conta.
 */
fun UtilizadorRepository.findByIdentificador(identificador: String): Utilizador? {
    val valor = identificador.trim()
    return findByEmailIgnoreCase(valor).or { findByUsernameIgnoreCase(valor) }.orElse(null)
}
