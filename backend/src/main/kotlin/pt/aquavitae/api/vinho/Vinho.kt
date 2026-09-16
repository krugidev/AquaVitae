package pt.aquavitae.api.vinho

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.MapsId
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import pt.aquavitae.api.bebida.Bebida
import pt.aquavitae.api.lookup.Casta
import pt.aquavitae.api.lookup.VinhoCorpo
import pt.aquavitae.api.lookup.VinhoTanino
import pt.aquavitae.api.lookup.VinhoTipo
import java.math.BigDecimal

// Subtype da bebida "Vinho": bebida_id é, ao mesmo tempo, PK própria e FK
// 1:1 para `bebida` — padrão supertype/subtype do schema. @MapsId faz o
// Hibernate copiar o ID gerado em `bebida` para aqui automaticamente
// quando se associa `bebida` a este objeto antes de gravar.
@Entity
@Table(name = "vinho")
class Vinho(
    @Id
    @Column(name = "bebida_id")
    var bebidaId: Long = 0,

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "bebida_id")
    var bebida: Bebida? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vinho_corpo_id")
    var corpo: VinhoCorpo? = null,

    @Column(name = "vinho_nivel_acidez")
    var nivelAcidez: Int? = null,

    @Column(name = "vinho_nivel_docura")
    var nivelDocura: Int? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vinho_tanino_id")
    var tanino: VinhoTanino? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vinho_tipo_id")
    var tipo: VinhoTipo? = null,
)

@Entity
@Table(name = "vinho_casta")
class VinhoCasta(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vinho_casta_id")
    var id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vinho_id")
    var vinho: Vinho? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "casta_id")
    var casta: Casta? = null,

    @Column(name = "vinho_casta_percentagem")
    var percentagem: BigDecimal? = null,
)
