package pt.aquavitae.api.bebida

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import pt.aquavitae.api.lookup.BebidaCategoria
import pt.aquavitae.api.lookup.Pais
import pt.aquavitae.api.produtor.Produtor
import java.math.BigDecimal
import java.time.Instant

@Entity
@Table(name = "bebida")
class Bebida(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bebida_id")
    var id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bebida_category_id")
    var categoria: BebidaCategoria? = null,

    @Column(name = "bebida_name")
    var nome: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bebida_producer_id")
    var produtor: Produtor? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bebida_pais_origem_id")
    var paisOrigem: Pais? = null,

    @Column(name = "bebida_ano_producao")
    var anoProducao: Int? = null,

    // BigDecimal, não Double: Hibernate mapeia Double para SQL FLOAT, que não bate
    // certo com o NUMBER(p,s) do Oracle em modo "validate" (SchemaManagementException).
    @Column(name = "bebida_teor_alcoolico")
    var teorAlcoolico: BigDecimal? = null,

    @Column(name = "bebida_volume_ml")
    var volumeMl: BigDecimal? = null,

    @Column(name = "bebida_path_image")
    var pathImage: String? = null,

    @Column(name = "bebida_data_criacao")
    var dataCriacao: Instant? = null,

    @Column(name = "bebida_rating_medio")
    var ratingMedio: BigDecimal = BigDecimal.ZERO,

    @Column(name = "bebida_total_reviews")
    var totalReviews: Int = 0,

    // GTIN (texto, com zeros à esquerda), único e anulável. Só interno: serve para reconhecer a mesma bebida vinda de
    // outro retalhista ao preparar os lotes; não é exposto na API.
    @Column(name = "bebida_ean")
    var ean: String? = null,
)
