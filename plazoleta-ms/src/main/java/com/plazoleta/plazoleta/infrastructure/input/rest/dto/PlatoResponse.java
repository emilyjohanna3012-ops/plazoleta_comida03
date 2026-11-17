package com.plazoleta.plazoleta.infrastructure.input.rest.dto;

public record PlatoResponse(
        Long id,
        String nombre,
        Integer precio,
        String descripcion,
        String urlImagen,
        String categoria,
        Long idRestaurante,
        Boolean activo) {
}
