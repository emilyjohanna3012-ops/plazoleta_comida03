package com.plazoleta.plazoleta;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/** Genera tokens iguales a los de usuarios-ms, firmados con la clave de src/test/resources/application.yml. */
final class TokenDePrueba {

    private static final String SECRETO = "clave-secreta-solo-para-pruebas-con-mas-de-32-bytes";

    private TokenDePrueba() { }

    static String bearer(long idUsuario, String rol) {
        SecretKey clave = Keys.hmacShaKeyFor(SECRETO.getBytes(StandardCharsets.UTF_8));
        Date ahora = new Date();
        String token = Jwts.builder()
                .subject("usuario" + idUsuario + "@correo.com")
                .claim("id", idUsuario)
                .claim("rol", rol)
                .issuedAt(ahora)
                .expiration(new Date(ahora.getTime() + 3_600_000))
                .signWith(clave)
                .compact();
        return "Bearer " + token;
    }
}
