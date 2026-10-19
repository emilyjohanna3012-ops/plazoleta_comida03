package com.plazoleta.usuarios.domain.spi;

/** Puerto de SALIDA: encriptar claves (la implementación real usa bcrypt). */
public interface PasswordEncoderPort {
    String encriptar(String claveSinEncriptar);
}
