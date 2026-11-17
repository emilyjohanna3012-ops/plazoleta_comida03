package com.plazoleta.plazoleta.infrastructure.input.rest.dto;

/** HU7: true = habilitar el plato, false = deshabilitarlo. */
public record PlatoEstadoRequest(Boolean activo) {
}
