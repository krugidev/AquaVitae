package pt.aquavitae.api.cave

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
import org.springframework.data.jpa.repository.Query
import pt.aquavitae.api.bebida.Bebida
import pt.aquavitae.api.utilizador.Utilizador
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate

// Um utilizador pode ter várias caves; uma bebida pode estar em várias
// caves e várias vezes na mesma cave — daí cave_bebida_id ser PK própria,
// não composta (ver briefing secção 5).

@Entity
@Table(name = "cave")
class Cave(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cave_id")
    var id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilizador_id")
    var utilizador: Utilizador? = null,

    @Column(name = "cave_nome")
    var nome: String? = null,

    @Column(name = "cave_descricao")
    var descricao: String? = null,

    @Column(name = "cave_data_criacao")
    var dataCriacao: Instant? = null,
)

@Entity
@Table(name = "cave_bebida")
class CaveBebida(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cave_bebida_id")
    var id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cave_id")
    var cave: Cave? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bebida_id")
    var bebida: Bebida? = null,

    @Column(name = "cave_bebida_quantidade")
    var quantidade: Int = 1,

    @Column(name = "cave_bebida_data_aquisicao")
    var dataAquisicao: LocalDate? = null,

    @Column(name = "cave_bebida_preco_pago")
    var precoPago: BigDecimal? = null,

    @Column(name = "cave_bebida_janela_inicio")
    var janelaInicio: LocalDate? = null,

    @Column(name = "cave_bebida_janela_fim")
    var janelaFim: LocalDate? = null,

    @Column(name = "cave_bebida_is_consumida")
    var isConsumida: Boolean = false,

    @Column(name = "cave_bebida_data_consumo")
    var dataConsumo: LocalDate? = null,

    @Column(name = "cave_bebida_notas")
    var notas: String? = null,

    @Column(name = "cave_bebida_data_criacao")
    var dataCriacao: Instant? = null,
)

interface CaveRepository : JpaRepository<Cave, Long> {
    fun findByUtilizador_Id(utilizadorId: Long): List<Cave>
    fun countByUtilizador_Id(utilizadorId: Long): Long
}

interface CaveBebidaRepository : JpaRepository<CaveBebida, Long> {
    fun findByCave_Id(caveId: Long): List<CaveBebida>
    fun findByIdAndCave_Id(id: Long, caveId: Long): CaveBebida?

    // Garrafas "ativas" (ainda não consumidas) em todas as caves do utilizador —
    // usado no resumo de estatísticas do perfil.
    @Query(
        "SELECT COALESCE(SUM(cb.quantidade), 0) FROM CaveBebida cb " +
            "WHERE cb.cave.utilizador.id = :utilizadorId AND cb.isConsumida = false",
    )
    fun sumQuantidadeAtivaByUtilizadorId(utilizadorId: Long): Int
}
