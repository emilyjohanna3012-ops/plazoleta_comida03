
package com.plazoleta.trazabilidad.infrastructure.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.IOException;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public JwtValidador jwtValidador(
            @Value("${jwt.secret}") String secreto) {
        return new JwtValidador(secreto);
    }

    // ==========================================
    // CORS - PERMITIR CONEXIÓN CON EL FRONTEND
    // ==========================================

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuracion =
                new CorsConfiguration();

        configuracion.setAllowedOrigins(List.of(
                "http://localhost:5500",
                "http://127.0.0.1:5500"
        ));

        configuracion.setAllowedMethods(List.of(
                "GET",
                "POST",
                "OPTIONS"
        ));

        configuracion.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type",
                "Accept"
        ));

        UrlBasedCorsConfigurationSource fuente =
                new UrlBasedCorsConfigurationSource();

        fuente.registerCorsConfiguration(
                "/**",
                configuracion
        );

        return fuente;
    }

    // ==========================================
    // SEGURIDAD DE LOS ENDPOINTS
    // ==========================================

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtValidador jwtValidador
    ) throws Exception {

        return http
                .csrf(csrf -> csrf.disable())

                // Habilitar CORS
                .cors(cors -> cors.configurationSource(
                        corsConfigurationSource()
                ))

                .sessionManagement(s ->
                        s.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .authorizeHttpRequests(auth -> auth

                        // Swagger
                        .requestMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/error"
                        ).permitAll()

                        // HU17 - Registrar trazabilidad
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/trazas"
                        ).authenticated()

                        // HU17 - Consultar historial del cliente
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/trazas/pedido/*"
                        ).hasRole("CLIENTE")

                        // HU18 - Eficiencia del restaurante
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/trazas/eficiencia/restaurante/*"
                        ).hasRole("PROPIETARIO")

                        .anyRequest().denyAll()
                )

                .exceptionHandling(e -> e

                        .authenticationEntryPoint(
                                (request, response, ex) ->
                                        escribirError(
                                                response,
                                                HttpServletResponse.SC_UNAUTHORIZED,
                                                "Debes iniciar sesión: falta el token o no es válido"
                                        )
                        )

                        .accessDeniedHandler(
                                (request, response, ex) ->
                                        escribirError(
                                                response,
                                                HttpServletResponse.SC_FORBIDDEN,
                                                "Tu rol no tiene permiso para esta operación"
                                        )
                        )
                )

                .addFilterBefore(
                        new JwtAuthenticationFilter(jwtValidador),
                        UsernamePasswordAuthenticationFilter.class
                )

                .build();
    }

    private static void escribirError(
            HttpServletResponse response,
            int estado,
            String mensaje
    ) throws IOException {

        response.setStatus(estado);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        response.getWriter().write(
                "{\"mensaje\":\"" + mensaje + "\"}"
        );
    }
}
