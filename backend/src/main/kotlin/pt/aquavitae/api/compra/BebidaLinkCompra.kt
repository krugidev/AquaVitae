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
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.transaction.annotation.Transactional
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

    // Página do produto sem tracking, usada pelo job de verificação (ver 01_tables.sql).
    @Column(name = "bebida_link_compra_url_verificacao")
    var urlVerificacao: String? = null,

    // false = link já não está utilizável (sem stock ou página desaparecida): esconde-se o botão
    // de compra, mas a linha fica como histórico. Voltam a true sozinhos se a página voltar.
    @Column(name = "bebida_link_compra_is_ativo")
    var isAtivo: Boolean = true,

    @Column(name = "bebida_link_compra_data_verificacao")
    var dataVerificacao: Instant? = null,

    @Column(name = "bebida_link_compra_falhas_seguidas")
    var falhasSeguidas: Int = 0,

    @Column(name = "bebida_link_compra_data_indisponivel")
    var dataIndisponivel: Instant? = null,

    // Texto curto e apresentável ("Sem stock", "Página indisponível") — vai para o cliente.
    @Column(name = "bebida_link_compra_motivo")
    var motivo: String? = null,
)

interface BebidaLinkCompraRepository : JpaRepository<BebidaLinkCompra, Long> {

    // Todos os links (ativos e não) de retalhistas ativos: a lista "Onde comprar" mostra os
    // indisponíveis como tal, em vez de os esconder.
    fun findByBebida_IdAndRetalhista_IsAtivoTrue(bebidaId: Long): List<BebidaLinkCompra>

    // Só links utilizáveis — usado para o "preço desde" do catálogo.
    fun findByBebida_IdInAndIsAtivoTrueAndRetalhista_IsAtivoTrue(bebidaIds: Collection<Long>): List<BebidaLinkCompra>

    // Para o job de verificação, que corre fora de um pedido web (sem Open-Session-In-View):
    // o retalhista (LAZY) tem de vir já carregado, senão dá LazyInitializationException.
    @Query("SELECT l FROM BebidaLinkCompra l JOIN FETCH l.retalhista r WHERE r.isAtivo = true")
    fun findAllParaVerificacao(): List<BebidaLinkCompra>

    // Atualiza só as colunas de estado (nunca um save() da entidade inteira): o job carrega os
    // links no início e demora minutos — um save() sobrescreveria com valores antigos, por
    // exemplo, um preço que tenha sido alterado por SQL entretanto.
    @Modifying
    @Transactional
    @Query(
        """
        UPDATE BebidaLinkCompra l
        SET l.isAtivo = :ativo, l.falhasSeguidas = :falhas, l.dataIndisponivel = :indisponivelDesde,
            l.motivo = :motivo, l.dataVerificacao = :verificadoEm
        WHERE l.id = :id
        """,
    )
    fun atualizarEstado(
        @Param("id") id: Long,
        @Param("ativo") ativo: Boolean,
        @Param("falhas") falhas: Int,
        @Param("indisponivelDesde") indisponivelDesde: Instant?,
        @Param("motivo") motivo: String?,
        @Param("verificadoEm") verificadoEm: Instant,
    ): Int
}
