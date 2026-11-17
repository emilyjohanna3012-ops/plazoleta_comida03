package com.plazoleta.plazoleta.infrastructure.input.rest.dto;

public record PlatoRequest(
        String nombre,
        Integer precio,
        String descripcion,
        String urlImagen,
        String categoria,
        Long idRestaurante) {
}
