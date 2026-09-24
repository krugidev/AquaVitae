package pt.aquavitae.api.produtor

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.Lob
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import pt.aquavitae.api.lookup.ProdutorPais
import pt.aquavitae.api.lookup.Regiao
import java.math.BigDecimal
import java.time.Instant

@Entity
@Table(name = "produtor")
class Produtor(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "produtor_id")
    var id: Long = 0,

    @Column(name = "produtor_nome")
    var nome: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produtor_pais_id")
    var pais: ProdutorPais? = null,

    // Lookup `regiao`; a BD garante (FK composta) que é do país do produtor. LAZY: ver ProdutorRepository.findByIdWithPais.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produtor_regiao_id")
    var regiao: Regiao? = null,

    @Lob
    @Column(name = "produtor_historia")
    var historia: String? = null,

    @Column(name = "produtor_ano_fundacao")
    var anoFundacao: Int? = null,

    @Column(name = "produtor_website")
    var website: String? = null,

    @Column(name = "produtor_path_imagem")
    var pathImagem: String? = null,

    // Morada em texto livre (rua, código postal, localidade); null = não disponível.
    @Column(name = "produtor_morada")
    var morada: String? = null,

    @Column(name = "produtor_latitude")
    var latitude: BigDecimal? = null,

    @Column(name = "produtor_longitude")
    var longitude: BigDecimal? = null,

    @Column(name = "produtor_permite_visitas")
    var permiteVisitas: Boolean = false,

    @Column(name = "produtor_data_criacao")
    var dataCriacao: Instant? = null,
)
