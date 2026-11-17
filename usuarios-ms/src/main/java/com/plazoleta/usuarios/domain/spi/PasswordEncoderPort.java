package com.plazoleta.usuarios.domain.spi;

/** Puerto de SALIDA: proteger y verificar contraseñas usando BCrypt. */
public interface PasswordEncoderPort {

    String encriptar(String claveSinEncriptar);

    boolean verificar(String claveSinEncriptar, String claveEncriptada);
}