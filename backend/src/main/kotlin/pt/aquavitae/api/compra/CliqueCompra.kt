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
import pt.aquavitae.api.utilizador.Utilizador
import java.time.Instant

// Regista cada clique num link de compra (para efeitos de comissão/analytics
// do lado do admin — a API só grava, não expõe leitura disto ainda).
// clique_compra_is_perguntado/resposta (inquérito "chegaste a comprar?") ficam
// por mapear até essa funcionalidade entrar em âmbito.
@Entity
@Table(name = "clique_compra")
class CliqueCompra(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "clique_compra_id")
    var id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilizador_id")
    var utilizador: Utilizador? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bebida_link_compra_id")
    var linkCompra: BebidaLinkCompra? = null,

    @Column(name = "clique_compra_data")
    var dataClique: Instant? = null,
)

interface CliqueCompraRepository : JpaRepository<CliqueCompra, Long>
