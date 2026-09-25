package pt.aquavitae.api.config

import jakarta.servlet.DispatcherType
import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
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

    // A cadeia da API apanha qualquer pedido, por isso vem depois da do painel `/admin/**` (ver AdminSecurityConfig, `@Order(1)`).
    @Bean
    @Order(2)
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { auth ->
                auth
                    // Quando o Spring recusa um pedido (ex.: 403), reencaminha internamente para /error para
                    // gerar a resposta. Esse reencaminhamento não passa pelo filtro JWT, por isso, sem esta
                    // linha, chega sem autenticação e o 403 era convertido em 401 (o cliente não conseguiria
                    // distinguir "faz login" de "sem permissão").
                    .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                    .requestMatchers("/api/auth/**").permitAll()
                    // Mais específico primeiro: sugeridas exige auth (usa as preferências do
                    // utilizador), senão cairia no permitAll genérico de /api/bebidas/** abaixo.
                    .requestMatchers(HttpMethod.GET, "/api/bebidas/sugeridas").authenticated()
                    .requestMatchers(HttpMethod.GET, "/api/bebidas/**", "/api/produtores/**", "/api/lookup/**").permitAll()
                    // Os termos e condições têm de se poder ler antes de haver conta (popup do registo e do login): o
                    // texto em JSON para a app (/api/legal) e a página pública (/legal/termos.html).
                    .requestMatchers(HttpMethod.GET, "/api/legal/**", "/legal/**").permitAll()
                    // Recursos estáticos (avatares, e futuramente fotos de bebidas/produtores) — sem
                    // auth, servidos diretamente pelo Spring de src/main/resources/static/.
                    .requestMatchers(HttpMethod.GET, "/icones/**").permitAll()
                    // Manutenção (ex.: disparar a verificação de links de compra) — só admins.
                    .requestMatchers("/api/admin/**").hasRole("ADMIN")
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
