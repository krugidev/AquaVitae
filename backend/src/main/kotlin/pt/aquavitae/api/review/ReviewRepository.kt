package pt.aquavitae.api.review

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.Optional

interface ReviewRepository : JpaRepository<Review, Long> {

    // Tab "Reviews" da bebida: cada review mostra o avatar e o nome do autor, ambos LAZY — por isso vêm já
    // carregados. Mais recentes primeiro; o id desempata (sem isso, reviews do mesmo instante trocavam de ordem).
    @Query(
        """
        SELECT r FROM Review r JOIN FETCH r.utilizador u LEFT JOIN FETCH u.avatar
        WHERE r.bebida.id = :bebidaId
        ORDER BY r.dataCriacao DESC, r.id DESC
        """,
    )
    fun findByBebidaIdComAutor(@Param("bebidaId") bebidaId: Long): List<Review>

    // Histórico de reviews do perfil: a bebida (com categoria e produtor, que o cartão mostra) já carregada.
    @Query(
        """
        SELECT r FROM Review r JOIN FETCH r.bebida b LEFT JOIN FETCH b.categoria LEFT JOIN FETCH b.produtor
        WHERE r.utilizador.id = :utilizadorId
        ORDER BY r.dataCriacao DESC, r.id DESC
        """,
    )
    fun findByUtilizadorIdComBebida(@Param("utilizadorId") utilizadorId: Long): List<Review>

    fun findByBebida_IdAndUtilizador_Id(bebidaId: Long, utilizadorId: Long): Optional<Review>
    fun findByBebida_IdInAndUtilizador_Id(bebidaIds: Collection<Long>, utilizadorId: Long): List<Review>
    fun countByUtilizador_Id(utilizadorId: Long): Long
}
