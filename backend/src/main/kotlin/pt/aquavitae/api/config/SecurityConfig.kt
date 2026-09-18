package pt.aquavitae.api.config

import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import pt.aquavitae.api.security.JwtAuthenticationFilter

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val jwtAuthenticationFilter: JwtAuthenticationFilter,
) {

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers("/api/auth/**").permitAll()
                    // Mais específico primeiro: sugeridas exige auth (usa as preferências do
                    // utilizador), senão cairia no permitAll genérico de /api/bebidas/** abaixo.
                    .requestMatchers(HttpMethod.GET, "/api/bebidas/sugeridas").authenticated()
                    .requestMatchers(HttpMethod.GET, "/api/bebidas/**", "/api/produtores/**", "/api/lookup/**").permitAll()
                    // Recursos estáticos (avatares, e futuramente fotos de bebidas/produtores) — sem
                    // auth, servidos diretamente pelo Spring de src/main/resources/static/.
                    .requestMatchers(HttpMethod.GET, "/icones/**").permitAll()
                    .anyRequest().authenticated()
            }
            .exceptionHandling { exceptions ->
                // Sem token / token inválido -> 401 (não o 403 que o Spring Security devolve por
                // omissão), para o cliente Android conseguir distinguir "faz login" de "sem permissão".
                exceptions.authenticationEntryPoint { _, response, _ ->
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED)
                }
            }
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }
}
