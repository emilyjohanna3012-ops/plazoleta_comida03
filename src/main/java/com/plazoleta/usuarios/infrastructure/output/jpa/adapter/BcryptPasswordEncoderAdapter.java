package com.plazoleta.usuarios.infrastructure.output.jpa.adapter;

import com.plazoleta.usuarios.domain.spi.PasswordEncoderPort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/** Adaptador de SALIDA: protege y verifica contraseñas con BCrypt. */
public class BcryptPasswordEncoderAdapter implements PasswordEncoderPort {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Override
    public String encriptar(String claveSinEncriptar) {
        return encoder.encode(claveSinEncriptar);
    }

    @Override
    public boolean verificar(String claveSinEncriptar, String claveEncriptada) {
        return encoder.matches(claveSinEncriptar, claveEncriptada);
    }
}