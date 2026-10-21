package com.plazoleta.usuarios.infrastructure.input.rest.dto;

import java.time.LocalDate;

/** Nunca devolvemos la clave. */
public record UserResponse(
        Long id,
        String nombre,
        String apellido,
        String documento,
        String celular,
        LocalDate fechaNacimiento,
        String correo,
        String rol,
        Long idRestaurante) {
}
