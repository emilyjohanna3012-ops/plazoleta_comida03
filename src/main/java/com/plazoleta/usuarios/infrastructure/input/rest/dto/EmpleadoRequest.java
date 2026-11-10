package com.plazoleta.usuarios.infrastructure.input.rest.dto;

import java.time.LocalDate;

/** HU6: datos del empleado. El rol no se envía: el sistema siempre asigna EMPLEADO. */
public record EmpleadoRequest(
        String nombre,
        String apellido,
        String documento,
        String celular,
        LocalDate fechaNacimiento,
        String correo,
        String clave,
        Long idRestaurante) {
}
