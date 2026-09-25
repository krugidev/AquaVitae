package pt.aquavitae.api.admin

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy

const val ADMIN_LOGIN = "/admin/login"

/**
 * O painel de administração (as páginas em `/admin/...`) tem a sua própria cadeia de segurança, à parte da da API: **sessão com cookie** (a
 * API é sem estado, com JWT), formulário de login e **CSRF ligado** (os formulários do Thymeleaf levam o token sozinhos).
 * Só entra quem tem o papel `Admin`; um utilizador normal com a password certa é recusado no próprio login (ver
 * [AdminUserDetailsService]), sem ficar com uma sessão que não serve para nada.
 *
 * Tem de ser a **primeira** (`@Order(1)`): a cadeia da API apanha qualquer pedido e só pode vir depois.
 */
@Configuration
class AdminSecurityConfig {

    @Bean
    @Order(1)
    fun adminFilterChain(
        http: HttpSecurity,
        userDetailsService: AdminUserDetailsService,
        passwordEncoder: PasswordEncoder,
    ): SecurityFilterChain {
        val provider = DaoAuthenticationProvider(userDetailsService).apply { setPasswordEncoder(passwordEncoder) }
        val paraOLogin = LoginUrlAuthenticationEntryPoint(ADMIN_LOGIN)

        http
            // O htmx (webjar) é servido em /webjars/**: entra nesta cadeia para se poder pedir antes do login (a página de
            // login também o usa) sem abrir mais nada da API.
            .securityMatcher("/admin/**", "/webjars/**")
            .authenticationProvider(provider)
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers(ADMIN_LOGIN, "/admin/css/**", "/admin/js/**", "/webjars/**").permitAll()
                    .anyRequest().hasRole("ADMIN")
            }
            .formLogin { form ->
                form.loginPage(ADMIN_LOGIN)
                    .loginProcessingUrl(ADMIN_LOGIN)
                    .usernameParameter("identificador")
                    .passwordParameter("password")
                    .defaultSuccessUrl("/admin")
                    .failureUrl("$ADMIN_LOGIN?erro")
            }
            .logout { it.logoutUrl("/admin/logout").logoutSuccessUrl("$ADMIN_LOGIN?saiu") }
            .exceptionHandling { exceptions ->
                // Sessão expirada a meio de uma pesquisa (pedido do htmx): o htmx não segue um redirecionamento para trocar
                // a página inteira — pede-se-lhe, com HX-Redirect, que leve o browser ao login.
                exceptions.authenticationEntryPoint { request, response, ex ->
                    if (request.getHeader("HX-Request") != null) {
                        response.setHeader("HX-Redirect", ADMIN_LOGIN)
                        response.status = 401
                    } else {
                        paraOLogin.commence(request, response, ex)
                    }
                }
            }
            .headers { headers ->
                // Só recursos próprios (o htmx e o CSS são do servidor); imagens de qualquer HTTPS, porque as das bebidas são
                // os URLs dos retalhistas. Sem scripts nem estilos embutidos.
                headers.contentSecurityPolicy { csp ->
                    csp.policyDirectives(
                        "default-src 'self'; img-src 'self' https: data:; style-src 'self'; script-src 'self'; " +
                            "form-action 'self'; frame-ancestors 'none'; base-uri 'self'",
                    )
                }
                headers.referrerPolicy { it.policy(ReferrerPolicy.SAME_ORIGIN) }
            }

        return http.build()
    }
}
