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
import pt.aquavitae.api.utilizador.Utilizador
import java.time.Instant

@Entity
@Table(name = "utilizador_password_reset")
class UtilizadorPasswordReset(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "utilizador_password_reset_id")
    var id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilizador_id")
    var utilizador: Utilizador? = null,

    @Column(name = "codigo")
    var codigo: String? = null,

    @Column(name = "expira_em")
    var expiraEm: Instant? = null,

    @Column(name = "usado")
    var usado: Boolean = false,

    @Column(name = "data_criacao")
    var dataCriacao: Instant? = null,
)

interface UtilizadorPasswordResetRepository : JpaRepository<UtilizadorPasswordReset, Long> {
    fun findByUtilizador_IdAndCodigoAndUsadoFalse(utilizadorId: Long, codigo: String): UtilizadorPasswordReset?
}
