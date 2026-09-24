package pt.aquavitae.api.admin

import org.mockito.Mockito.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import pt.aquavitae.api.bebida.BebidaRepository
import pt.aquavitae.api.common.ResourceNotFoundException
import pt.aquavitae.api.lookup.ProdutorPais
import pt.aquavitae.api.lookup.ProdutorPaisRepository
import pt.aquavitae.api.lookup.Regiao
import pt.aquavitae.api.lookup.RegiaoRepository
import pt.aquavitae.api.produtor.Produtor
import pt.aquavitae.api.produtor.ProdutorRepository
import java.math.BigDecimal
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.util.Optional
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

// O serviço do formulário do produtor com os repositórios de mentira (só interfaces do Spring Data, por isso os argumentos são
// concretos ou `any()` — os métodos Java aceitam o null do `any()`; os do Kotlin levam valores a sério).
class AdminProdutorServiceTest {

    private val agora = Instant.parse("2026-09-24T12:00:00Z")
    private val relogio = Clock.fixed(agora, ZoneId.of("Europe/Lisbon"))

    private val produtores = mock(ProdutorRepository::class.java)
    private val listas = mock(AdminProdutorRepository::class.java)
    private val paises = mock(ProdutorPaisRepository::class.java)
    private val regioes = mock(RegiaoRepository::class.java)
    private val bebidas = mock(BebidaRepository::class.java)
    private val servico = AdminProdutorService(produtores, listas, paises, regioes, bebidas, relogio)

    private val portugal = ProdutorPais(1, "Portugal")
    private val espanha = ProdutorPais(2, "Espanha")
    private val douro = Regiao(10, "Douro", portugal)
    private val rioja = Regiao(20, "Rioja", espanha)

    private var ultimoGuardado: Produtor? = null

    private val form = ProdutorFormulario(nome = "  Quinta  do Vale Meão ", paisId = "1", regiaoId = "10", latitude = "41,1621", longitude = "-7.7891")

    init {
        `when`(paises.findById(1L)).thenReturn(Optional.of(portugal))
        `when`(paises.findById(2L)).thenReturn(Optional.of(espanha))
        `when`(paises.findById(99L)).thenReturn(Optional.empty())
        `when`(regioes.findById(10L)).thenReturn(Optional.of(douro))
        `when`(regioes.findById(20L)).thenReturn(Optional.of(rioja))
        `when`(regioes.findById(88L)).thenReturn(Optional.empty())
        // Uma gravação a sério dá o id ao produtor (identity); aqui é o 5.
        `when`(produtores.save(any(Produtor::class.java))).thenAnswer { invocacao ->
            (invocacao.arguments[0] as Produtor).also { if (it.id == 0L) it.id = 5; ultimoGuardado = it }
        }
    }

    @Test
    fun `criar guarda o produtor com os dados limpos e a data de criacao`() {
        `when`(listas.idsComNome("QUINTA DO VALE MEAO", -1L)).thenReturn(emptyList())

        val resultado = servico.criar(form)

        assertEquals(ResultadoGravacao.Guardado(5), resultado)
        val guardado = assertNotNull(ultimoGuardado)
        assertEquals("Quinta do Vale Meão", guardado.nome)
        assertEquals(portugal, guardado.pais)
        assertEquals(douro, guardado.regiao)
        assertEquals(BigDecimal("41.162100"), guardado.latitude)
        assertEquals(BigDecimal("-7.789100"), guardado.longitude)
        assertEquals(agora, guardado.dataCriacao)
        assertNull(guardado.website)
    }

    @Test
    fun `um nome repetido (sem contar acentos nem maiusculas) nao grava`() {
        `when`(listas.idsComNome("QUINTA DO VALE MEAO", -1L)).thenReturn(listOf(8L))

        val resultado = servico.criar(form.copy(nome = "quinta do vale meao"))

        val invalido = assertNotNull(resultado as? ResultadoGravacao.Invalido)
        assertTrue("id 8" in invalido.erros.getValue("nome"), invalido.erros.toString())
        verify(produtores, never()).save(any(Produtor::class.java))
    }

    @Test
    fun `uma regiao de outro pais nao grava`() {
        `when`(listas.idsComNome("QUINTA DO VALE MEAO", -1L)).thenReturn(emptyList())

        val resultado = servico.criar(form.copy(regiaoId = "20"))

        val invalido = assertNotNull(resultado as? ResultadoGravacao.Invalido)
        assertEquals(setOf("regiaoId"), invalido.erros.keys)
        verify(produtores, never()).save(any(Produtor::class.java))
    }

