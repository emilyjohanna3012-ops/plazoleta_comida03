package com.plazoleta.trazabilidad.infrastructure.security;

import io.jsonwebtoken.JwtException;
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

/** Lee el encabezado "Authorization: Bearer <token>" y deja identificado al usuario. */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtValidador jwtValidador;

    public JwtAuthenticationFilter(JwtValidador jwtValidador) {
        this.jwtValidador = jwtValidador;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String autorizacion = request.getHeader("Authorization");

        if (autorizacion != null && autorizacion.startsWith("Bearer ")) {
            try {
                UsuarioAutenticado usuario = jwtValidador.validar(autorizacion.substring(7));
                var autenticacion = new UsernamePasswordAuthenticationToken(
                        usuario, null, List.of(new SimpleGrantedAuthority("ROLE_" + usuario.rol())));
                SecurityContextHolder.getContext().setAuthentication(autenticacion);
            } catch (JwtException | IllegalArgumentException e) {
                SecurityContextHolder.clearContext(); // token inválido o vencido: queda sin autenticar (401)
            }
        }

        filterChain.doFilter(request, response);
    }
}
