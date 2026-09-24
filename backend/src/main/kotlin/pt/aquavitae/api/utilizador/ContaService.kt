package pt.aquavitae.api.utilizador

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import pt.aquavitae.api.admin.PAPEL_ADMIN
import pt.aquavitae.api.common.ConflictException
import pt.aquavitae.api.common.ResourceNotFoundException
import pt.aquavitae.api.common.UnauthorizedActionException

/**
 * Os `DELETE` em massa que apagam **tudo** o que é de um utilizador (o popup "Apagar conta"). Todas as tabelas que apontam para
 * `utilizador` têm FK sem `ON DELETE CASCADE`, por isso apaga-se à mão, dos filhos para os pais ([ContaService] chama-os por
 * essa ordem). O trigger das reviews recalcula o rating e o nº de reviews das bebidas afetadas (ver `03_triggers.sql`).
 */
interface ApagarContaRepository : JpaRepository<Utilizador, Long> {

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM CaveBebida cb WHERE cb.cave.id IN (SELECT c.id FROM Cave c WHERE c.utilizador.id = :id)")
    fun apagarGarrafas(@Param("id") id: Long): Int

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM Cave c WHERE c.utilizador.id = :id")
    fun apagarCaves(@Param("id") id: Long): Int

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM Wishlist w WHERE w.utilizador.id = :id")
    fun apagarWishlist(@Param("id") id: Long): Int

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM Favorito f WHERE f.utilizador.id = :id")
    fun apagarFavoritos(@Param("id") id: Long): Int

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM BebidaProvada p WHERE p.utilizador.id = :id")
    fun apagarProvadas(@Param("id") id: Long): Int

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM Review r WHERE r.utilizador.id = :id")
    fun apagarReviews(@Param("id") id: Long): Int

    // Os cliques em links de compra não se apagam: ficam **anónimos** (sem utilizador) — já não são dados pessoais e continuam
    // a servir para saber que links são clicados. A coluna é anulável.
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE CliqueCompra c SET c.utilizador = NULL WHERE c.utilizador.id = :id")
    fun anonimizarCliques(@Param("id") id: Long): Int

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM UtilizadorCastaPreferida p WHERE p.utilizador.id = :id")
    fun apagarCastasPreferidas(@Param("id") id: Long): Int

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM UtilizadorCategoriaPreferida p WHERE p.utilizador.id = :id")
    fun apagarCategoriasPreferidas(@Param("id") id: Long): Int

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM UtilizadorPreferencia p WHERE p.utilizador.id = :id")
    fun apagarPreferencias(@Param("id") id: Long): Int

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM UtilizadorPasswordReset r WHERE r.utilizador.id = :id")
    fun apagarCodigosDeRecuperacao(@Param("id") id: Long): Int

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM RefreshToken t WHERE t.utilizador.id = :id")
    fun apagarSessoes(@Param("id") id: Long): Int
}

/**
 * Apagar a própria conta (`POST /api/users/me/apagar`, exigido pela Google Play a apps com registo): confirma com a password da
 * conta e apaga, numa só transação, caves e garrafas, wishlist, favoritos, "provadas", reviews, preferências, códigos de
 * recuperação, sessões e por fim o próprio utilizador. Não se desfaz. Os cliques em links de compra ficam anónimos.
 */
@Service
class ContaService(
    private val utilizadores: UtilizadorRepository,
    private val apagar: ApagarContaRepository,
    private val passwordEncoder: PasswordEncoder,
) {

    @Transactional
    fun apagarConta(utilizadorId: Long, password: String) {
        val utilizador = utilizadores.findByIdWithRole(utilizadorId)
            .orElseThrow { ResourceNotFoundException("Conta $utilizadorId não encontrada") }

        // 403 (não 401): a app tem um Authenticator que tenta renovar a sessão a cada 401, e aqui a sessão está boa.
        val hash = utilizador.password
        if (hash == null || !passwordEncoder.matches(password, hash)) throw UnauthorizedActionException("Password incorreta.")

        // Um administrador não se apaga por aqui (deixava o painel sem ninguém que o abra por engano).
        if (utilizador.role?.value.equals(PAPEL_ADMIN, ignoreCase = true)) {
            throw ConflictException("As contas de administrador não se apagam pela app.")
        }

        // Dos filhos para os pais (as garrafas antes das caves).
        apagar.apagarGarrafas(utilizadorId)
        apagar.apagarCaves(utilizadorId)
        apagar.apagarWishlist(utilizadorId)
        apagar.apagarFavoritos(utilizadorId)
        apagar.apagarProvadas(utilizadorId)
        apagar.apagarReviews(utilizadorId)
        apagar.anonimizarCliques(utilizadorId)
        apagar.apagarCastasPreferidas(utilizadorId)
        apagar.apagarCategoriasPreferidas(utilizadorId)
        apagar.apagarPreferencias(utilizadorId)
        apagar.apagarCodigosDeRecuperacao(utilizadorId)
        apagar.apagarSessoes(utilizadorId)
        apagar.deleteById(utilizadorId)
    }
}
