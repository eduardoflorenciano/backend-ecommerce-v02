package com.autoCenterSilva.demo.security;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> jwtFilterRegistration(
            JwtAuthenticationFilter filter
    ) {
        FilterRegistrationBean<JwtAuthenticationFilter> registration =
                new FilterRegistrationBean<>(filter);

        registration.setEnabled(false);

        return registration;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        return http

                .cors(Customizer.withDefaults())

                .csrf(csrf -> csrf.disable())

                .exceptionHandling(exception -> exception

                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json");
                            response.setCharacterEncoding("UTF-8");

                            response.getWriter().write("""
                                    {
                                        "status": 401,
                                        "error": "Não autorizado",
                                        "message": "Autenticação necessária. Informe um token JWT válido"
                                    }
                                    """);
                        })

                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.setContentType("application/json");
                            response.setCharacterEncoding("UTF-8");

                            response.getWriter().write("""
                                    {
                                        "status": 403,
                                        "error": "Acesso proibido",
                                        "message": "Você não possui permissão para acessar este recurso"
                                    }
                                    """);
                        })
                )

                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .authorizeHttpRequests(auth -> auth

                        .requestMatchers("/api/clientes/criar").permitAll()
                        .requestMatchers("/api/clientes/login").permitAll()

                        .requestMatchers("/api/clientes/usuarios/**").hasRole("ADM")

                        .requestMatchers(HttpMethod.GET, "/api/produtos/listar").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/produtos/pesquisar").permitAll()
                        .requestMatchers(HttpMethod.GET, "/cotacao/produto/*/converter-para-reais").permitAll()

                        .requestMatchers("/api/produtos/criar").hasRole("ADM")
                        .requestMatchers("/api/produtos/variacao/**").hasRole("ADM")
                        .requestMatchers("/api/produtos/variacoes/**").hasRole("ADM")
                        .requestMatchers("/api/produtos/admin/estoque/**").hasRole("ADM")
                        .requestMatchers(HttpMethod.POST, "/api/carro/**").hasRole("ADM")
                        .requestMatchers(HttpMethod.DELETE, "/api/carro/**").hasRole("ADM")

                        .requestMatchers("/api/pedidos/admin/**", "/api/pedidos/listar")
                        .hasAnyRole("ATENDENTE", "ADM")

                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/pedidos/*/confirmar-pagamento"
                        ).hasAnyRole("ATENDENTE", "ADM")

                        .anyRequest().authenticated()
                )

                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                )

                .build();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration
    ) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(
            @Value("${app.cors.allowed-origins}") String allowedOrigins
    ) {
        List<String> origens = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(origem -> !origem.isEmpty())
                .toList();

        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(origens);

        configuration.setAllowedMethods(
                List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
        );

        configuration.setAllowedHeaders(
                List.of("Authorization", "Content-Type", "Accept")
        );

        configuration.setExposedHeaders(
                List.of("Authorization")
        );

        configuration.setAllowCredentials(false);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}
