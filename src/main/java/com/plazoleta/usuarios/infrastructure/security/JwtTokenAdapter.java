package com.plazoleta.usuarios.infrastructure.security;

import com.plazoleta.usuarios.domain.model.User;
import com.plazoleta.usuarios.domain.spi.TokenPort;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

public class JwtTokenAdapter implements TokenPort {

    private final SecretKey claveSecreta;
    private final long expiracion;

    public JwtTokenAdapter(
            @Value("${jwt.secret}") String secreto,
            @Value("${jwt.expiration}") long expiracion) {

        if (secreto.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalArgumentException(
                    "JWT_SECRET debe tener al menos 32 bytes");
        }

        this.claveSecreta = Keys.hmacShaKeyFor(
                secreto.getBytes(StandardCharsets.UTF_8));
        this.expiracion = expiracion;
    }

    @Override
    public String generarToken(User usuario) {
        Date ahora = new Date();
        Date vencimiento = new Date(ahora.getTime() + expiracion);

        var constructor = Jwts.builder()
                .subject(usuario.getCorreo())
                .claim("rol", usuario.getRol().name());

        // Plazoleta necesita el id del usuario (por ejemplo, para saber de quién es cada restaurante)
        if (usuario.getId() != null) {
            constructor.claim("id", usuario.getId());
        }
        // Solo los empleados: así Plazoleta sabe a qué restaurante pertenecen (HU6, HU12, HU13)
        if (usuario.getIdRestaurante() != null) {
            constructor.claim("idRestaurante", usuario.getIdRestaurante());
        }

        return constructor
                .issuedAt(ahora)
                .expiration(vencimiento)
                .signWith(claveSecreta)
                .compact();
    }
        public String validarToken(String token) {
        return Jwts.parser()
                .verifyWith(claveSecreta)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }
}