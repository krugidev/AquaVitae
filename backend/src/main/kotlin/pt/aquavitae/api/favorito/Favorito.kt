package pt.aquavitae.api.favorito

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

// Uma das 5 relações independentes utilizador<->bebida do schema (ver
// briefing secção 5) — uma bebida pode estar em favoritos, wishlist, cave
// e "provada" ao mesmo tempo, sem relação entre as tabelas.
@Entity
@Table(name = "favorito")
class Favorito(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "favorito_id")
    var id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilizador_id")
    var utilizador: Utilizador? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bebida_id")
    var bebida: Bebida? = null,

    @Column(name = "favorito_data_criacao")
    var dataCriacao: Instant? = null,
)

interface FavoritoRepository : JpaRepository<Favorito, Long> {
    fun findByUtilizador_Id(utilizadorId: Long): List<Favorito>
    fun findByUtilizador_IdAndBebida_Id(utilizadorId: Long, bebidaId: Long): Favorito?
    fun countByUtilizador_Id(utilizadorId: Long): Long
}
