package pt.aquavitae.android.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

private fun produtor(
    nome: String? = "Casa Ferreirinha",
    website: String? = null,
    latitude: Double? = null,
    longitude: Double? = null,
    morada: String? = null,
) = ProdutorDetail(
    id = 1,
    nome = nome,
    paisNome = "Portugal",
    regiaoId = 1,
    regiao = "Douro",
    historia = null,
    anoFundacao = 1751,
    website = website,
    imagePath = null,
    morada = morada,
    latitude = latitude,
    longitude = longitude,
    permiteVisitas = true,
)

class ProdutorFormatacaoTest {

    @Test
    fun `o site mostra so o dominio`() {
        assertEquals("ferreirinha.pt", produtor(website = "https://www.ferreirinha.pt").websiteParaMostrar())
        assertEquals("quintadocrasto.pt", produtor(website = "http://quintadocrasto.pt/").websiteParaMostrar())
        assertEquals("niepoort-vinhos.com", produtor(website = "https://www.niepoort-vinhos.com/pt/vinhos?x=1").websiteParaMostrar())
        assertEquals("sharish.pt", produtor(website = "  sharish.pt  ").websiteParaMostrar())
    }

    @Test
    fun `sem site nao ha pilula`() {
        assertNull(produtor(website = null).websiteParaMostrar())
        assertNull(produtor(website = "   ").websiteParaMostrar())
        assertNull(produtor(website = "https://").websiteParaMostrar())
    }

    @Test
    fun `o url do site ganha https quando so ha o dominio`() {
        assertEquals("https://www.esporao.com", produtor(website = "https://www.esporao.com").websiteUrl())
        assertEquals("http://exemplo.pt", produtor(website = "http://exemplo.pt").websiteUrl())
        assertEquals("https://luispato.com", produtor(website = "luispato.com").websiteUrl())
        assertNull(produtor(website = "").websiteUrl())
    }

    @Test
    fun `so ha mapa com as duas coordenadas`() {
        assertTrue(produtor(latitude = 41.1621, longitude = -7.7891).temCoordenadas)
        assertFalse(produtor(latitude = 41.1621).temCoordenadas)
        assertFalse(produtor(longitude = -7.7891).temCoordenadas)
        assertFalse(produtor().temCoordenadas)
        assertNull(produtor(latitude = 41.1621).coordenadasTexto())
        assertNull(produtor().geoUri())
        assertNull(produtor().mapaWebUrl())
    }

    @Test
    fun `as coordenadas levam ponto decimal e 4 casas`() {
        assertEquals("41.1621, -7.7891", produtor(latitude = 41.16214, longitude = -7.78906).coordenadasTexto())
    }

    @Test
    fun `o uri geo leva o ponto e o nome escapado`() {
        val geo = produtor(nome = "Casa Ferreirinha", latitude = 41.1621, longitude = -7.7891).geoUri()
        assertEquals("geo:41.162100,-7.789100?q=41.162100,-7.789100(Casa%20Ferreirinha)", geo)
    }

    @Test
    fun `o uri geo escapa acentos e parentesis do nome`() {
        val geo = produtor(nome = "Quinta (Velha) do Vale Meão", latitude = 41.0, longitude = -7.0).geoUri()
        assertEquals("geo:41.000000,-7.000000?q=41.000000,-7.000000(Quinta%20%28Velha%29%20do%20Vale%20Me%C3%A3o)", geo)
    }

    @Test
    fun `o uri geo sem nome fica so com o ponto`() {
        assertEquals("geo:41.000000,-7.000000?q=41.000000,-7.000000", produtor(nome = null, latitude = 41.0, longitude = -7.0).geoUri())
    }

    @Test
    fun `o mapa web usa o mesmo ponto`() {
        assertEquals(
            "https://www.google.com/maps/search/?api=1&query=41.162100,-7.789100",
            produtor(latitude = 41.1621, longitude = -7.7891).mapaWebUrl(),
        )
    }

    @Test
    fun `a morada limpa os espacos e em branco conta como nao ha`() {
        assertEquals("Rua Direita 12, 5050-237 Peso da Régua", produtor(morada = "  Rua Direita 12, 5050-237 Peso da Régua  ").moradaParaMostrar)
        assertNull(produtor(morada = "   ").moradaParaMostrar)
        assertNull(produtor(morada = null).moradaParaMostrar)
    }

    @Test
    fun `a localizacao existe com morada ou com coordenadas`() {
        assertTrue(produtor(morada = "Rua Direita 12").temLocalizacao)
        assertTrue(produtor(latitude = 41.1621, longitude = -7.7891).temLocalizacao)
        assertTrue(produtor(morada = "Rua Direita 12", latitude = 41.1621, longitude = -7.7891).temLocalizacao)
        assertFalse(produtor().temLocalizacao)
        assertFalse(produtor(morada = "  ").temLocalizacao)
        // Uma coordenada sozinha não é um ponto.
        assertFalse(produtor(latitude = 41.1621).temLocalizacao)
    }

    @Test
    fun `sem coordenadas o mapa pesquisa pela morada`() {
        val p = produtor(morada = "Rua Direita 12, 5050-237 Peso da Régua")
        assertEquals("geo:0,0?q=Rua%20Direita%2012%2C%205050-237%20Peso%20da%20R%C3%A9gua", p.geoUri())
        assertEquals(
            "https://www.google.com/maps/search/?api=1&query=Rua%20Direita%2012%2C%205050-237%20Peso%20da%20R%C3%A9gua",
            p.mapaWebUrl(),
        )
    }

    @Test
    fun `as coordenadas ganham a morada no mapa porque sao o ponto exato da quinta`() {
        val p = produtor(nome = "Casa Ferreirinha", morada = "Rua Direita 12", latitude = 41.1621, longitude = -7.7891)
        assertEquals("geo:41.162100,-7.789100?q=41.162100,-7.789100(Casa%20Ferreirinha)", p.geoUri())
        assertEquals("https://www.google.com/maps/search/?api=1&query=41.162100,-7.789100", p.mapaWebUrl())
    }

    @Test
    fun `garrafas no singular e no plural`() {
        assertEquals("1 garrafa", garrafasTexto(1))
        assertEquals("3 garrafas", garrafasTexto(3))
        assertEquals("0 garrafas", garrafasTexto(0))
    }
}
