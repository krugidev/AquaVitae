package pt.aquavitae.api.email

import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RecuperacaoPasswordEmailTest {

    private val email = RecuperacaoPasswordEmail.compor(nome = "Ana Silva", codigo = "482913", validadeMinutos = 15)

    @Test
    fun `o codigo e a validade vao nas duas versoes do corpo`() {
        listOf(email.texto, email.html).forEach {
            assertContains(it, "482913")
            assertContains(it, "15 minutos")
        }
    }

    @Test
    fun `cumprimenta pelo nome`() {
        assertContains(email.texto, "Olá Ana Silva,")
        assertContains(email.html, "Olá Ana Silva,")
    }

    @Test
    fun `sem nome cumprimenta sem nome`() {
        val semNome = RecuperacaoPasswordEmail.compor(nome = null, codigo = "111111", validadeMinutos = 15)
        assertContains(semNome.texto, "Olá,")
        val soEspacos = RecuperacaoPasswordEmail.compor(nome = "   ", codigo = "111111", validadeMinutos = 15)
        assertContains(soEspacos.texto, "Olá,")
    }

    // O nome é do utilizador: um username com HTML não pode entrar tal e qual no email (nem partir o layout nem injetar nada).
    @Test
    fun `o nome vai escapado na versao html`() {
        val malicioso = RecuperacaoPasswordEmail.compor(nome = "<script>alert(1)</script>", codigo = "482913", validadeMinutos = 15)
        assertFalse(malicioso.html.contains("<script>"), "o HTML do email não pode conter o <script> do nome")
        assertContains(malicioso.html, "&lt;script&gt;")
    }

    @Test
    fun `o assunto nao revela o codigo`() {
        assertFalse(email.assunto.contains("482913"))
        assertContains(email.assunto, "AquaVitae")
    }

    @Test
    fun `diz que se pode ignorar e nao traz ligacoes`() {
        assertContains(email.texto, "ignora este email")
        assertFalse(email.html.contains("href="), "sem ligações no email de recuperação (menos superfície para phishing)")
        assertTrue(email.html.contains("lang=\"pt-PT\""))
    }
}
