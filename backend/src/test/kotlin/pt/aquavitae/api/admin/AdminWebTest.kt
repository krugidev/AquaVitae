package pt.aquavitae.api.admin

import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Primary
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.AbstractPlatformTransactionManager
import org.springframework.transaction.support.DefaultTransactionStatus
import org.springframework.transaction.TransactionDefinition
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import pt.aquavitae.api.common.ResourceNotFoundException
import pt.aquavitae.api.lookup.BebidaCategoria
import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

// O painel de ponta a ponta SEM base de dados: o contexto Spring completo, a cadeia de segurança do /admin e as páginas
// Thymeleaf a desenhar-se de verdade (um erro numa expressão de um template só rebenta em execução), com o serviço de catálogo
// substituído por dados fixos. Não valida as queries contra o Oracle (isso é ao vivo).
@SpringBootTest(
    properties = [
        "spring.datasource.url=jdbc:oracle:thin:@//localhost:1/sem-bd",
        "spring.datasource.hikari.initialization-fail-timeout=-1",
        "spring.jpa.hibernate.ddl-auto=none",
        "spring.jpa.properties.hibernate.boot.allow_jdbc_metadata_access=false",
        "aquavitae.links.verificacao.ativo=false",
    ],
)
@AutoConfigureMockMvc
@Import(AdminWebTest.CatalogoFalso::class, AdminWebTest.ProdutorFalso::class)
class AdminWebTest {

    @TestConfiguration
    class CatalogoFalso {
        // O serviço real é `@Transactional` e o Spring põe um proxy à volta do falso também: sem BD, a transação tem de ser de faz-de-conta.
        @Bean
        @Primary
        fun transacoesDeFazDeConta(): PlatformTransactionManager = object : AbstractPlatformTransactionManager() {
            override fun doGetTransaction(): Any = Any()
            override fun doBegin(transaction: Any, definition: TransactionDefinition) {}
            override fun doCommit(status: DefaultTransactionStatus) {}
            override fun doRollback(status: DefaultTransactionStatus) {}
        }

        // Um serviço que responde por nome do método (sem matchers do Mockito, que não se dão bem com os parâmetros não nulos do Kotlin).
        @Bean
        @Primary
        fun catalogoFalso(): AdminCatalogoService = Mockito.mock(AdminCatalogoService::class.java) { invocacao ->
            when (invocacao.method.name) {
                "resumo" -> ResumoAdmin(
                    totalBebidas = 16, totalProdutores = 8, totalUtilizadores = 3, linksAtivos = 15, linksInativos = 2,
                    problemasBebidas = listOf(ContagemQualidade("sem-imagem", "Sem imagem", 4), ContagemQualidade("sem-ean", "Sem EAN", 0)),
                    problemasProdutores = listOf(ContagemQualidade("sem-historia", "Sem história", 8)),
                )
                "categorias" -> listOf(BebidaCategoria(1, "Vinho"), BebidaCategoria(2, "Gin"))
                "listarBebidas" -> PaginaAdmin(
                    itens = listOf(
                        BebidaAdminLinha(1, "Esporão Reserva Tinto 2018", "Vinho", "Herdade do Esporão", "Portugal", "5601234567890", "https://exemplo.pt/a.jpg", 2018, BigDecimal("14.5"), BigDecimal("750"), 1),
                        BebidaAdminLinha(2, "Gin 44°", "Gin", null, null, null, null, null, null, null, 0),
                    ),
                    pagina = 0, totalPaginas = 3, total = 51,
                )
                "listarProdutores" -> PaginaAdmin(
                    itens = listOf(ProdutorAdminLinha(1, "Herdade do Esporão", "Portugal", "Alentejo", 1973, null, null, null, null, null, 0, 3)),
                    pagina = 0, totalPaginas = 1, total = 1,
                )
                else -> null
            }
        }
    }

    // O mesmo para o serviço do formulário do produtor: `criar` recusa um nome em branco e aceita o resto (fica com o id 7).
    @TestConfiguration
    class ProdutorFalso {
        @Bean
        @Primary
        fun produtorFalso(): AdminProdutorService = Mockito.mock(AdminProdutorService::class.java) { invocacao ->
            when (invocacao.method.name) {
                "paises" -> listOf(OpcaoLookup(1, "Portugal"), OpcaoLookup(2, "Espanha"))
                "regioesDoPais" -> listOf(OpcaoLookup(10, "Douro"), OpcaoLookup(11, "Alentejo"))
                "carregar" -> if (invocacao.arguments[0] == 404L) throw ResourceNotFoundException("Produtor 404 não encontrado") else ProdutorParaEditar(
                    id = 7, nome = "Quinta do Vale Meão",
                    formulario = ProdutorFormulario(nome = "Quinta do Vale Meão", paisId = "1", regiaoId = "10", morada = "Foz Côa", permiteVisitas = true),
                    totalBebidas = 3,
                )
                "criar" -> resposta(invocacao.arguments[0] as ProdutorFormulario, 7)
                "atualizar" -> resposta(invocacao.arguments[1] as ProdutorFormulario, invocacao.arguments[0] as Long)
                else -> null
            }
        }

