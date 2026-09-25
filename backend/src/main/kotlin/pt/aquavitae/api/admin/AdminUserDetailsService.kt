package pt.aquavitae.api.admin

import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import pt.aquavitae.api.utilizador.UtilizadorRepository
import pt.aquavitae.api.utilizador.findByIdentificador

const val PAPEL_ADMIN = "Admin"

/**
 * Quem pode entrar no painel: só contas com o papel `Admin`. O campo do login é o mesmo do da app ("username ou email", sem
 * distinguir maiúsculas). Uma conta que não existe, sem password ou **sem o papel** dá a mesma resposta ("credenciais
 * inválidas") — não se diz a quem tenta que a conta existe mas não é de administrador.
 */
@Service
class AdminUserDetailsService(
    private val utilizadores: UtilizadorRepository,
) : UserDetailsService {

    // `role` é LAZY: precisa da transação para o ler (o painel não pode confiar no Open-Session-In-View aqui — o login corre
    // no filtro de segurança, antes dele).
    @Transactional(readOnly = true)
    override fun loadUserByUsername(identificador: String): UserDetails {
        val utilizador = utilizadores.findByIdentificador(identificador)
        val hash = utilizador?.password
        if (utilizador == null || hash == null || !utilizador.role?.value.equals(PAPEL_ADMIN, ignoreCase = true)) {
            throw UsernameNotFoundException("Sem conta de administrador para '$identificador'")
        }
        return User.withUsername(utilizador.username ?: utilizador.email ?: identificador)
            .password(hash)
            .roles("ADMIN")
            .build()
    }
}
