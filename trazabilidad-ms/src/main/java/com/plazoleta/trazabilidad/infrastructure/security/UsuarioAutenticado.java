package com.plazoleta.trazabilidad.infrastructure.security;

/** Quién hace la petición, según el token JWT emitido por usuarios-ms. */
public record UsuarioAutenticado(Long id, String correo, String rol, Long idRestaurante) {
}
