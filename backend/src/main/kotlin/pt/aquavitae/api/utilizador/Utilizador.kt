package pt.aquavitae.api.utilizador

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import pt.aquavitae.api.lookup.UtilizadorNationality
import pt.aquavitae.api.lookup.UtilizadorRole
import java.time.Instant

@Entity
@Table(name = "utilizador")
class Utilizador(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "utilizador_id")
    var id: Long = 0,

    @Column(name = "utilizador_username")
    var username: String? = null,

    @Column(name = "utilizador_first_name")
    var firstName: String? = null,

    @Column(name = "utilizador_last_name")
    var lastName: String? = null,

    @Column(name = "utilizador_email")
    var email: String? = null,

    // Hash da password (BCrypt) — nunca texto em claro.
    @Column(name = "utilizador_password")
    var password: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilizador_nationality_id")
    var nationality: UtilizadorNationality? = null,

    @Column(name = "utilizador_account_created_at")
    var accountCreatedAt: Instant? = null,

    @Column(name = "utilizador_bio_desc")
    var bioDesc: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilizador_role_id")
    var role: UtilizadorRole? = null,
)
