package com.plazoleta.plazoleta.infrastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

/**
 * Valida los tokens que genera usuarios-ms. Ambos microservicios deben usar el MISMO JWT_SECRET.
 * Del token se leen: subject = correo, claim "id" = id del usuario, claim "rol" = rol.
 */
public class JwtValidador {

    private final SecretKey claveSecreta;

    public JwtValidador(String secreto) {
        if (secreto == null || secreto.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalArgumentException("JWT_SECRET debe tener al menos 32 bytes");
        }
        this.claveSecreta = Keys.hmacShaKeyFor(secreto.getBytes(StandardCharsets.UTF_8));
    }

    public UsuarioAutenticado validar(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(claveSecreta)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        Long id = claims.get("id", Long.class);
        String rol = claims.get("rol", String.class);
        if (id == null || rol == null) {
            throw new JwtException("El token no trae el id o el rol del usuario");
        }
        return new UsuarioAutenticado(id, claims.getSubject(), rol, claims.get("idRestaurante", Long.class));
    }
}
