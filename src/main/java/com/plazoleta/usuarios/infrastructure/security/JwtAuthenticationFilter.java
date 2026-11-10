package com.plazoleta.usuarios.infrastructure.security;

import com.plazoleta.usuarios.domain.model.User;
import com.plazoleta.usuarios.domain.spi.UserPersistencePort;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenAdapter jwtTokenAdapter;
    private final UserPersistencePort userPersistencePort;

    public JwtAuthenticationFilter(
            JwtTokenAdapter jwtTokenAdapter,
            UserPersistencePort userPersistencePort) {
        this.jwtTokenAdapter = jwtTokenAdapter;
        this.userPersistencePort = userPersistencePort;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String autorizacion = request.getHeader("Authorization");

        if (autorizacion != null && autorizacion.startsWith("Bearer ")) {

            String token = autorizacion.substring(7);

            try {
                String correo = jwtTokenAdapter.validarToken(token);

                User usuario = userPersistencePort.buscarPorCorreo(correo)
                        .orElse(null);

                if (usuario != null) {
                    var autoridad = new SimpleGrantedAuthority(
                            "ROLE_" + usuario.getRol().name());

                    var autenticacion =
                            new UsernamePasswordAuthenticationToken(
                                    correo,
                                    null,
                                    List.of(autoridad));

                    SecurityContextHolder.getContext()
                            .setAuthentication(autenticacion);
                }

            } catch (Exception e) {
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }
}