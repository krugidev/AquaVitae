package pt.aquavitae.api.provada

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
import pt.aquavitae.api.bebida.Bebida
import pt.aquavitae.api.utilizador.Utilizador
import java.time.Instant

// Histórico de "bebidas provadas" (flag simples, ver briefing secção 1 e 5) —
// independente de favoritos/wishlist/cave.
@Entity
@Table(name = "bebida_provada")
class BebidaProvada(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bebida_provada_id")
    var id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilizador_id")
    var utilizador: Utilizador? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bebida_id")
    var bebida: Bebida? = null,

    @Column(name = "bebida_provada_data")
    var data: Instant? = null,
)

interface BebidaProvadaRepository : JpaRepository<BebidaProvada, Long> {
    fun findByUtilizador_Id(utilizadorId: Long): List<BebidaProvada>
    fun findByUtilizador_IdAndBebida_Id(utilizadorId: Long, bebidaId: Long): BebidaProvada?
}
