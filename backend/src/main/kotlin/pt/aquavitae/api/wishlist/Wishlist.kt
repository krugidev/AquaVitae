package pt.aquavitae.api.wishlist

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

@Entity
@Table(name = "wishlist")
class Wishlist(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "wishlist_id")
    var id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilizador_id")
    var utilizador: Utilizador? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bebida_id")
    var bebida: Bebida? = null,

    @Column(name = "wishlist_data_criacao")
    var dataCriacao: Instant? = null,
)

interface WishlistRepository : JpaRepository<Wishlist, Long> {
    fun findByUtilizador_Id(utilizadorId: Long): List<Wishlist>
    fun findByUtilizador_IdAndBebida_Id(utilizadorId: Long, bebidaId: Long): Wishlist?
    fun countByUtilizador_Id(utilizadorId: Long): Long
}
