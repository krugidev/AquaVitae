package pt.aquavitae.api.preferencia

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
import pt.aquavitae.api.lookup.BebidaCategoria
import pt.aquavitae.api.lookup.Casta
import pt.aquavitae.api.utilizador.Utilizador
import java.time.Instant
import java.util.Optional

// Preferências de onboarding (ver briefing secção 1/7): escalas
// sensoriais + categorias/castas preferidas. Três tabelas independentes
// no schema, tratadas aqui como um único agregado a partir do PUT.

@Entity
@Table(name = "utilizador_preferencia")
class UtilizadorPreferencia(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "utilizador_preferencia_id")
    var id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilizador_id")
    var utilizador: Utilizador? = null,

    @Column(name = "utilizador_preferencia_acidez_min")
    var acidezMin: Int? = null,

    @Column(name = "utilizador_preferencia_acidez_max")
    var acidezMax: Int? = null,

    @Column(name = "utilizador_preferencia_docura_min")
    var docuraMin: Int? = null,

    @Column(name = "utilizador_preferencia_docura_max")
    var docuraMax: Int? = null,

    @Column(name = "utilizador_preferencia_data_atualizacao")
    var dataAtualizacao: Instant? = null,
)

@Entity
@Table(name = "utilizador_categoria_preferida")
class UtilizadorCategoriaPreferida(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "utilizador_categoria_preferida_id")
    var id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilizador_id")
    var utilizador: Utilizador? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bebida_categoria_id")
    var categoria: BebidaCategoria? = null,
)

@Entity
@Table(name = "utilizador_casta_preferida")
class UtilizadorCastaPreferida(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "utilizador_casta_preferida_id")
    var id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilizador_id")
    var utilizador: Utilizador? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "casta_id")
    var casta: Casta? = null,
)

interface UtilizadorPreferenciaRepository : JpaRepository<UtilizadorPreferencia, Long> {
    fun findByUtilizador_Id(utilizadorId: Long): Optional<UtilizadorPreferencia>
}

interface UtilizadorCategoriaPreferidaRepository : JpaRepository<UtilizadorCategoriaPreferida, Long> {
    fun deleteByUtilizador_Id(utilizadorId: Long)
}

interface UtilizadorCastaPreferidaRepository : JpaRepository<UtilizadorCastaPreferida, Long> {
    fun deleteByUtilizador_Id(utilizadorId: Long)
}
