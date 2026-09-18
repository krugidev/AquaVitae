package pt.aquavitae.api.compra

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.springframework.data.jpa.repository.JpaRepository

// Tabela de conteúdo gerida pelo admin (ver briefing secção 4) — a API só lê.
// Campos de negócio do afiliado (rede/código/comissão) não são expostos por
// nenhum endpoint: o bebida_link_compra_url já é o link de afiliado final,
// preenchido pelo admin ao inserir o produto.
@Entity
@Table(name = "retalhista")
class Retalhista(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "retalhista_id")
    var id: Long = 0,

    @Column(name = "retalhista_nome")
    var nome: String? = null,

    @Column(name = "retalhista_path_logo")
    var pathLogo: String? = null,

    @Column(name = "retalhista_is_ativo")
    var isAtivo: Boolean = true,
)

interface RetalhistaRepository : JpaRepository<Retalhista, Long>
