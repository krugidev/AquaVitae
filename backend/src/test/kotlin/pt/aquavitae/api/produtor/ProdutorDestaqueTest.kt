package pt.aquavitae.api.produtor

import pt.aquavitae.api.produtor.ProdutorDestaque.Candidato
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ProdutorDestaqueTest {

    // 1970-01-05 é a segunda-feira de referência (semana 0).
    private val semanaZero = LocalDate.of(1970, 1, 5)

    private fun candidatos(n: Int) = (1..n).map { Candidato(id = it.toLong(), nome = "Produtor %02d".format(it), ratingMedio = 5.0 - it * 0.1) }

    @Test
    fun `sem produtores nao ha destaque`() = assertNull(ProdutorDestaque.escolher(emptyList(), LocalDate.of(2026, 9, 19)))

    @Test
    fun `o ranking e por rating medio e a semana zero escolhe o melhor`() {
        val embaralhados = listOf(
            Candidato(1, "Baixo", 3.0),
            Candidato(2, "Alto", 4.8),
            Candidato(3, "Medio", 4.0),
        )
        assertEquals(2L, ProdutorDestaque.escolher(embaralhados, semanaZero)?.id)
    }

    @Test
    fun `empates de rating desempatam por nome (ordem alfabetica portuguesa) e depois por id`() {
        val iguais = listOf(
            Candidato(1, "Zé Pinto", 0.0),
            Candidato(2, "Álvaro Castro", 0.0),
            Candidato(3, "Bairro Velho", 0.0),
        )
        // Álvaro conta como A, não como depois do Z.
        assertEquals(2L, ProdutorDestaque.escolher(iguais, semanaZero)?.id)
        assertEquals(3L, ProdutorDestaque.escolher(iguais, semanaZero.plusWeeks(1))?.id)
        assertEquals(1L, ProdutorDestaque.escolher(iguais, semanaZero.plusWeeks(2))?.id)
    }

    @Test
    fun `o produtor mantem-se de segunda a domingo e muda na segunda seguinte`() {
        val lista = candidatos(5)
        val segunda = LocalDate.of(2026, 9, 14) // uma segunda-feira
        val daSemana = ProdutorDestaque.escolher(lista, segunda)?.id
        (0..6).forEach { dia ->
            assertEquals(daSemana, ProdutorDestaque.escolher(lista, segunda.plusDays(dia.toLong()))?.id, "dia $dia")
        }
        val seguinte = ProdutorDestaque.escolher(lista, segunda.plusWeeks(1))?.id
        assertEquals(((daSemana!! % 5) + 1), seguinte) // ids 1..5 por ordem de rating: avança uma posição
    }

    @Test
    fun `depois de percorrer todos volta ao primeiro`() {
        val lista = candidatos(4)
        assertEquals(
            ProdutorDestaque.escolher(lista, semanaZero)?.id,
            ProdutorDestaque.escolher(lista, semanaZero.plusWeeks(4))?.id,
        )
    }

    @Test
    fun `so roda pelos primeiros do ranking, nunca por quem tem pior rating`() {
        val lista = candidatos(25)
        val escolhidos = (0L..60L).map { ProdutorDestaque.escolher(lista, semanaZero.plusWeeks(it))!!.id }.toSet()
        assertEquals((1L..ProdutorDestaque.TAMANHO_ROTACAO.toLong()).toSet(), escolhidos)
    }

    @Test
    fun `a rotacao nao salta na mudanca de ano`() {
        val lista = candidatos(7)
        // Segunda-feira 2025-12-29 (semana ISO 1 de 2026) e a seguinte: posições consecutivas.
        val a = ProdutorDestaque.escolher(lista, LocalDate.of(2025, 12, 22))!!.id
        val b = ProdutorDestaque.escolher(lista, LocalDate.of(2025, 12, 29))!!.id
        val c = ProdutorDestaque.escolher(lista, LocalDate.of(2026, 1, 5))!!.id
        assertEquals(b, a % 7 + 1)
        assertEquals(c, b % 7 + 1)
    }
}
