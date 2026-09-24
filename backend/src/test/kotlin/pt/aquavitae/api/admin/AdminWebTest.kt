package pt.aquavitae.api.admin

import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Primary
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.AbstractPlatformTransactionManager
import org.springframework.transaction.support.DefaultTransactionStatus
import org.springframework.transaction.TransactionDefinition
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
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
@Import(AdminWebTest.CatalogoFalso::class)
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
    fun `um pedido do htmx com a sessao expirada manda o browser para o login`() {
        mvc.perform(get("/admin/bebidas").header("HX-Request", "true"))
            .andExpect(status().isUnauthorized)
            .andExpect(header().string("HX-Redirect", "/admin/login"))
    }
}
