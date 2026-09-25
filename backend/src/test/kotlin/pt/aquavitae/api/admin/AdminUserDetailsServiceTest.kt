package pt.aquavitae.api.admin

import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.security.core.userdetails.UsernameNotFoundException
import pt.aquavitae.api.lookup.UtilizadorRole
import pt.aquavitae.api.utilizador.Utilizador
import pt.aquavitae.api.utilizador.UtilizadorRepository
import java.util.Optional
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class AdminUserDetailsServiceTest {

    private val repositorio = mock(UtilizadorRepository::class.java)
    private val servico = AdminUserDetailsService(repositorio)

    private fun conta(papel: String?, password: String? = "hash-bcrypt") = Utilizador(
        id = 7,
        username = "miguel",
        email = "miguel@aquavitae.pt",
        password = password,
        role = papel?.let { UtilizadorRole(1, it) },
    )

    private fun comEmail(email: String, utilizador: Utilizador) {
        `when`(repositorio.findByEmailIgnoreCase(email)).thenReturn(Optional.of(utilizador))
    }

    @Test
    fun `um administrador entra pelo email, com o papel ADMIN e o hash da password`() {
        comEmail("miguel@aquavitae.pt", conta("Admin"))
        val detalhes = servico.loadUserByUsername("miguel@aquavitae.pt")
        assertEquals("miguel", detalhes.username)
        assertEquals("hash-bcrypt", detalhes.password)
        assertEquals(listOf("ROLE_ADMIN"), detalhes.authorities.map { it.authority })
    }

    @Test
    fun `entra tambem pelo username, sem distinguir maiusculas e com espacos nas pontas`() {
        `when`(repositorio.findByEmailIgnoreCase("Miguel")).thenReturn(Optional.empty())
        `when`(repositorio.findByUsernameIgnoreCase("Miguel")).thenReturn(Optional.of(conta("ADMIN")))
        assertEquals("miguel", servico.loadUserByUsername("  Miguel ").username)
    }

    @Test
    fun `um utilizador normal e recusado como se a conta nao existisse`() {
        comEmail("ana@aquavitae.pt", conta("Utilizador"))
        assertFailsWith<UsernameNotFoundException> { servico.loadUserByUsername("ana@aquavitae.pt") }
    }

    @Test
    fun `uma conta sem papel ou sem password e recusada`() {
        comEmail("a@x.pt", conta(papel = null))
        comEmail("b@x.pt", conta("Admin", password = null))
        assertFailsWith<UsernameNotFoundException> { servico.loadUserByUsername("a@x.pt") }
        assertFailsWith<UsernameNotFoundException> { servico.loadUserByUsername("b@x.pt") }
    }

    @Test
    fun `uma conta que nao existe e recusada`() {
        `when`(repositorio.findByEmailIgnoreCase("ninguem")).thenReturn(Optional.empty())
        `when`(repositorio.findByUsernameIgnoreCase("ninguem")).thenReturn(Optional.empty())
        val erro = assertFailsWith<UsernameNotFoundException> { servico.loadUserByUsername("ninguem") }
        assertTrue(erro.message!!.isNotBlank())
    }
}
