package pt.aquavitae.android.ui.components

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import pt.aquavitae.android.data.model.CodigoInfo

class CodigoContagemTest {

    // O servidor manda 15 min de validade e 60 s até se poder pedir outro; a app arranca os contadores à chegada da resposta.
    private val contagem = CodigoContagem.aPartirDe(CodigoInfo(validadeSegundos = 900, novoPedidoEmSegundos = 60), agoraMs = 10_000)

    @Test
    fun `os contadores arrancam a partir do momento da resposta`() {
        assertEquals(10_000 + 900_000, contagem.expiraEmMs)
        assertEquals(10_000 + 60_000, contagem.novoPedidoEmMs)
        assertEquals(900, contagem.segundosParaExpirar(agoraMs = 10_000))
        assertEquals(60, contagem.segundosParaNovoPedido(agoraMs = 10_000))
    }

    @Test
    fun `os segundos arredondam para cima e so chegam a zero quando acabou mesmo`() {
        assertEquals(900, contagem.segundosParaExpirar(agoraMs = 10_500)) // faltam 899,5 s: mostra 15:00, não 14:59
        assertEquals(899, contagem.segundosParaExpirar(agoraMs = 11_000)) // faltam 899 s certos
        assertEquals(1, contagem.segundosParaExpirar(agoraMs = 10_000 + 899_001)) // falta 1 ms
        assertEquals(0, contagem.segundosParaExpirar(agoraMs = 10_000 + 900_000))
        assertEquals(0, contagem.segundosParaExpirar(agoraMs = 10_000 + 950_000)) // nunca negativo
    }

    @Test
    fun `o codigo expira ao fim da validade`() {
        assertFalse(contagem.expirou(agoraMs = 10_000 + 899_999))
        assertTrue(contagem.expirou(agoraMs = 10_000 + 900_000))
    }

    @Test
    fun `so se pode pedir outro depois do intervalo`() {
        assertFalse(contagem.podePedirNovo(agoraMs = 10_000 + 59_999))
        assertTrue(contagem.podePedirNovo(agoraMs = 10_000 + 60_000))
        assertEquals(45, contagem.segundosParaNovoPedido(agoraMs = 10_000 + 15_000))
    }

    @Test
    fun `os minutos e os segundos escrevem-se com dois digitos`() {
        assertEquals("15:00", formatarContagem(900))
        assertEquals("14:32", formatarContagem(872))
        assertEquals("00:07", formatarContagem(7))
        assertEquals("00:00", formatarContagem(0))
        assertEquals("00:00", formatarContagem(-5))
        assertEquals("61:05", formatarContagem(3665))
    }

    @Test
    fun `as estrelas de uma nota ficam cheias, meias ou vazias`() {
        val notas = mapOf(5.0 to "CCCCC", 4.5 to "CCCCM", 4.0 to "CCCCV", 3.5 to "CCCMV", 0.5 to "MVVVV", 0.0 to "VVVVV", 4.4 to "CCCCV", 4.9 to "CCCCM")
        notas.forEach { (nota, esperado) ->
            val obtido = (1..5).joinToString("") {
                when (tipoDaEstrela(nota, it)) {
                    TipoEstrela.Cheia -> "C"
                    TipoEstrela.Meia -> "M"
                    TipoEstrela.Vazia -> "V"
                }
            }
            assertEquals("nota $nota", esperado, obtido)
        }
    }
}
