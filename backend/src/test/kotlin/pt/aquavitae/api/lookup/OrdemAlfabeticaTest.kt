package pt.aquavitae.api.lookup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class OrdemAlfabeticaTest {

    private val paises = listOf("Zimbábue", "Índia", "África do Sul", "Albânia", "Áustria", "Argélia", "Portugal", "Óbidos")

    @Test
    fun `letras acentuadas ordenam com a letra base`() {
        assertEquals(
            listOf("África do Sul", "Albânia", "Argélia", "Áustria", "Índia", "Óbidos", "Portugal", "Zimbábue"),
            paises.ordenadoPorNome { it },
        )
    }

    // É o comportamento que o Oracle (ordenação binária) daria e que esta função evita.
    @Test
    fun `a ordem binaria poria os acentuados depois do Z`() {
        assertNotEquals(paises.sorted(), paises.ordenadoPorNome { it })
        assertEquals("Óbidos", paises.sorted().last())
        assertEquals("Zimbábue", paises.ordenadoPorNome { it }.last())
    }

    @Test
    fun `o destaque fica no topo e o resto por ordem alfabetica`() {
        assertEquals(
            listOf("Portugal", "África do Sul", "Albânia", "Argélia", "Áustria", "Índia", "Óbidos", "Zimbábue"),
            paises.ordenadoPorNomeComDestaque("Portugal") { it },
        )
    }

    @Test
    fun `sem o destaque na lista fica so a ordem alfabetica`() {
        val semPortugal = paises - "Portugal"
        assertEquals(semPortugal.ordenadoPorNome { it }, semPortugal.ordenadoPorNomeComDestaque("Portugal") { it })
    }

    @Test
    fun `o destaque compara o nome exato e nao um prefixo`() {
        val lista = listOf("Portugal Continental", "Espanha", "Portugal")
        assertEquals(listOf("Portugal", "Espanha", "Portugal Continental"), lista.ordenadoPorNomeComDestaque("Portugal") { it })
    }

    @Test
    fun `nomes nulos ficam primeiro sem rebentar`() {
        val itens = listOf<String?>("Douro", null, "Alentejo")
        assertEquals(listOf(null, "Alentejo", "Douro"), itens.ordenadoPorNome { it })
    }

    @Test
    fun `usa o seletor dado e nao altera a lista original`() {
        data class Casta(val id: Int, val nome: String)
        val castas = listOf(Casta(1, "Touriga Nacional"), Casta(2, "Aragonez (Tinta Roriz)"), Casta(3, "Alvarinho"))
        assertEquals(listOf(3, 2, 1), castas.ordenadoPorNome { it.nome }.map { it.id })
        assertEquals(listOf(1, 2, 3), castas.map { it.id })
    }
}
