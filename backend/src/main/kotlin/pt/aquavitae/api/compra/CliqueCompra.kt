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
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import pt.aquavitae.api.utilizador.Utilizador
import java.time.Instant

// Regista cada clique num link de compra (para efeitos de comissão/analytics do lado do admin) e serve o inquérito
// "chegaste a comprar?" (fatia "Comprar", 2026-09-24): `isPerguntado` passa a true quando o utilizador responde e
// `resposta` guarda o que disse (ver RespostaClique). A app só pergunta por cliques com idade dentro de uma janela
// (CliquePergunta) e enquanto `isPerguntado` for false.
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

    @Column(name = "clique_compra_is_perguntado")
    var isPerguntado: Boolean = false,

    @Column(name = "clique_compra_resposta")
    var resposta: String? = null,
)

interface CliqueCompraRepository : JpaRepository<CliqueCompra, Long> {

    // Os cliques ainda por perguntar de um utilizador, dentro da janela [desde, ate], do mais recente para o mais
    // antigo. O link, a bebida e o retalhista (LAZY) vêm já carregados: o DTO monta-se sem depender do OSIV.
    @Query(
        """
        SELECT c FROM CliqueCompra c
        JOIN FETCH c.linkCompra l JOIN FETCH l.bebida b LEFT JOIN FETCH l.retalhista
        WHERE c.utilizador.id = :utilizadorId AND c.isPerguntado = false
          AND c.dataClique BETWEEN :desde AND :ate
        ORDER BY c.dataClique DESC
        """,
    )
    fun findPendentes(
        @Param("utilizadorId") utilizadorId: Long,
        @Param("desde") desde: Instant,
        @Param("ate") ate: Instant,
    ): List<CliqueCompra>

    fun findByIdAndUtilizador_Id(id: Long, utilizadorId: Long): CliqueCompra?

    // Todos os cliques por perguntar do utilizador na mesma bebida (responder a um responde a todos).
    @Query(
        """
        SELECT c FROM CliqueCompra c JOIN c.linkCompra l
        WHERE c.utilizador.id = :utilizadorId AND l.bebida.id = :bebidaId AND c.isPerguntado = false
        """,
    )
    fun findPorPerguntarDaBebida(
        @Param("utilizadorId") utilizadorId: Long,
        @Param("bebidaId") bebidaId: Long,
    ): List<CliqueCompra>
}
