package pt.aquavitae.api.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import pt.aquavitae.api.utilizador.UtilizadorRepository

@Component
class JwtAuthenticationFilter(
    private val jwtService: JwtService,
    private val utilizadorRepository: UtilizadorRepository,
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val header = request.getHeader("Authorization")
        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response)
            return
        }

        val token = header.substring(7)
        if (jwtService.isTokenValid(token) && SecurityContextHolder.getContext().authentication == null) {
            val userId = jwtService.extractUserId(token)
            utilizadorRepository.findById(userId).ifPresent { utilizador ->
                val roleName = utilizador.role?.value?.uppercase() ?: "UTILIZADOR"
                val authentication = UsernamePasswordAuthenticationToken(
                    utilizador,
                    null,
                    listOf(SimpleGrantedAuthority("ROLE_$roleName")),
                )
                authentication.details = WebAuthenticationDetailsSource().buildDetails(request)
                SecurityContextHolder.getContext().authentication = authentication
            }
        }

        filterChain.doFilter(request, response)
    }
}