        private fun resposta(form: ProdutorFormulario, id: Long): ResultadoGravacao =
            if (form.nome.isBlank()) ResultadoGravacao.Invalido(mapOf("nome" to "O nome é obrigatório.")) else ResultadoGravacao.Guardado(id)
    }

    @Autowired
    lateinit var mvc: MockMvc

    private val admin = user("admin").roles("ADMIN")

    @Test
    fun `sem sessao o painel manda para o login`() {
        mvc.perform(get("/admin")).andExpect(status().is3xxRedirection).andExpect(header().string("Location", "http://localhost/admin/login"))
        mvc.perform(get("/admin/bebidas")).andExpect(status().is3xxRedirection)
    }

    @Test
    fun `a pagina de login abre sem sessao e traz o formulario com o token csrf`() {
        val html = mvc.perform(get("/admin/login")).andExpect(status().isOk).andReturn().response.contentAsString
        assertTrue("name=\"identificador\"" in html)
        assertTrue("name=\"_csrf\"" in html, "o formulário de login tem de levar o token CSRF")
        assertFalse("Credenciais inválidas" in html)
    }

    @Test
    fun `depois de um login falhado a pagina avisa`() {
        val html = mvc.perform(get("/admin/login?erro")).andExpect(status().isOk).andReturn().response.contentAsString
        assertTrue("Credenciais inválidas" in html)
    }

    @Test
    fun `o htmx do webjar e servido sem sessao e o css tambem`() {
        mvc.perform(get("/webjars/htmx.org/dist/htmx.min.js")).andExpect(status().isOk)
        mvc.perform(get("/admin/css/admin.css")).andExpect(status().isOk)
    }

    @Test
    fun `a API continua a pedir token e nao redireciona para o login do painel`() {
        mvc.perform(get("/api/bebidas/sugeridas")).andExpect(status().isUnauthorized)
    }

    @Test
    fun `um utilizador sem o papel Admin nao entra no painel`() {
        mvc.perform(get("/admin").with(user("ana").roles("UTILIZADOR"))).andExpect(status().isForbidden)
    }

    @Test
    fun `o painel mostra os totais e a qualidade do catalogo`() {
        val html = mvc.perform(get("/admin").with(admin)).andExpect(status().isOk).andReturn().response.contentAsString
        assertTrue("Qualidade do catálogo" in html)
        assertTrue("Sem imagem" in html && ">4<" in html, "a contagem de bebidas sem imagem")
        assertTrue("/admin/bebidas?qualidade=sem-imagem" in html, "cada linha leva à lista já filtrada")
        assertTrue("✓" in html, "0 problemas aparece como visto")
    }

    @Test
    fun `a lista de bebidas mostra as linhas, os avisos e a paginacao`() {
        val html = mvc.perform(get("/admin/bebidas").with(admin)).andExpect(status().isOk).andReturn().response.contentAsString
        assertTrue("<html" in html)
        assertTrue("Esporão Reserva Tinto 2018" in html)
        assertTrue("2018 · 14,5% · 750 ml" in html, "os detalhes usam a vírgula decimal")
        assertTrue("https://exemplo.pt/a.jpg" in html)
        assertTrue("sem produtor" in html && "sem link ativo" in html, "os avisos da bebida incompleta")
        assertTrue("51" in html && "Página" in html && "Seguinte" in html)
        assertTrue("<option value=\"1\">Vinho</option>" in html, "o filtro de categorias vem do lookup")
    }

    @Test
    fun `um pedido do htmx devolve so o bloco de resultados`() {
        val resposta = mvc.perform(get("/admin/bebidas?q=esporao").with(admin).header("HX-Request", "true"))
            .andExpect(status().isOk)
            .andExpect(header().string("Vary", "HX-Request"))
            .andReturn().response.contentAsString
        assertFalse("<html" in resposta, "o htmx só quer o bloco, não a página")
        assertTrue("id=\"resultado\"" in resposta && "Esporão Reserva Tinto 2018" in resposta)
    }

    @Test
    fun `o regresso pelo historico do htmx recebe a pagina inteira`() {
        val resposta = mvc.perform(get("/admin/bebidas").with(admin).header("HX-Request", "true").header("HX-History-Restore-Request", "true"))
            .andExpect(status().isOk).andReturn().response.contentAsString
        assertTrue("<html" in resposta)
    }

