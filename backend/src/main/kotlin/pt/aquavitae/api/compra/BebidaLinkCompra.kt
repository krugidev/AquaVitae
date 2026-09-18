package pt.aquavitae.api.compra

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
import java.math.BigDecimal
import java.time.Instant

@Entity
@Table(name = "bebida_link_compra")
class BebidaLinkCompra(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bebida_link_compra_id")
    var id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bebida_id")
    var bebida: Bebida? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "retalhista_id")
    var retalhista: Retalhista? = null,

    @Column(name = "bebida_link_compra_url")
    var url: String? = null,

    @Column(name = "bebida_link_compra_preco_atual")
    var precoAtual: BigDecimal? = null,

    @Column(name = "bebida_link_compra_data_atualizacao")
    var dataAtualizacao: Instant? = null,
)

interface BebidaLinkCompraRepository : JpaRepository<BebidaLinkCompra, Long> {
    fun findByBebida_IdAndRetalhista_IsAtivoTrueOrderByPrecoAtualAsc(bebidaId: Long): List<BebidaLinkCompra>
    fun findByBebida_IdInAndRetalhista_IsAtivoTrue(bebidaIds: Collection<Long>): List<BebidaLinkCompra>
}
