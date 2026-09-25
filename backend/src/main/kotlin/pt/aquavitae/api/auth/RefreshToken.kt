package pt.aquavitae.api.auth

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import pt.aquavitae.api.utilizador.Utilizador
import java.time.Instant

// Um refresh token emitido (ver RefreshTokenRegras/RefreshTokenService). Só existe o HASH: o token em claro só passa pelo
// cliente. `revogadoEm` preenche-se quando é rodado (usado numa renovação), no logout ou numa revogação em massa.
@Entity
@Table(name = "utilizador_refresh_token")
class RefreshToken(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "refresh_token_id")
    var id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilizador_id")
    var utilizador: Utilizador? = null,

    @Column(name = "refresh_token_hash")
    var tokenHash: String = "",

    @Column(name = "refresh_token_criado_em")
    var criadoEm: Instant? = null,

    @Column(name = "refresh_token_expira_em")
    var expiraEm: Instant = Instant.EPOCH,

    @Column(name = "refresh_token_revogado_em")
    var revogadoEm: Instant? = null,

    // RefreshTokenRegras.Motivo em texto (ROTACAO, LOGOUT, PASSWORD, REUTILIZACAO); null enquanto não foi revogado.
    @Column(name = "refresh_token_revogado_motivo")
    var revogadoMotivo: String? = null,
)

interface RefreshTokenRepository : JpaRepository<RefreshToken, Long> {

    // O utilizador (LAZY) vem já carregado: a renovação emite o novo JWT com o id e o email dele.
    @Query("SELECT t FROM RefreshToken t JOIN FETCH t.utilizador WHERE t.tokenHash = :hash")
    fun findByHashComUtilizador(@Param("hash") hash: String): RefreshToken?

    // Sinal de token roubado (um já rodado voltou a aparecer), fim da sessão noutro dispositivo, password redefinida...
    @Modifying
    @Query("UPDATE RefreshToken t SET t.revogadoEm = :agora, t.revogadoMotivo = :motivo WHERE t.utilizador.id = :utilizadorId AND t.revogadoEm IS NULL")
    fun revogarTodosDoUtilizador(
        @Param("utilizadorId") utilizadorId: Long,
        @Param("agora") agora: Instant,
        @Param("motivo") motivo: String,
    ): Int

    // Limpeza: expirados há tempo suficiente para já não serem precisos nem como prova de reutilização.
    @Modifying
    @Query("DELETE FROM RefreshToken t WHERE t.expiraEm < :limite")
    fun apagarExpiradosAntesDe(@Param("limite") limite: Instant): Int
}