    @Test
    fun `a lista de produtores mostra as linhas e o que falta`() {
        val html = mvc.perform(get("/admin/produtores").with(admin)).andExpect(status().isOk).andReturn().response.contentAsString
        assertTrue("Herdade do Esporão" in html && "desde 1973" in html)
        assertTrue("sem imagem" in html && "sem história" in html && "sem morada" in html)
    }

    @Test
    fun `o formulario de novo produtor abre com os campos, as listas e o token csrf`() {
        val html = mvc.perform(get("/admin/produtores/novo").with(admin)).andExpect(status().isOk).andReturn().response.contentAsString
        assertTrue("Novo produtor" in html)
        assertTrue("name=\"nome\"" in html && "name=\"paisId\"" in html && "name=\"regiaoId\"" in html && "name=\"historia\"" in html)
        assertTrue("name=\"_csrf\"" in html, "o formulário de escrita tem de levar o token CSRF")
        assertTrue(">Portugal<" in html && ">Espanha<" in html)
        assertTrue("action=\"/admin/produtores\"" in html)
        assertFalse("Nada foi guardado" in html)
    }

    @Test
    fun `gravar um produtor novo redireciona para a edicao`() {
        mvc.perform(post("/admin/produtores").with(admin).with(csrf()).param("nome", "Esporão").param("paisId", "1"))
            .andExpect(status().is3xxRedirection)
            .andExpect(header().string("Location", "/admin/produtores/7?criado"))
    }

    @Test
    fun `um formulario com erros volta com o que foi escrito e a mensagem junto ao campo`() {
        val html = mvc.perform(post("/admin/produtores").with(admin).with(csrf()).param("nome", "").param("morada", "Rua Direita 12").param("permiteVisitas", "true"))
            .andExpect(status().isOk).andReturn().response.contentAsString
        assertTrue("O nome é obrigatório." in html)
        assertTrue("Nada foi guardado" in html)
        assertTrue("value=\"Rua Direita 12\"" in html, "o que se escreveu não se perde")
        assertTrue("checked" in html, "a caixa \"recebe visitas\" continua marcada")
    }

    @Test
    fun `sem o token csrf nao se grava, e um utilizador sem o papel Admin tambem nao`() {
        mvc.perform(post("/admin/produtores").with(admin).param("nome", "X").param("paisId", "1")).andExpect(status().isForbidden)
        mvc.perform(post("/admin/produtores").with(user("ana").roles("UTILIZADOR")).with(csrf()).param("nome", "X")).andExpect(status().isForbidden)
    }

    @Test
    fun `o formulario de edicao vem preenchido e mostra o aviso depois de gravar`() {
        val html = mvc.perform(get("/admin/produtores/7?guardado").with(admin)).andExpect(status().isOk).andReturn().response.contentAsString
        assertTrue("Quinta do Vale Meão" in html)
        assertTrue("3 bebidas" in html && "/admin/bebidas?q=Quinta" in html)
        assertTrue("value=\"Foz Côa\"" in html)
        assertTrue("Alterações guardadas." in html)
        assertTrue("action=\"/admin/produtores/7\"" in html)
        assertTrue("<option value=\"10\" selected=\"selected\">Douro</option>" in html, "a região guardada vem escolhida")
    }

    @Test
    fun `um produtor que nao existe da uma pagina 404 do painel, nao o JSON da API`() {
        val resposta = mvc.perform(get("/admin/produtores/404").with(admin)).andExpect(status().isNotFound).andReturn().response
        assertTrue("Produtor 404 não encontrado" in resposta.contentAsString)
        assertTrue("<html" in resposta.contentAsString && "Voltar ao painel" in resposta.contentAsString)
    }

    @Test
    fun `atualizar um produtor redireciona com o aviso`() {
        mvc.perform(post("/admin/produtores/7").with(admin).with(csrf()).param("nome", "Novo nome").param("paisId", "1"))
            .andExpect(status().is3xxRedirection)
            .andExpect(header().string("Location", "/admin/produtores/7?guardado"))
    }

    @Test
    fun `o htmx pede as regioes do pais e recebe so as opcoes`() {
        val resposta = mvc.perform(get("/admin/produtores/regioes?paisId=1").with(admin).header("HX-Request", "true"))
            .andExpect(status().isOk).andReturn().response.contentAsString
        assertFalse("<html" in resposta || "<select" in resposta)
        assertTrue(">Douro<" in resposta && ">Alentejo<" in resposta && "sem região" in resposta)
    }

    @Test
    fun `um pedido do htmx com a sessao expirada manda o browser para o login`() {
        mvc.perform(get("/admin/bebidas").header("HX-Request", "true"))
            .andExpect(status().isUnauthorized)
            .andExpect(header().string("HX-Redirect", "/admin/login"))
    }
}
