package com.plazoleta.plazoleta.infrastructure.security;

/**
 * Quién hace la petición, según el token JWT emitido por usuarios-ms.
 * idRestaurante solo viene para los EMPLEADOS (HU6); para los demás es null.
 */
public record UsuarioAutenticado(Long id, String correo, String rol, Long idRestaurante) {
}
