package com.plazoleta.plazoleta.infrastructure.input.rest.dto;

public record RestauranteResponse(
        Long id,
        String nombre,
        String nit,
        String direccion,
        String telefono,
        String urlLogo,
        Long idPropietario) {
}
