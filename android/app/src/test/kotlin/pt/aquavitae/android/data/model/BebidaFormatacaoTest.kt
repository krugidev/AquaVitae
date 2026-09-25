package pt.aquavitae.android.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

private fun bebida(
    categoriaNome: String? = "Vinho",
    tipo: String? = null,
    corpo: String? = null,
    nivelAcidez: Int? = null,
    nivelDocura: Int? = null,
    tanino: String? = null,
    nome: String? = "Bebida Exemplo",
) = BebidaSummary(
    id = 1,
    nome = nome,
    categoriaNome = categoriaNome,
    produtorNome = null,
    ratingMedio = 0.0,
    totalReviews = 0,
    imagePath = null,
    corpo = corpo,
    nivelAcidez = nivelAcidez,
    nivelDocura = nivelDocura,
    tipo = tipo,
    tanino = tanino,
)

class BebidaFormatacaoTest {

    @Test
    fun `ano no fim do nome`() {
        assertEquals("2015", bebida(nome = "Barca Velha 2015").anoNoNome())
        assertEquals("2021", bebida(nome = "Soalheiro Alvarinho 2021").anoNoNome())
    }

    @Test
    fun `sem ano no fim do nome fica nulo`() {
        assertNull(bebida(nome = "Licor Beirão").anoNoNome())
        assertNull(bebida(nome = null).anoNoNome())
    }

    @Test
    fun `tinto com corpo e tanino mostra os dois`() {
        assertEquals(
            "VINHO TINTO • CORPO ENCORPADO • TANINO ELEVADO",
            bebida(tipo = "Tinto", corpo = "Encorpado", tanino = "Elevado", nivelAcidez = 3).linhaAtributos(),
        )
    }

    @Test
    fun `branco com corpo e acidez usa acidez em vez de tanino`() {
        assertEquals(
            "VINHO BRANCO • CORPO LEVE • ACIDEZ 4",
            bebida(tipo = "Branco", corpo = "Leve", nivelAcidez = 4, tanino = "Suave").linhaAtributos(),
        )
    }

    @Test
    fun `sem corpo cai para acidez e docura`() {
        assertEquals(
            "VINHO BRANCO • ACIDEZ 3 • DOÇURA 1",
            bebida(tipo = "Branco", nivelAcidez = 3, nivelDocura = 1).linhaAtributos(),
        )
    }

    @Test
    fun `sem nenhum atributo mostra so a categoria e o tipo`() {
        assertEquals("VINHO BRANCO", bebida(tipo = "Branco").linhaAtributos())
    }

    @Test
    fun `categoria sem tipo de vinho (outras categorias)`() {
        assertEquals("WHISKY", bebida(categoriaNome = "Whisky").linhaAtributos())
    }

    @Test
    fun `nunca mostra mais de 2 atributos alem do cabecalho`() {
        val linha = bebida(tipo = "Tinto", corpo = "Encorpado", tanino = "Elevado", nivelDocura = 1).linhaAtributos()
        assertEquals("VINHO TINTO • CORPO ENCORPADO • TANINO ELEVADO", linha)
    }

    @Test
    fun `o numero de reviews tem singular e plural`() {
        assertEquals("0 reviews", textoReviews(0))
        assertEquals("1 review", textoReviews(1))
        assertEquals("2 reviews", textoReviews(2))
    }
}
