package com.plazoleta.plazoleta.infrastructure.input.rest.dto;

/** HU4: solo se pueden modificar el precio y la descripción. */
public record PlatoModificacionRequest(
        Integer precio,
        String descripcion) {
}