    @Test
    fun `um pais ou uma regiao que nao existem nao gravam`() {
        `when`(listas.idsComNome("QUINTA DO VALE MEAO", -1L)).thenReturn(emptyList())

        val semPais = assertNotNull(servico.criar(form.copy(paisId = "99")) as? ResultadoGravacao.Invalido)
        assertTrue(semPais.erros.containsKey("paisId"))
        val semRegiao = assertNotNull(servico.criar(form.copy(regiaoId = "88")) as? ResultadoGravacao.Invalido)
        assertTrue(semRegiao.erros.containsKey("regiaoId"))
        verify(produtores, never()).save(any(Produtor::class.java))
    }

    @Test
    fun `os erros do formulario chegam sem ir a BD`() {
        val resultado = servico.criar(ProdutorFormulario())
        assertEquals(setOf("nome", "paisId"), (resultado as ResultadoGravacao.Invalido).erros.keys)
        verify(produtores, never()).save(any(Produtor::class.java))
    }

    @Test
    fun `atualizar muda o produtor que existe e ignora-o a ele proprio no nome repetido`() {
        val existente = Produtor(id = 7, nome = "Antigo", pais = espanha, regiao = rioja, permiteVisitas = false)
        `when`(produtores.existsById(7L)).thenReturn(true)
        `when`(produtores.findById(7L)).thenReturn(Optional.of(existente))
        `when`(listas.idsComNome("QUINTA DO VALE MEAO", 7L)).thenReturn(emptyList())

        val resultado = servico.atualizar(7, form.copy(permiteVisitas = true, website = "www.valemeao.pt"))

        assertEquals(ResultadoGravacao.Guardado(7), resultado)
        assertEquals("Quinta do Vale Meão", existente.nome)
        assertEquals(portugal, existente.pais)
        assertEquals(douro, existente.regiao)
        assertEquals("https://www.valemeao.pt", existente.website)
        assertTrue(existente.permiteVisitas)
        assertNull(existente.dataCriacao, "a data de criação não se mexe numa edição")
    }

    @Test
    fun `atualizar com o nome de OUTRO produtor nao grava nem toca no produtor`() {
        val existente = Produtor(id = 7, nome = "Antigo", pais = portugal, regiao = douro)
        `when`(produtores.existsById(7L)).thenReturn(true)
        `when`(produtores.findById(7L)).thenReturn(Optional.of(existente))
        `when`(listas.idsComNome("QUINTA DO VALE MEAO", 7L)).thenReturn(listOf(8L))

        val resultado = servico.atualizar(7, form)

        val invalido = assertNotNull(resultado as? ResultadoGravacao.Invalido)
        assertTrue("id 8" in invalido.erros.getValue("nome"))
        assertEquals("Antigo", existente.nome, "o produtor não pode ser mudado a meio de uma validação falhada")
        verify(produtores, never()).save(any(Produtor::class.java))
    }

    @Test
    fun `atualizar um produtor que nao existe e 404`() {
        `when`(produtores.existsById(404L)).thenReturn(false)
        assertFailsWith<ResourceNotFoundException> { servico.atualizar(404, form) }
    }

    @Test
    fun `carregar devolve o formulario preenchido e o total de bebidas`() {
        val existente = Produtor(
            id = 7, nome = "Quinta do Vale Meão", pais = portugal, regiao = douro, anoFundacao = 1877, website = "https://valemeao.pt",
            pathImagem = "/icones/x.svg", morada = "Foz Côa", latitude = BigDecimal("41.100000"), longitude = BigDecimal("-7.100000"),
            historia = "Uma história.", permiteVisitas = true,
        )
        `when`(produtores.findByIdWithPais(7L)).thenReturn(Optional.of(existente))
        `when`(bebidas.countByProdutor_Id(7L)).thenReturn(3L)

        val p = servico.carregar(7)

        assertEquals(3L, p.totalBebidas)
        assertEquals(ProdutorFormulario("Quinta do Vale Meão", "1", "10", "1877", "https://valemeao.pt", "/icones/x.svg", "Foz Côa", "41.100000", "-7.100000", "Uma história.", true), p.formulario)
        `when`(produtores.findByIdWithPais(404L)).thenReturn(Optional.empty())
        assertFailsWith<ResourceNotFoundException> { servico.carregar(404) }
    }

    @Test
    fun `os paises vem com Portugal primeiro e as regioes por ordem alfabetica`() {
        `when`(paises.findAll()).thenReturn(listOf(ProdutorPais(3, "Alemanha"), ProdutorPais(1, "Portugal"), ProdutorPais(2, "Espanha")))
        assertEquals(listOf("Portugal", "Alemanha", "Espanha"), servico.paises().map { it.nome })

        `when`(regioes.findByPais_Id(1L)).thenReturn(listOf(Regiao(2, "Tejo", portugal), Regiao(3, "Ábidos", portugal), Regiao(1, "Douro", portugal)))
        assertEquals(listOf("Ábidos", "Douro", "Tejo"), servico.regioesDoPais(1L).map { it.nome })
        assertEquals(emptyList(), servico.regioesDoPais(null))
    }
}
