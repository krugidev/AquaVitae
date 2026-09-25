package pt.aquavitae.api.lookup

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

// Tabelas de lookup simples (geridas pelo admin via SQL Developer, ver
// briefing secção 4 — a API só as lê, nunca escreve nelas). Agrupadas num
// único ficheiro por serem, de propósito, pequenas e sem comportamento.

@Entity
@Table(name = "utilizador_role")
class UtilizadorRole(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "utilizador_role_id")
    var id: Long = 0,

    @Column(name = "utilizador_role_value")
    var value: String? = null,
)

@Entity
@Table(name = "utilizador_nationality")
class UtilizadorNationality(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "nationality_id")
    var id: Long = 0,

    @Column(name = "nationality_value")
    var value: String? = null,

    // ISO 3166-1 alfa-2 (PT, ES, ...): a app desenha a bandeira a partir dele. Null = sem bandeira.
    @Column(name = "nationality_codigo_pais")
    var codigoPais: String? = null,
)

@Entity
@Table(name = "bebida_categoria")
class BebidaCategoria(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bebida_categoria_id")
    var id: Long = 0,

    @Column(name = "bebida_categoria_value")
    var value: String? = null,
)

@Entity
@Table(name = "pais")
class Pais(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bebida_pais_id")
    var id: Long = 0,

    @Column(name = "bebida_pais_value")
    var value: String? = null,
)

@Entity
@Table(name = "produtor_pais")
class ProdutorPais(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "produtor_pais_id")
    var id: Long = 0,

    @Column(name = "produtor_pais_value")
    var value: String? = null,
)

@Entity
@Table(name = "vinho_corpo")
class VinhoCorpo(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vinho_corpo_id")
    var id: Long = 0,

    @Column(name = "vinho_corpo_value")
    var value: String? = null,
)

@Entity
@Table(name = "vinho_tanino")
class VinhoTanino(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vinho_tanino_id")
    var id: Long = 0,

    @Column(name = "vinho_tanino_value")
    var value: String? = null,
)

@Entity
@Table(name = "vinho_tipo")
class VinhoTipo(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vinho_tipo_id")
    var id: Long = 0,

    @Column(name = "vinho_tipo_value")
    var value: String? = null,
)

@Entity
@Table(name = "casta_tipo")
class CastaTipo(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "casta_tipo_id")
    var id: Long = 0,

    @Column(name = "casta_tipo_value")
    var value: String? = null,
)

@Entity
@Table(name = "casta")
class Casta(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "casta_id")
    var id: Long = 0,

    @Column(name = "casta_name")
    var name: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "casta_tipo_id")
    var tipo: CastaTipo? = null,
)

@Entity
@Table(name = "avatar_categoria")
class AvatarCategoria(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "avatar_categoria_id")
    var id: Long = 0,

    @Column(name = "avatar_categoria_value")
    var value: String? = null,
)

@Entity
@Table(name = "utilizador_avatar")
class UtilizadorAvatar(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "avatar_id")
    var id: Long = 0,

    @Column(name = "avatar_name")
    var nome: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "avatar_category_id")
    var categoria: AvatarCategoria? = null,

    @Column(name = "avatar_path_image")
    var pathImage: String? = null,

    @Column(name = "avatar_is_active")
    var isActive: Boolean = true,
)

// Região de um país (Douro, Rioja, Bordéus, ...): o que o filtro "Origem" do catálogo mostra depois do país. Lookup
// gerido à mão (só leitura na API). `pais` é produtor_pais (mesmos ids que `pais`, ver database/README.md).
@Entity
@Table(name = "regiao")
class Regiao(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "regiao_id")
    var id: Long = 0,

    @Column(name = "regiao_nome")
    var nome: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "regiao_pais_id")
    var pais: ProdutorPais? = null,
)
