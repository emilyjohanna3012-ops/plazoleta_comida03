
package com.plazoleta.plazoleta.infrastructure.security;

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
    public JwtValidador jwtValidador(@Value("${jwt.secret}") String secreto) {
        return new JwtValidador(secreto);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtValidador jwtValidador) throws Exception {

        return http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(s ->
                        s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth

                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        .requestMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/error"
                        ).permitAll()

                        // HU2: Crear restaurantes
                        .requestMatchers(HttpMethod.POST, "/api/v1/restaurantes")
                        .hasRole("ADMINISTRADOR")

                        // HU9: Listar restaurantes
                        .requestMatchers(HttpMethod.GET, "/api/v1/restaurantes")
                        .hasRole("CLIENTE")

                        // HU6: Consultar restaurante por ID
                        .requestMatchers(HttpMethod.GET, "/api/v1/restaurantes/*")
                        .authenticated()

                        // HU10: Consultar platos disponibles
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/platos/restaurante/*"
                        ).hasRole("CLIENTE")

                        // HU3, HU4 y HU7: Administración de platos
                        .requestMatchers("/api/v1/platos/**")
                        .hasRole("PROPIETARIO")

                        // HU11: El cliente puede crear pedidos
                        .requestMatchers(HttpMethod.POST, "/api/v1/pedidos")
                        .hasRole("CLIENTE")

                        .anyRequest().denyAll()
                )
                .exceptionHandling(e -> e
                        .authenticationEntryPoint((request, response, ex) ->
                                escribirError(
                                        response,
                                        HttpServletResponse.SC_UNAUTHORIZED,
                                        "Debes iniciar sesión: falta el token o no es válido"
                                ))
                        .accessDeniedHandler((request, response, ex) ->
                                escribirError(
                                        response,
                                        HttpServletResponse.SC_FORBIDDEN,
                                        "Tu rol no tiene permiso para esta operación"
                                ))
                )
                .addFilterBefore(
                        new JwtAuthenticationFilter(jwtValidador),
                        UsernamePasswordAuthenticationFilter.class
                )
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuracion = new CorsConfiguration();

        configuracion.setAllowedOrigins(List.of(
                "http://127.0.0.1:5500",
                "http://localhost:5500"
        ));

        configuracion.setAllowedMethods(List.of(
                "GET",
                "POST",
                "PUT",
                "PATCH",
                "DELETE",
                "OPTIONS"
        ));

        configuracion.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type"
        ));

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", configuracion);

        return source;
    }

    private static void escribirError(
            HttpServletResponse response,
            int estado,
            String mensaje) throws IOException {

        response.setStatus(estado);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"mensaje\":\"" + mensaje + "\"}");
    }
}
