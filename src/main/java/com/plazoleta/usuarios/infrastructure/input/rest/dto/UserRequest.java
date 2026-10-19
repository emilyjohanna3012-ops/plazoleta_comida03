package com.plazoleta.usuarios.infrastructure.input.rest.dto;

import java.time.LocalDate;

public record UserRequest(
        String nombre,
        String apellido,
        String documento,
        String celular,
        LocalDate fechaNacimiento,
        String correo,
        String clave) {
}
