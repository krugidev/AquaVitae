package pt.aquavitae.api.cave

import pt.aquavitae.api.common.ConflictException
import pt.aquavitae.api.common.PedidoInvalidoException
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotSame
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class CaveRegrasTest {

    private val hoje = LocalDate.of(2026, 9, 19)

    private fun nova(
        id: Long,
        quantidade: Int = 1,
        preco: String? = null,
        inicio: String? = null,
        fim: String? = null,
        consumida: Boolean = false,
    ) = CaveBebida(
        id = id,
        quantidade = quantidade,
        precoPago = preco?.let(::BigDecimal),
        janelaInicio = inicio?.let(LocalDate::parse),
        janelaFim = fim?.let(LocalDate::parse),
        isConsumida = consumida,
    )

    private fun estado(inicio: String?, fim: String?, consumida: Boolean = false) =
        EstadoCaveBebida.calcular(consumida, inicio?.let(LocalDate::parse), fim?.let(LocalDate::parse), hoje)

    // --- estado -------------------------------------------------------------------------------------

    @Test
    fun `sem janela fica em guarda`() = assertEquals(EstadoCaveBebida.EM_GUARDA, estado(null, null))

    @Test
    fun `antes de a janela comecar fica em guarda`() =
        assertEquals(EstadoCaveBebida.EM_GUARDA, estado("2026-09-20", "2027-01-01"))

    @Test
    fun `no primeiro dia da janela ja esta pronta`() =
        assertEquals(EstadoCaveBebida.PRONTA, estado("2026-09-19", "2027-01-01"))

    @Test
    fun `dentro da janela esta pronta`() = assertEquals(EstadoCaveBebida.PRONTA, estado("2026-06-01", "2027-01-01"))

    @Test
    fun `no ultimo dia da janela ainda esta pronta`() =
        assertEquals(EstadoCaveBebida.PRONTA, estado("2026-01-01", "2026-09-19"))

    @Test
    fun `no dia a seguir ao fim da janela esta em atraso`() =
        assertEquals(EstadoCaveBebida.EM_ATRASO, estado("2026-01-01", "2026-09-18"))

    @Test
    fun `so com inicio ja passado esta pronta e nunca fica em atraso`() =
        assertEquals(EstadoCaveBebida.PRONTA, estado("2020-01-01", null))

    @Test
    fun `so com inicio futuro fica em guarda`() = assertEquals(EstadoCaveBebida.EM_GUARDA, estado("2027-01-01", null))

    @Test
    fun `so com fim por chegar esta pronta`() = assertEquals(EstadoCaveBebida.PRONTA, estado(null, "2026-12-31"))

    @Test
    fun `so com fim passado esta em atraso`() = assertEquals(EstadoCaveBebida.EM_ATRASO, estado(null, "2026-01-31"))

    @Test
    fun `consumida e sempre consumida`() =
        assertEquals(EstadoCaveBebida.CONSUMIDA, estado("2026-01-01", "2026-01-02", consumida = true))

    // --- totais -------------------------------------------------------------------------------------

    @Test
    fun `totais contam garrafas por consumir, o valor pago e as prontas incluindo as em atraso`() {
        val linhas = listOf(
            nova(1, quantidade = 2, preco = "10.50", inicio = "2026-01-01", fim = "2027-01-01"), // pronta
            nova(2, quantidade = 1, preco = "20.00", inicio = "2025-01-01", fim = "2026-01-01"), // em atraso
            nova(3, quantidade = 3), // sem janela e sem preço: em guarda, não entra no valor
            nova(4, quantidade = 5, preco = "100.00", consumida = true), // histórico: não conta
        )

        val resumo = CaveRegras.resumir(linhas, hoje)

        assertEquals(6, resumo.totalGarrafas)
        assertEquals(BigDecimal("41.00"), resumo.valorTotal)
        assertEquals(3, resumo.totalProntasAAbrir)
    }

    @Test
    fun `cave vazia tem totais a zero`() {
        val resumo = CaveRegras.resumir(emptyList(), hoje)
        assertEquals(0, resumo.totalGarrafas)
        assertEquals(BigDecimal("0.00"), resumo.valorTotal)
        assertEquals(0, resumo.totalProntasAAbrir)
    }

    // --- ordenação ----------------------------------------------------------------------------------

    @Test
    fun `prontas por data de consumo - primeiro o prazo mais proximo (ou ja passado), sem prazo no fim`() {
        val linhas = listOf(
            nova(1, fim = "2026-12-01"),
            nova(2, fim = null),
            nova(3, fim = "2026-10-01"),
            nova(4, fim = "2026-09-01"), // em atraso: o mais urgente
        )
        assertEquals(listOf(4L, 3L, 1L, 2L), CaveRegras.ordenarProntas(linhas, CaveOrdenacao.DATA_CONSUMO).map { it.id })
    }

    @Test
    fun `em guarda por data de consumo - primeiro o que fica pronto mais cedo, sem janela no fim`() {
        val linhas = listOf(
            nova(1, inicio = "2027-03-01"),
            nova(2),
            nova(3, inicio = "2026-11-01"),
        )
        assertEquals(listOf(3L, 1L, 2L), CaveRegras.ordenarEmGuarda(linhas, CaveOrdenacao.DATA_CONSUMO).map { it.id })
    }

    @Test
    fun `por preco - o mais caro primeiro, sem preco no fim, id a desempatar`() {
        val linhas = listOf(
            nova(1, preco = "10.00"),
            nova(2),
            nova(3, preco = "30.00"),
            nova(4, preco = "10.00"),
        )
        assertEquals(listOf(3L, 1L, 4L, 2L), CaveRegras.ordenarProntas(linhas, CaveOrdenacao.PRECO).map { it.id })
        assertEquals(listOf(3L, 1L, 4L, 2L), CaveRegras.ordenarEmGuarda(linhas, CaveOrdenacao.PRECO).map { it.id })
    }

    @Test
    fun `o parametro sort aceita preco e dataConsumo e rejeita o resto`() {
        assertEquals(CaveOrdenacao.DATA_CONSUMO, CaveOrdenacao.de(null))
        assertEquals(CaveOrdenacao.DATA_CONSUMO, CaveOrdenacao.de("dataConsumo"))
        assertEquals(CaveOrdenacao.PRECO, CaveOrdenacao.de("preco"))
        assertEquals(CaveOrdenacao.PRECO, CaveOrdenacao.de("PRECO"))
        assertFailsWith<PedidoInvalidoException> { CaveOrdenacao.de("nome") }
    }

    // --- janela -------------------------------------------------------------------------------------

    @Test
    fun `a janela nao pode acabar antes de comecar`() {
        assertFailsWith<PedidoInvalidoException> {
            CaveRegras.validarJanela(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 9, 30))
        }
        CaveRegras.validarJanela(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 1))
        CaveRegras.validarJanela(null, LocalDate.of(2026, 10, 1))
        CaveRegras.validarJanela(LocalDate.of(2026, 10, 1), null)
        CaveRegras.validarJanela(null, null)
    }

    // --- consumo ------------------------------------------------------------------------------------

    @Test
    fun `consumir uma de varias decrementa e guarda a consumida numa linha propria`() {
        val linha = nova(7, quantidade = 3, preco = "12.50", inicio = "2026-01-01", fim = "2027-01-01")
        linha.dataAquisicao = LocalDate.of(2025, 5, 5)
        linha.notas = "notas da compra"

        val consumo = CaveRegras.consumirUma(linha, LocalDate.of(2026, 9, 18), "estava ótimo", Instant.parse("2026-09-19T10:00:00Z"))

        assertEquals(2, consumo.restantes)
        assertEquals(2, linha.quantidade)
        assertFalse(linha.isConsumida)
        assertNull(linha.dataConsumo)

        val consumida = consumo.consumida
        assertNotSame(linha, consumida)
        assertEquals(0L, consumida.id) // ainda por gravar
        assertEquals(1, consumida.quantidade)
        assertTrue(consumida.isConsumida)
        assertEquals(LocalDate.of(2026, 9, 18), consumida.dataConsumo)
        assertEquals("estava ótimo", consumida.notas) // as notas do consumo, não as da compra
        assertEquals(BigDecimal("12.50"), consumida.precoPago)
        assertEquals(LocalDate.of(2025, 5, 5), consumida.dataAquisicao)
        assertEquals(LocalDate.of(2026, 1, 1), consumida.janelaInicio)
        assertEquals(LocalDate.of(2027, 1, 1), consumida.janelaFim)
    }

    @Test
    fun `consumir a ultima garrafa marca a propria linha como consumida`() {
        val linha = nova(8, quantidade = 1, preco = "9.00")
        linha.notas = "notas da compra"

        val consumo = CaveRegras.consumirUma(linha, LocalDate.of(2026, 9, 19), null, Instant.now())

        assertEquals(0, consumo.restantes)
        assertSame(linha, consumo.consumida)
        assertTrue(linha.isConsumida)
        assertEquals(1, linha.quantidade)
        assertEquals(LocalDate.of(2026, 9, 19), linha.dataConsumo)
        assertEquals("notas da compra", linha.notas) // sem notas novas, mantém as que tinha
    }

    @Test
    fun `consumir a ultima garrafa com notas novas substitui as notas`() {
        val linha = nova(9, quantidade = 1)
        linha.notas = "antigas"
        CaveRegras.consumirUma(linha, hoje, "novas", Instant.now())
        assertEquals("novas", linha.notas)
    }

    @Test
    fun `uma garrafa ja consumida nao se consome outra vez`() {
        val linha = nova(10, quantidade = 1, consumida = true)
        assertFailsWith<ConflictException> { CaveRegras.consumirUma(linha, hoje, null, Instant.now()) }
    }
}
