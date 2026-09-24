package pt.aquavitae.api.utilizador

import org.mockito.Mockito.inOrder
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import pt.aquavitae.api.common.ConflictException
import pt.aquavitae.api.common.ResourceNotFoundException
import pt.aquavitae.api.common.UnauthorizedActionException
import pt.aquavitae.api.lookup.UtilizadorRole
import java.util.Optional
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ContaServiceTest {

    private val encoder = BCryptPasswordEncoder()
    private val utilizadores = mock(UtilizadorRepository::class.java)
    private val apagar = mock(ApagarContaRepository::class.java)
    private val servico = ContaService(utilizadores, apagar, encoder)

    private fun conta(papel: String = "Utilizador", password: String? = "password123") = Utilizador(
        id = 22,
        username = "demo_user2",
        password = password?.let(encoder::encode),
        role = UtilizadorRole(1, papel),
    )

    @Test
    fun `com a password certa apaga tudo, dos filhos para os pais, e por fim a conta`() {
        `when`(utilizadores.findByIdWithRole(22L)).thenReturn(Optional.of(conta()))

        servico.apagarConta(22, "password123")

        val ordem = inOrder(apagar)
        ordem.verify(apagar).apagarGarrafas(22)
        ordem.verify(apagar).apagarCaves(22) // as garrafas têm FK para a cave
        ordem.verify(apagar).apagarWishlist(22)
        ordem.verify(apagar).apagarFavoritos(22)
        ordem.verify(apagar).apagarProvadas(22)
        ordem.verify(apagar).apagarReviews(22)
        ordem.verify(apagar).anonimizarCliques(22)
        ordem.verify(apagar).apagarCastasPreferidas(22)
        ordem.verify(apagar).apagarCategoriasPreferidas(22)
        ordem.verify(apagar).apagarPreferencias(22)
        ordem.verify(apagar).apagarCodigosDeRecuperacao(22)
        ordem.verify(apagar).apagarSessoes(22)
        ordem.verify(apagar).deleteById(22L) // o utilizador é o último: tudo o resto aponta para ele
    }

    @Test
    fun `com a password errada nao apaga nada e responde 403`() {
        `when`(utilizadores.findByIdWithRole(22L)).thenReturn(Optional.of(conta()))

        val erro = assertFailsWith<UnauthorizedActionException> { servico.apagarConta(22, "errada") }

        assertEquals("Password incorreta.", erro.message)
        verify(apagar, never()).apagarGarrafas(22)
        verify(apagar, never()).deleteById(22L)
    }

    @Test
    fun `uma conta sem password (nao devia haver) tambem e recusada`() {
        `when`(utilizadores.findByIdWithRole(22L)).thenReturn(Optional.of(conta(password = null)))
        assertFailsWith<UnauthorizedActionException> { servico.apagarConta(22, "password123") }
        verify(apagar, never()).deleteById(22L)
    }

    @Test
    fun `um administrador nao se apaga pela app, mesmo com a password certa`() {
        `when`(utilizadores.findByIdWithRole(22L)).thenReturn(Optional.of(conta(papel = "Admin")))

        assertFailsWith<ConflictException> { servico.apagarConta(22, "password123") }

        verify(apagar, never()).apagarReviews(22)
        verify(apagar, never()).deleteById(22L)
    }

    @Test
    fun `uma conta que nao existe e 404`() {
        `when`(utilizadores.findByIdWithRole(99L)).thenReturn(Optional.empty())
        assertFailsWith<ResourceNotFoundException> { servico.apagarConta(99, "x") }
    }
}
